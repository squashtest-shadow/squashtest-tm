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
package org.squashtest.tm.web.internal.controller.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.domain.IdentifiedUtil;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldOption;
import org.squashtest.tm.domain.customfield.MultiSelectField;
import org.squashtest.tm.domain.customfield.SingleSelectField;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.domain.requirement.RequirementVersion;
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
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.service.requirement.RequirementVersionAdvancedSearchService;
import org.squashtest.tm.service.requirement.VerifiedRequirement;
import org.squashtest.tm.service.requirement.VerifiedRequirementsManagerService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.testcase.TestCaseAdvancedSearchService;
import org.squashtest.tm.service.testcase.VerifyingTestCaseManagerService;
import org.squashtest.tm.web.internal.controller.AcceptHeaders;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.builder.JsonProjectBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.model.datatable.DataTableMultiSorting;
import org.squashtest.tm.web.internal.model.json.JsonProject;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/advanced-search")
public class AdvancedSearchController {
	private static final String PROJECTS_META = "projects";
	private static final Logger LOGGER = LoggerFactory.getLogger(AdvancedSearchController.class);

	private static interface FormModelBuilder {
		SearchInputInterfaceModel build(Locale locale);
	}

	private static final String TEXTFIELD = "textfield";
	private static final String DATE = "date";
	private static final String COMBOMULTISELECT = "combomultiselect";
	private static final String TAGS = "tags";

	private static final String TESTCASE = "test-case";
	private static final String REQUIREMENT = "requirement";
	private static final String SEARCH_MODEL = "searchModel";
	private static final String SEARCH_DOMAIN = "searchDomain";
	private static final String TESTCASE_VIA_REQUIREMENT = "testcaseViaRequirement";

	@Inject
	private ProjectFinder projectFinder;

	@Inject
	private JsonProjectBuilder jsProjectBuilder;

	private Map<String, FormModelBuilder> formModelBuilder = new HashMap<String, AdvancedSearchController.FormModelBuilder>();

	{
		formModelBuilder.put(TESTCASE, new FormModelBuilder() {
			@Override
			public SearchInputInterfaceModel build(Locale locale) {
				SearchInputInterfaceModel model = getTestCaseSearchInputInterfaceModel(locale);
				populateMetadata(model);
				return model;
			}
		});

		formModelBuilder.put(TESTCASE_VIA_REQUIREMENT, new FormModelBuilder() {
			@Override
			public SearchInputInterfaceModel build(Locale locale) {
				SearchInputInterfaceModel model =  getTestCaseViaRequirementSearchInputInterfaceModel(locale);
				populateMetadata(model);
				return model;
			}
		});

		formModelBuilder.put(REQUIREMENT, new FormModelBuilder() {
			@Override
			public SearchInputInterfaceModel build(Locale locale) {
				SearchInputInterfaceModel model =  getRequirementSearchInputInterfaceModel(locale);
				populateMetadata(model);
				return model;
			}
		});
	}

	@Inject
	private TestCaseAdvancedSearchService testCaseAdvancedSearchService;

	@Inject
	private RequirementVersionAdvancedSearchService requirementVersionAdvancedSearchService;

	@Inject
	private PermissionEvaluationService permissionService;

	@Inject
	private IterationModificationService iterationService;

	@Inject
	private VerifyingTestCaseManagerService verifyingTestCaseManagerService;

	@Inject
	private VerifiedRequirementsManagerService verifiedRequirementsManagerService;

	@Inject
	private InternationalizationHelper messageSource;

	@Inject
	private RequirementVersionSearchInterfaceDescription requirementVersionSearchInterfaceDescription;

	@Inject
	private TestcaseSearchInterfaceDescription testcaseVersionSearchInterfaceDescription;

	@Inject
	private CampaignTestPlanManagerService campaignTestPlanManagerService;

	@Inject
	private IterationTestPlanManagerService iterationTestPlanManagerService;

	@Inject
	private TestSuiteTestPlanManagerService testSuiteTestPlanManagerService;

