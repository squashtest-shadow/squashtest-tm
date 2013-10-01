/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignTestPlanItem;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldOption;
import org.squashtest.tm.domain.customfield.SingleSelectField;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.search.AdvancedSearchModel;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseNature;
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.domain.testcase.TestCaseType;
import org.squashtest.tm.service.campaign.CampaignTestPlanManagerService;
import org.squashtest.tm.service.campaign.IterationModificationService;
import org.squashtest.tm.service.campaign.IterationTestPlanManagerService;
import org.squashtest.tm.service.campaign.TestSuiteTestPlanManagerService;
import org.squashtest.tm.service.library.AdvancedSearchService;
import org.squashtest.tm.service.project.ProjectFilterModificationService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.testcase.VerifyingTestCaseManagerService;
import org.squashtest.tm.web.internal.controller.RequestHeaders;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.testcase.TestCaseImportanceJeditableComboDataBuilder;
import org.squashtest.tm.web.internal.controller.testcase.TestCaseNatureJeditableComboDataBuilder;
import org.squashtest.tm.web.internal.controller.testcase.TestCaseStatusJeditableComboDataBuilder;
import org.squashtest.tm.web.internal.controller.testcase.TestCaseTypeJeditableComboDataBuilder;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.model.datatable.DataTableSorting;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

@Controller
@RequestMapping("/advanced-search")
public class AdvancedSearchController {

	private static final String TEXTFIELD = "textfield";
	private static final String TEXTAREA = "textarea";
	private static final String RANGE = "range";
	private static final String EXISTS = "exists";
	private static final String DATE = "date";
	private static final String MULTISELECT = "multiselect";
	private static final String COMBOMULTISELECT = "combomultiselect";
	private static final String ATLEASTONE = "1";
	private static final String NONE = "0";

	@Inject
	private AdvancedSearchService advancedSearchService;

	@Inject
	private PermissionEvaluationService permissionService;

	@Inject
	private IterationModificationService iterationService;

	@Inject
	private VerifyingTestCaseManagerService verifyingTestCaseManagerService;

	@Inject
	private InternationalizationHelper messageSource;

	@Inject
	private Provider<TestCaseImportanceJeditableComboDataBuilder> importanceComboBuilderProvider;

	@Inject
	private Provider<TestCaseNatureJeditableComboDataBuilder> natureComboBuilderProvider;

	@Inject
	private Provider<TestCaseTypeJeditableComboDataBuilder> typeComboBuilderProvider;

	@Inject
	private Provider<TestCaseStatusJeditableComboDataBuilder> statusComboBuilderProvider;

	@Inject
	private CampaignTestPlanManagerService campaignTestPlanManagerService;

	@Inject
	private IterationTestPlanManagerService iterationTestPlanManagerService;

	@Inject
	private TestSuiteTestPlanManagerService testSuiteTestPlanManagerService;

	private ProjectFilterModificationService projectFilterService;

	@ServiceReference
	public void setProjectFilterModificationService(
			ProjectFilterModificationService service) {
		this.projectFilterService = service;
	}

	// These are used by Lucene - Thus the columns are mapped to index
	// properties rather than class properties
	private DatatableMapper<String> testCaseSearchResultMapper = new NameBasedMapper(
			11)
			.mapAttribute("project-name", "name", Project.class)
			.mapAttribute("test-case-id", "id", TestCase.class)
			.mapAttribute("test-case-ref", "reference", TestCase.class)
			.mapAttribute("test-case-label", "label", TestCase.class)
			.mapAttribute("test-case-weight", "importance", TestCase.class)
			.mapAttribute("test-case-nature", "nature", TestCase.class)
			.mapAttribute("test-case-type", "type", TestCase.class)
			.mapAttribute("test-case-status", "status", TestCase.class)
			.mapAttribute("test-case-requirement-nb", "requirements",
					TestCase.class)
			.mapAttribute("test-case-teststep-nb", "steps", TestCase.class)
			.mapAttribute("test-case-iteration-nb", "iterations",
					TestCase.class)
			.mapAttribute("test-case-attachment-nb", "attachments",
					TestCase.class)
			.mapAttribute("test-case-created-by", "createdBy", TestCase.class)
			.mapAttribute("test-case-modified-by", "modifiedBy", TestCase.class);

