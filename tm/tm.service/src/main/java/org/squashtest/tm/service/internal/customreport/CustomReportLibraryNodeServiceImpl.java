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
package org.squashtest.tm.service.internal.customreport;

import java.util.List;

import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.customreport.CustomReportFolder;
import org.squashtest.tm.domain.customreport.CustomReportLibrary;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.customreport.CustomReportTreeDefinition;
import org.squashtest.tm.domain.tree.TreeEntity;
import org.squashtest.tm.domain.tree.TreeLibraryNode;
import org.squashtest.tm.service.customreport.CustomReportLibraryNodeService;
import org.squashtest.tm.service.internal.repository.CustomReportLibraryNodeDao;

@Service("org.squashtest.tm.service.customreport.CustomReportLibraryNodeService")
@Transactional
public class CustomReportLibraryNodeServiceImpl implements
		CustomReportLibraryNodeService {
	
	@Inject 
	private CustomReportLibraryNodeDao customReportLibraryNodeDao;
	
	@Inject
	private SessionFactory sessionFactory;

	@Override
	public CustomReportLibrary findCustomReportLibraryById(Long libraryId) {
		TreeEntity entity = findEntityAndCheckType(libraryId, CustomReportTreeDefinition.LIBRARY);
		return (CustomReportLibrary) entity;//NOSONAR cast is checked by findEntityAndCheckType method
	}

	@Override
	public CustomReportFolder findCustomReportFolderById(Long folderId) {
		TreeEntity entity = findEntityAndCheckType(folderId, CustomReportTreeDefinition.FOLDER);
		return (CustomReportFolder) entity;//NOSONAR cast is checked by findEntityAndCheckType method
	}
	
	@Override
	public CustomReportLibraryNode createNewCustomReportLibraryNode(Long parentId, TreeEntity entity) {
		CustomReportLibraryNode parentNode = customReportLibraryNodeDao.findById(parentId);
		if (parentNode == null) {
			throw new IllegalArgumentException("The node designed by parentId doesn't exist, can't add new node");
		}
		CustomReportLibraryNode newNode = new CustomReportLibraryNodeBuilder(parentNode, entity).build();
		customReportLibraryNodeDao.persist(newNode);
		Session session = sessionFactory.getCurrentSession();
		session.flush();
		session.clear();//needed to force hibernate to reload the persisted entities...
		return customReportLibraryNodeDao.findById(newNode.getId());
	}
	
	@Override
	public void deleteCustomReportLibraryNode(List<Long> nodeIds) {
		for (Long nodeId : nodeIds) {
			CustomReportLibraryNode targetNode = customReportLibraryNodeDao.findById(nodeId);
			TreeLibraryNode parentNode = targetNode.getParent();
			parentNode.removeChild(targetNode);
			customReportLibraryNodeDao.remove(targetNode);
		}
	}
	
	//--------------- PRIVATE METHODS --------------
	

	private CustomReportLibraryNode findCustomReportLibraryNodeById (Long id){
		return customReportLibraryNodeDao.findById(id);
	}
	
	private TreeEntity findEntityAndCheckType(Long nodeId, CustomReportTreeDefinition entityDef){
		TreeLibraryNode node = findCustomReportLibraryNodeById(nodeId);
		
		if (node==null||node.getEntityType()!=entityDef) {
			String message = "the node for given id %d doesn't exist or doesn't represent a %s entity";
			throw new IllegalArgumentException(String.format(message, nodeId,entityDef.getTypeName()));
		}
		
		TreeEntity entity = node.getEntity();
		
		if (entity==null) {
			String message = "the node for given id %d represent a null entity";
			throw new IllegalArgumentException(String.format(message,nodeId));
		}
		return entity;
	}
}
