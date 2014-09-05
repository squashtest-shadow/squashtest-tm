/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.service.requirement

import javax.inject.Inject

import org.hibernate.Query
import org.springframework.transaction.annotation.Transactional
import org.squashtest.csp.tools.unittest.reflection.ReflectionCategory;
import org.squashtest.tm.domain.customfield.BindableEntity
import org.squashtest.tm.domain.customfield.CustomFieldValue
import org.squashtest.tm.domain.requirement.Requirement
import org.squashtest.tm.domain.requirement.RequirementFolder
import org.squashtest.tm.domain.requirement.RequirementLibraryNode
import org.squashtest.tm.domain.requirement.RequirementVersion
import org.squashtest.tm.exception.library.CannotMoveInHimselfException;
import org.squashtest.tm.exception.requirement.CopyPasteObsoleteException;
import org.squashtest.tm.exception.requirement.IllegalRequirementModificationException;
import org.squashtest.tm.service.DbunitServiceSpecification;
import org.squashtest.tm.service.internal.requirement.RequirementNodeDeletionHandler;
import org.squashtest.tm.service.requirement.RequirementLibraryNavigationService
import org.unitils.dbunit.annotation.DataSet
import org.unitils.dbunit.annotation.ExpectedDataSet;
import org.squashtest.tm.service.internal.repository.RequirementFolderDao;

import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
class RequirementLibraryNavigationServiceIT extends DbunitServiceSpecification {


	@Inject
	RequirementLibraryNavigationService navService;

	@Inject
	RequirementFolderDao folderDao;
	
	@Inject
	private RequirementNodeDeletionHandler deletionHandler;
	
	private int requirementId = 10

	def setup() {
		// we deactivate auditor because it breaks tests when run in batch. Auditor opens a second tx which cannot see the requirement which is not yet committed but to be audited.
		org.squashtest.tm.service.internal.event.RequirementCreationEventPublisherAspect.aspectOf().setAuditor(null)
	}

	@DataSet("RequirementLibraryNavigationServiceIT.should return all the requirements in a hierarchy given some ids.xml")
	def "should return all the requirements in a hierarchy given some ids"(){

		given:
		List<Long> listReq = new ArrayList()
		listReq.add(1l)
		listReq.add(250l)

		when :
		def reqs = navService.findRequirementsToExportFromNodes (listReq)
		println reqs.toString()

		then :
		reqs != null
		reqs.size() == 3

		def export1 = reqs.findAll{r -> r.name == "1req"}[0];
		def export2 = reqs.findAll{r -> r.name == "req2"}[0];
		def export3 = reqs.findAll{r -> r.name == "req3"}[0];

		export1.name == "1req"
		export1.folderName == "folder"
		export1.project == "projet1"

		export2.name == "req2"
		export2.folderName == "folder/subfolder"
		export2.project == "projet1"

		export3.name == "req3"
		export3.folderName == ""
	}
	
	

	@DataSet("RequirementLibraryNavigationServiceIT.should not copy paste obsolete.xml")
	def "should not copy paste selection containing obsolete"(){
		given:
		Long[] sourceIds = [1L]
		Long destinationId = 2L

		when:
		List<RequirementLibraryNode> nodes = navService.copyNodesToFolder(destinationId, sourceIds)

		then:"exception is thrown"
		thrown (CopyPasteObsoleteException)
	}

	@DataSet("RequirementLibraryNavigationServiceIT.should move selection containing obsolete.xml")
	def "should move selection containing obsolete"(){
		given:
		Long[] sourceIds = [1L]
		Long destinationId = 2L

		when:
		navService.moveNodesToFolder(destinationId, sourceIds);

		then:"no exception is thrown"
		true
	}

	@DataSet("RequirementLibraryNavigationServiceIT.should not move in himself.xml")
	def "should not move in himself"(){
		given:
		Long[] sourceIds = [1L]
		Long destinationId = 1L

		when:
		navService.moveNodesToFolder(destinationId, sourceIds);

		then:
		thrown (CannotMoveInHimselfException)
	}

	@DataSet("RequirementLibraryNavigationServiceIT.should not move in himself.xml")
	def "should not move in his descendent"(){
		given:
		Long[] sourceIds = [13L]
		Long destinationId = 1L

		when:
		navService.moveNodesToFolder(destinationId, sourceIds);

		then:
		thrown (CannotMoveInHimselfException)
	}