	@RequestMapping(method = RequestMethod.GET, params = "testcase")
	public String getTestCaseSearchTab(Model model,
			@RequestParam(required = false) String associateResultWithType,
			@RequestParam(required = false) Long id) {

		initModelForPage(model, associateResultWithType, id);
		return "test-case-search-input.html";
	}

	private void initModelForPage(Model model, String associateResultWithType,
			Long id) {
		if (StringUtils.isNotBlank(associateResultWithType)) {
			model.addAttribute("associateResult", true);
			model.addAttribute("associateResultWithType",
					associateResultWithType);
			model.addAttribute("associateId", id);
		} else {
			model.addAttribute("associateResult", false);
		}
	}

	@RequestMapping(method = RequestMethod.POST, params = "testcase")
	public String getTestCaseSearchTab(Model model,
			@RequestParam String searchModel,
			@RequestParam(required = false) String associateResultWithType,
			@RequestParam(required = false) Long id) {

		initModelForPage(model, associateResultWithType, id);
		model.addAttribute("searchModel", searchModel);

		return "test-case-search-input.html";
	}

	@RequestMapping(value = "/results", method = RequestMethod.POST, params = "testcase")
	public String getTestCaseSearchResultPage(Model model,
			@RequestParam String searchModel,
			@RequestParam(required = false) String associateResultWithType,
			@RequestParam(required = false) Long id) {

		initModelForPage(model, associateResultWithType, id);
		model.addAttribute("searchModel", searchModel);

		return "test-case-search-result.html";
	}

	private boolean isInAssociationContext(String associateResultWithType) {
		boolean isInAssociationContext = false;

		if (associateResultWithType != null) {
			isInAssociationContext = true;
		}

		return isInAssociationContext;
	}

	@RequestMapping(value = "/table", method = RequestMethod.POST, params = {
			"model", RequestParams.S_ECHO_PARAM })
	@ResponseBody
	public DataTableModel getTableModel(final DataTableDrawParameters params,
			final Locale locale, @RequestParam(value = "model") String model,
			@RequestParam(required = false) String associateResultWithType,
			@RequestParam(required = false) Long id) throws JsonParseException,
			JsonMappingException, IOException {

		AdvancedSearchModel searchModel = new ObjectMapper().readValue(model,
				AdvancedSearchModel.class);

		PagingAndSorting paging = new DataTableSorting(params,
				testCaseSearchResultMapper);

		PagedCollectionHolder<List<TestCase>> holder = advancedSearchService
				.searchForTestCases(searchModel, paging);

		boolean isInAssociationContext = isInAssociationContext(associateResultWithType);

		Set<Long> ids = null;

		if (isInAssociationContext) {
			ids = getIdsOfTestCasesAssociatedWithObjects(
					associateResultWithType, id);
		}

		return new TestCaseSearchResultDataTableModelHelper(locale,
				messageSource, permissionService, iterationService,
				isInAssociationContext, ids).buildDataModel(holder,
				params.getsEcho());
	}

	private Set<Long> getIdsOfTestCasesAssociatedWithObjects(
			String associateResultWithType, Long id) {

		Set<Long> ids = new HashSet<Long>();

		if ("requirement".equals(associateResultWithType)) {
			List<TestCase> testCases = verifyingTestCaseManagerService
					.findAllByRequirementVersion(id);
			for (TestCase testCase : testCases) {
				ids.add(testCase.getId());
			}
		} else if ("campaign".equals(associateResultWithType)) {
			Campaign campaign = this.campaignTestPlanManagerService.findCampaign(id);
			for (CampaignTestPlanItem item : campaign.getTestPlan()) {
				if (item.getReferencedTestCase() != null) {
					ids.add(item.getReferencedTestCase().getId());
				}
			}
		} else if ("iteration".equals(associateResultWithType)) {
			List<TestCase> testCases = this.iterationTestPlanManagerService
					.findPlannedTestCases(id);
			for (TestCase testCase : testCases) {
				ids.add(testCase.getId());
			}
		} else if ("testsuite".equals(associateResultWithType)) {
			TestSuite testSuite = this.testSuiteTestPlanManagerService.findTestSuite(id);
			for (IterationTestPlanItem item : testSuite.getTestPlan()) {
				if (item.getReferencedTestCase() != null) {
					ids.add(item.getReferencedTestCase().getId());
				}
			}
		}

		return ids;
	}

