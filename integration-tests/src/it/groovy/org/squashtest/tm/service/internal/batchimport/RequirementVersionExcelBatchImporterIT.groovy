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
package org.squashtest.tm.service.internal.batchimport

import javax.inject.Inject;

import org.junit.runner.RunWith;
import org.spockframework.runtime.Sputnik;
import org.springframework.transaction.annotation.Transactional;
import  org.squashtest.tm.service.internal.batchimport.requirement.excel.RequirementExcelBatchImporter
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService;

import spock.unitils.UnitilsSupport

import org.unitils.dbunit.annotation.DataSet
import org.squashtest.tm.service.RequirementImportCustomDbunitServiceSpecification;
import org.squashtest.tm.service.DbunitServiceSpecification;
import org.squashtest.tm.service.importer.ImportLog;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.service.importer.EntityType;
import org.squashtest.tm.service.requirement.RequirementLibraryNavigationService;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementStatus;

@UnitilsSupport
@Transactional
@RunWith(Sputnik)
class RequirementVersionExcelBatchImporterIT extends RequirementImportCustomDbunitServiceSpecification{


	@Inject
	private RequirementExcelBatchImporter importer
	@Inject
	private RequirementLibraryNavigationService navService
	
	def importFile = {
		fileName ->
		URL url = RequirementExcelBatchImporter.class.getClassLoader().getResource(fileName)
		File file = new File(url.toURI())
		ImportLog summary = importer.performImport(file)
		return summary
	}
	
	def simulateImportFile = {
		fileName ->
		URL url = RequirementExcelBatchImporter.class.getClassLoader().getResource(fileName)
		File file = new File(url.toURI())
		ImportLog summary = importer.simulateImport(file)
		return summary
	}
	
	def findVersion = {
		id -> findEntity(RequirementVersion.class, id)
	}
	def findReq = {
		id -> findEntity(Requirement.class, id)
	}
	
	def attachRequirementVersionMap(reqVersionMap){
		
		def attachVersionToReq = { reqId, vId ->
			Requirement req = findReq(reqId); req.addVersion(findVersion(vId))
		}

		reqVersionMap.each{ reqId, versionIds ->
			versionIds.each{
				attachVersionToReq(-reqId, -it)
			}
		}
	}

	
	
	@DataSet("RequirementExcelBatchImportIT.should import requirement.xml")
	def "should import requirement hierarchy test"(){
		given:
		def paths =[
			"/Projet1/Exigence1",
			"/Projet1/Dossier1/Dossier1/Dossier1/Exigence1",
			"/Projet1/Dossier1/Dossier2/Dossier2/Exigence1",
			"/Projet1/Dossier1/Dossier2/Dossier2/Dossier3/Exigence1",
			"/Projet1/Exigence1/S-Exigence1",
			"/Projet1/Exigence1/S-Exigence1/S-S-Exigence1",
			"/Projet1/Exigence1/Exigence2/Exi1/Exi2",
			"/Projet1/Exigence1B/Exigence2/Exi1/Exi2",
			"/Projet1/Dossier1/Dossier2/Dossier2/Dossier3/Exigence2/Exi1/Exi2/Exigence1",
			"/Projet1/Dossier1/Dossier2/Dossier2/Dossier3/Exigence1/Exi1/Exi2/Exigence1",
			"/Projet1/Exigence2",
			"/Projet1/Dossier1/Dossier1/Dossier1/Exigence2",
			"/Projet1/Dossier1/Dossier2/Dossier2/Exigence2",
			"/Projet1/Dossier1/Dossier2/Dossier2/Dossier3/Exigence2/Exi1/Exi2/Exigence1",
			"/Projet1/Exigence1/S-Exigence2",
			"/Projet1/Exigence1/S-Exigence1/S-S-Exigence2",
			"/Projet1/Exigence1/Exigence2/Exi1/Exi3",
			"/Projet1/Exigence1B/Exigence2/Exi1/Exi3",
			"/Projet1/Dossier1/Dossier2/Dossier2/Dossier3/Exigence2/Exi1/Exi2/Exigence2",
			"/Projet1/Dossier1/Dossier2/Dossier2/Dossier3/Exigence1/Exi1/Exi2/Exigence2"
			]
		
		when:
		
		ImportLog summary = importFile("import/requirements/requirement-hierarchy.xls")
		summary.recompute()
		def versions = findAll("RequirementVersion");
		def requirements = findAll("Requirement");
		
		then:
		summary.requirementVersionSuccesses == 19
		summary.requirementVersionWarnings == 1
		summary.requirementVersionFailures == 0
		paths.collect {navService.findNodeIdByPath(it)!=null}
	}
	
