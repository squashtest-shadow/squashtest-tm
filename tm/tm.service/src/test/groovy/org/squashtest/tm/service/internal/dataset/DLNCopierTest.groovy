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

import org.squashtest.tm.domain.dataset.DatasetFolder
import org.squashtest.tm.domain.dataset.DatasetLibrary
import org.squashtest.tm.domain.dataset.DatasetLibraryNode
import org.squashtest.tm.domain.dataset.DatasetTreeDefinition
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.service.internal.repository.DatasetLibraryNodeDao
import org.squashtest.tm.service.treelibrarynode.NameResolver
import spock.lang.Specification

/**
 * @author aguilhem
 */
class DLNCopierTest extends Specification {
	DatasetLibraryNodeDao datasetLibraryNodeDao = Mock()

	def setup(){
		Project project = Mock()
		DatasetLibrary library = Mock()
		DatasetLibraryNode nodeFolder1 = new DatasetLibraryNode()
		DatasetFolder folder1 = new DatasetFolder()
		folder1.setName("Folder1")
		folder1.setDescription("un superbe repertoire")
		folder1.setProject(project)
		nodeFolder1.setName("Folder1")
		nodeFolder1.setEntity(folder1)
		nodeFolder1.setLibrary(library)
		datasetLibraryNodeDao.findOne(1L) >> nodeFolder1

		DatasetLibraryNode targetNode = new DatasetLibraryNode()
		DatasetFolder targetFolder = new DatasetFolder()
		targetFolder.setName("FolderTarget")
		targetFolder.setDescription("un autre repertoire")
		targetNode.setName("FolderTarget")
		targetNode.setEntity(targetFolder)
		targetNode.setLibrary(library)
		targetNode.entityType = DatasetTreeDefinition.FOLDER
		datasetLibraryNodeDao.findOne(2L) >> targetNode

	}

	def "shouldResolveNameConflict"(){
		given:
		DatasetLibraryNode target = Mock()
		target.childNameAlreadyUsed('name1') >> true
		target.childNameAlreadyUsed('name2') >> true
		target.childNameAlreadyUsed('name2-Copie1') >> true

		DatasetLibraryNode origin = new DatasetLibraryNode()
		origin.setEntity(new DatasetFolder())
		origin.setName('name1')

		DatasetLibraryNode origin2 = new DatasetLibraryNode()
		origin2.setEntity(new DatasetFolder())
		origin2.setName('name2')

		and:
		NameResolver resolver = new NameResolver()

		when:
		resolver.resolveNewName(origin,target)
		resolver.resolveNewName(origin2,target)

		then:
		origin.getName().equals("name1-Copie1")
		origin2.getName().equals("name2-Copie2")
	}

	def "should copy a single node"(){
		given:
		DatasetLibraryNode source = datasetLibraryNodeDao.findOne(1L)
		DatasetLibraryNode target = datasetLibraryNodeDao.findOne(2L)

		and:
		DLNCopier treeLibraryNodeCopier = new DLNCopier()
		treeLibraryNodeCopier.nameResolver = Mock(NameResolver)

		when:
		treeLibraryNodeCopier.copyNodes([source],target)

		then:
		def children = target.getChildren()
		children.size() == 1
		def copy = children.get(0)
		copy.name == source.getName()
		copy.id != 1L
		DatasetFolder entity = copy.getEntity()
		entity.description == source.entity.getDescription()

	}
}
