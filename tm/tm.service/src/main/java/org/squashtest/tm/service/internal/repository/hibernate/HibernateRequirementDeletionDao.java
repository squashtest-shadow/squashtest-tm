/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
import java.util.ListIterator;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.event.RequirementAuditEvent;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.service.internal.repository.ParameterNames;
import org.squashtest.tm.service.internal.repository.RequirementDeletionDao;

@Repository
public class HibernateRequirementDeletionDao extends HibernateDeletionDao implements RequirementDeletionDao {

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

			Query query = null;
			for(Long entityId : entityIds){

				query = getSession().getNamedQuery("requirementLibraryNode.findById");
				query.setParameter(ParameterNames.LIBRARY_NODE_ID, entityId);
				RequirementLibraryNode node = (RequirementLibraryNode) query.uniqueResult();

				removeEntitiesFromParentLibraryIfExists(entityId, node);

				removeEntitiesFromParentFolderIfExists(entityId, node);

				removeEntitiesFromParentRequirementIfExists(entityId, node);

				if(node!=null){
					getSession().delete(node);
					getSession().flush();
				}
			}
		}
	}



	private void removeEntitiesFromParentLibraryIfExists(Long entityId, RequirementLibraryNode node){
		Query query = getSession().getNamedQuery("requirementLibraryNode.findParentLibraryIfExists");
		query.setParameter(ParameterNames.LIBRARY_NODE_ID, entityId);
		RequirementLibrary library = (RequirementLibrary) query.uniqueResult();
		if(library != null){
			ListIterator<RequirementLibraryNode> iterator = library.getContent().listIterator();
			while (iterator.hasNext()) {
				RequirementLibraryNode tcln = iterator.next();
				if (tcln.getId().equals(node.getId())) {
					library.removeContent(tcln);
					break;
				}
			}
		}
	}

	private void removeEntitiesFromParentFolderIfExists(Long entityId, RequirementLibraryNode node){
		Query query = getSession().getNamedQuery("requirementLibraryNode.findParentFolderIfExists");
		query.setParameter(ParameterNames.LIBRARY_NODE_ID, entityId);
		RequirementFolder folder = (RequirementFolder) query.uniqueResult();
		if(folder != null){
			ListIterator<RequirementLibraryNode> iterator = folder.getContent().listIterator();
			while (iterator.hasNext()) {
				RequirementLibraryNode tcln = iterator.next();
				if (tcln.getId().equals(node.getId())) {
					folder.removeContent(tcln);
					break;
				}
			}
		}
	}

	private void removeEntitiesFromParentRequirementIfExists(Long entityId, RequirementLibraryNode node){
		Query query = getSession().getNamedQuery("requirementLibraryNode.findParentRequirementIfExists");
		query.setParameter(ParameterNames.LIBRARY_NODE_ID, entityId);
		Requirement requirement = (Requirement) query.uniqueResult();
		if(requirement  != null){
			ListIterator<Requirement> iterator = requirement.getContent().listIterator();
			while (iterator.hasNext()) {
				Requirement tcln = iterator.next();
				if (tcln.getId().equals(node.getId())) {
					requirement.removeContent(tcln);
					break;
				}
			}
		}
	}

	@Override
	public List<Long>[] separateFolderFromRequirementIds(List<Long> originalIds) {

		List<Long> folderIds = new ArrayList<Long>();
		List<Long> requirementIds = new ArrayList<Long>();

		List<BigInteger> filtredFolderIds = executeSelectSQLQuery(
				NativeQueries.REQUIREMENTLIBRARYNODE_SQL_FILTERFOLDERIDS, REQUIREMENT_IDS, originalIds);

		for (Long oId : originalIds){
			if (filtredFolderIds.contains(BigInteger.valueOf(oId))){
				folderIds.add(oId);
			}
			else{
				requirementIds.add(oId);
			}
		}

		return new List[] {folderIds, requirementIds};
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


	@SuppressWarnings("unchecked")
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