	/**
	 * Checking version number collision for root requirement, 
	 * requirement under folder, requirement under requirement
	 * @return
	 */
	@DataSet("RequirementExcelBatchImportIT.should import requirement.xml")
	def "should import requirement version test"(){
		given:

		when:
		ImportLog summary = importFile("import/requirements/requirement_test_version_number.xls")
		summary.recompute()
		def id = navService.findNodeIdByPath(path)
		def req = navService.findRequirement(id)
		def version = req.findRequirementVersion(versionNumber)
		def check = version == null

		then:
		
		summary.requirementVersionSuccesses == 13
		summary.requirementVersionWarnings == 11
		summary.requirementVersionFailures == 0
		check == result
		
		
		where:
		
		path							| versionNumber  	|| 	result
		"/Projet1/Exigence1"			|	1				||	false
		"/Projet1/Exigence1"			|	2				||	false
		"/Projet1/Exigence1"			|	3				||	false
		"/Projet1/Exigence1"			|	4				||	false
		"/Projet1/Exigence1"			|	5				||	false
		"/Projet1/Exigence1"			|	6				||	false
		"/Projet1/Exigence1"			|	7				||	true
		"/Projet1/Exigence1"			|	22				||	false
		"/Projet1/Exigence1"			|	40				||	false
		"/Projet1/Exigence1/Exigence"	|	1				||	false
		"/Projet1/Exigence1/Exigence"	|	2				||	false
		"/Projet1/Exigence1/Exigence"	|	3				||	false
		"/Projet1/Exigence1/Exigence"	|	4				||	false
		"/Projet1/Exigence1/Exigence"	|	5				||	false
		"/Projet1/Exigence1/Exigence"	|	6				||	true
		"/Projet1/Exigence1/Exigence"	|	8				||	true
		"/Projet1/Exigence1/Exigence"	|	9				||	false
		"/Projet1/Exigence1/Exigence"	|	22				||	false
		"/Projet1/Exigence1/Exigence"	|	40				||	false
		"/Projet1/Dossier1/Exigence"	|	1				||	false
		"/Projet1/Dossier1/Exigence"	|	2				||	false
		"/Projet1/Dossier1/Exigence"	|	3				||	false
		"/Projet1/Dossier1/Exigence"	|	4				||	false
		"/Projet1/Dossier1/Exigence"	|	5				||	false
		"/Projet1/Dossier1/Exigence"	|	6				||	true
		"/Projet1/Dossier1/Exigence"	|	9				||	false
		"/Projet1/Dossier1/Exigence"	|	22				||	false
		"/Projet1/Dossier1/Exigence"	|	40				||	false
	}
	