	// These are used by Lucene - Thus the columns are mapped to index
	// properties rather than class properties
	private DatatableMapper<String> testCaseSearchResultMapper = new NameBasedMapper(11)
	.mapAttribute(DataTableModelConstants.PROJECT_NAME_KEY, "name", Project.class).mapAttribute("test-case-id", "id", TestCase.class)
	.mapAttribute("test-case-ref", "reference", TestCase.class)
	.mapAttribute("test-case-label", "labelUpperCased", TestCase.class)
	.mapAttribute("test-case-weight", "importance", TestCase.class)
	.mapAttribute("test-case-nature", "nature", TestCase.class)
	.mapAttribute("test-case-type", "type", TestCase.class)
	.mapAttribute("test-case-status", "status", TestCase.class)
	.mapAttribute("test-case-requirement-nb", "requirements", TestCase.class)
	.mapAttribute("test-case-teststep-nb", "steps", TestCase.class)
	.mapAttribute("test-case-iteration-nb", "iterations", TestCase.class)
	.mapAttribute("test-case-attachment-nb", "attachments", TestCase.class)
	.mapAttribute("test-case-created-by", "createdBy", TestCase.class)
	.mapAttribute("test-case-modified-by", "lastModifiedBy", TestCase.class);

	private DatatableMapper<String> requirementSearchResultMapper = new NameBasedMapper(11)
	.mapAttribute(DataTableModelConstants.PROJECT_NAME_KEY, "name", Project.class)
	.mapAttribute("requirement-id", "requirement.id", RequirementVersion.class)
	.mapAttribute("requirement-reference", "reference", RequirementVersion.class)
	.mapAttribute("requirement-label", "labelUpperCased", RequirementVersion.class)
	.mapAttribute("requirement-criticality", "criticality", RequirementVersion.class)
	.mapAttribute("requirement-category", "category", RequirementVersion.class)
	.mapAttribute("requirement-status", "status", RequirementVersion.class)
	.mapAttribute("requirement-version", "versionNumber", RequirementVersion.class)
	.mapAttribute("requirement-version-nb", "versions", Requirement.class)
	.mapAttribute("requirement-testcase-nb", "testcases", RequirementVersion.class)
	.mapAttribute("requirement-attachment-nb", "attachments", RequirementVersion.class)
	.mapAttribute("requirement-created-by", "createdBy", RequirementVersion.class)
	.mapAttribute("requirement-modified-by", "lastModifiedBy", RequirementVersion.class);

	@RequestMapping(method = RequestMethod.GET)
	public String showSearchPage(Model model, @RequestParam String searchDomain,
			@RequestParam(required = false) String associateResultWithType, @RequestParam(required = false) Long id,
			Locale locale) {

		initModelForPage(model, associateResultWithType, id);
		model.addAttribute(SEARCH_DOMAIN, searchDomain);
		if (TESTCASE_VIA_REQUIREMENT.equals(searchDomain)) {
			searchDomain = REQUIREMENT;
		}

		FormModelBuilder builder = formModelBuilder.get(searchDomain);

		if (builder != null) {
			model.addAttribute("formModel", builder.build(locale));
		} else {
			LOGGER.error(
					"Could not find a FormModelBuilder for search domain : {}. This is either caused by a bug or a hand-written request",
					searchDomain);
		}

		return searchDomain + "-search-input.html";
	}

	private void initModelForPage(Model model, String associateResultWithType, Long id) {
		if (StringUtils.isNotBlank(associateResultWithType)) {
			model.addAttribute("associateResult", true);
			model.addAttribute("associateResultWithType", associateResultWithType);
			model.addAttribute("associateId", id);
		} else {
			model.addAttribute("associateResult", false);
		}
	}

	@RequestMapping(method = RequestMethod.POST)
	public String showSearchPageFilledWithParams(Model model, @RequestParam String searchDomain,
			@RequestParam String searchModel, @RequestParam(required = false) String associateResultWithType,
			@RequestParam(required = false) Long id, Locale locale) {
		model.addAttribute(SEARCH_MODEL, searchModel);
		return showSearchPage(model, searchDomain, associateResultWithType, id, locale);
	}

	@RequestMapping(value = "/results", params = TESTCASE)
	public String getTestCaseSearchResultPage(Model model, @RequestParam String searchModel,
			@RequestParam(required = false) String associateResultWithType, @RequestParam(required = false) Long id) {

		initModelForPage(model, associateResultWithType, id);
		model.addAttribute(SEARCH_MODEL, searchModel);
		model.addAttribute(SEARCH_DOMAIN, TESTCASE);

		populateMetadata(model);

		return "test-case-search-result.html";
	}