	@DataSet("RequirementLibraryNavigationServiceIT.should copy paste folder with requirements.xml")
	def "should copy paste a folder into itself"(){
		given:
		Long[] sourceIds = [1L]
		Long destinationId = 1L

		when:
		List<RequirementLibraryNode> nodes = navService.copyNodesToFolder(destinationId, sourceIds)

		then:"requirement folder is copied"
		nodes.get(0) instanceof RequirementFolder
		RequirementFolder folder = findEntity(RequirementFolder.class, destinationId);
		folder.content.size() == 3
	}

	@DataSet("RequirementLibraryNavigationServiceIT.should copy paste a requirement into itself.xml")
	def "should copy paste a requirement into itself"(){
		given :
		Long[] sourceIds = [10L]
		long destinationId = 10L
		
		when :
		List<Requirement> result = navService.copyNodesToRequirement(destinationId, sourceIds)
		
		then:
		//expected dataset is verified
		Requirement destination = findEntity(Requirement.class, 10L)
		destination.content.size() == 1
	}

	@DataSet("RequirementLibraryNavigationServiceIT.should copy paste folder with requirements.xml")
	def "should copy paste folder with requirements"(){
		given:
		Long[] sourceIds = [1L]
		Long destinationId = 2L

		when:
		List<RequirementLibraryNode> nodes = navService.copyNodesToFolder(destinationId, sourceIds)

		then:"requirement folder has 2 requirements"
		nodes.get(0) instanceof RequirementFolder
		RequirementFolder folderCopy = (RequirementFolder) nodes.get(0)
		folderCopy.content.size() == 2
		folderCopy.content.find {it.name == "requirement10"} != null
		folderCopy.content.find {it.name == "requirement11"} != null
	}

	@DataSet("RequirementLibraryNavigationServiceIT.should move folder with requirements.xml")
	def "should move folder with requirements"(){
		given:
		Long[] sourceIds = [1L]
		Long destinationId = 2L

		when:
		navService.moveNodesToFolder(destinationId, sourceIds)

		then:"requirement folder is moved"
		RequirementFolder destination = findEntity(RequirementFolder.class, 2L)
		destination.getContent().collectAll({it.id}).containsAll([1L])
		and : "it's requirements are moved too"
		RequirementFolder movedFolder = findEntity(RequirementFolder.class, 1L)
		movedFolder.getContent().collectAll({it.id}).containsAll([10L, 11L])
	}

	@DataSet("RequirementLibraryNavigationServiceIT.should copy paste requirement with non obsolete versions.xml")
	def "should copy paste requirement with non obsolete versions"(){
		given:
		Long[] sourceIds = [10L]
		Long destinationId = 2L

		when:
		List<RequirementLibraryNode> nodes = navService.copyNodesToFolder(destinationId, sourceIds)

		then:"requirement has 2 versions"
		nodes.get(0) instanceof Requirement
		Requirement requirement = (Requirement) nodes.get(0)
		requirement.versions.size() == 2
		requirement.versions.find {it.name == "version100"} != null
		requirement.versions.find {it.name == "version102"} != null
	}

	@DataSet("RequirementLibraryNavigationServiceIT.should copy paste requirement with non obsolete versions.xml")
	def "should move requirement with non obsolete versions"(){
		given:
		Long[] sourceIds = [10L]
		Long destinationId = 2L

		when: navService.moveNodesToFolder(destinationId, sourceIds)

		then:"no exception is thrown"
		true
	}

	@DataSet("RequirementLibraryNavigationServiceIT.should copy paste requirement versions with cuf.xml")
	def "should copy paste requirement versions with cufs"(){
		given:
		Long[] sourceIds = [10L]
		Long destinationId = 2L

		when:
		List<RequirementLibraryNode> nodes = navService.copyNodesToFolder(destinationId, sourceIds)

		then:"requirement has 2 versions"
		nodes.get(0) instanceof Requirement
		Requirement requirement = (Requirement) nodes.get(0)
		requirement.versions.size() == 2
		RequirementVersion firstV = requirement.versions.find {it.name == "version100"}
		RequirementVersion currV = requirement.versions.find {it.name == "version102"}
		findCufValueForEntity(BindableEntity.REQUIREMENT_VERSION, currV.id).get(0).value == "current-cuf"
		findCufValueForEntity(BindableEntity.REQUIREMENT_VERSION, firstV.id).get(0).value == "first-cuf"
	}

