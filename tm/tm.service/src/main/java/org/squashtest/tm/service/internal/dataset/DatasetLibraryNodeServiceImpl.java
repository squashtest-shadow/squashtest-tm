/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) Henix, henix.fr
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
package org.squashtest.tm.service.internal.dataset;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.customreport.CustomReportTreeDefinition;
import org.squashtest.tm.domain.dataset.*;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.tree.TreeEntity;
import org.squashtest.tm.domain.tree.TreeLibraryNode;
import org.squashtest.tm.exception.DuplicateNameException;
import org.squashtest.tm.service.dataset.DatasetLibraryNodeService;
import org.squashtest.tm.service.internal.customreport.CRLNCopier;
import org.squashtest.tm.service.internal.customreport.CRLNDeletionHandler;
import org.squashtest.tm.service.internal.customreport.CRLNMover;
import org.squashtest.tm.service.internal.repository.DatasetLibraryNodeDao;
import org.squashtest.tm.service.security.PermissionEvaluationService;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

/**
 * @author aguilhem
 */
@Service("org.squashtest.tm.service.dataset.DatasetLibraryNodeService")
@Transactional
public class DatasetLibraryNodeServiceImpl implements
	DatasetLibraryNodeService {

	@Inject
	protected PermissionEvaluationService permissionService;

	@Inject
	private DatasetLibraryNodeDao datasetLibraryNodeDao;

	@PersistenceContext
	private EntityManager em;

	@Override
	public DatasetLibraryNode findDatasetLibraryNodeById(Long id) {
		return datasetLibraryNodeDao.findOne(id);
	}

	@Override
	@Transactional(readOnly = true)
	public DatasetLibrary findLibraryByTreeNodeId(Long treeNodeId) {
		TreeEntity entity = findEntityAndCheckType(treeNodeId, DatasetTreeDefinition.LIBRARY);
		return (DatasetLibrary) entity;//NOSONAR cast is checked by findEntityAndCheckTy
	}

	@Override
	@Transactional(readOnly = true)
	public DatasetFolder findFolderByTreeNodeId(Long treeNodeId) {
		TreeEntity entity = findEntityAndCheckType(treeNodeId, DatasetTreeDefinition.FOLDER);
		return (DatasetFolder) entity;//NOSONAR cast is checked by findEntityAndCheckType method
	}

	@Override
	public GlobalDataset findGlobalDatasetByTreeNodeId(Long treeNodeId) {
		TreeEntity entity = findEntityAndCheckType(treeNodeId, DatasetTreeDefinition.GLOBAL_DATASET);
		return (GlobalDataset) entity;
	}

	@Override
	public CompositeDataset findCompositeDatasetByTreeNodeId(Long treeNodeId) {
		TreeEntity entity = findEntityAndCheckType(treeNodeId, DatasetTreeDefinition.COMPOSITE);
		return (CompositeDataset) entity;
	}

	@Override
	public DatasetTemplate findDatasetTemplateByTreeNodeId(Long treeNodeId) {
		TreeEntity entity = findEntityAndCheckType(treeNodeId, DatasetTreeDefinition.TEMPLATE);
		return (DatasetTemplate) entity;
	}

	@Override
	@PreAuthorize("hasPermission(#parentId,'org.squashtest.tm.domain.customreport.DatasetLibraryNode' ,'WRITE') "
		+ OR_HAS_ROLE_ADMIN)
	public DatasetLibraryNode createNewNode(Long parentId, TreeEntity entity) {
		DatasetLibraryNode parentNode = datasetLibraryNodeDao.findOne(parentId);
		if (parentNode == null) {
			throw new IllegalArgumentException("The node designed by parentId doesn't exist, can't add new node");
		}
		DatasetLibraryNode newNode = new DatasetLibraryNodeBuilder(parentNode, entity).build();
		datasetLibraryNodeDao.save(newNode);
		em.flush();
		em.clear();//needed to force hibernate to reload the persisted entities...
		return datasetLibraryNodeDao.findOne(newNode.getId());
	}

	@Override
	public void renameNode(Long nodeId, String newName) throws DuplicateNameException {
		DatasetLibraryNode dln = datasetLibraryNodeDao.findOne(nodeId);
		dln.renameNode(newName);
	}

	//--------------- PRIVATE METHODS --------------

	private TreeEntity findEntityAndCheckType(Long nodeId, DatasetTreeDefinition entityDef) {
		TreeLibraryNode node = findDatasetLibraryNodeById(nodeId);

		if (node == null || node.getEntityType() != entityDef) {
			String message = "the node for given id %d doesn't exist or doesn't represent a %s entity";
			throw new IllegalArgumentException(String.format(message, nodeId, entityDef.getTypeName()));
		}

		TreeEntity entity = node.getEntity();

		if (entity == null) {
			String message = "the node for given id %d represent a null entity";
			throw new IllegalArgumentException(String.format(message, nodeId));
		}
		return entity;
	}
}