	private SearchInputPanelModel createGeneralInfoPanel(Locale locale) {
		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(messageSource.internationalize(
				"search.testcase.generalinfos.panel.title", locale));
		panel.setOpen(true);
		panel.setId("general-information");
		panel.setLocation("column1");
		panel.addCssClass("search-icon-information");

		SearchInputFieldModel idField = new SearchInputFieldModel("id",
				messageSource.internationalize("label.id", locale), TEXTFIELD);
		panel.addField(idField);
		SearchInputFieldModel referenceField = new SearchInputFieldModel(
				"reference", messageSource.internationalize("label.reference",
						locale), TEXTFIELD);
		panel.addField(referenceField);
		SearchInputFieldModel labelField = new SearchInputFieldModel("name",
				messageSource.internationalize("label.Label", locale),
				TEXTFIELD);
		panel.addField(labelField);
		SearchInputFieldModel descriptionField = new SearchInputFieldModel(
				"description", messageSource.internationalize(
						"label.Description", locale), TEXTAREA);
		panel.addField(descriptionField);
		SearchInputFieldModel prerequisiteField = new SearchInputFieldModel(
				"prerequisite", messageSource.internationalize(
						"test-case.prerequisite.label", locale), TEXTAREA);
		panel.addField(prerequisiteField);

		return panel;
	}

	private SearchInputPanelModel createAttributePanel(Locale locale) {
		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(messageSource.internationalize(
				"search.testcase.attributes.panel.title", locale));
		panel.setOpen(true);
		panel.setId("attributes");
		panel.setLocation("column1");
		panel.addCssClass("search-icon-attributes");

		SearchInputFieldModel importanceField = new SearchInputFieldModel(
				"importance", messageSource.internationalize(
						"test-case.importance.label", locale), MULTISELECT);
		panel.addField(importanceField);

		Map<String, String> map = importanceComboBuilderProvider.get()
				.useLocale(locale).buildMap();

		int i = 1;
		for (Entry<String, String> entry : map.entrySet()) {
			SearchInputPossibleValueModel importanceOption = new SearchInputPossibleValueModel(
					entry.getValue(), i + "-" + entry.getKey());
			importanceField.addPossibleValue(importanceOption);
			i++;
		}

		SearchInputFieldModel natureField = new SearchInputFieldModel("nature",
				messageSource
						.internationalize("test-case.nature.label", locale),
				MULTISELECT);
		panel.addField(natureField);

		map = natureComboBuilderProvider.get().useLocale(locale).buildMap();

		for (Entry<String, String> entry : map.entrySet()) {
			SearchInputPossibleValueModel natureOption = new SearchInputPossibleValueModel(
					entry.getValue(), entry.getKey());
			natureField.addPossibleValue(natureOption);
		}

		SearchInputFieldModel typeField = new SearchInputFieldModel("type",
				messageSource.internationalize("test-case.type.label", locale),
				MULTISELECT);
		panel.addField(typeField);

		map = typeComboBuilderProvider.get().useLocale(locale).buildMap();

		for (Entry<String, String> entry : map.entrySet()) {
			SearchInputPossibleValueModel typeOption = new SearchInputPossibleValueModel(
					entry.getValue(), entry.getKey());
			typeField.addPossibleValue(typeOption);
		}

		SearchInputFieldModel statusField = new SearchInputFieldModel("status",
				messageSource
						.internationalize("test-case.status.label", locale),
				MULTISELECT);
		panel.addField(statusField);

		map = statusComboBuilderProvider.get().useLocale(locale).buildMap();

		int j = 1;
		for (Entry<String, String> entry : map.entrySet()) {
			SearchInputPossibleValueModel statusOption = new SearchInputPossibleValueModel(
					entry.getValue(), j + "-" + entry.getKey());
			statusField.addPossibleValue(statusOption);
			j++;
		}
		return panel;
	}