	/**
	 * Checking escaping "/" in import for root requirement,
	 * requirement under folder, requirement under requirement
	 * @return
	 */
	@DataSet("RequirementExcelBatchImportIT.should import requirement.xml")
	def "should import requirement escape test"(){
		given:

		when:
		ImportLog summary = importFile("import/requirements/requirement_escape.xls")
		summary.recompute()
		def id = navService.findNodeIdByPath(path)
		def req = navService.findRequirement(id)
		def version = req.findRequirementVersion(versionNumber)
		def check = version == null

		then:

		summary.requirementVersionSuccesses == 5
		summary.requirementVersionWarnings == 4
		summary.requirementVersionFailures == 0
		check == result


		where:

		path												| versionNumber  	|| 	result
		"/Projet1/Dos\\/sier/Exigence"						|	1				||	false
		"/Projet1/Dos\\/sier/Exigence"						|	6				||	false
		"/Projet1/Dos\\/sier/Exigence"						|	98				||	false
		"/Projet1/Dossier/Dossier/Dos\\/sier/Exigence"		|	6				||	false
		"/Projet1/Dossier/Dossier/Dos\\/sier/Exigence"		|	7				||	false
		"/Projet1/Dossier/Dossier/Dos\\/sier/Exigence"		|	8				||	false
		"/Projet1/Dos\\/sier/Dossier/Dos\\/sier/Exig\\/ence"|	6				||	false
		"/Projet1/Dos\\/sier/Dossier/Dos\\/sier/Exig\\/ence"|	7				||	false
		"/Projet1/Dos\\/sier/Dossier/Dos\\/sier/Exig\\/ence"|	8				||	false
	}
	
	@DataSet("RequirementExcelBatchImportIT.should import requirement.xml")
	def "should import requirement crit and status test"(){
		given:

		when:
		ImportLog summary = importFile("import/requirements/requirement_status_crit.xls")
		summary.recompute()
		def id = navService.findNodeIdByPath(path)
		def req = navService.findRequirement(id)
		def version = req.findRequirementVersion(versionNumber)
		def reqCrit = version.getCriticality();
		def reqStatus = version.getStatus();

		then:

		summary.requirementVersionSuccesses == 5
		summary.requirementVersionWarnings == 2
		summary.requirementVersionFailures == 0
		reqCrit == criticality
		reqStatus == status


		where:

		path					| versionNumber  || criticality							|status
		"/Projet1/Exigence"		|	1			 ||	RequirementCriticality.MAJOR		|RequirementStatus.WORK_IN_PROGRESS
		"/Projet1/Exigence"		|	2			 ||	RequirementCriticality.CRITICAL		|RequirementStatus.WORK_IN_PROGRESS
		"/Projet1/Exigence"		|	3			 ||	RequirementCriticality.UNDEFINED	|RequirementStatus.WORK_IN_PROGRESS
		"/Projet1/Exigence"		|	4			 ||	RequirementCriticality.MAJOR		|RequirementStatus.UNDER_REVIEW
		"/Projet1/Exigence"		|	5			 ||	RequirementCriticality.MAJOR		|RequirementStatus.APPROVED
		"/Projet1/Exigence"		|	6			 ||	RequirementCriticality.MAJOR		|RequirementStatus.OBSOLETE
		"/Projet1/Exigence"		|	7			 ||	RequirementCriticality.MAJOR		|RequirementStatus.WORK_IN_PROGRESS
	}
	
	@DataSet("RequirementExcelBatchImportIT.should import requirement.xml")
	def "should import requirement category test"(){
		given:

		when:
		ImportLog summary = importFile("import/requirements/requirement_test_category.xls")
		summary.recompute()
		def id = navService.findNodeIdByPath(path)
		def req = navService.findRequirement(id)
		def version = req.findRequirementVersion(versionNumber)
		def categoryCode = version.getCategory().getCode();

		then:
		summary.requirementVersionSuccesses == 1
		summary.requirementVersionWarnings == 1
		summary.requirementVersionFailures == 0
		categoryCode == expectedCode

		where:

		path					| versionNumber  || expectedCode							
		"/Projet1/Exigence"		|	1			 ||	"CAT_BUSINESS"
		"/Projet1/Exigence"		|	2			 ||	"CAT_UNDEFINED"
	}
	
