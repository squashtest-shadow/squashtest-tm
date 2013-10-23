/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.service.internal.repository.TeamDao;
import org.squashtest.tm.service.internal.repository.UserDao;
import org.squashtest.tm.service.internal.repository.hibernate.SqLIdResultTransformer;

@Component
class DerivedPermissionsManager {
	
	private static final String PROJECT_CLASS_NAME = Project.class.getName();
	
	
	private static final String FIND_TEAM_MEMBERS_OR_USER = 
			"select cu.PARTY_ID from CORE_USER cu " +
			"where cu.PARTY_ID = :id " +
			"UNION " +
			"select cu.PARTY_ID from CORE_USER cu "+
			"inner join CORE_TEAM_MEMBER ctm on ctm.USER_ID = cu.PARTY_ID "+
			"inner join CORE_TEAM ct on ct.PARTY_ID = ctm.TEAM_ID "+
			"where ct.PARTY_ID = :id";

	private static final String REMOVE_CORE_PARTY_MANAGER_AUTHORITY = "delete from CORE_PARTY_AUTHORITY where PARTY_ID in (:ids) and AUTHORITY = 'ROLE_TM_PROJECT_MANAGER'";
	private static final String INSERT_CORE_PARTY_MANAGER_AUTHORITY = "insert into CORE_PARTY_AUTHORITY(PARTY_ID, AUTHORITY) values (:id, 'ROLE_TM_PROJECT_MANAGER')";


	private static final String FIND_PARTIES_USING_IDENTITY = 
			"select arse.PARTY_ID from ACL_RESPONSIBILITY_SCOPE_ENTRY arse " +
	 		"inner join ACL_OBJECT_IDENTITY aoi on arse.OBJECT_IDENTITY_ID = aoi.IDENTITY "+
	 		"inner join ACL_CLASS acc on aoi.CLASS_ID = acc.ID " +
	 		"inner join ACL_GROUP_PERMISSION acp on acp.ACL_GROUP_ID = arse.ACL_GROUP_ID " +
	 		"where acp.CLASS_ID = acc.ID " +
	 		"and aoi.IDENTITY = :id and acc.CLASSNAME = :class ";
	
	
	
	private static final String RETAIN_USERS_MANAGING_ANYTHING =  
			"select arse.PARTY_ID from ACL_RESPONSIBILITY_SCOPE_ENTRY arse " +
	 		"inner join ACL_OBJECT_IDENTITY aoi on arse.OBJECT_IDENTITY_ID = aoi.IDENTITY "+
	 		"inner join ACL_CLASS acc on aoi.CLASS_ID = acc.ID " +
	 		"inner join ACL_GROUP_PERMISSION acp on acp.ACL_GROUP_ID = arse.ACL_GROUP_ID " +
	 		"where acp.CLASS_ID = acc.ID and acp.PERMISSION_MASK = 32 " +
	 		"and acc.CLASSNAME in ('org.squashtest.tm.domain.project.Project', 'org.squashtest.tm.domain.project.ProjectTemplate') " +
	 		"and arse.PARTY_ID in (:ids)" ;

	
	private static final String RETAIN_MEMBERS_OF_TEAMS_MANAGING_ANYTHING = 
			"select cu.PARTY_ID from CORE_USER cu " +
			"inner join CORE_TEAM_MEMBER ctm on ctm.USER_ID = cu.PARTY_ID " +
			"inner join ACL_RESPONSIBILITY_SCOPE_ENTRY arse on arse.PARTY_ID = ctm.TEAM_ID "+
			"inner join ACL_OBJECT_IDENTITY aoi on arse.OBJECT_IDENTITY_ID = aoi.IDENTITY "+
	 		"inner join ACL_CLASS acc on aoi.CLASS_ID = acc.ID " +
	 		"inner join ACL_GROUP_PERMISSION acp on acp.ACL_GROUP_ID = arse.ACL_GROUP_ID " +
	 		"where acp.CLASS_ID = acc.ID and acp.PERMISSION_MASK = 32 " +
	 		"and acc.CLASSNAME in ('org.squashtest.tm.domain.project.Project', 'org.squashtest.tm.domain.project.ProjectTemplate') " +
	 		"and cu.PARTY_ID in (:ids)" ;
	
	@Inject
	private SessionFactory sessionFactory; 
	
	@Inject 
	private TeamDao teamDao;
	
	@Inject 
	private UserDao userDao;
	
	
	
	
	void recomputeDerivedPermissions(ObjectIdentity identity){
		recomputeDerivedAcl(identity);
		recomputeDerivedAuths(identity);
	}
	
	void recomputeDerivedPermissions(long partyId){
		recomputeDerivedAcl(partyId);
		recomputeDerivedAuths(partyId);
	}
	

