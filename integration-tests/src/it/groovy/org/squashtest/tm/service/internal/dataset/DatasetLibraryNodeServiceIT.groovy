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

import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.domain.dataset.DatasetFolder
import org.squashtest.tm.domain.dataset.DatasetLibraryNode
import org.squashtest.tm.service.dataset.DatasetLibraryNodeService
import org.squashtest.tm.service.internal.repository.DatasetLibraryNodeDao
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@UnitilsSupport
@Transactional
@DataSet("DatasetLibraryNodeServiceIT.xml")
class DatasetLibraryNodeServiceIT extends DbunitServiceSpecification {

	@Inject
	DatasetLibraryNodeService service;

	@Inject
	DatasetLibraryNodeDao dlnDao;

	def "should add new folder to library"(){
		given :
		def parent = dlnDao.findOne(-1L)

		DatasetFolder folder = new DatasetFolder()
		folder.setName("newFolder")

		when:
		def res = service.createNewNode(-1L, folder)
		def resId = res.getId()
		em.flush()
		em.clear()
		def newChildAfterPersist = dlnDao.findOne(resId)
		def parentNode = newChildAfterPersist.getParent()
		def entityLinkedToNode = newChildAfterPersist.getEntity()
		def projectLinked = entityLinkedToNode.getProject()

		then:
		resId != null
		parentNode.id == parent.id
		entityLinkedToNode != null
		projectLinked.id == -1L

	}

	def "should rename node and entity"() {

		when:
		service.renameNode(nodeId, newName)


		then:
		DatasetLibraryNode node = dlnDao.findOne(nodeId)
		node.name == newName
		node.entity.name == newName

		where:
		nodeId 	|| newName
		-20L	|| "newFolderName"
		-2L		|| "newGlobalDatasetName"

	}
}