	private SearchInputPanelModel createAssociationPanel(Locale locale) {

		SearchInputPanelModel panel = new SearchInputPanelModel();

		panel.setTitle(messageSource.internationalize(
				"search.testcase.association.panel.title", locale));
		panel.setOpen(true);
		panel.setId("association");
		panel.setLocation("column2");
		panel.addCssClass("search-icon-associations");

		SearchInputFieldModel requirementsField = new SearchInputFieldModel(
				"requirements",
				messageSource
						.internationalize(
								"search.testcase.association.requirement.label",
								locale), RANGE);
		panel.addField(requirementsField);

		SearchInputFieldModel iterationsField = new SearchInputFieldModel(
				"iterations", messageSource.internationalize(
						"search.testcase.association.iteration.label", locale),
				EXISTS);
		panel.addField(iterationsField);

		iterationsField.addPossibleValue(new SearchInputPossibleValueModel(
				messageSource.internationalize(
						"search.testcase.association.iteration.atleastone",
						locale), ATLEASTONE));
		iterationsField.addPossibleValue(new SearchInputPossibleValueModel(
				messageSource.internationalize(
						"search.testcase.association.iteration.none", locale),
				NONE));

		SearchInputFieldModel executionsField = new SearchInputFieldModel(
				"executions", messageSource.internationalize(
						"search.testcase.association.execution.label", locale),
				EXISTS);
		panel.addField(executionsField);

		executionsField.addPossibleValue(new SearchInputPossibleValueModel(
				messageSource.internationalize(
						"search.testcase.association.execution.atleastone",
						locale), ATLEASTONE));
		executionsField.addPossibleValue(new SearchInputPossibleValueModel(
				messageSource.internationalize(
						"search.testcase.association.execution.none", locale),
				NONE));

		SearchInputFieldModel issuesField = new SearchInputFieldModel("issues",
				messageSource.internationalize(
						"search.testcase.association.issue.label", locale),
				RANGE);
		panel.addField(issuesField);

		return panel;
	}

	private SearchInputPanelModel createPerimeterPanel(Locale locale) {

		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(messageSource.internationalize(
				"search.testcase.perimeter.panel.title", locale));
		panel.setOpen(true);
		panel.setId("perimeter");
		panel.setLocation("column2");
		panel.addCssClass("search-icon-perimeter");

		SearchInputFieldModel projectField = new SearchInputFieldModel(
				"project.id", messageSource.internationalize(
						"search.testcase.perimeter.field.title", locale),
				MULTISELECT);
		panel.addField(projectField);

		List<Project> projects = this.projectFilterService.getAllProjects();
		for (Project project : projects) {
			SearchInputPossibleValueModel projectOption = new SearchInputPossibleValueModel(
					project.getName(), project.getId().toString());
			projectField.addPossibleValue(projectOption);
		}

		return panel;
	}

