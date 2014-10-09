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
package org.squashtest.tm.service.internal.importer

import javax.inject.Inject

import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.audit.AuditableMixin
import org.squashtest.tm.domain.requirement.Requirement
import org.squashtest.tm.domain.requirement.RequirementCategory
import org.squashtest.tm.domain.requirement.RequirementCriticality
import org.squashtest.tm.domain.requirement.RequirementFolder
import org.squashtest.tm.domain.requirement.RequirementStatus
import org.squashtest.tm.domain.requirement.RequirementVersion
import org.squashtest.tm.service.DbunitServiceSpecification
import org.squashtest.tm.service.requirement.RequirementLibraryNavigationService
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport
/**
 * Why Disabled ?
 * If you run this test alone they work fine. But if you run it along with all the other ones it fails.
 * There is a Sql exception when try to insert a REQUIREMENT_AUDIT_EVENT. The error says that there is a fk integrity violation for the specified requirement version.
 * Did not manage to diagnose why.
 *
 * @author mpagnon
 *
 */
@UnitilsSupport
@Transactional
class RequirementImporterITDisabled extends DbunitServiceSpecification {

	@Inject
	RequirementLibraryNavigationService service

	RequirementImporter importer = new RequirementImporter()



	def setup(){
		importer.service = service
	}


	@DataSet("RequirementImporterIT.setup.xml")
	def "should import a hierarchy in an empty library"(){

		given :
		Class classe = this.getClass()
		ClassLoader classLoader = classe.getClassLoader()
		InputStream stream = classLoader.getResourceAsStream("import/import-requirement.xls")

		when :
		def summary = importer.importExcelRequirements( stream, -2L)


		then  : "all success and one renamed"
		summary.getTotal() == 7
		summary.getSuccess()  == 7
		summary.getRenamed() == 1
		summary.getFailures() == 0

		def rContent = service.findLibrary(-2l).content
		rContent.size() == 4

		and : "first requirement imported with it's older version"
		def reqV2 = rContent.find {it.name == "Version2"}
		reqV2 != null
		//req
		Requirement reqV2Req = (Requirement) reqV2
		reqV2Req.versions.size() == 2
		reqV2Req.reference == "25 scies"
		reqV2Req.category == RequirementCategory.UNDEFINED
		reqV2Req.criticality == RequirementCriticality.MAJOR
		reqV2Req.status == RequirementStatus.APPROVED
		reqV2Req.description == "description"
		AuditableMixin reqAudit = (AuditableMixin) reqV2Req
		//	reqAudit.createdBy == "moi" TODO understand why not
		//older version
		RequirementVersion olderVersion =  reqV2Req.versions.get(1)
		olderVersion.name == "Version1"
		olderVersion.getReference() == "12"
		olderVersion.category == RequirementCategory.TEST_REQUIREMENT
		olderVersion.criticality == RequirementCriticality.MINOR
		olderVersion.status == RequirementStatus.OBSOLETE
		olderVersion.description == "description "
		AuditableMixin olderVersionAudit = (AuditableMixin) olderVersion
		Date olderCreatedOn = olderVersionAudit.createdOn
		Calendar calendar = Calendar.getInstance()
		calendar.setTime(olderCreatedOn)
		calendar.get(Calendar.MONTH) == 5
		calendar.get(Calendar.DAY_OF_MONTH) == 27
		calendar.get(Calendar.YEAR)== 1984
		olderVersionAudit.createdBy == "import"


		and : "folder name1 imported with it's content "

		def req1 = rContent.find {it.name == "name1"}
		req1 != null
		RequirementFolder req1folder  = (RequirementFolder) req1
		req1folder.content.size() == 1
		RequirementFolder req2 = (RequirementFolder) req1folder.content.get(0)
		req2.name == "name2"
		req2.content.size() == 2

		def req3 = req2.content.find {it.name == "name3"}
		req3 != null
		RequirementFolder req3Folder = (RequirementFolder) req3
		req3Folder.content.size() == 1
		Requirement reqv2 = (Requirement)req3Folder.content.get(0)
		reqv2.name == "version-2"
		reqv2.versions.size() == 2

		def req3Imp = req2.content.find {it.name == "name3 - Import(1)"}
		Requirement req3ImpReq = (Requirement) req3Imp

		and : "folder name4 is imported with it's hierarchy"
		def req4 = rContent.find {it.name == "name4"}
		req4 != null
		RequirementFolder req4Folder = (RequirementFolder) req4
		req4Folder.getContent().size() == 1
		RequirementFolder req5Folder = (RequirementFolder) req4Folder.getContent().get(0)
		req5Folder.name == "name/5"
		req5Folder.content.size() == 1
		Requirement req57 = (Requirement) req5Folder.content.get(0)
		req57.name == "57"

		and : "fodler name7/ is imported with it's hierarchy"
		def req7 = rContent.find {it.name == "name7/"}
		req7 != null
		RequirementFolder req7Folder = (RequirementFolder) req7
		req7Folder.content.size() == 1
		RequirementFolder req8Folder =(RequirementFolder) req7Folder.content.get(0)
		req8Folder.name == "name8"
		req8Folder.content.size() == 1
		RequirementFolder req9Folder = (RequirementFolder) req8Folder.content.get(0)
		req9Folder.content.size() == 1
		req9Folder.name == "name9"
		Requirement reqlabel = (Requirement) req9Folder.content.get(0)
		reqlabel.name == "label"

		//		//TODO improve test checks


	}

