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

import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.internal.repository.RequirementDao;
import org.squashtest.csp.tm.domain.requirement.RequirementCriticality;

import spock.lang.Specification;

class RequirementModificationServiceImplTest extends Specification {
	RequirementModificationServiceImpl service = new RequirementModificationServiceImpl()
	RequirementDao requirementDao = Mock()

	def setup() {
		service.requirementDao = requirementDao
	}

	def "should find requirement and change its criticality"(){
		given:
		def Requirement requirement = new Requirement()
		requirementDao.findById(5) >> requirement

		when:
		service.updateRequirementCriticality(5, RequirementCriticality.MINOR)

		then:
		requirement.criticality == RequirementCriticality.MINOR
	}

	def "should find requirement and change its reference"(){
		given:
		def Requirement requirement = new Requirement()
		requirementDao.findById(5) >> requirement
		def reference = "a reference"

		when:
		service.updateRequirementReference(5, reference)

		then:
		requirement.reference == reference
	}
}
