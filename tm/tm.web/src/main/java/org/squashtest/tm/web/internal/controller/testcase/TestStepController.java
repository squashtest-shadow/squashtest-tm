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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.service.customfield.CustomFieldValueFinderService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.testcase.TestStepModificationService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.customfield.CustomFieldValueConfigurationBean;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableMapperPagingAndSortingAdapter;
import org.squashtest.tm.web.internal.model.datatable.DataTableMapperPagingAndSortingAdapter.SortedAttributeSource;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelHelper;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

@Controller
@RequestMapping("/test-steps/{testStepId}")
public class TestStepController {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestStepController.class);

	@Inject
	private TestStepModificationService testStepService;

	@Inject
	private CustomFieldValueFinderService cufValueFinder;

	@Inject
	private PermissionEvaluationService permissionEvaluationService;
	
	@Inject
	private InternationalizationHelper messageSource;

	/**
	 * Shows the step modification page.
	 * 
	 * @param testStepId
	 *            the id of the step to show
	 * @param model
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String showStepInfos(@PathVariable long testStepId, Model model) {
		
		LOGGER.info("Show Test Step initiated");
		LOGGER.debug("Find and show TestStep #{}", testStepId);
		TestStep testStep = testStepService.findById(testStepId);
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
		// ------------------------------------ATTACHMENT  AND CUF PART
		boolean hasCUF = false;
		List<CustomFieldValue> values = Collections.emptyList();
		if (testStepView.getActionStep() != null) {
			model.addAttribute("attachableEntity", testStepView.getActionStep());
			values = cufValueFinder.findAllCustomFieldValues(testStepView.getActionStep().getBoundEntityId(),
					testStepView.getActionStep().getBoundEntityType());hasCUF = cufValueFinder.hasCustomFields(testStepView.getActionStep());
			boolean linkable = permissionEvaluationService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "LINK", testStep);
			model.addAttribute("linkable", linkable); // right to bind steps to requirement
			RequirementVerifierView requirementVerifierView = new RequirementVerifierView(testStepView.getActionStep());
			model.addAttribute("requirementVerifier", requirementVerifierView);
		}
		
		
		else{
			values = Collections.emptyList();
		}
		CustomFieldValueConfigurationBean conf =  new CustomFieldValueConfigurationBean(values);
		model.addAttribute("configuration", conf);
		
		
		model.addAttribute("hasCUF", hasCUF);

		return "edit-test-step.html";
	}

	/**
	 * update the TestStep infos
	 * 
	 * @param testStepId
	 * @param testStepModel
	 */
	@RequestMapping(method = RequestMethod.POST, headers = { "Content-Type=application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	public void updateStep(@PathVariable Long testStepId, @RequestBody TestStepUpdateFormModel testStepModel) {
		testStepService.updateTestStep(testStepId, testStepModel.getAction(), testStepModel.getExpectedResult(), testStepModel.getCufValues());
	}

	/**
	 * gets the table model for step's verified requirement versions.
	 * 
	 * @param params: the {@link DataTableDrawParameters}
	 * @param testStepId : the id of the concerned {@link TestStep}
	 * @return a {@link DataTableModel} for the table of verified {@link RequirementVersion}
	 */
	@RequestMapping(value="/verified-requirement-versions", method=RequestMethod.GET, params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getVerifiedRequirementTable(DataTableDrawParameters params, @PathVariable long testStepId){
		PagingAndSorting paging = new DataTableMapperPagingAndSortingAdapter(params, verifiedRequirementVersionsMapper, SortedAttributeSource.SINGLE_ENTITY);
		Locale locale = LocaleContextHolder.getLocale();
		PagedCollectionHolder<List<RequirementVersion>> holder = testStepService.findSortedVerifiedRequirementVersions(testStepId, paging);
		return new VerifiedRequirementsDataTableModelHelper(locale, messageSource).buildDataModel(holder, params.getsEcho());
	}
	
	private DatatableMapper<String> verifiedRequirementVersionsMapper = new NameBasedMapper(1).mapAttribute(RequirementVersion.class, "id", String.class, "entity-id")
			.mapAttribute(RequirementVersion.class, "name", String.class, "name")
			.mapAttribute(RequirementVersion.class, "requirement.project", String.class, "project")
	.mapAttribute(RequirementVersion.class, "reference", String.class, "reference")
	.mapAttribute(RequirementVersion.class, "version", String.class, "versionNumber")
	.mapAttribute(RequirementVersion.class, "criticality", String.class, "criticality")
	.mapAttribute(RequirementVersion.class, "category", String.class, "category");
	
	private static final class VerifiedRequirementsDataTableModelHelper extends DataTableModelHelper<RequirementVersion> {
		private InternationalizationHelper messageSource;
		private Locale locale;
		private VerifiedRequirementsDataTableModelHelper(Locale locale, InternationalizationHelper messageSource){
			this.locale = locale;
			this.messageSource = messageSource;
		}
		@Override
		public Map<String, Object> buildItemData(RequirementVersion item) {
			Map<String, Object> res = new HashMap<String, Object>();
			res.put(DataTableModelHelper.DEFAULT_ENTITY_ID_KEY, item.getId());
			res.put(DataTableModelHelper.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex());
			res.put("name", item.getName());
			res.put("project", item.getProject());
			res.put("reference", item.getReference());
			res.put("version", item.getVersionNumber());
			res.put("criticality", messageSource.getMessage(item.getCriticality().getI18nKey(), null, locale));
			res.put("category", messageSource.getMessage(item.getCategory().getI18nKey(), null, locale));
			res.put("status", item.getStatus());			
			res.put(DataTableModelHelper.DEFAULT_EMPTY_DELETE_HOLDER_KEY, " ");
			return res;
		}
	}
	
}