	@RequestMapping(value = "/results", params = REQUIREMENT)
	public String getRequirementSearchResultPage(Model model, @RequestParam String searchModel,
			@RequestParam(required = false) String associateResultWithType, @RequestParam(required = false) Long id) {

		initModelForPage(model, associateResultWithType, id);
		model.addAttribute(SEARCH_MODEL, searchModel);
		model.addAttribute(SEARCH_DOMAIN, REQUIREMENT);

		populateMetadata(model);

		return "requirement-search-result.html";
	}

	@RequestMapping(value = "/results", params = TESTCASE_VIA_REQUIREMENT)
	public String getTestCaseThroughRequirementSearchResultPage(Model model, @RequestParam String searchModel,
			@RequestParam(required = false) String associateResultWithType, @RequestParam(required = false) Long id) {

		initModelForPage(model, associateResultWithType, id);
		model.addAttribute(SEARCH_MODEL, searchModel);
		model.addAttribute(SEARCH_DOMAIN, TESTCASE_VIA_REQUIREMENT);


		populateMetadata(model);

		return "test-case-search-result.html";
	}

	private boolean isInAssociationContext(String associateResultWithType) {
		boolean isInAssociationContext = false;

		if (associateResultWithType != null) {
			isInAssociationContext = true;
		}

		return isInAssociationContext;
	}

	@RequestMapping(value = "/table", method = RequestMethod.POST, params = { RequestParams.MODEL, TESTCASE_VIA_REQUIREMENT,
			RequestParams.S_ECHO_PARAM })
	@ResponseBody
	public DataTableModel getTestCaseThroughRequirementTableModel(final DataTableDrawParameters params,
			final Locale locale, @RequestParam(value = RequestParams.MODEL) String model,
			@RequestParam(required = false) String associateResultWithType, @RequestParam(required = false) Long id)
					throws JsonParseException, JsonMappingException, IOException {

		AdvancedSearchModel searchModel = new ObjectMapper().readValue(model, AdvancedSearchModel.class);

		PagingAndMultiSorting paging = new DataTableMultiSorting(params, testCaseSearchResultMapper);

		PagedCollectionHolder<List<TestCase>> holder = testCaseAdvancedSearchService.searchForTestCasesThroughRequirementModel(
				searchModel, paging, locale);

		boolean isInAssociationContext = isInAssociationContext(associateResultWithType);

		Set<Long> ids = null;

		if (isInAssociationContext) {
			ids = getIdsOfTestCasesAssociatedWithObjects(associateResultWithType, id);
		}

		return new TestCaseSearchResultDataTableModelHelper(locale, messageSource, permissionService, iterationService,
				isInAssociationContext, ids).buildDataModel(holder, params.getsEcho());
	}

	@RequestMapping(value = "/table", method = RequestMethod.POST, params = { RequestParams.MODEL, TESTCASE,
			RequestParams.S_ECHO_PARAM })
	@ResponseBody
	public DataTableModel getTestCaseTableModel(final DataTableDrawParameters params, final Locale locale,
			@RequestParam(value = RequestParams.MODEL) String model,
			@RequestParam(required = false) String associateResultWithType, @RequestParam(required = false) Long id)
					throws JsonParseException, JsonMappingException, IOException {

		AdvancedSearchModel searchModel = new ObjectMapper().readValue(model, AdvancedSearchModel.class);

		PagingAndMultiSorting paging = new DataTableMultiSorting(params, testCaseSearchResultMapper);

		PagedCollectionHolder<List<TestCase>> holder = testCaseAdvancedSearchService.searchForTestCases(searchModel, paging,
				locale);

		boolean isInAssociationContext = isInAssociationContext(associateResultWithType);

		Set<Long> ids = null;

		if (isInAssociationContext) {
			ids = getIdsOfTestCasesAssociatedWithObjects(associateResultWithType, id);
		}

		return new TestCaseSearchResultDataTableModelHelper(locale, messageSource, permissionService, iterationService,
				isInAssociationContext, ids).buildDataModel(holder, params.getsEcho());
	}

