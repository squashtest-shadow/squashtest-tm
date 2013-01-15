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
package org.squashtest.tm.web.internal.controller.testcase;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.squashtest.csp.core.service.security.PermissionEvaluationService;
import org.squashtest.csp.tm.domain.testcase.TestStep;
import org.squashtest.csp.tm.service.TestCaseFinder;
import org.squashtest.csp.tm.service.testcase.TestStepFinder;


@Controller
@RequestMapping("/test-steps/{testStepId}")
public class TestStepController {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestStepController.class);
	
	@Inject
	private TestStepFinder testStepFinder; 
	
	@Inject
	private TestCaseFinder testCaseFinder;
	
	@Inject
	private PermissionEvaluationService permissionEvaluationService;
	
	/**
	 * Shows the custom field modification page.
	 * 
	 * @param customFieldId
	 *            the id of the custom field to show
	 * @param model
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String showCustomFieldModificationPage(@PathVariable Long testStepId, Model model) {
		LOGGER.info("Show Test Step initiated");
		LOGGER.debug("Find and show TestStep #{}", testStepId);
		TestStep testStep = testStepFinder.findById(testStepId);
		TestStepView testStepView = new TestStepViewBuilder().buildTestStepView(testStep);
		model.addAttribute("testStepView", testStepView);
		model.addAttribute("workspace", "test-case");
		//waiting for [Task 1843]
		boolean writable = permissionEvaluationService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "WRITE", testStep);
		model.addAttribute("writable", writable); //right to modify steps
		boolean attachable = permissionEvaluationService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "ATTACH", testStep);
		model.addAttribute("attachable", attachable); //right to modify steps
		//end waiting for [Task 1843]
		if(testStepView.getActionStep() != null){
			model.addAttribute("attachableEntity", testStepView.getActionStep());
		}
		return "edit-test-step.html";
	}

}