	@DataSet("RequirementExcelBatchImportIT.should import requirement.xml")
	def "shouldn't import requirement because milestone isn't binded to project"(){
		given:
		def path = "/Projet1/Exigence"
		
		when:
		ImportLog summary = importFile("import/requirements/requirement_failed_milestone_not_exist.xls")
		summary.recompute()
		def id = navService.findNodeIdByPath(path)
		
		then:
		summary.requirementVersionSuccesses == 0
		summary.requirementVersionWarnings == 0
		summary.requirementVersionFailures == 1
		id == null
	}
	
	@DataSet("RequirementExcelBatchImportIT.should import requirement.xml")
	def "simulate : shouldn't import requirement because milestone isn't binded to project"(){
		given:
		def path = "/Projet1/Exigence"
		def expectedErrors = [Messages.ERROR_UNKNOWN_MILESTONE]

		when:
		ImportLog summary = importFile("import/requirements/requirement_failed_milestone_not_exist.xls")
		summary.recompute()
		def errors = summary.findAllFor(EntityType.REQUIREMENT_VERSION);
		
		then:
		summary.requirementVersionSuccesses == 0
		summary.requirementVersionWarnings == 0
		summary.requirementVersionFailures == 1
		errors*.i18nError == expectedErrors
	}
	
	@DataSet("RequirementExcelBatchImportIT.should import requirement.xml")
	def "should import requirement and bind milestone"(){
		given:
		def path = "/Projet1/Exigence"
		
		when:
		ImportLog summary = importFile("import/requirements/requirement_milestone_ok.xls")
		summary.recompute()
		def id = navService.findNodeIdByPath(path)
		def requirementVersion = navService.findRequirement(id).findRequirementVersion(1);
		
		then:
		summary.requirementVersionSuccesses == 1
		summary.requirementVersionWarnings == 0
		summary.requirementVersionFailures == 0
		id != null
		requirementVersion.getMilestones().size()==1
		requirementVersion.getMilestones()*.id == [-3]
	}
	
	@DataSet("RequirementExcelBatchImportIT.should import requirement.xml")
	def "simulate : should import requirement and bind milestone"(){
		given:
		def path = "/Projet1/Exigence"

		when:
		ImportLog summary = simulateImportFile("import/requirements/requirement_milestone_ok.xls")
		summary.recompute()
		def id = navService.findNodeIdByPath(path)

		then:
		summary.requirementVersionSuccesses == 1
		summary.requirementVersionWarnings == 0
		summary.requirementVersionFailures == 0
		id == null
	}
	
	@DataSet("RequirementExcelBatchImportIT.should import requirement.xml")
	def "should import 2 requirement version and bind only one time the same milestone"(){
		given:
		def path = "/Projet1/Exigence"

		when:
		ImportLog summary = importFile("import/requirements/requirement_milestone_multi_bind.xls")
		summary.recompute()
		def id = navService.findNodeIdByPath(path)
		def requirementVersionWithMilestoneBinded = navService.findRequirement(id).findRequirementVersion(1);
		def requirementVersionWithoutMilestone = navService.findRequirement(id).findRequirementVersion(2);
		def errors = summary.findAllFor(EntityType.REQUIREMENT_VERSION);
		
		then:
		summary.requirementVersionSuccesses == 1
		summary.requirementVersionWarnings == 1
		summary.requirementVersionFailures == 0
		requirementVersionWithMilestoneBinded != null
		requirementVersionWithMilestoneBinded.getMilestones().size()==1
		requirementVersionWithoutMilestone != null
		requirementVersionWithoutMilestone.getMilestones().size()==0
		errors*.i18nError == [null,Messages.WARN_MILESTONE_USED]
	}
	
	@DataSet("RequirementExcelBatchImportIT.should import requirement.xml")
	def "simulate : should import 2 requirement version and bind only one time the same milestone"(){
		given:
		def path = "/Projet1/Exigence"

		when:
		ImportLog summary = simulateImportFile("import/requirements/requirement_milestone_multi_bind.xls")
		summary.recompute()
		def id = navService.findNodeIdByPath(path)
		def errors = summary.findAllFor(EntityType.REQUIREMENT_VERSION);
		
		then:
		summary.requirementVersionSuccesses == 1
		summary.requirementVersionWarnings == 1
		summary.requirementVersionFailures == 0
		
		id == null
		
		errors*.i18nError == [null,Messages.WARN_MILESTONE_USED]
	}
	
