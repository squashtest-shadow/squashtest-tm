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
package org.squashtest.csp.tm.web.internal.controller.requirement;

import org.springframework.context.MessageSource;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.service.RequirementModificationService;

import spock.lang.Specification;



class RequirementModificationControllerTest extends Specification {
	RequirementModificationController controller = new RequirementModificationController()
	RequirementModificationService requirementModificationService= Mock()
	MessageSource messageSource = Mock()
	
	def setup() {
		controller.requirementModificationService = requirementModificationService 
		controller.messageSource = messageSource
	} 
	
	def "should return requirement page fragment"() {
		given:
		Requirement req = Mock(Requirement.class)
		long reqId=15
		requirementModificationService.findById(15) >> req
		
		when:
		ModelAndView res = controller.showRequirement(reqId, null)
		
		then:
		res.viewName == "fragment/requirements/edit-requirement"
		res.modelMap['requirement'] == req
	}

	
	
}
