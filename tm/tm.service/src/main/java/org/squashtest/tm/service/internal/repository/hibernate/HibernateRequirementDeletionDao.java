/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.service.internal.repository.hibernate;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.event.RequirementAuditEvent;
import org.squashtest.tm.service.internal.repository.RequirementDeletionDao;

@Repository
public class HibernateRequirementDeletionDao extends HibernateDeletionDao implements RequirementDeletionDao {

	private static final String NODE_IDS = "nodeIds";
	private static final String REQUIREMENT_VERSION_IDS = "requirementVersionIds";
	private static final String RESOURCE_IDS = "resourceIds";	
	private static final String SIMPLE_RESOURCE_IDS = "simpleResourceIds";
	private static final String REQUIREMENT_IDS = "requirementIds";
	private static final String FOLDER_IDS = "folderIds";

	
	// note 1 : this method will be ran twice per batch : one for folder deletion, one for requirement deletion
	// ( is is so because two distincts calls to #deleteNodes, see RequirementDeletionHandlerImpl#deleteNodes() )
	// It should run fine tho, at the cost of a few useless extra queries.
	
	// note 2 : the code below must handle the references of requirements and requirement folders to 
	// their Resource and SimpleResource, making the thing a lot more funny and pleasant to maintain.
	@Override
	public void removeEntities(List<Long> entityIds) {
		if (!entityIds.isEmpty()) {
			
			// Unbinds the nodes from their parent container
			removeNodesFromFolders(entityIds);
			executeDeleteSQLQuery(NativeQueries.REQUIREMENT_SQL_REMOVE_FROM_LIBRARY, REQUIREMENT_IDS, entityIds);
			
			// Retrieval of the Resource or SimpleResource that those entities refers to.
			List<Long> requirementVersionIds = findAllVersionsIdsFromRequirements(entityIds);
			List<Long> folderResourceIds = findAllSimpleResourceIdsFromFolders(entityIds);

			// Remove the foreign keys between the Requirements and their Resource, and between the RequirementFolders 
			// and their SimpleResource
			executeDeleteSQLQuery(NativeQueries.REQUIREMENT_SET_NULL_REQUIREMENT_VERSION, REQUIREMENT_IDS, entityIds);
			executeDeleteSQLQuery(NativeQueries.REQUIREMENT_FOLDER_SET_NULL_SIMPLE_RESOURCE, FOLDER_IDS, entityIds);

			// Delete the resources
			executeDeleteSQLQuery(NativeQueries.REQUIREMENT_VERSION_SQL_REMOVE, REQUIREMENT_VERSION_IDS,requirementVersionIds);
			executeDeleteSQLQuery(NativeQueries.RESOURCE_SQL_REMOVE, RESOURCE_IDS, requirementVersionIds);
			
			executeDeleteSQLQuery(NativeQueries.SIMPLE_RESOURCE_SQL_REMOVE, SIMPLE_RESOURCE_IDS, folderResourceIds);
			executeDeleteSQLQuery(NativeQueries.RESOURCE_SQL_REMOVE, RESOURCE_IDS, folderResourceIds);	
			
			// Now we can remove the nodes
			executeDeleteSQLQuery(NativeQueries.REQUIREMENT_SQL_REMOVE, NODE_IDS, entityIds);
			executeDeleteSQLQuery(NativeQueries.REQUIREMENT_FOLDER_SQL_REMOVE, NODE_IDS, entityIds);
			executeDeleteSQLQuery(NativeQueries.REQUIREMENTLIBRARYNODE_SQL_REMOVE, NODE_IDS, entityIds);
		

		}
	}

	private List<Long> findAllVersionsIdsFromRequirements(List<Long> entityIds) {
		List<BigInteger> requirementVersionIdsBigInt = executeSelectSQLQuery(
				NativeQueries.REQUIREMENT_VERSION_FIND_ID_FROM_REQUIREMENT, REQUIREMENT_IDS, entityIds);

		List<Long> requirementVersionIds = new ArrayList<Long>(requirementVersionIdsBigInt.size());

		for (BigInteger bigIntId : requirementVersionIdsBigInt) {
			requirementVersionIds.add(bigIntId.longValue());
		}

		return requirementVersionIds;
	}
	
