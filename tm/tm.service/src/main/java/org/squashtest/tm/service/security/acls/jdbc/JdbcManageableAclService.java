/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.service.security.acls.jdbc;

import java.io.Serializable;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.jdbc.JdbcAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.AlreadyExistsException;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.Sorting;
import org.squashtest.tm.service.security.acls.CustomPermission;
import org.squashtest.tm.service.security.acls.PermissionGroup;
import org.squashtest.tm.service.security.acls.model.ObjectAclService;

/**
 * Specialization of {@link JdbcAclService} with management methods. Rem : as we tweaked Spring's ACL database model,
 * Spring's JdbcMutableAclService does not fit.
 *
 * This class is inspired by Spring Security's JdbcMutableAclService, Copyright 2004, 2005, 2006 Acegi Technology Pty
 * Limited
 *
 * @author Gregory Fouquet
 *
 */

/**
 * 
 * When one update the Acl of an object (ie the permissions of a user), one want to refresh the aclCache if there is
 * one. The right way to do this would have been to delegate such task to the LookupStrategy when it's relevant to do
 * so. However we cannot subclass BasicLookupStrategy because it's final and duplicating its code for a class of ours
 * would be illegal.
 * 
 * So we're bypassing the cache encapsulation and expose it right here.
 * 
 * 
 * @author bsiri
 */
@Transactional
public class JdbcManageableAclService extends JdbcAclService implements ManageableAclService, ObjectAclService {

	private static final Logger LOGGER = LoggerFactory.getLogger(JdbcManageableAclService.class);

	private AclCache aclCache;

	public void setAclCache(AclCache aclCache) {
		this.aclCache = aclCache;
	}