	@RequestMapping(value = "/table", method = RequestMethod.POST, params = { RequestParams.MODEL, REQUIREMENT,
			RequestParams.S_ECHO_PARAM })
	@ResponseBody
	public DataTableModel getRequirementTableModel(final DataTableDrawParameters params, final Locale locale,
			@RequestParam(value = RequestParams.MODEL) String model,
			@RequestParam(required = false) String associateResultWithType, @RequestParam(required = false) Long id)
					throws JsonParseException, JsonMappingException, IOException {

		AdvancedSearchModel searchModel = new ObjectMapper().readValue(model, AdvancedSearchModel.class);

		PagingAndMultiSorting paging = new DataTableMultiSorting(params, requirementSearchResultMapper);

		PagedCollectionHolder<List<RequirementVersion>> holder = requirementVersionAdvancedSearchService.searchForRequirementVersions(
				searchModel, paging, messageSource, locale);

		boolean isInAssociationContext = isInAssociationContext(associateResultWithType);

		Set<Long> ids = null;

		if (isInAssociationContext) {
			ids = getIdsOfRequirementsAssociatedWithObjects(associateResultWithType, id);
		}

		return new RequirementSearchResultDataTableModelHelper(locale, messageSource, permissionService,
				isInAssociationContext, ids).buildDataModel(holder, params.getsEcho());
	}

	private Set<Long> getIdsOfRequirementsAssociatedWithObjects(String associateResultWithType, Long id) {

		Set<Long> ids = new HashSet<Long>();

		if (TESTCASE.equals(associateResultWithType)) {
			List<VerifiedRequirement> requirements = verifiedRequirementsManagerService
					.findAllVerifiedRequirementsByTestCaseId(id);
			for (VerifiedRequirement requirement : requirements) {
				ids.add(requirement.getId());
			}
		}

		return ids;
	}

	private Set<Long> getIdsOfTestCasesAssociatedWithObjects(String associateResultWithType, Long id) {

		Set<Long> ids = new HashSet<Long>();

		if (REQUIREMENT.equals(associateResultWithType)) {
			List<TestCase> testCases = verifyingTestCaseManagerService.findAllByRequirementVersion(id);
			List<Long> tcIds = IdentifiedUtil.extractIds(testCases);
			ids.addAll(tcIds);

		} else if ("campaign".equals(associateResultWithType)) {
			List<Long> referencedTestCasesIds = this.campaignTestPlanManagerService.findPlannedTestCasesIds(id);
			ids.addAll(referencedTestCasesIds);
		} else if ("iteration".equals(associateResultWithType)) {
			List<TestCase> testCases = this.iterationTestPlanManagerService.findPlannedTestCases(id);
			List<Long> tcIds = IdentifiedUtil.extractIds(testCases);
			ids.addAll(tcIds);
		} else if ("testsuite".equals(associateResultWithType)) {
			List<Long> referencedTestCasesIds = this.testSuiteTestPlanManagerService.findPlannedTestCasesIds(id);
			ids.addAll(referencedTestCasesIds);
		}

		return ids;
	}

	private SearchInputPanelModel createCUFPanel(Locale locale, BindableEntity bindableEntity) {

		SearchInputPanelModel panel = getCustomFielModel(locale, bindableEntity);
		panel.setTitle(messageSource.internationalize("search.testcase.cuf.panel.title", locale));
		panel.setOpen(true);
		panel.setId("cuf");
		panel.setLocation("column1");
		panel.addCssClass("search-icon-cuf");
		return panel;
	}

	@RequestMapping(value = "/input", method = RequestMethod.GET, headers = AcceptHeaders.CONTENT_JSON, params = TESTCASE_VIA_REQUIREMENT)
	@ResponseBody
	public SearchInputInterfaceModel getTestCaseViaRequirementSearchInputInterfaceModel(Locale locale) {
		// TODO should no longer be called through HTTP, put it private
		return getRequirementSearchInputInterfaceModel(locale);
	}

	@RequestMapping(value = "/input", method = RequestMethod.GET, headers = AcceptHeaders.CONTENT_JSON, params = REQUIREMENT)
	@ResponseBody
	public SearchInputInterfaceModel getRequirementSearchInputInterfaceModel(Locale locale) {
		// TODO should no longer be called through HTTP, put it private
		SearchInputInterfaceModel model = new SearchInputInterfaceModel();

		// Perimeter
		model.addPanel(requirementVersionSearchInterfaceDescription.createRequirementPerimeterPanel(locale));

		// Information
		model.addPanel(requirementVersionSearchInterfaceDescription.createRequirementInformationPanel(locale));

		// Attributes
		model.addPanel(requirementVersionSearchInterfaceDescription.createRequirementAttributePanel(locale));

		// Version
		model.addPanel(requirementVersionSearchInterfaceDescription.createRequirementVersionPanel(locale));

		// Content
		model.addPanel(requirementVersionSearchInterfaceDescription.createRequirementContentPanel(locale));

		// Associations
		model.addPanel(requirementVersionSearchInterfaceDescription.createRequirementAssociationPanel(locale));

		// History
		model.addPanel(requirementVersionSearchInterfaceDescription.createRequirementHistoryPanel(locale));

		// CUFs
		model.addPanel(createCUFPanel(locale, BindableEntity.REQUIREMENT_VERSION));

		return model;
	}