	private SearchInputPanelModel createContentPanel(Locale locale) {

		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(messageSource.internationalize(
				"search.testcase.content.panel.title", locale));
		panel.setOpen(true);
		panel.setId("content");
		panel.setLocation("column2");
		panel.addCssClass("search-icon-content");

		SearchInputFieldModel teststepField = new SearchInputFieldModel(
				"steps", messageSource.internationalize(
						"search.testcase.content.teststep.label", locale),
				RANGE);
		panel.addField(teststepField);

		SearchInputFieldModel parameterField = new SearchInputFieldModel(
				"parameters", messageSource.internationalize(
						"search.testcase.content.parameter.label", locale),
				EXISTS);
		panel.addField(parameterField);

		parameterField
				.addPossibleValue(new SearchInputPossibleValueModel(
						messageSource.internationalize(
								"search.testcase.content.parameter.atleastone",
								locale), ATLEASTONE));
		parameterField
				.addPossibleValue(new SearchInputPossibleValueModel(
						messageSource.internationalize(
								"search.testcase.content.parameter.none",
								locale), NONE));

		SearchInputFieldModel datasetField = new SearchInputFieldModel(
				"datasets", messageSource.internationalize(
						"search.testcase.content.dataset.label", locale),
				EXISTS);
		panel.addField(datasetField);

		datasetField.addPossibleValue(new SearchInputPossibleValueModel(
				messageSource.internationalize(
						"search.testcase.content.dataset.atleastone", locale),
				ATLEASTONE));
		datasetField.addPossibleValue(new SearchInputPossibleValueModel(
				messageSource.internationalize(
						"search.testcase.content.dataset.none", locale), NONE));

		SearchInputFieldModel callstepField = new SearchInputFieldModel(
				"callsteps", messageSource.internationalize(
						"search.testcase.content.callstep.label", locale),
				EXISTS);
		panel.addField(callstepField);

		callstepField.addPossibleValue(new SearchInputPossibleValueModel(
				messageSource.internationalize(
						"search.testcase.content.callstep.atleastone", locale),
				ATLEASTONE));
		callstepField
				.addPossibleValue(new SearchInputPossibleValueModel(
						messageSource
								.internationalize(
										"search.testcase.content.callstep.none",
										locale), NONE));

		SearchInputFieldModel attachmentField = new SearchInputFieldModel(
				"attachments", messageSource.internationalize(
						"search.testcase.content.attachment.label", locale),
				EXISTS);
		panel.addField(attachmentField);

		attachmentField
				.addPossibleValue(new SearchInputPossibleValueModel(
						messageSource
								.internationalize(
										"search.testcase.content.attachment.atleastone",
										locale), ATLEASTONE));
		attachmentField.addPossibleValue(new SearchInputPossibleValueModel(
				messageSource.internationalize(
						"search.testcase.content.attachment.none", locale),
				NONE));

		return panel;
	}

	private SearchInputPanelModel createHistoryPanel(Locale locale) {

		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(messageSource.internationalize(
				"search.testcase.history.panel.title", locale));
		panel.setOpen(true);
		panel.setId("history");
		panel.setLocation("column3");
		panel.addCssClass("search-icon-history");

		SearchInputFieldModel createdByField = new SearchInputFieldModel(
				"createdBy", messageSource.internationalize(
						"search.testcase.history.createdBy.label", locale),
				MULTISELECT);
		panel.addField(createdByField);

		List<String> users = advancedSearchService
				.findAllUsersWhoCreatedTestCases();
		for (String user : users) {
			createdByField.addPossibleValue(new SearchInputPossibleValueModel(
					user, user));
		}

		SearchInputFieldModel createdOnField = new SearchInputFieldModel(
				"createdOn", messageSource.internationalize(
						"search.testcase.history.createdOn.label", locale),
				DATE);
		panel.addField(createdOnField);

		SearchInputFieldModel modifiedByField = new SearchInputFieldModel(
				"modifiedBy", messageSource.internationalize(
						"search.testcase.history.modifiedBy.label", locale),
				MULTISELECT);
		panel.addField(modifiedByField);

		users = advancedSearchService.findAllUsersWhoModifiedTestCases();
		for (String user : users) {
			if (user == null || "".equals(user.trim())) {
				modifiedByField
						.addPossibleValue(new SearchInputPossibleValueModel(
								messageSource.internationalize(
										"label.NeverModified", locale), ""));
			} else {
				modifiedByField
						.addPossibleValue(new SearchInputPossibleValueModel(
								user, user));
			}
		}

		SearchInputFieldModel modifiedOnField = new SearchInputFieldModel(
				"modifiedOn", messageSource.internationalize(
						"search.testcase.history.modifiedOn.label", locale),
				DATE);
		panel.addField(modifiedOnField);

		return panel;
	}