	private final RowMapper<PermissionGroup> permissionGroupMapper = new RowMapper<PermissionGroup>() {
		@Override
		public PermissionGroup mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new PermissionGroup(rs.getLong(1), rs.getString(2));
		}
	};

	private final RowMapper<Object[]> AclGroupMapper = new RowMapper<Object[]>() {
		@Override
		public Object[] mapRow(ResultSet rs, int rowNum) throws SQLException {
			Object objTab[] = new Object[2];
			objTab[0] = rs.getLong(1);
			objTab[1] = new PermissionGroup(rs.getLong(2), rs.getString(3));
			return objTab;
		}
	};

	private static final String INSERT_OBJECT_IDENTITY = "insert into ACL_OBJECT_IDENTITY (IDENTITY, CLASS_ID) values (?, ?)";
	private static final String SELECT_OBJECT_IDENTITY_PRIMARY_KEY = "select oid.ID from ACL_OBJECT_IDENTITY oid inner join ACL_CLASS c on c.ID = oid.CLASS_ID where c.CLASSNAME = ? and oid.IDENTITY = ?";
	private static final String SELECT_CLASS_PRIMARY_KEY = "select ID from ACL_CLASS where CLASSNAME = ?";
	private static final String FIND_ALL_ACL_GROUPS_BY_NAMESPACE = "select ID, QUALIFIED_NAME from ACL_GROUP where QUALIFIED_NAME like ?";


	private static final String INSERT_PARTY_ACL_RESPONSABILITY_SCOPE = "insert into ACL_RESPONSIBILITY_SCOPE_ENTRY (PARTY_ID, ACL_GROUP_ID, OBJECT_IDENTITY_ID) "
			+ "values (?, "
			+ "(select ID from ACL_GROUP where QUALIFIED_NAME = ?), "
			+ "(select oid.ID from ACL_OBJECT_IDENTITY oid "
			+ "inner join ACL_CLASS c on c.ID = oid.CLASS_ID " 
			+ "where CLASSNAME = ?  and oid.IDENTITY = ? )) ";
	
	private static final String INSERT_ACL_RESPONSABILITY_SCOPE = "insert into ACL_RESPONSIBILITY_SCOPE_ENTRY (PARTY_ID, ACL_GROUP_ID, OBJECT_IDENTITY_ID) "
			+ "values ((select PARTY_ID from CORE_USER where login = ?), "
			+ "(select ID from ACL_GROUP where QUALIFIED_NAME = ?), "
			+ "(select oid.ID from ACL_OBJECT_IDENTITY oid "
			+ "inner join ACL_CLASS c on c.ID = oid.CLASS_ID " 
			+ "where CLASSNAME = ?  and oid.IDENTITY = ? )) ";

	private static final String FIND_ACL_FOR_CLASS_FROM_USER = "select oid.IDENTITY, ag.ID, ag.QUALIFIED_NAME from "
			+ "ACL_GROUP ag  inner join ACL_RESPONSIBILITY_SCOPE_ENTRY arse on ag.ID = arse.ACL_GROUP_ID "
			+ "inner join CORE_USER cu on arse.PARTY_ID = cu.PARTY_ID "
			+ "inner join ACL_OBJECT_IDENTITY oid on oid.ID = arse.OBJECT_IDENTITY_ID "
			+ "inner join ACL_CLASS ac on ac.ID = oid.CLASS_ID  where cu.LOGIN = ? and ac.CLASSNAME = ?";

	private static final String FIND_ACL_FOR_CLASS_FROM_PARTY = "select oid.IDENTITY, ag.ID, ag.QUALIFIED_NAME from "
			+ "ACL_GROUP ag  inner join ACL_RESPONSIBILITY_SCOPE_ENTRY arse on ag.ID = arse.ACL_GROUP_ID "
			+ "inner join ACL_OBJECT_IDENTITY oid on oid.ID = arse.OBJECT_IDENTITY_ID "
			+ "inner join ACL_CLASS ac on ac.ID = oid.CLASS_ID  where arse.PARTY_ID = ? and ac.CLASSNAME = ?";
	
	//11-02-13 : this query is ready for task 1865 
	private static final String USER_AND_ACL_GROUP_NAME_FROM_IDENTITY_AND_CLASS = "select arse.PARTY_ID, ag.ID, ag.QUALIFIED_NAME, CONCAT(IFNULL(cu.LOGIN, ''), IFNULL(ct.NAME, '')) as sorting_key, CONCAT(IFNULL(cu.LOGIN, 'TEAM'), IFNULL(ct.NAME, 'USER')) as party_type from "
			+ "ACL_GROUP ag inner join ACL_RESPONSIBILITY_SCOPE_ENTRY arse on ag.ID = arse.ACL_GROUP_ID "
			+ "inner join ACL_OBJECT_IDENTITY oid on oid.ID = arse.OBJECT_IDENTITY_ID "
			+ "inner join ACL_CLASS ac on ac.ID = oid.CLASS_ID " 
			+ "left outer join CORE_USER cu on arse.PARTY_ID = cu.PARTY_ID "
			+ "left outer join CORE_TEAM ct on arse.PARTY_ID = ct.PARTY_ID "
			+ "where oid.IDENTITY = ? and ac.CLASSNAME = ? ";
	
	//11-02-13 : this query is ready for task 1865 
	private static final String USER_AND_ACL_GROUP_NAME_FROM_IDENTITY_AND_CLASS_FILTERED = "select arse.PARTY_ID, ag.ID, ag.QUALIFIED_NAME, CONCAT(IFNULL(cu.LOGIN, ''), IFNULL(ct.NAME, '')) as sorting_key, CONCAT(IFNULL(cu.LOGIN, 'TEAM'), IFNULL(ct.NAME, 'USER')) as party_type from "
			+ "ACL_GROUP ag inner join ACL_RESPONSIBILITY_SCOPE_ENTRY arse on ag.ID = arse.ACL_GROUP_ID "
			+ "inner join ACL_OBJECT_IDENTITY oid on oid.ID = arse.OBJECT_IDENTITY_ID "
			+ "inner join ACL_CLASS ac on ac.ID = oid.CLASS_ID " 
			+ "left outer join CORE_USER cu on arse.PARTY_ID = cu.PARTY_ID "
			+ "left outer join CORE_TEAM ct on arse.PARTY_ID = ct.PARTY_ID "
			+ "where oid.IDENTITY = ? and ac.CLASSNAME = ? "
			+ "and cu.LOGIN like ? or ct.name = ?";

	private static final String DELETE_PARTY_RESPONSABILITY_ENTRY = "delete from ACL_RESPONSIBILITY_SCOPE_ENTRY "
			+ "where PARTY_ID = ?  and OBJECT_IDENTITY_ID = "
			+ "(select oid.ID from ACL_OBJECT_IDENTITY oid  inner join ACL_CLASS c on c.ID = oid.CLASS_ID "
			+ "where oid.IDENTITY = ? and c.CLASSNAME = ?)";
	
	private static final String DELETE_RESPONSABILITY_ENTRY = "delete from ACL_RESPONSIBILITY_SCOPE_ENTRY "
			+ "where PARTY_ID = (select PARTY_ID from CORE_USER where login = ?)  and OBJECT_IDENTITY_ID = "
			+ "(select oid.ID from ACL_OBJECT_IDENTITY oid  inner join ACL_CLASS c on c.ID = oid.CLASS_ID "
			+ "where oid.IDENTITY = ? and c.CLASSNAME = ?)";

	private static final String FIND_OBJECT_WITHOUT_PERMISSION = "select nro.IDENTITY from ACL_OBJECT_IDENTITY nro "
			+ "inner join ACL_CLASS nrc on nro.CLASS_ID = nrc.ID " 
			+ "where nrc.CLASSNAME = ? "
			+ "and not exists (select 1 "
			+ "from ACL_OBJECT_IDENTITY ro "
			+ "inner join ACL_CLASS rc on rc.ID = ro.CLASS_ID "
			+ "inner join ACL_RESPONSIBILITY_SCOPE_ENTRY r on r.OBJECT_IDENTITY_ID = ro.ID "
			+ "inner join CORE_USER u on u.PARTY_ID = r.PARTY_ID "
			+ "where ro.ID = nro.ID and rc.ID = nrc.ID and u.LOGIN = ?) ";

	private static final String FIND_OBJECT_WITHOUT_PERMISSION_BY_PARTY = "select nro.IDENTITY from ACL_OBJECT_IDENTITY nro "
			+ "inner join ACL_CLASS nrc on nro.CLASS_ID = nrc.ID " 
			+ "where nrc.CLASSNAME = ? "
			+ "and not exists (select 1 "
			+ "from ACL_OBJECT_IDENTITY ro "
			+ "inner join ACL_CLASS rc on rc.ID = ro.CLASS_ID "
			+ "inner join ACL_RESPONSIBILITY_SCOPE_ENTRY r on r.OBJECT_IDENTITY_ID = ro.ID "
			+ "where ro.ID = nro.ID and rc.ID = nrc.ID and r.PARTY_ID = ?) ";
	
	private static final String FIND_USERS_WITHOUT_PERMISSION_BY_OBJECT = "select u.PARTY_ID from CORE_USER u "
			+ "where not exists (select 1  from ACL_OBJECT_IDENTITY aoi "
			+ "inner join ACL_CLASS ac on ac.ID = aoi.CLASS_ID "
			+ "inner join ACL_RESPONSIBILITY_SCOPE_ENTRY arse on arse.OBJECT_IDENTITY_ID = aoi.ID "
			+ "where u.PARTY_ID = arse.PARTY_ID  and ac.CLASSNAME = ?  and aoi.IDENTITY = ?) ";

	private static final String FIND_PARTIES_WITHOUT_PERMISSION_BY_OBJECT = "select p.PARTY_ID from CORE_PARTY p "
			+ "where not exists (select 1  from ACL_OBJECT_IDENTITY aoi "
			+ "inner join ACL_CLASS ac on ac.ID = aoi.CLASS_ID "
			+ "inner join ACL_RESPONSIBILITY_SCOPE_ENTRY arse on arse.OBJECT_IDENTITY_ID = aoi.ID "
			+ "where p.PARTY_ID = arse.PARTY_ID  and ac.CLASSNAME = ?  and aoi.IDENTITY = ?) ";
	
	private static final String DELETE_OBJECT_IDENTITY = "delete from ACL_OBJECT_IDENTITY where IDENTITY = ? and CLASS_ID = ?";
	
	private static final String DELETE_ALL_RESPONSABILITY_ENTRIES = "delete from ACL_RESPONSIBILITY_SCOPE_ENTRY "
			+ "where OBJECT_IDENTITY_ID = (select oid.ID from ACL_OBJECT_IDENTITY oid "
			+ "inner join ACL_CLASS c on c.ID = oid.CLASS_ID "
			+ "where oid.IDENTITY = ? and c.CLASSNAME = ?)";
	
	private static final String DELETE_ALL_RESPONSABILITY_ENTRIES_FOR_PARTY = "delete from ACL_RESPONSIBILITY_SCOPE_ENTRY "
			+ "where PARTY_ID = ?";

	public JdbcManageableAclService(DataSource dataSource, LookupStrategy lookupStrategy) {
		super(dataSource, lookupStrategy);
	}

	/* (non-Javadoc)
	 * @see org.squashtest.tm.service.security.acls.jdbc.ManageableAclService#createObjectIdentity(org.springframework.security.acls.model.ObjectIdentity)
	 */
	@Override
	public void createObjectIdentity(@NotNull ObjectIdentity objectIdentity) throws AlreadyExistsException {
		LOGGER.info("Attempting to create the Object Identity " + objectIdentity);

		checkObjectIdentityDoesNotExist(objectIdentity);

		long classId = retrieveClassPrimaryKey(objectIdentity.getType());

		createObjectIdentity(objectIdentity.getIdentifier(), classId);

	}

	private void createObjectIdentity(Serializable objectIdentifier, long classId) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Will attempt to perform '" + INSERT_OBJECT_IDENTITY + "' with args [" + objectIdentifier
					+ ',' + classId + ']');
		}
		jdbcTemplate.update(INSERT_OBJECT_IDENTITY, objectIdentifier, classId);
	}

	private void checkObjectIdentityDoesNotExist(ObjectIdentity objectIdentity) {
		if (retrieveObjectIdentityPrimaryKey(objectIdentity) != null) {
			throw new AlreadyExistsException("Object identity '" + objectIdentity + "' already exists");
		}
	}

	private Long retrieveClassPrimaryKey(String type) throws UnknownAclClassException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Will attempt to perform '" + SELECT_CLASS_PRIMARY_KEY + "' with args [" + type + ']');
		}
		List<Long> classIds = jdbcTemplate.queryForList(SELECT_CLASS_PRIMARY_KEY, new Object[] { type }, Long.class);

		if (!classIds.isEmpty()) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Found classId " + classIds.get(0));
			}
			return classIds.get(0);
		}

		throw new UnknownAclClassException(type);
	}

	private Long retrieveObjectIdentityPrimaryKey(ObjectIdentity objectIdentity) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Will attempt to perform '" + SELECT_OBJECT_IDENTITY_PRIMARY_KEY + "' with args ["
					+ objectIdentity.getType() + ',' + objectIdentity.getIdentifier() + ']');
		}

		try {
			return Long.valueOf(jdbcTemplate.queryForLong(SELECT_OBJECT_IDENTITY_PRIMARY_KEY, new Object[] {
					objectIdentity.getType(), objectIdentity.getIdentifier() }));
		} catch (DataAccessException notFound) {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.squashtest.tm.service.security.acls.jdbc.ManageableAclService#findAllPermissionGroupsByNamespace(java.lang.String)
	 */
	@Override
	public List<PermissionGroup> findAllPermissionGroupsByNamespace(@NotNull String namespace) {
		return jdbcTemplate.query(FIND_ALL_ACL_GROUPS_BY_NAMESPACE, new Object[] { namespace + '%' },
				permissionGroupMapper);
	}

	/**
	 * Removes all responsibilities a user might have on a entity. In other words, the given user will no longer have
	 * any permission on the entity.
	 * 
	 * @param userLogin
	 * @param objectIdentity
	 */
	@Override
	public void removeAllResponsibilities(@NotNull String userLogin, @NotNull ObjectIdentity entityRef) {
		jdbcTemplate.update(DELETE_RESPONSABILITY_ENTRY,
				new Object[] { userLogin, entityRef.getIdentifier(), entityRef.getType() });

		evictFromCache(entityRef);
	}
	
	public void removeAllResponsibilities(@NotNull long partyId, @NotNull ObjectIdentity entityRef) {
		jdbcTemplate.update(DELETE_PARTY_RESPONSABILITY_ENTRY,
				new Object[] { partyId, entityRef.getIdentifier(), entityRef.getType() });

		evictFromCache(entityRef);
	}

	/* (non-Javadoc)
	 * @see org.squashtest.tm.service.security.acls.jdbc.ManageableAclService#addNewResponsibility(java.lang.String, org.springframework.security.acls.model.ObjectIdentity, java.lang.String)
	 */
	@Override
	public void addNewResponsibility(@NotNull String userLogin, @NotNull ObjectIdentity entityRef,
			@NotNull String qualifiedName) {
		removeAllResponsibilities(userLogin, entityRef);
		insertResponsibility(userLogin, entityRef, qualifiedName);
	}

	@Override
	public void addNewResponsibility(@NotNull long partyId, @NotNull ObjectIdentity entityRef,
			@NotNull String qualifiedName) {
		removeAllResponsibilities(partyId, entityRef);
		insertResponsibility(partyId, entityRef, qualifiedName);
	}
	
	private void insertResponsibility(String userLogin, ObjectIdentity entityRef, String permissionGroupName) {
		jdbcTemplate.update(INSERT_ACL_RESPONSABILITY_SCOPE,
				new Object[] { userLogin, permissionGroupName, entityRef.getType(), entityRef.getIdentifier() });

		evictFromCache(entityRef);
	}

	private void insertResponsibility(long partyId, ObjectIdentity entityRef, String permissionGroupName) {
		jdbcTemplate.update(INSERT_PARTY_ACL_RESPONSABILITY_SCOPE,
				new Object[] { partyId, permissionGroupName, entityRef.getType(), entityRef.getIdentifier() });

		evictFromCache(entityRef);
	}
	/* (non-Javadoc)
	 * @see org.squashtest.tm.service.security.acls.jdbc.ManageableAclService#retrieveClassAclGroupFromUserLogin(java.lang.String, java.lang.String)
	 */
	@Override
	public List<Object[]> retrieveClassAclGroupFromUserLogin(@NotNull String userLogin, String qualifiedClassName) {
		return jdbcTemplate.query(FIND_ACL_FOR_CLASS_FROM_USER, new Object[] { userLogin, qualifiedClassName },
				AclGroupMapper);
	}

	
	@Override
	public List<Object[]> retrieveClassAclGroupFromPartyId(@NotNull long partyId, String qualifiedClassName) {

		return jdbcTemplate.query(FIND_ACL_FOR_CLASS_FROM_PARTY, new Object[] { partyId, qualifiedClassName },
				AclGroupMapper);
	}
	
	/* (non-Javadoc)
	 * @see org.squashtest.tm.service.security.acls.jdbc.ManageableAclService#findObjectWithoutPermissionByLogin(java.lang.String, java.lang.String)
	 */
	@Override
	public List<Long> findObjectWithoutPermissionByLogin(String userLogin, String qualifiedClass) {
		List<BigInteger> reslult = jdbcTemplate.queryForList(FIND_OBJECT_WITHOUT_PERMISSION, new Object[] {
				qualifiedClass, userLogin }, BigInteger.class);
		List<Long> finalResult = new ArrayList<Long>();
		for (BigInteger bigInteger : reslult) {
			finalResult.add(bigInteger.longValue());
		}
		return finalResult;
	}

	
	@Override
	public List<Long> findObjectWithoutPermissionByPartyId(long partyId, String qualifiedClass) {
		List<BigInteger> reslult = jdbcTemplate.queryForList(FIND_OBJECT_WITHOUT_PERMISSION_BY_PARTY, new Object[] {
				qualifiedClass, partyId }, BigInteger.class);
		List<Long> finalResult = new ArrayList<Long>();
		for (BigInteger bigInteger : reslult) {
			finalResult.add(bigInteger.longValue());
		}
		return finalResult;
	}
	
	/* (non-Javadoc)
	 * @see org.squashtest.tm.service.security.acls.jdbc.ManageableAclService#findUsersWithExecutePermission(java.util.List)
	 */
	@Override
	public List<String> findUsersWithExecutePermission(List<ObjectIdentity> entityRefs) {
		List<Permission> permissions = new ArrayList<Permission>();
		permissions.add(CustomPermission.EXECUTE);
		return findUsersWithPermissions(entityRefs, permissions);
	}

	private List<String> findUsersWithPermissions(List<ObjectIdentity> entityRefs, List<Permission> permissionsList) {
		List<String> resultSidList = new ArrayList<String>();
		Collection<Acl> aclList;
		try {
			aclList = readAclsById(entityRefs).values();
		} catch (NotFoundException nfe) {
			LOGGER.debug("Acl not found for entities.");
			aclList = Collections.emptyList();
		}
		for (Acl acl : aclList) {
			List<AccessControlEntry> aces = acl.getEntries();

			for (AccessControlEntry ctrlEntry : aces) {

				List<Sid> sids = new ArrayList<Sid>();
				List<Permission> permissions = new ArrayList<Permission>();
				for (Permission permission : permissionsList) {
					permissions.add(permission);
				}
				sids.add(ctrlEntry.getSid());
				try {
					if (acl.isGranted(permissions, sids, false)) {
						PrincipalSid principalSid = (PrincipalSid) ctrlEntry.getSid();
						if (!resultSidList.contains(principalSid.getPrincipal())) {
							resultSidList.add(principalSid.getPrincipal());
						}
					}
				} catch (Exception e) {
					LOGGER.warn("Error while processing acl list ", e);
					continue;
				}
			}
		}

		return resultSidList;
	}

	/* (non-Javadoc)
	 * @see org.squashtest.tm.service.security.acls.jdbc.ManageableAclService#findUsersWithWritePermission(java.util.List)
	 */
	@Override
	public List<String> findUsersWithWritePermission(@NotNull List<ObjectIdentity> entityRefs) {
		List<Permission> permissions = new ArrayList<Permission>();
		permissions.add(BasePermission.WRITE);
		return findUsersWithPermissions(entityRefs, permissions);
	}

	protected void evictFromCache(ObjectIdentity oIdentity) {
		if (aclCache != null) {
			aclCache.evictFromCache(oIdentity);
		}
	}

	/* (non-Javadoc)
	 * @see org.squashtest.tm.service.security.acls.jdbc.ManageableAclService#retriveUserAndAclGroupNameFromIdentityAndClass(long, java.lang.Class)
	 */
    @Override
    public List<Object[]> retriveUserAndAclGroupNameFromIdentityAndClass(long entityId, Class<?> entityClass) {
            return jdbcTemplate.query(USER_AND_ACL_GROUP_NAME_FROM_IDENTITY_AND_CLASS, new Object[] { entityId, entityClass.getCanonicalName() },
                            AclGroupMapper);

    }

    
    @Override
    public List<Object[]> retrievePartyAndAclGroupNameFromIdentityAndClass(long entityId, Class<?> entityClass) {
            return jdbcTemplate.query(USER_AND_ACL_GROUP_NAME_FROM_IDENTITY_AND_CLASS, new Object[] { entityId, entityClass.getCanonicalName() },
                            AclGroupMapper);

    }
    
	/* (non-Javadoc)
	 * @see org.squashtest.tm.service.security.acls.jdbc.ManageableAclService#findUsersWithoutPermissionByObject(long, java.lang.String)
	 */
	@Override
	public List<Object[]> retriveUserAndAclGroupNameFromIdentityAndClass(long entityId, Class<?> entityClass, Sorting sorting, Filtering filtering) {
		
		String baseQuery;
		String orderByClause;
		Object[] arguments;
		
		if (filtering.isDefined()){ 
			baseQuery = USER_AND_ACL_GROUP_NAME_FROM_IDENTITY_AND_CLASS_FILTERED;
			String filter = "%"+filtering.getFilter()+"%";
			arguments = new Object[]{entityId, entityClass.getCanonicalName(), filter, filter};
		}
		else{
			baseQuery = USER_AND_ACL_GROUP_NAME_FROM_IDENTITY_AND_CLASS;
			arguments = new Object[]{entityId, entityClass.getCanonicalName()};
		}
	
		if (sorting.getSortedAttribute().equals("login")){
			orderByClause=" order by sorting_key ";
		}
		else{
			orderByClause=" order by ag.QUALIFIED_NAME ";
		}
		orderByClause+= sorting.getSortOrder().getCode();
		
		
		String finalQuery = baseQuery + orderByClause;
		
		return jdbcTemplate.query(finalQuery, arguments , AclGroupMapper);

	}

	@Override
	public List<Object[]> retrievePartyAndAclGroupNameFromIdentityAndClass(long entityId, Class<?> entityClass, Sorting sorting, Filtering filtering) {
		
		String baseQuery;
		String orderByClause;
		Object[] arguments;
		
		if (filtering.isDefined()){ 
			baseQuery = USER_AND_ACL_GROUP_NAME_FROM_IDENTITY_AND_CLASS_FILTERED;
			String filter = "%"+filtering.getFilter()+"%";
			arguments = new Object[]{entityId, entityClass.getCanonicalName(), filter, filter};
		}
		else{
			baseQuery = USER_AND_ACL_GROUP_NAME_FROM_IDENTITY_AND_CLASS;
			arguments = new Object[]{entityId, entityClass.getCanonicalName()};
		}
		
		if (sorting.getSortedAttribute().equals("name")){
			orderByClause=" order by sorting_key ";
		}
		else if(sorting.getSortedAttribute().equals("qualifiedName")){
			orderByClause=" order by ag.QUALIFIED_NAME ";
		} else {
			orderByClause=" order by party_type ";
		}
		orderByClause+= sorting.getSortOrder().getCode();
		
		
		String finalQuery = baseQuery + orderByClause;
		
		return jdbcTemplate.query(finalQuery, arguments , AclGroupMapper);

	}
	
	@Override
	public List<Long> findUsersWithoutPermissionByObject(long objectId, String qualifiedClassName) {
		List<BigInteger> result = jdbcTemplate.queryForList(FIND_USERS_WITHOUT_PERMISSION_BY_OBJECT, new Object[] {
				qualifiedClassName, objectId }, BigInteger.class);
		List<Long> finalResult = new ArrayList<Long>();
		for (BigInteger bigInteger : result) {
			finalResult.add(bigInteger.longValue());
		}
		return finalResult;
	}

	@Override
	public List<Long> findPartiesWithoutPermissionByObject(long objectId, String qualifiedClassName) {
		List<BigInteger> result = jdbcTemplate.queryForList(FIND_PARTIES_WITHOUT_PERMISSION_BY_OBJECT, new Object[] {
				qualifiedClassName, objectId }, BigInteger.class);
		List<Long> finalResult = new ArrayList<Long>();
		for (BigInteger bigInteger : result) {
			finalResult.add(bigInteger.longValue());
		}
		return finalResult;
	}
	
	/* (non-Javadoc)
	 * @see org.squashtest.tm.service.security.acls.jdbc.ManageableAclService#removeObjectIdentity(org.springframework.security.acls.model.ObjectIdentity)
	 */
	public void removeObjectIdentity(ObjectIdentity objectIdentity) {
		LOGGER.info("Attempting to delete the Object Identity " + objectIdentity);

		long classId = retrieveClassPrimaryKey(objectIdentity.getType());

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Will attempt to perform '" + DELETE_OBJECT_IDENTITY + "' with args ["
					+ objectIdentity.getIdentifier() + ',' + classId + ']');
		}
		jdbcTemplate.update(DELETE_OBJECT_IDENTITY, objectIdentity.getIdentifier(), classId);
		evictFromCache(objectIdentity);
	}

	/* (non-Javadoc)
	 * @see org.squashtest.tm.service.security.acls.jdbc.ManageableAclService#removeAllResponsibilities(org.springframework.security.acls.model.ObjectIdentity)
	 */
	@Override
	public void removeAllResponsibilities(ObjectIdentity entityRef) {
		jdbcTemplate.update(DELETE_ALL_RESPONSABILITY_ENTRIES,
				new Object[] { entityRef.getIdentifier(), entityRef.getType() });

		evictFromCache(entityRef);
	}

	@Override
	public void removeAllResponsibilitiesForParty(long partyId) {
		jdbcTemplate.update(DELETE_ALL_RESPONSABILITY_ENTRIES_FOR_PARTY, partyId);
		
	}

}
