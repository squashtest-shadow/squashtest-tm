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
package org.squashtest.tm.service.internal.dataset

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.domain.dataset.DatasetFolder
import org.squashtest.tm.domain.dataset.DatasetLibraryNode
import org.squashtest.tm.domain.dataset.DatasetTreeDefinition
import org.squashtest.tm.domain.dataset.GlobalDataset
import org.squashtest.tm.domain.parameter.GlobalParameter
import org.squashtest.tm.domain.tree.TreeLibraryNode
import org.squashtest.tm.service.dataset.DatasetLibraryNodeService
import org.squashtest.tm.service.internal.repository.DatasetLibraryNodeDao
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

/**
 * @author aguilhem
 */
@UnitilsSupport
@DataSet("DatasetLibraryNodeServiceCopyNodeIT.xml")
@Transactional
class DatasetLibraryNodeServiceCopyNodeIT extends DbunitServiceSpecification {

	@Inject
	DatasetLibraryNodeService service;

	@Inject
	DatasetLibraryNodeDao dlnDao;

	@Autowired
	private ApplicationContext applicationContext;

	def "should copy a folder and it's content"(){

		when:

		def result = service.copyNodes([-15L], -10L);
		em.flush()
		em.clear()

		then:
		DatasetLibraryNode targetFolderNode = findEntity(DatasetLibraryNode.class,-10L)
		List<DatasetLibraryNode> childrens = targetFolderNode.getChildren()
		childrens.size() == 7

		//verify content order and ids
		childrens.get(0).id == -20L
		DatasetLibraryNode baseFolderNode = childrens.get(6)
		DatasetFolder baseFolder = baseFolderNode.entity
		baseFolderNode.id != -20L && -10L
		baseFolder.id != -15L

		//checking root baseFolder attributes
		baseFolderNode.name == "folder3"
		baseFolderNode.entityType == DatasetTreeDefinition.FOLDER
		baseFolderNode.entityId == baseFolder.id
		baseFolderNode.library.id == -1L
		baseFolderNode.parent == targetFolderNode
		baseFolder.name == "folder3"
		baseFolder.description == "description folder 3"
		baseFolder.project.id == -1L

		//checking childs of baseFolder, wich should have been copied with their parent
		List<DatasetLibraryNode> copiedChildrens = baseFolderNode.getChildren()
		copiedChildrens.size() == 1


	}

	def "should copy a folder and check first folder child "(){
		when:
		def result = service.copyNodes([-12L], -10L);
		em.flush()
		em.clear()

		then:
		DatasetLibraryNode targetFolderNode = findEntity(DatasetLibraryNode.class,-10L)
		List<TreeLibraryNode> childrens = targetFolderNode.getChildren()
		childrens.size() == 7

		DatasetLibraryNode baseFolderNode = childrens.get(6)
		List<DatasetLibraryNode> copiedChildrens = baseFolderNode.getChildren()


		//checking first child, it should be a folder
		DatasetLibraryNode childFolderNode = copiedChildrens.get(0)
		childFolderNode.id != -13L
		childFolderNode.name == "s-folder2"
		childFolderNode.parent == baseFolderNode
		DatasetFolder childFolder = childFolderNode.entity
		childFolder.getId() != -12L

	}

	def "should copy a folder and check global dataset child"(){
		when:
		def result = service.copyNodes([-15L], -10L);
		em.flush()
		em.clear()

		then:
		DatasetLibraryNode targetFolderNode = findEntity(DatasetLibraryNode.class,-10L)
		List<DatasetLibraryNode> childrens = targetFolderNode.getChildren()

		//verify content order and ids
		DatasetLibraryNode baseFolderNode = childrens.get(6)
		List<DatasetLibraryNode> copiedChildrens = baseFolderNode.getChildren()

		//checking global dataset copy
		GlobalDataset originaldataset = findEntity(GlobalDataset.class,-6L)
		DatasetLibraryNode globalDatasetNode = copiedChildrens.get(0)
		globalDatasetNode.getId() != -17L
		globalDatasetNode.name == "Global3"
		globalDatasetNode.parent == baseFolderNode
		GlobalDataset globalDataset = globalDatasetNode.getEntity()
		globalDataset.id != -6L
		globalDataset.description == "description global dataset 3"
		globalDataset.reference == "GD3"

		//checking global dataset parameters
		globalDataset.globalParameters.get(0).id != -1
		globalDataset.globalParameters.get(0).id != -2
		globalDataset.globalParameters.size() == 2

		//checking global dataset paramValue
		globalDataset.parameterValues.size() == 2
		globalDataset.parameterValues.first().dataset == globalDataset
		globalDataset.parameterValues.first().parameter.id != -1
		globalDataset.parameterValues.first().parameter.id != -2

	}

	def "should copy a folder and sub tree recursively"(){
		when:
		def result = service.copyNodes([-20L], -12L);
		em.flush()
		em.clear()

		then:
		DatasetLibraryNode targetFolderNode = findEntity(DatasetLibraryNode.class,-12L)
		List<DatasetLibraryNode> childrens = targetFolderNode.getChildren()
		childrens.size() == 2

		//verify content order and ids
		childrens.get(0).id == -13L
		DatasetLibraryNode baseFolderNode = childrens.get(1)
		DatasetFolder baseFolder = baseFolderNode.getEntity()

		//checking root baseFolder attributes
		baseFolderNode.name == "s-folder1"
		baseFolder.name == "s-folder1"

		//checking hierarchy
		List<DatasetLibraryNode> copiedChildrens = baseFolderNode.getChildren()
		copiedChildrens.size() == 1
		DatasetLibraryNode node = copiedChildrens.get(0).getChildren().get(0)
		node.name == "Global Dataset"
		node.entityType == DatasetTreeDefinition.GLOBAL_DATASET
		node.entity.id != -5L
		node.parent.parent.id == baseFolderNode.id

	}


}