	private SearchInputPanelModel createCUFPanel(Locale locale) {

		SearchInputPanelModel panel = getCustomFielModel(locale);
		panel.setTitle(messageSource.internationalize(
				"search.testcase.cuf.panel.title", locale));
		panel.setOpen(true);
		panel.setId("cuf");
		panel.setLocation("column3");
		panel.addCssClass("search-icon-cuf");
		return panel;
	}

	@RequestMapping(value = "/input", method = RequestMethod.GET, headers = RequestHeaders.CONTENT_JSON)
	@ResponseBody
	public SearchInputInterfaceModel getSearchInputInterfaceModel(Locale locale) {

		SearchInputInterfaceModel model = new SearchInputInterfaceModel();

		// Information
		model.addPanel(createGeneralInfoPanel(locale));

		// Attributes
		model.addPanel(createAttributePanel(locale));

		// Perimeter
		model.addPanel(createPerimeterPanel(locale));

		// Content
		model.addPanel(createContentPanel(locale));

		// Associations
		model.addPanel(createAssociationPanel(locale));

		// Historique
		model.addPanel(createHistoryPanel(locale));

		// CUF
		model.addPanel(createCUFPanel(locale));

		return model;
	}

	private static final class TestCaseSearchResultDataTableModelHelper extends
			DataTableModelBuilder<TestCase> {
		private InternationalizationHelper messageSource;
		private Locale locale;
		private PermissionEvaluationService permissionService;
		private IterationModificationService iterationService;
		private boolean isInAssociationContext;
		private Set<Long> associatedTestCaseIds;

		private TestCaseSearchResultDataTableModelHelper(Locale locale,
				InternationalizationHelper messageSource,
				PermissionEvaluationService permissionService,
				IterationModificationService iterationService,
				boolean isInAssociationContext, Set<Long> associatedTestCaseIds) {
			this.locale = locale;
			this.messageSource = messageSource;
			this.permissionService = permissionService;
			this.iterationService = iterationService;
			this.isInAssociationContext = isInAssociationContext;
			this.associatedTestCaseIds = associatedTestCaseIds;
		}

		private String formatImportance(TestCaseImportance importance,
				Locale locale) {

			return importance.getLevel() + "-"
					+ messageSource.internationalize(importance, locale);
		}

		private String formatStatus(TestCaseStatus status, Locale locale) {
			return status.getLevel() + "-"
					+ messageSource.internationalize(status, locale);
		}

		private String formatNature(TestCaseNature nature, Locale locale) {
			return messageSource.internationalize(nature, locale);
		}

		private String formatType(TestCaseType type, Locale locale) {
			return messageSource.internationalize(type, locale);
		}

		private boolean isTestCaseEditable(TestCase item) {
			return permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN",
					"WRITE", item);
		}

		private boolean isInAssociationContext() {
			return this.isInAssociationContext;
		}

