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
package org.squashtest.tm.service.internal.importer

import javax.inject.Inject

import org.hibernate.SessionFactory
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.testcase.TestCaseFolder
import org.squashtest.tm.service.DbunitServiceSpecification
import org.squashtest.tm.service.requirement.RequirementLibraryNavigationService
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
class RequirementImporterITDisabled extends DbunitServiceSpecification {

	@Inject
	RequirementLibraryNavigationService service

	RequirementImporter importer = new RequirementImporter()



	def setup(){
		importer.service = service

	}


	//Still not working
	//	@DataSet("RequirementImporterIT.setup.xml")
	//	def "should import a hierarchy in an empty library"(){
	//
	//		given :
	//		Class classe = this.getClass()
	//		ClassLoader classLoader = classe.getClassLoader()
	//		InputStream stream = classLoader.getResourceAsStream("import/import-requirement.xlsx")
	//
	//		when :
	//		def summary = importer.importExcelRequirements( stream, 1L)
	//
	//		then :
	//		summary.getTotal() == 7
	//		summary.getSuccess()  == 7
	//		summary.getRenamed() == 1
	//		summary.getFailures() == 0
	//
	//		def rContent = service.findLibrary(1l).content
	//
	//		def names = rContent*.name as Set
	//		names ==  ["Version2", "name1", "name4", "name7/" ] as Set
	//		//TODO improve test checks
	//
	//	}



}