	@DataSet("RequirementExcelBatchImportIT.should import requirement.xml")
	def "shouldn't import requirement because milestone status"(){
		given:
		def paths = ["/Projet1/Exigence","/Projet1/Exigence1","/Projet1/Exigence2","/Projet1/Exigence3"]

		when:
		ImportLog summary = importFile("import/requirements/requirement_milestone_failed_status.xls")
		summary.recompute()
		def errors = summary.findAllFor(EntityType.REQUIREMENT_VERSION);

		then:

		summary.requirementVersionSuccesses == 0
		summary.requirementVersionWarnings == 0
		summary.requirementVersionFailures == 4
		paths.collect {
			navService.findNodeIdByPath(it)==null
		}
		errors*.i18nError == [Messages.ERROR_WRONG_MILESTONE_STATUS,
			Messages.ERROR_WRONG_MILESTONE_STATUS,
			Messages.ERROR_WRONG_MILESTONE_STATUS,
			Messages.ERROR_WRONG_MILESTONE_STATUS]
	}
	
	@DataSet("RequirementExcelBatchImportIT.should import requirement.xml")
	def "simulate : shouldn't import requirement because milestone status"(){
		given:
		def paths = ["/Projet1/Exigence", "/Projet1/Exigence1", "/Projet1/Exigence2", "/Projet1/Exigence3"]

		when:
		ImportLog summary = simulateImportFile("import/requirements/requirement_milestone_failed_status.xls")
		summary.recompute()
		def errors = summary.findAllFor(EntityType.REQUIREMENT_VERSION);

		then:

		summary.requirementVersionSuccesses == 0
		summary.requirementVersionWarnings == 0
		summary.requirementVersionFailures == 4
		paths.collect {
			navService.findNodeIdByPath(it)==null
		}
		errors*.i18nError == [Messages.ERROR_WRONG_MILESTONE_STATUS, Messages.ERROR_WRONG_MILESTONE_STATUS, Messages.ERROR_WRONG_MILESTONE_STATUS, Messages.ERROR_WRONG_MILESTONE_STATUS]
	}
	
	//************************ UPDATE MODE ************************//
	
	@DataSet("RequirementExcelBatchImportIT.should update requirement.xml")
	def "should modify core attribute"(){
		given:
		def paths = ["/project/req2"]
		def reqVersionMap = [11:[11, 12, 13], 21:[21], 31:[31], 41:[41]]
		attachRequirementVersionMap(reqVersionMap)

		when:
		ImportLog summary = importFile("import/requirements/requirement_update_core_attribute.xls")
		summary.recompute()
		def requirement = navService.findRequirement(-21)
		def requirementVersion = requirement.findRequirementVersion(1)

		then:
		summary.requirementVersionSuccesses == 1
		summary.requirementVersionWarnings == 0
		summary.requirementVersionFailures == 0
		requirementVersion.getReference()=="ref_modif"
		requirementVersion.getDescription()=="description_modif"
		
	}
	
	@DataSet("RequirementExcelBatchImportIT.should clear milestone binding from requirement.xml")
	def "should clear milestone binding from requirement"(){
		given:
		def reqVersionMap = [11:[11, 12, 13], 21:[21], 31:[31], 41:[41]]
		attachRequirementVersionMap(reqVersionMap)

		when:
		ImportLog summary = importFile("import/requirements/requirement_update_core_attribute.xls")
		summary.recompute()
		def requirement = navService.findRequirement(-21)
		def requirementVersion = requirement.findRequirementVersion(1)

		then:
		summary.requirementVersionSuccesses == 1
		summary.requirementVersionWarnings == 0
		summary.requirementVersionFailures == 0
		requirementVersion.getMilestones().size()==0
									
	}
}