	@RequestMapping(value = "/input", method = RequestMethod.GET, headers = AcceptHeaders.CONTENT_JSON, params = TESTCASE)
	@ResponseBody
	public SearchInputInterfaceModel getTestCaseSearchInputInterfaceModel(Locale locale) {
		// TODO should no longer be called through HTTP, put it private
		SearchInputInterfaceModel model = new SearchInputInterfaceModel();

		// Perimeter
		model.addPanel(testcaseVersionSearchInterfaceDescription.createPerimeterPanel(locale));

		// Information
		model.addPanel(testcaseVersionSearchInterfaceDescription.createGeneralInfoPanel(locale));

		// Attributes
		model.addPanel(testcaseVersionSearchInterfaceDescription.createAttributePanel(locale));

		// Content
		model.addPanel(testcaseVersionSearchInterfaceDescription.createContentPanel(locale));

		// Associations
		model.addPanel(testcaseVersionSearchInterfaceDescription.createAssociationPanel(locale));

		// History
		model.addPanel(testcaseVersionSearchInterfaceDescription.createTestCaseHistoryPanel(locale));

		// CUF
		model.addPanel(createCUFPanel(locale, BindableEntity.TEST_CASE));

		return model;
	}

	private static final class RequirementSearchResultDataTableModelHelper extends
	DataTableModelBuilder<RequirementVersion> {

		private boolean isInAssociationContext;
		private Set<Long> associatedRequirementIds;
		private InternationalizationHelper messageSource;
		private PermissionEvaluationService permissionService;
		private Locale locale;

		private boolean isInAssociationContext() {
			return this.isInAssociationContext;
		}

		private String formatStatus(RequirementStatus status, Locale locale) {
			return status.getLevel() + "-" + messageSource.internationalize(status, locale);
		}

		private String formatCriticality(RequirementCriticality criticality, Locale locale) {
			return criticality.getLevel() + "-" + messageSource.internationalize(criticality, locale);
		}



		private String formatInfoItem(InfoListItem item, Locale locale) {
			return messageSource.getMessage(item.getLabel(), null, item.getLabel(), locale);
		}

		private RequirementSearchResultDataTableModelHelper(Locale locale, InternationalizationHelper messageSource,
				PermissionEvaluationService permissionService, boolean isInAssociationContext,
				Set<Long> associatedTestCaseIds) {

			this.locale = locale;
			this.permissionService = permissionService;
			this.messageSource = messageSource;
			this.isInAssociationContext = isInAssociationContext;
			this.associatedRequirementIds = associatedTestCaseIds;
		}

		@Override
		protected Map<String, Object> buildItemData(RequirementVersion item) {

			final AuditableMixin auditable = (AuditableMixin) item;
			Map<String, Object> res = new HashMap<String, Object>();
			res.put(DataTableModelConstants.PROJECT_NAME_KEY, item.getProject().getName());
			res.put("project-id", item.getProject().getId());
			if (isInAssociationContext()) {
				res.put("empty-is-associated-holder", " ");
				res.put("is-associated", associatedRequirementIds.contains(item.getId()));
			}
			res.put(DataTableModelConstants.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex());
			res.put("requirement-id", item.getRequirement().getId());
			res.put("requirement-reference", item.getReference());
			res.put("requirement-label", item.getName());
			res.put("editable", isRequirementVersionEditable(item));
			res.put("requirement-criticality", formatCriticality(item.getCriticality(), locale));
			res.put("requirement-category", formatInfoItem(item.getCategory(), locale));
			res.put("requirement-status", formatStatus(item.getStatus(), locale));
			res.put("requirement-version", item.getVersionNumber());
			res.put("requirement-version-nb", item.getRequirement().getRequirementVersions().size());
			res.put("requirement-testcase-nb", item.getVerifyingTestCases().size());
			res.put("requirement-attachment-nb", item.getAttachmentList().size());
			res.put("requirement-created-by", formatUsername(auditable.getCreatedBy()));
			res.put("requirement-modified-by", formatUsername(auditable.getLastModifiedBy()));
			res.put("empty-openinterface2-holder", " ");
			res.put("empty-opentree-holder", " ");
			return res;
		}

		private boolean isRequirementVersionEditable(RequirementVersion item) {
			if (item.isModifiable()) {
				return permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "WRITE", item);
			} else {
				return false;
			}
		}

	}

	private static final class TestCaseSearchResultDataTableModelHelper extends DataTableModelBuilder<TestCase> {
		private InternationalizationHelper messageSource;
		private Locale locale;
		private PermissionEvaluationService permissionService;
		private IterationModificationService iterationService;
		private boolean isInAssociationContext;
		private Set<Long> associatedTestCaseIds;

		private TestCaseSearchResultDataTableModelHelper(Locale locale, InternationalizationHelper messageSource,
				PermissionEvaluationService permissionService, IterationModificationService iterationService,
				boolean isInAssociationContext, Set<Long> associatedTestCaseIds) {
			this.locale = locale;
			this.messageSource = messageSource;
			this.permissionService = permissionService;
			this.iterationService = iterationService;
			this.isInAssociationContext = isInAssociationContext;
			this.associatedTestCaseIds = associatedTestCaseIds;
		}

		private String formatImportance(TestCaseImportance importance, Locale locale) {

			return importance.getLevel() + "-" + messageSource.internationalize(importance, locale);
		}

		private String formatStatus(TestCaseStatus status, Locale locale) {
			return status.getLevel() + "-" + messageSource.internationalize(status, locale);
		}

		private String formatNature(TestCaseNature nature, Locale locale) {
			return messageSource.internationalize(nature, locale);
		}

		private String formatType(TestCaseType type, Locale locale) {
			return messageSource.internationalize(type, locale);
		}

		private boolean isTestCaseEditable(TestCase item) {
			return permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "WRITE", item);
		}

		private boolean isInAssociationContext() {
			return this.isInAssociationContext;
		}

		@Override
		public Map<String, Object> buildItemData(TestCase item) {
			final AuditableMixin auditable = (AuditableMixin) item;
			Map<String, Object> res = new HashMap<String, Object>();
			res.put(DataTableModelConstants.PROJECT_NAME_KEY, item.getProject().getName());
			res.put("project-id", item.getProject().getId());
			if (isInAssociationContext()) {
				res.put("empty-is-associated-holder", " ");
				res.put("is-associated", associatedTestCaseIds.contains(item.getId()));
			}
			res.put(DataTableModelConstants.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex());
			res.put("test-case-id", item.getId());
			res.put("test-case-ref", item.getReference());
			res.put("test-case-label", item.getName());
			res.put("editable", isTestCaseEditable(item));
			res.put("test-case-weight", formatImportance(item.getImportance(), locale));
			res.put("test-case-weight-auto", item.getImportanceAuto());
			res.put("test-case-nature", formatInfoItem(item.getNature(), locale));
			res.put("test-case-type", formatInfoItem(item.getType(), locale));
			res.put("test-case-status", formatStatus(item.getStatus(), locale));
			res.put("test-case-requirement-nb", item.getVerifiedRequirementVersions().size());
			res.put("test-case-teststep-nb", item.getSteps().size());
			res.put("test-case-iteration-nb", iterationService.findIterationContainingTestCase(item.getId()).size());
			res.put("test-case-attachment-nb", item.getAllAttachments().size());
			res.put("test-case-created-by", formatUsername(auditable.getCreatedBy()));
			res.put("test-case-modified-by", formatUsername(auditable.getLastModifiedBy()));
			res.put("empty-openinterface2-holder", " ");
			res.put("empty-opentree-holder", " ");
			return res;
		}


		private String formatInfoItem(InfoListItem item, Locale locale) {
			return messageSource.getMessage(item.getLabel(), null, item.getLabel(), locale);
		}
	}



	public SearchInputPanelModel getCustomFielModel(Locale locale, BindableEntity bindableEntity) {
		List<CustomField> customFields = testCaseAdvancedSearchService
				.findAllQueryableCustomFieldsByBoundEntityType(bindableEntity);
		return convertToSearchInputPanelModel(customFields, locale);
	}

	private SearchInputPanelModel convertToSearchInputPanelModel(List<CustomField> customFields, Locale locale) {
		SearchInputPanelModel model = new SearchInputPanelModel();
		for (CustomField customField : customFields) {

			switch(customField.getInputType()){
			case DROPDOWN_LIST :
				SingleSelectField selectField = (SingleSelectField) customField;
				model.getFields().add(convertToSearchInputFieldModel(selectField, locale));
				break;

			case PLAIN_TEXT :
				model.getFields().add(convertToSearchInputFieldModel(customField));
				break;

			case CHECKBOX :
				model.getFields().add(createCheckBoxField(customField, locale));
				break;

			case DATE_PICKER :
				model.getFields().add(createDatePickerField(customField));
				break;

			case TAG :
				model.getFields().add(convertToSearchInputFieldModel((MultiSelectField)customField));
				break;

			case RICH_TEXT :
				break;	// not supported for now
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

	private SearchInputFieldModel createCheckBoxField(CustomField customField, Locale locale) {
		SearchInputFieldModel model = new SearchInputFieldModel();

		List<SearchInputPossibleValueModel> possibleValues = new ArrayList<SearchInputPossibleValueModel>();

		possibleValues.add(new SearchInputPossibleValueModel(messageSource.internationalize("label.True", locale),
				"true"));
		possibleValues.add(new SearchInputPossibleValueModel(messageSource.internationalize("label.False", locale),
				"false"));

		model.setPossibleValues(possibleValues);
		model.setInputType(COMBOMULTISELECT);
		model.setTitle(customField.getLabel());
		model.setId(customField.getCode());
		model.setIgnoreBridge(true);
		return model;
	}

	private SearchInputFieldModel convertToSearchInputFieldModel(CustomField customField) {
		SearchInputFieldModel model = new SearchInputFieldModel();
		model.setInputType(TEXTFIELD);
		model.setTitle(customField.getLabel());
		model.setId(customField.getCode());
		model.setIgnoreBridge(true);
		return model;
	}

	private SearchInputFieldModel convertToSearchInputFieldModel(SingleSelectField selectField, Locale locale) {
		List<SearchInputPossibleValueModel> possibleValues = new ArrayList<SearchInputPossibleValueModel>();
		possibleValues
		.add(new SearchInputPossibleValueModel(messageSource.internationalize("label.Empty", locale), ""));
		for (CustomFieldOption option : selectField.getOptions()) {
			possibleValues.add(new SearchInputPossibleValueModel(option.getLabel(), option.getLabel()));
		}
		SearchInputFieldModel model = new SearchInputFieldModel();
		model.setInputType(COMBOMULTISELECT);
		model.setTitle(selectField.getLabel());
		model.setPossibleValues(possibleValues);
		model.setId(selectField.getCode());
		model.setIgnoreBridge(true);
		return model;
	}

	private SearchInputFieldModel convertToSearchInputFieldModel(MultiSelectField multifield){
		List<SearchInputPossibleValueModel> possibleValues = new ArrayList<SearchInputPossibleValueModel>(multifield.getOptions().size());

		for (CustomFieldOption option : multifield.getOptions()){
			possibleValues.add(new SearchInputPossibleValueModel(option.getLabel(), option.getLabel()));
		}

		SearchInputFieldModel model = new SearchInputFieldModel();
		model.setInputType(TAGS);
		model.setTitle(multifield.getLabel());
		model.setPossibleValues(possibleValues);
		model.setId(multifield.getCode());
		model.setIgnoreBridge(true);
		return model;

	}

	private void populateMetadata(Model model){
		model.addAttribute("projects", readableJsonProjects());
	}

	private void populateMetadata(SearchInputInterfaceModel model){

		model.addMetadata(PROJECTS_META, readableJsonProjects());

	}

	private List<JsonProject> readableJsonProjects(){
		List<Project> readableProjects = projectFinder.findAllReadable();
		List<JsonProject> jsonified = new ArrayList<JsonProject>(readableProjects.size());

		for (Project p : readableProjects){
			jsonified.add(jsProjectBuilder.toExtendedProject(p));
		}

		return jsonified;
	}
}