	private List<Long> findAllSimpleResourceIdsFromFolders(List<Long> entityIds){
		List<BigInteger> folderResourceIdsBigInt = executeSelectSQLQuery(NativeQueries.SIMPLE_RESOURCE_FIND_ID_FROM_FOLDER, FOLDER_IDS, entityIds);
		
		List<Long> folderResourceIds = new ArrayList<Long>(folderResourceIdsBigInt.size());

		for (BigInteger bigIntId : folderResourceIdsBigInt) {
			folderResourceIds.add(bigIntId.longValue());
		}

		return folderResourceIds;
	}

	private void removeNodesFromFolders(List<Long> entityIds) {
		Query query = getSession().createSQLQuery(NativeQueries.REQUIREMENT_SQL_REMOVE_FROM_FOLDER);
		query.setParameterList("ancIds", entityIds, LongType.INSTANCE);
		query.setParameterList("descIds", entityIds, LongType.INSTANCE);
		query.executeUpdate();
	}
	
	
	@Override
	public List<Long>[] separateFolderFromRequirementIds(List<Long> originalIds) {

		List<Long> folderIds = new ArrayList<Long>();
		List<Long> requirementIds = new ArrayList<Long>();
		
		List<BigInteger> _folderIds = executeSelectSQLQuery(
						NativeQueries.REQUIREMENTLIBRARYNODE_SQL_FILTERFOLDERIDS, REQUIREMENT_IDS, originalIds);
		
		for (Long oId : originalIds){
			if (_folderIds.contains(BigInteger.valueOf(oId))){
				folderIds.add(oId);
			}
			else{
				requirementIds.add(oId);
			}
		}
		
		List<Long>[] result = new List[2];
		result[0] = folderIds;
		result[1] = requirementIds;
		
		return result;
	}
	
	

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> findRequirementAttachmentListIds(List<Long> requirementIds) {
		if (!requirementIds.isEmpty()) {
			Query query = getSession().getNamedQuery("requirement.findAllAttachmentLists");
			query.setParameterList(REQUIREMENT_IDS, requirementIds);
			return query.list();
		}
		return Collections.emptyList();
	}

	
	@Override
	public List<Long> findRequirementFolderAttachmentListIds(
			List<Long> folderIds) {
		if (!folderIds.isEmpty()) {
			Query query = getSession().getNamedQuery("requirementFolder.findAllAttachmentLists");
			query.setParameterList(FOLDER_IDS, folderIds);
			return query.list();
		}
		return Collections.emptyList();
	}
	
	
	@Override
	public void removeFromVerifiedRequirementLists(List<Long> requirementIds) {
		if (!requirementIds.isEmpty()) {
			executeDeleteSQLQuery(NativeQueries.REQUIREMENT_SQL_REMOVEFROMVERIFIEDREQUIREMENTLISTS, REQUIREMENT_IDS,
					requirementIds);
		}

	}

	@Override
	public void removeTestStepsCoverageByRequirementVersionIds(List<Long> requirementVersionIds) {
		if (!requirementVersionIds.isEmpty()) {
			executeDeleteSQLQuery(NativeQueries.REQUIREMENT_SQL_REMOVE_TEST_STEP_COVERAGE_BY_REQ_VERSION_IDS, "versionIds",
					requirementVersionIds);
		}

	}

	@Override
	public void deleteRequirementAuditEvents(List<Long> requirementIds) {
		if (!requirementIds.isEmpty()) {
			// we borrow the following from RequirementAuditDao
			List<RequirementAuditEvent> events = executeSelectNamedQuery(
					"requirementAuditEvent.findAllByRequirementIds", "ids", requirementIds);

			// because Hibernate sucks so much at polymorphic bulk delete, we're going to remove
			// them one by one.
			for (RequirementAuditEvent event : events) {
				removeEntity(event);
			}

			flush();
		}

	}
	
	@Override
	public List<Long> findVersionIds(List<Long> requirementIds) {
		return executeSelectNamedQuery("requirementDeletionDao.findVersionIds", "reqIds", requirementIds);
	}

}