		@Override
		public Map<String, Object> buildItemData(TestCase item) {
			final AuditableMixin auditable = (AuditableMixin) item;
			Map<String, Object> res = new HashMap<String, Object>();
			res.put("project-name", item.getProject().getName());
			if (isInAssociationContext()) {
				res.put("is-associated",
						associatedTestCaseIds.contains(item.getId()));
			}
			res.put(DataTableModelConstants.DEFAULT_ENTITY_INDEX_KEY,
					getCurrentIndex());
			res.put("test-case-id", item.getId());
			res.put("test-case-ref", item.getReference());
			res.put("test-case-label", item.getName());
			res.put("editable", isTestCaseEditable(item));
			res.put("test-case-weight",
					formatImportance(item.getImportance(), locale));
			res.put("test-case-nature", formatNature(item.getNature(), locale));
			res.put("test-case-type", formatType(item.getType(), locale));
			res.put("test-case-status", formatStatus(item.getStatus(), locale));
			res.put("test-case-requirement-nb", item
					.getVerifiedRequirementVersions().size());
			res.put("test-case-teststep-nb", item.getSteps().size());
			res.put("test-case-iteration-nb", iterationService
					.findIterationContainingTestCase(item.getId()).size());
			res.put("test-case-attachment-nb", item.getAllAttachments().size());
			res.put("test-case-created-by",
					formatUsername(auditable.getCreatedBy()));
			res.put("test-case-modified-by",
					formatUsername(auditable.getLastModifiedBy()));
			res.put("empty-openinterface2-holder", " ");
			res.put("empty-opentree-holder", " ");
			return res;
		}
	}

	private static String formatUsername(String username) {
		if (username == null || "".equals(username.trim())) {
			return "-";
		}
		return username;
	}

	public SearchInputPanelModel getCustomFielModel(Locale locale) {
		List<CustomField> customFields = advancedSearchService
				.findAllQueryableCustomFieldsByBoundEntityType(BindableEntity.TEST_CASE);
		return convertToSearchInputPanelModel(customFields, locale);
	}

	private SearchInputPanelModel convertToSearchInputPanelModel(
			List<CustomField> customFields, Locale locale) {
		SearchInputPanelModel model = new SearchInputPanelModel();
		for (CustomField customField : customFields) {
			if (org.squashtest.tm.domain.customfield.InputType.DROPDOWN_LIST
					.equals(customField.getInputType())) {
				SingleSelectField selectField = (SingleSelectField) customField;
				model.getFields().add(
						convertToSearchInputFieldModel(selectField, locale));

			} else if (org.squashtest.tm.domain.customfield.InputType.PLAIN_TEXT
					.equals(customField.getInputType())) {
				model.getFields().add(
						convertToSearchInputFieldModel(customField));

			} else if (org.squashtest.tm.domain.customfield.InputType.CHECKBOX
					.equals(customField.getInputType())) {
				model.getFields().add(createCheckBoxField(customField, locale));

			} else if (org.squashtest.tm.domain.customfield.InputType.DATE_PICKER
					.equals(customField.getInputType())) {
				model.getFields().add(createDatePickerField(customField));
			}
		}
		return model;
	}

	private SearchInputFieldModel createDatePickerField(CustomField customField) {

		SearchInputFieldModel model = new SearchInputFieldModel();
		model.setInputType(DATE);
		model.setTitle(customField.getLabel());
		model.setId(customField.getCode());
		model.setIgnoreBridge(true);
		return model;
	}

	private SearchInputFieldModel createCheckBoxField(CustomField customField,
			Locale locale) {
		SearchInputFieldModel model = new SearchInputFieldModel();

		List<SearchInputPossibleValueModel> possibleValues = new ArrayList<SearchInputPossibleValueModel>();

		possibleValues.add(new SearchInputPossibleValueModel(messageSource
				.internationalize("label.True", locale), "true"));
		possibleValues.add(new SearchInputPossibleValueModel(messageSource
				.internationalize("label.False", locale), "false"));

		model.setPossibleValues(possibleValues);
		model.setInputType(COMBOMULTISELECT);
		model.setTitle(customField.getLabel());
		model.setId(customField.getCode());
		model.setIgnoreBridge(true);
		return model;
	}

	private SearchInputFieldModel convertToSearchInputFieldModel(
			CustomField customField) {
		SearchInputFieldModel model = new SearchInputFieldModel();
		model.setInputType(TEXTFIELD);
		model.setTitle(customField.getLabel());
		model.setId(customField.getCode());
		model.setIgnoreBridge(true);
		return model;
	}

	private SearchInputFieldModel convertToSearchInputFieldModel(
			SingleSelectField selectField, Locale locale) {
		List<SearchInputPossibleValueModel> possibleValues = new ArrayList<SearchInputPossibleValueModel>();
		possibleValues.add(new SearchInputPossibleValueModel(messageSource
				.internationalize("label.Empty", locale), ""));
		for (CustomFieldOption option : selectField.getOptions()) {
			possibleValues.add(new SearchInputPossibleValueModel(option
					.getLabel(), option.getCode()));
		}
		SearchInputFieldModel model = new SearchInputFieldModel();
		model.setInputType(COMBOMULTISELECT);
		model.setTitle(selectField.getLabel());
		model.setPossibleValues(possibleValues);
		model.setId(selectField.getCode());
		model.setIgnoreBridge(true);
		return model;
	}
}