	@DataSet("RequirementLibraryNavigationServiceIT.should move reqs to other project.xml")
	def "should move req to other project"(){
		given:
		Long[] sourceIds = [1L]
		Long destinationId = 2L

		when:
		navService.moveNodesToFolder(destinationId, sourceIds)

		then:"hierarchy is moved"
		RequirementFolder destination = findEntity(RequirementFolder.class, 2L)
		destination.getContent().collectAll({it.id}).containsAll([1L])
		RequirementFolder movedFolder = findEntity(RequirementFolder.class, 1L)
		movedFolder.getContent().collectAll({it.id}).containsAll([10L, 11L])
		and: "they all know their rightfull project"
		movedFolder.getProject() == destination.getProject();
		movedFolder.getContent().each( {it.getProject() == destination.getProject()});
	}

	@DataSet("RequirementLibraryNavigationServiceIT.should move reqs to project with cufs.xml")
	@ExpectedDataSet("RequirementLibraryNavigationServiceIT.should move reqs to project with cufs-result.xml")
	def "should move req to other project and update cufs"(){
		given:
		Requirement req = findEntity(Requirement.class, 10L)
		RequirementVersion cur = req.getCurrentVersion()
		use(ReflectionCategory){
			RequirementVersion.set field: "requirement", of:cur, to: req
		}
		req.getCurrentVersion().setRequirement(req)
		Long[] sourceIds = [1L]
		Long destinationId = 2L

		when:
		navService.moveNodesToFolder(destinationId, sourceIds)

		then: "expected result verified"
	}

	private List<CustomFieldValue> findCufValueForEntity(BindableEntity entity, long entityId){
		Query query = session.createQuery("from CustomFieldValue cufV where cufV.boundEntityType = :type and cufV.boundEntityId = :id")
		query.setParameter("type", entity)
		query.setParameter("id", entityId)

		return query.list()
	}
	
	@DataSet("RequirementLibraryNavigationServiceIT.should remove reqs after move.xml")
	def "should remove requirement without move 1"(){

		when:
		deletionHandler.deleteNodes([10L]);
		
		then:
		! found("requirement", "rln_id", 10L);
	}
	
	@DataSet("RequirementLibraryNavigationServiceIT.should remove reqs after move.xml")
	def "should remove requirement without move 2"(){

		when:
		deletionHandler.deleteNodes([20L]);
		
		then:
		! found("requirement", "rln_id", 20L);
	}
	
	
	@DataSet("RequirementLibraryNavigationServiceIT.should remove reqs after move.xml")
	def "should remove requirement after move 1"(){
		given:
		Long[] reqIds = [20L]
		navService.moveNodesToRequirement(10L, reqIds);
		reqIds = [30L]
		navService.moveNodesToRequirement(20L, reqIds);
		
		when:
		deletionHandler.deleteNodes([20L]);
		
		then:
		! found("requirement", "rln_id", 20L);
	}
	
	@DataSet("RequirementLibraryNavigationServiceIT.should remove reqs after move.xml")
	def "should remove requirement after move 2"(){
		given:
		Long[] reqIds = [10L]
		navService.moveNodesToRequirement(20L, reqIds);
		reqIds = [30L]
		navService.moveNodesToRequirement(10L, reqIds);
		
		when:
		deletionHandler.deleteNodes([10L]);
		
		then:
		! found("requirement", "rln_id", 10L);
	}
	
	@DataSet("RequirementLibraryNavigationServiceIT.should move to same project at right position.xml")
	def "should move folder with requirements to the right position - first"(){
		given:
		Long[] sourceIds = [1L]
		Long destinationId = 2L
		
		when:
		navService.moveNodesToFolder(destinationId, sourceIds, 0)
		
		then:
		RequirementFolder parentFolder = (RequirementFolder) folderDao.findById(2L);
		parentFolder.content.collect {it.id} == [1L, 20L, 21L];
	}
	
	@DataSet("RequirementLibraryNavigationServiceIT.should move to same project at right position.xml")
	def "should move folder with requirements to the right position - middle"(){
		given:
		Long[] sourceIds = [1L]
		Long destinationId = 2L
		
		when:
		navService.moveNodesToFolder(destinationId, sourceIds, 1)
		
		then:
		RequirementFolder parentFolder = (RequirementFolder) folderDao.findById(2L);
		parentFolder.content.collect {it.id} == [20L, 1L, 21L];
	}
	
	@DataSet("RequirementLibraryNavigationServiceIT.should move to same project at right position.xml")
	def "should move folder with requirements to the right position - last"(){
		given:
		Long[] sourceIds = [1L]
		Long destinationId = 2L
		
		when:
		navService.moveNodesToFolder(destinationId, sourceIds, 2)
		
		then:
		RequirementFolder parentFolder = (RequirementFolder) folderDao.findById(2L);
		parentFolder.content.collect {it.id} == [20L, 21L, 1L];
	}
	

}

