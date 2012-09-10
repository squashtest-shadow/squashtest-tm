/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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

import org.springframework.transaction.annotation.Transactional;

import spock.unitils.UnitilsSupport;
import java.util.List;
import java.util.ArrayList;

import org.squashtest.csp.tm.service.RequirementLibraryNavigationService;
import org.unitils.dbunit.annotation.DataSet;




import javax.inject.Inject;

@UnitilsSupport
@Transactional
class RequirementLibraryNavigationServiceIT extends DbunitServiceSpecification {


	@Inject
	RequirementLibraryNavigationService navService;

	private int requirementId = 10

	@DataSet("RequirementLibraryNavigationServiceIT.should return all the requirements in a hierarchy given some ids.xml")
	def "should return all the requirements in a hierarchy given some ids"(){

		given:
		List<Long> listReq = new ArrayList()
		listReq.add(1l)
		listReq.add(250l)

		when :
		def reqs = navService.findRequirementsToExportFromFolder (listReq)
		println reqs.toString()

		then :
		reqs != null
		reqs.size() == 3

		def export1 = reqs.findAll{r -> r.name == "1req"}[0];
		def export2 = reqs.findAll{r -> r.name == "req2"}[0];
		def export3 = reqs.findAll{r -> r.name == "req3"}[0];


		export1.name == "1req"
		export1.folderName == "projet1/folder/1req"
		export1.project == "projet1"

		export2.name == "req2"
		export2.folderName == "projet1/folder/subfolder/req2"
		export2.project == "projet1"

		export3.name == "req3"
		export3.folderName == "projet1/req3"
	}
}