	void recomputeDerivedPermissions(long partyId, ObjectIdentity identity){
		recomputeDerivedAcl(partyId, identity);
		recomputeDerivedAuths(partyId, identity);
	}
	
	
	

	
	// *************************** private ******************************
	
	
	void recomputeDerivedAuths(ObjectIdentity identity){
		
		if (! isSortOfProject(identity)){
			return;
		}
		
		Collection<Long> userIds = findUsers(identity);
		
		removeProjectManagerAuthorities(userIds);
		
		Collection<Long> managerIds = retainUsersManagingAnything(userIds);
		
		grantProjectManagerAuthorities(managerIds);
		
	}
	
	void recomputeDerivedAuths(long partyId){

		Collection<Long> memberIds = findMembers(partyId);
		
		removeProjectManagerAuthorities(memberIds);
		
		Collection<Long> managerIds = retainUsersManagingAnything(memberIds);
		
		grantProjectManagerAuthorities(managerIds);
		
	}
	
	void recomputeDerivedAuths(long partyId, ObjectIdentity identity){
		
		if (! isSortOfProject(identity)){
			return;
		}
		
		recomputeDerivedAcl(identity);
		
	}
	
	
	void recomputeDerivedAcl(ObjectIdentity identity){
		//nothing yet
	}
	
	void recomputeDerivedAcl(long partyId){
		// nothing yet
	}
	
	void recomputeDerivedAcl(long partyId, ObjectIdentity identity){
		// nothing yet
	}
	
	
	// ******************************** helpers ***********************************
	
	/*
	 *  will help to cut some uneeded computations and DB calls. For now only project-level permissions induces derived permissions.
	 *  also, remember that : DONT BE SHY and modify/remove it if the permission management specs ever changes, instead of working around !
	 */
	private boolean isSortOfProject(ObjectIdentity identity){
		String type = identity.getType();
		return (type.equals(PROJECT_CLASS_NAME));
	}

	
	// will find all members of a team given its id. It the id actually refers to a user, that user id will be the only result.
	private Collection<Long> findMembers(long partyId){
		
		Query query = sessionFactory.getCurrentSession().createSQLQuery(FIND_TEAM_MEMBERS_OR_USER);
		query.setParameter("id", partyId, LongType.INSTANCE);
		query.setResultTransformer(new SqLIdResultTransformer());
		return query.list();	
		
	}
	
	// will find all the users 
	private Collection<Long> findUsers(ObjectIdentity identity){
		
		// first find the parties managing that thing
		Query query = sessionFactory.getCurrentSession().createSQLQuery(FIND_PARTIES_USING_IDENTITY);
		query.setParameter("id", identity.getIdentifier(), LongType.INSTANCE);
		query.setParameter("class", identity.getType(), StringType.INSTANCE);
		query.setResultTransformer(new SqLIdResultTransformer());
		
		Collection<Long> partyIds = query.list();
		
		// then find the corresponding users
		Collection<Long> userIds = new HashSet<Long>();
		for (Long id : partyIds){
			userIds.addAll(findMembers(id));
		}
		
		return userIds;
		
	}
	
	private void removeProjectManagerAuthorities(Collection<Long> ids){
		if (! ids.isEmpty()){
			Query query = sessionFactory.getCurrentSession().createSQLQuery(REMOVE_CORE_PARTY_MANAGER_AUTHORITY);
			query.setParameterList("ids", ids, LongType.INSTANCE);
			query.executeUpdate();
		}
	}
	
	private Collection<Long> retainUsersManagingAnything(Collection<Long> ids){
		if (! ids.isEmpty()){
			
			Set<Long> userIds = new HashSet<Long>();
			Collection<Long> buffer;
			
			// first, get users directly managing anything 
			Query query = sessionFactory.getCurrentSession().createSQLQuery(RETAIN_USERS_MANAGING_ANYTHING);
			query.setParameterList("ids", ids, LongType.INSTANCE);
			query.setResultTransformer(new SqLIdResultTransformer());
			
			buffer = query.list();
			userIds.addAll(buffer);
			
			// second, get users managing through teams or project leaders (which sounds quite silly I agree)
			query = sessionFactory.getCurrentSession().createSQLQuery(RETAIN_MEMBERS_OF_TEAMS_MANAGING_ANYTHING);
			query.setParameterList("ids", ids, LongType.INSTANCE);
			query.setResultTransformer(new SqLIdResultTransformer());
			
			buffer = query.list();
			userIds.addAll(buffer);			
			
			return userIds;
		}
		else{
			return Collections.emptyList();
		}
	}
	
	private void grantProjectManagerAuthorities(Collection<Long> ids){
		Query query;
		for (Long id : ids){
			query = sessionFactory.getCurrentSession().createSQLQuery(INSERT_CORE_PARTY_MANAGER_AUTHORITY);
			query.setParameter("id", id, LongType.INSTANCE);
			query.executeUpdate();
		}
		
		
		
	}
	
}