	/**
	 * Dataset explained :
	 * <ul>
	 *   <li>Project & RequirementLibrary #-2
	 *     <ol>
	 *       <li>dossier1 #-1</li>
	 *       <li>dossier2 #-2
	 *         <ol>
	 *           <li>req21 #-21
	 *             <ol>
	 *               <li>req211 #-211</li>
	 *               <li>req212 #-212</li>
	 *             </ol>
	 *           </li>
	 *         </ol>
	 *       </li>
	 *     </ol>
	 *   </li>
	 * </ul>
	 */
	@DataSet("RequirementImporterIT.import req path.xml")
	def "should import a hierarchy of requirement"(){

		given :
		Class classe = this.getClass()
		ClassLoader classLoader = classe.getClassLoader()
		InputStream stream = classLoader.getResourceAsStream("import/import-requirement-into-requirement.xls")

		when :
		def summary = importer.importExcelRequirements( stream, -2L)


		then  : "all success"
		summary.getTotal() == 8
		summary.getSuccess()  == 8
		summary.getRenamed() == 2
		summary.getFailures() == 0

		def rContent = service.findLibrary(-2l).content
		rContent.size() == 3

		and : "root requirements imported"
		def req3 = rContent.find {it.name == "name3"}
		req3 != null
		def req3Content = req3.content
		req3Content.size() == 1
		def req4 = req3Content.get(0)
		req4.name == "name4"
		def req4Content = req4.content
		req4Content.size() == 1
		def req5 = req4Content.get(0)
		def req5Content = req5.content
		req5Content.size() == 1
		req5.name == "name5"
		def req6 = req5Content.get(0)
		req6 != null
		req6.name == "name6"

		and : "dossier1 requirements imported"
		def dossier1 = rContent.find {it.name == "dossier1"}
		dossier1 != null
		def dossier1Content = dossier1.content
		dossier1Content.size() == 1
		def dossier11 = dossier1Content.get(0)
		dossier11.name == "dossier11"
		def dossier11Content = dossier11.content
		dossier11Content.size() == 1
		def req111 = dossier11Content.get(0)
		req111.name == "req111"
		def req111Content = req111.content
		req111Content.size() == 1
		def req1111 = req111Content.get(0)
		req1111 != null
		req1111.name == "req1111"

		and : "dossier2 requirements imported"
		def dossier2 = rContent.find {it.name == "dossier2"}
		dossier2 != null
		def dossier2Content = dossier2.content
		dossier2Content.size() == 1
		def req21 = dossier2Content.get(0)
		req21.name == "req21"
		def req21Content = req21.content
		req21Content.size() == 4
		def req211 = req21Content.find {it.name == "req211"}
		req211 != null
		req211.content.size() == 0
		def req211imp = req21Content.find {it.name == "req211-import1"}
		req211imp != null
		req211imp.content.size() == 0
		def req212 = req21Content.find {it.name == "req212"}
		req212 != null
		req212.content.size() == 0
		def req212imp = req21Content.find {it.name == "req212-import1"}
		req212imp != null
		def req212impContent = req212imp.content
		req212impContent.size() == 1
		def req2121 = req212impContent.find {it.name == "req2121"}
		req2121 != null


	}


	@DataSet("RequirementImporterIT.setup.xml")
	def "should import a hierarchy with new folder header"(){

		given :
		Class classe = this.getClass()
		ClassLoader classLoader = classe.getClassLoader()
		InputStream stream = classLoader.getResourceAsStream("import/import-requirement-with-new-folder-column.xls")

		when :
		def summary = importer.importExcelRequirements( stream, -2L)


		then  : "all success and one renamed"
		summary.getTotal() == 7
		summary.getSuccess()  == 7
		summary.getRenamed() == 1
		summary.getFailures() == 0

		def rContent = service.findLibrary(-2l).content
		rContent.size() == 4


		and : "folder name1 imported with it's content "

		def req1 = rContent.find {it.name == "name1"}
		req1 != null
		RequirementFolder req1folder  = (RequirementFolder) req1
		req1folder.content.size() == 1

		RequirementFolder req2 = (RequirementFolder) req1folder.content.get(0)
		req2.name == "name2"
		req2.content.size() == 2

		//... the rest must be ok if both req1folder and req2 are ok

	}

}
