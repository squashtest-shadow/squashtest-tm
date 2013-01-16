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

import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.squashtest.csp.core.service.security.PermissionEvaluationService;
import org.squashtest.csp.tm.domain.customfield.CustomFieldValue;
import org.squashtest.csp.tm.domain.testcase.TestStep;
import org.squashtest.csp.tm.service.TestCaseModificationService;
import org.squashtest.csp.tm.service.customfield.CustomFieldValueManagerService;
import org.squashtest.csp.tm.service.testcase.TestStepFinder;
import org.squashtest.tm.web.internal.model.customfield.CustomFieldValueConfigurationBean;

@Controller
@RequestMapping("/test-steps/{testStepId}")
public class TestStepController {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestStepController.class);

	@Inject
	private TestStepFinder testStepFinder;

	@Inject
	private CustomFieldValueManagerService cufValueService;

	@Inject
	private TestCaseModificationService testCaseModService;

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
		// ------------------------------------RIGHTS PART
		// waiting for [Task 1843]
		boolean writable = permissionEvaluationService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "WRITE", testStep);
		model.addAttribute("writable", writable); // right to modify steps
		boolean attachable = permissionEvaluationService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "ATTACH", testStep);
		model.addAttribute("attachable", attachable); // right to modify steps
		// end waiting for [Task 1843]
		// ------------------------------------ATTACHMENT PART
		if (testStepView.getActionStep() != null) {
			model.addAttribute("attachableEntity", testStepView.getActionStep());
		}
		// -----------------------------------------CUF PART
		List<CustomFieldValue> values = cufValueService.findAllCustomFieldValues(testStep.getBoundEntityId(),
				testStep.getBoundEntityType());
		CustomFieldValueConfigurationBean conf = new CustomFieldValueConfigurationBean(values);
		model.addAttribute("configuration", conf);
		boolean hasCUF = cufValueService.hasCustomFields(testStep);
		model.addAttribute("hasCUF", hasCUF);

		return "edit-test-step.html";
	}

	/**
	 * update the TestStep infos
	 * 
	 * @param testStepId
	 * @param testStepUpdateFormModel
	 */
	@RequestMapping(method = RequestMethod.POST, headers = { "Content-Type=application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	public void updateStep(@PathVariable Long testStepId, @RequestBody TestStepUpdateFormModel testStepUpdateFormModel) {
		TestStep stepToUpdate = testStepFinder.findById(testStepId);
		if(testStepUpdateFormModel.getAction() != null ){
		testCaseModService.updateTestStepAction(testStepId, testStepUpdateFormModel.getAction());
		testCaseModService.updateTestStepExpectedResult(testStepId, testStepUpdateFormModel.getExpectedResult());
		}
		updateCufValues(stepToUpdate, testStepUpdateFormModel);

	}

	private void updateCufValues(TestStep step, TestStepUpdateFormModel testStepUpdateFormModel) {
		if (testStepUpdateFormModel.getCufValues() != null) {
			if (!permissionEvaluationService
					.hasRoleOrPermissionOnObject("ROLE_ADMIN", "WRITE", step)) {
				throw new AccessDeniedException("Access is denied");
			}

			for (Entry<Long, String> cufValue : testStepUpdateFormModel.getCufValues().entrySet()) {
				cufValueService.update(cufValue.getKey(), cufValue.getValue());
			}
		}
	}

}
