/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.internal.service

import java.util.List;

import javax.inject.Inject;

import org.apache.tools.ant.taskdefs.Copy;
import org.hibernate.Query
import org.hibernate.Session
import org.junit.runner.RunWith;
import org.spockframework.runtime.Sputnik;
import org.spockframework.util.NotThreadSafe;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.DuplicateNameException;
import org.squashtest.csp.tm.domain.campaign.Campaign;
import org.squashtest.csp.tm.domain.campaign.CampaignFolder;
import org.squashtest.csp.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.csp.tm.domain.campaign.Iteration;
import org.squashtest.csp.tm.domain.campaign.TestSuite
import org.squashtest.csp.tm.service.CampaignLibrariesCrudService;
import org.squashtest.csp.tm.service.CampaignLibraryNavigationService;
import org.unitils.dbunit.annotation.DataSet;

import spock.unitils.UnitilsSupport;

@UnitilsSupport
@Transactional
@RunWith(Sputnik)
class CampaignLibraryNavigationServiceIT extends DbunitServiceSpecification {


	@Inject
	private CampaignLibraryNavigationService navService


	@Inject
	private CampaignLibrariesCrudService libcrud


	private Long libId=-1
	private Long campId=-1;
	private Long folderId = -1;

	def setup(){

		libcrud.addLibrary();

		def libList= libcrud.findAllLibraries()

		def lib = libList.get(libList.size()-1);

		def folder =  new CampaignFolder(name:"folder")
		def campaign = new Campaign(name: "campaign 1", description: "the first campaign")

		navService.addFolderToLibrary(lib.id,folder)
		navService.addCampaignToCampaignFolder (folder.id, campaign)

		libId=lib.id;
		folderId = folder.id;
		campId= campaign.id;
	}

	def "should not persist a nameless campaign"(){
		given :
		Campaign camp = new Campaign();
		when :
		navService.addCampaignToCampaignLibrary(libId, camp)

		then :
		thrown (RuntimeException)
	}
	
	def "should not persist a nameless iteration"(){
		given :
		Campaign camp = new Campaign(name:"cp")
		navService.addCampaignToCampaignLibrary(libId, camp)
		Iteration iter = new Iteration();
		when :
		navService.addIterationToCampaign(iter, camp.id)
		then :
		thrown (RuntimeException)
	}


	def "should add folder to library and fetch it back"(){
		given :
		def folder = new CampaignFolder( name:"folder 2")
		navService.addFolderToLibrary(libId, folder)
		when :
		def obj = navService.findFolder(folder.id)

		then :
		obj !=null
		obj.id!=null
		obj.name == folder.name
	}

	def "should not add a folder to library"(){
		given :
		def folder = new CampaignFolder(name:"folder")	//same as the one in the setup() clause

		when :
		navService.addFolderToLibrary(libId, folder)

		then :
		thrown(DuplicateNameException)

	}



	def "should add folder to folder and fetch it back"() {
		given :
		def folder = new CampaignFolder( name:"folder 2")
		navService.addFolderToFolder(folderId, folder)


		when:
		def obj = navService.findFolder(folder.id)

		then :
		obj !=null
		obj.id!=null
		obj.name == folder.name
	}


	def "should find root content of library"() {
		given :
		def folder2 = new CampaignFolder(name: "folder 2")
		def folder3 = new CampaignFolder(name: "folder 3")
		def campaign = new Campaign(name: "campaign 1")

		navService.addFolderToLibrary(libId, folder2)
		navService.addFolderToLibrary(libId, folder3)
		navService.addCampaignToCampaignLibrary(libId, campaign)

		when :
		List<CampaignLibraryNode> rootContent = navService.findLibraryRootContent(libId);


		then :
		rootContent.size()==4
		rootContent.collect { it.id }.containsAll([
			folderId,
			folder2.id,
			folder3.id,
			campaign.id
		])
	}


	def "should find content of folder"() {
		given :
		def folder2 = new CampaignFolder( name:"folder 2")
		def folder3 = new CampaignFolder(name:"folder 3")
		def campaign = new Campaign(name:"campaign 2")

		navService.addFolderToFolder(folderId, folder2)
		navService.addFolderToFolder(folderId, folder3)
		navService.addCampaignToCampaignFolder(folderId, campaign)

		when :
		List<CampaignLibraryNode> folderContent = navService.findFolderContent(folderId)


		then :
		folderContent.size()==4
		folderContent.collect {it.id }.containsAll([
			campId,
			folder2.id,
			folder3.id,
			campaign.id
		])
	}

	def "should add campaign to campaign folder and fetch it back"(){
		given :
		def campaign = new Campaign(name:"new campaign", description:"test campaign")

		when :
		navService.addCampaignToCampaignFolder(folderId, campaign)
		def obj = navService.findCampaign(campaign.id)
		then :
		obj != null
		obj.name == "new campaign"
		obj.description == "test campaign"
	}

	def "should not add campaign to campaign folder"(){
		given :
		def campaign = new Campaign(name:"campaign 1", description:"test campaign") //same as in the setup() clause

		when :
		navService.addCampaignToCampaignFolder(folderId, campaign)

		then :
		thrown(DuplicateNameException)
	}


	def "sould add campaign to campaign library and fetch it back"(){
		given :
		def campaign = new Campaign(name:"test campaign", description:"test campaign")
		when :
		navService.addCampaignToCampaignLibrary(libId, campaign)
		def obj = navService.findCampaign(campaign.id)
		then :
		obj !=null
		obj.name=="test campaign"
		obj.description=="test campaign"
	}


	def "should not add campaign to campaign library"(){
		given :
		def campaign1 = new Campaign(name:"test campaign 1", description:"test campaign")
		navService.addCampaignToCampaignLibrary(libId, campaign1)
		when :
		def campaign2 = new Campaign(name:"test campaign 1", description:"test campaign")
		navService.addCampaignToCampaignLibrary libId, campaign2
		then :
		thrown(DuplicateNameException)
	}


	def "should find test campaign"() {
		given :
		true

		when :
		def obj = navService.findCampaign(campId)

		then :
		obj!=null
		obj.name=="campaign 1"
		obj.description=="the first campaign"
	}
	
	@DataSet("CampaignLibraryNavigationServiceIT.should copy paste iterations to campaign.xml")
	def "should copy paste iterations to campaign"(){
		given:
		Long[] iterationList = [1L,2L] 
		Long targetCampaignId = 11L
		
		when: 
		
		List<Iteration> iterations = navService.copyIterationsToCampaign(targetCampaignId , iterationList )
		
		then: 
		iterations.size() == 2
		iterations.get(0).name == "iter - tc1"
		
	}
	
	@DataSet("CampaignLibraryNavigationServiceIT.should copy paste iterations with testSuites.xml")
	def "should copy paste iterations with testSuites"(){
		given:
		Long[] iterationList = [10012L,2L]
		Long targetCampaignId = 11L
		
		when:
		List<Iteration> iterations = navService.copyIterationsToCampaign(targetCampaignId , iterationList )
		
		then:
		iterations.size() == 2
		iterations.find {it.getName()== "iter - tc1" } != null
		Iteration iteration1 = iterations.find {it.getName() == "iter - tc1" }
		iteration1.getTestSuites().size() == 2
		iteration1.getTestSuites().find {it.getName() == "testSuite1"} != null
		TestSuite testsSuite1 = iteration1.getTestSuites().find {it.getName() == "testSuite1"}
		testsSuite1.getTestPlan().size() == 1
		iteration1.getTestPlans().size() == 1
		iteration1.getTestSuites().find {it.getName() == "testSuite2"} != null
		TestSuite testsSuite2 = iteration1.getTestSuites().find {it.getName() == "testSuite2"}
		testsSuite2.getTestPlan().size() == 0
	}
	
	@DataSet("CampaignLibraryNavigationServiceIT.should copy paste campaigns with iterations.xml")
	def "should copy paste campaigns with iterations"(){
		given:
		Long[] targetIds = [10L]
		Long destinationId = 1L
		
		when:
		List<Campaign> campaigns = navService.copyNodesToFolder(destinationId, targetIds)
		
		then:
		campaigns.get(0).getIterations().size() == 2
		def iterations = campaigns.get(0).getIterations()
		iterations.find {it.getName() == "iter - tc1" } != null
		iterations.find {it.getName() == "iter - tc1 -2" } != null
	}
	
	@DataSet("CampaignLibraryNavigationServiceIT.should copy paste campaigns with testSuites.xml")
	def "should copy paste campaigns with testSuites"(){
		given:
			Long[] targetIds = [10L]
		Long destinationId = 1L
		
		when:
		List<Campaign> campaigns = navService.copyNodesToFolder(destinationId, targetIds)
		
		then:
		campaigns.get(0).getIterations().size() == 2
		def iterations = campaigns.get(0).getIterations()
		Iteration iteration1 = iterations.find {it.getName() == "iter - tc1" }
		iteration1.getTestSuites().size() == 2
		iteration1.getTestSuites().find {it.getName() == "testSuite1"} != null
		TestSuite testsSuite1 = iteration1.getTestSuites().find {it.getName() == "testSuite1"}
		testsSuite1.getTestPlan().size() == 1
		iteration1.getTestSuites().find {it.getName() == "testSuite2"} != null
		TestSuite testsSuite2 = iteration1.getTestSuites().find {it.getName() == "testSuite2"}
		testsSuite2.getTestPlan().size() == 0
		Iteration iteration2 = iterations.find {it.getName() == "iter - tc1 -2" }
		iteration2.getTestSuites().isEmpty()
	}
	
}
