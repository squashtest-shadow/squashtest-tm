/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.domain.IdentifiedUtil;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldOption;
import org.squashtest.tm.domain.customfield.MultiSelectField;
import org.squashtest.tm.domain.customfield.SingleSelectField;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.library.Library;
import org.squashtest.tm.domain.library.LibraryNode;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.search.AdvancedSearchListFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchModel;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseExecutionMode;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.domain.testcase.TestCaseType;
import org.squashtest.tm.service.campaign.CampaignAdvancedSearchService;
import org.squashtest.tm.service.campaign.CampaignLibraryNavigationService;
import org.squashtest.tm.service.campaign.CampaignTestPlanManagerService;
import org.squashtest.tm.service.campaign.IterationModificationService;
import org.squashtest.tm.service.campaign.IterationTestPlanManagerService;
import org.squashtest.tm.service.campaign.TestSuiteTestPlanManagerService;
import org.squashtest.tm.service.feature.FeatureManager;
import org.squashtest.tm.service.feature.FeatureManager.Feature;
import org.squashtest.tm.service.library.WorkspaceService;
import org.squashtest.tm.service.project.CustomProjectFinder;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.service.requirement.RequirementVersionAdvancedSearchService;
import org.squashtest.tm.service.requirement.RequirementVersionManagerService;
import org.squashtest.tm.service.requirement.VerifiedRequirement;
import org.squashtest.tm.service.requirement.VerifiedRequirementsManagerService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.testcase.TestCaseAdvancedSearchService;
import org.squashtest.tm.service.testcase.TestCaseModificationService;
import org.squashtest.tm.service.testcase.VerifyingTestCaseManagerService;
import org.squashtest.tm.web.internal.argumentresolver.MilestoneConfigResolver.CurrentMilestone;
import org.squashtest.tm.web.internal.controller.AcceptHeaders;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.administration.MilestoneDataTableModelHelper;
import org.squashtest.tm.web.internal.helper.JsTreeHelper;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
// Added more
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.JsTreeNodeListBuilder;
import org.squashtest.tm.web.internal.model.builder.JsonProjectBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.model.datatable.DataTableMultiSorting;
import org.squashtest.tm.web.internal.model.json.JsonProject;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;
import org.squashtest.tm.web.internal.model.search.MilestoneMassModifData;
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
		SearchInputInterfaceModel build(Locale locale, boolean isMilestoneMode);
	}

	private static final String IDS = "ids[]";
	private static final String TEXTFIELD = "textfield";
	private static final String DATE = "date";
	private static final String COMBOMULTISELECT = "combomultiselect";
	private static final String TAGS = "tags";
	private static final String CAMPAIGN = "campaign";
	private static final String TESTCASE = "test-case";
	private static final String REQUIREMENT = "requirement";
	private static final String SEARCH_MODEL = "searchModel";
	private static final String SEARCH_DOMAIN = "searchDomain";
	private static final String TESTCASE_VIA_REQUIREMENT = "testcaseViaRequirement";
	private static final String RESULTS = "/results";
	private static final String TABLE = "/table";
	private static final String INPUT = "/input";

	@Inject
	private RequirementVersionManagerService versionService;

	@Inject
	private ProjectFinder projectFinder;

	@Inject
	private JsonProjectBuilder jsProjectBuilder;

	@Inject
	@Named("campaign.driveNodeBuilder")
	private Provider<DriveNodeBuilder<CampaignLibraryNode>> cammpaignDriveNodeBuilder;


	@Inject
	private CustomProjectFinder customProjectFinder;

	@Inject
	private CampaignLibraryNavigationService campaignLibraryNavigationService;


	@Inject
	private TestCaseModificationService testCaseModificationService;

	@Inject
	private InternationalizationHelper internationalizationHelper;


	@Inject
	private FeatureManager featureManager;

	private Map<String, FormModelBuilder> formModelBuilder = new HashMap<String, AdvancedSearchController.FormModelBuilder>();

	{
		formModelBuilder.put(TESTCASE, new FormModelBuilder() {
			@Override
			public SearchInputInterfaceModel build(Locale locale, boolean isMilestoneMode) {
				SearchInputInterfaceModel model = getTestCaseSearchInputInterfaceModel(locale, isMilestoneMode);
				populateMetadata(model);
				return model;
			}
		});

		formModelBuilder.put(TESTCASE_VIA_REQUIREMENT, new FormModelBuilder() {
			@Override
			public SearchInputInterfaceModel build(Locale locale, boolean isMilestoneMode) {
				SearchInputInterfaceModel model = getTestCaseViaRequirementSearchInputInterfaceModel(locale,
						isMilestoneMode);
				populateMetadata(model);
				return model;
			}
		});

		formModelBuilder.put(CAMPAIGN, new FormModelBuilder() {
			@Override
			public SearchInputInterfaceModel build(Locale locale, boolean isMilestoneMode) {
				SearchInputInterfaceModel model = getCampaignSearchInputInterfaceModel(locale, isMilestoneMode);
				populateMetadata(model);
				return model;
			}
		});

		formModelBuilder.put(REQUIREMENT, new FormModelBuilder() {
			@Override
			public SearchInputInterfaceModel build(Locale locale, boolean isMilestoneMode) {
				SearchInputInterfaceModel model = getRequirementSearchInputInterfaceModel(locale, isMilestoneMode);
				populateMetadata(model);
				return model;
			}
		});
	}

	@Inject
	private TestCaseAdvancedSearchService testCaseAdvancedSearchService;

	@Inject
	private CampaignAdvancedSearchService campaignAdvancedSearchService;

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
	private CampaignSearchInterfaceDescription campaignSearchInterfaceDescription;

	@Inject
	private CampaignTestPlanManagerService campaignTestPlanManagerService;

	@Inject
	private IterationTestPlanManagerService iterationTestPlanManagerService;

	@Inject
	private TestSuiteTestPlanManagerService testSuiteTestPlanManagerService;

	@Inject
	@Named("campaign.driveNodeBuilder")
	private Provider<DriveNodeBuilder<LibraryNode>> driveNodeBuilderProvider;

	@Inject
	@Named("squashtest.tm.service.CampaignsWorkspaceService")
	private WorkspaceService<Library<CampaignLibraryNode>> workspaceService;

	// These are used by Lucene - Thus the columns are mapped to index
	// properties rather than class properties
	private DatatableMapper<String> testCaseSearchResultMapper = new NameBasedMapper(15)
	.mapAttribute(DataTableModelConstants.PROJECT_NAME_KEY, "name", Project.class)
	.mapAttribute("test-case-id", "id", TestCase.class)
	.mapAttribute("test-case-ref", "reference", TestCase.class)
	.mapAttribute("test-case-label", "labelUpperCased", TestCase.class)
	.mapAttribute("test-case-weight", "importance", TestCase.class)
	.mapAttribute("test-case-nature", "nature", TestCase.class)
	.mapAttribute("test-case-type", "type", TestCase.class)
	.mapAttribute("test-case-status", "status", TestCase.class)
	.mapAttribute("test-case-milestone-nb", "milestones", TestCase.class)
	.mapAttribute("test-case-requirement-nb", "requirements", TestCase.class)
	.mapAttribute("test-case-teststep-nb", "steps", TestCase.class)
	.mapAttribute("test-case-iteration-nb", "iterations", TestCase.class)
	.mapAttribute("test-case-attachment-nb", "attachments", TestCase.class)
	.mapAttribute("test-case-created-by", "createdBy", TestCase.class)
	.mapAttribute("test-case-modified-by", "lastModifiedBy", TestCase.class);

	private DatatableMapper<String> requirementSearchResultMapper = new NameBasedMapper(14)
	.mapAttribute(DataTableModelConstants.PROJECT_NAME_KEY, "name", Project.class)
	.mapAttribute("requirement-id", "requirement.id", RequirementVersion.class)
	.mapAttribute("requirement-reference", "reference", RequirementVersion.class)
	.mapAttribute("requirement-label", "labelUpperCased", RequirementVersion.class)
	.mapAttribute("requirement-criticality", "criticality", RequirementVersion.class)
	.mapAttribute("requirement-category", "category", RequirementVersion.class)
	.mapAttribute("requirement-status", "status", RequirementVersion.class)
	.mapAttribute("requirement-milestone-nb", "milestones", RequirementVersion.class)
	.mapAttribute("requirement-version", "versionNumber", RequirementVersion.class)
	.mapAttribute("requirement-version-nb", "versions", Requirement.class)
	.mapAttribute("requirement-testcase-nb", "testcases", RequirementVersion.class)
	.mapAttribute("requirement-attachment-nb", "attachments", RequirementVersion.class)
	.mapAttribute("requirement-created-by", "createdBy", RequirementVersion.class)
	.mapAttribute("requirement-modified-by", "lastModifiedBy", RequirementVersion.class);

	private DatatableMapper<String> campaignSearchResultMapper = new NameBasedMapper(11)
			.mapAttribute(DataTableModelConstants.PROJECT_NAME_KEY, "name", Project.class)
			.mapAttribute("campaign-name", "id", Execution.class)
			.mapAttribute("iteration-name", "name", Execution.class)
			.mapAttribute("execution-id", "id", Execution.class)
			.mapAttribute("execution-mode", "lastExecutedBy", Execution.class)
			.mapAttribute("execution-milestone-nb", "id", Execution.class)
			.mapAttribute("testsuite-execution", "id", Execution.class)
			.mapAttribute("execution-status", "status", Execution.class)
			.mapAttribute("execution-executed-by", "lastExecutedBy", Execution.class)
			.mapAttribute("execution-executed-on", "lastExecutedOn", Execution.class)
			.mapAttribute("execution-datasets", "id", Execution.class);

	// Could be change and discriminate params = "searchDomain=campaign" but not necessary
	@RequestMapping(method = RequestMethod.GET)
	public String showSearchPage(Model model, @RequestParam String searchDomain,
			@RequestParam(value = "cookieValueSelect", required = false, defaultValue = "") String cookieValueSelect,
			@RequestParam(value = "cookieValueOpen", required = false, defaultValue = "") String[] cookieValueOpen,
			@CookieValue(value = "workspace-prefs", required = false, defaultValue = "") String elementId,
			@RequestParam(required = false, defaultValue = "") String associateResultWithType,
			@RequestParam(required = false, defaultValue = "") Long id,
			Locale locale,
			@CurrentMilestone Milestone activeMilestone) {

		// Wow, so much params.
		// workspace-prefs could be useless
		// But those cookieValueOpen and cookieValueSelect are great. They allow to get the cookie value from the
		// workspace to be put in the search tree
		// Abracajava ! You get the value, you put it in the mixer, nodesToOpen or selectedNode, some go to
		// expansionCandidates then to the rootNodes

		initModelForPage(model, associateResultWithType, id, activeMilestone);
		model.addAttribute(SEARCH_DOMAIN, searchDomain);
		if (TESTCASE_VIA_REQUIREMENT.equals(searchDomain)) {
			searchDomain = REQUIREMENT;
		}
 else if (CAMPAIGN.equals(searchDomain)) {

			List<Library<CampaignLibraryNode>> libraries = getWorkspaceService().findAllLibraries();

				model.addAttribute("selectedNode", cookieValueSelect);
				model.addAttribute("openedNode", cookieValueOpen);

			MultiMap expansionCandidates = mapIdsByType(cookieValueOpen);

			DriveNodeBuilder<LibraryNode> nodeBuilder = driveNodeBuilderProvider().get();
			if (activeMilestone != null) {
				nodeBuilder.filterByMilestone(activeMilestone);
			}

			List<JsTreeNode> rootNodes = new JsTreeNodeListBuilder<Library<LibraryNode>>(nodeBuilder).expand(expansionCandidates)
					.setModel(libraries).build();
			model.addAttribute("rootModel", rootNodes);
			Collection<Project> numberOfCampaignsAvailable = customProjectFinder.findAllReadable();

			List<Project> projectList = new ArrayList<Project>();
			for (Project project : numberOfCampaignsAvailable) {
				projectList.add(project);
			}

			boolean isCampaignAvailable = campaignTestPlanManagerService.findCampaignByProjectId(projectList,
					activeMilestone);
			model.addAttribute("isCampaignAvailable", isCampaignAvailable);
		}

		FormModelBuilder builder = formModelBuilder.get(searchDomain);
		if (builder != null) {
			model.addAttribute("formModel", builder.build(locale, (activeMilestone != null)));
		} else {
			LOGGER.error(
					"Could not find a FormModelBuilder for search domain : {}. This is either caused by a bug or a hand-written request",
					searchDomain);
		}

		return searchDomain + "-search-input.html";
	}

	protected Provider<DriveNodeBuilder<LibraryNode>> driveNodeBuilderProvider() {
		return driveNodeBuilderProvider;
	}

	protected WorkspaceService<Library<CampaignLibraryNode>> getWorkspaceService() {
		return workspaceService;
	}

	protected String[] getNodeParentsInWorkspace(Long elementId) {
		List<String> parents = campaignLibraryNavigationService.getParentNodesAsStringList(elementId);
		return parents.toArray(new String[parents.size()]);
	}

	protected MultiMap mapIdsByType(String[] openedNodes) {
		return JsTreeHelper.mapIdsByType(openedNodes);
	}
	protected String getTreeElementIdInWorkspace(Long elementId) {
		return "Campaign-" + elementId;
	}

	private void initModelForPage(Model model, String associateResultWithType, Long id, Milestone activeMilestone) {
		model.addAttribute("isMilestoneMode", (activeMilestone != null));
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
			@RequestParam(value = "cookieValueSelect", required = false, defaultValue = "") String cookieValueSelect,
			@RequestParam(value = "cookieValueOpen", required = false, defaultValue = "") String[] cookieValueOpen,
			@CookieValue(value = "workspace-prefs", required = false, defaultValue = "") String elementId,
			@RequestParam String searchModel, @RequestParam(required = false) String associateResultWithType,
			@RequestParam(required = false) Long id, Locale locale,
			@CurrentMilestone Milestone activeMilestone) {
		model.addAttribute(SEARCH_MODEL, searchModel);
		return showSearchPage(model, searchDomain, cookieValueSelect, cookieValueOpen, elementId,
				associateResultWithType, id, locale,
				activeMilestone);
	}

	@RequestMapping(value = RESULTS, params = TESTCASE)
	public String getTestCaseSearchResultPage(Model model, @RequestParam String searchModel,
			@RequestParam(required = false) String associateResultWithType, @RequestParam(required = false) Long id,
			@CurrentMilestone Milestone activeMilestone) {

		initModelForPage(model, associateResultWithType, id, activeMilestone);
		model.addAttribute(SEARCH_MODEL, searchModel);
		model.addAttribute(SEARCH_DOMAIN, TESTCASE);

		populateMetadata(model);

		return "test-case-search-result.html";
	}

	@RequestMapping(value = RESULTS, params = REQUIREMENT)
	public String getRequirementSearchResultPage(Model model, @RequestParam String searchModel,
			@RequestParam(required = false) String associateResultWithType, @RequestParam(required = false) Long id,
			@CurrentMilestone Milestone activeMilestone) {

		initModelForPage(model, associateResultWithType, id, activeMilestone);
		model.addAttribute(SEARCH_MODEL, searchModel);
		model.addAttribute(SEARCH_DOMAIN, REQUIREMENT);

		populateMetadata(model);

		return "requirement-search-result.html";
	}

	@RequestMapping(value = RESULTS, params = CAMPAIGN)
	public String getCampaignSearchResultPage(Model model, @RequestParam String searchModel,
			@RequestParam(required = false) String associateResultWithType, @RequestParam(required = false) Long id,
			@CurrentMilestone Milestone activeMilestone) {

		initModelForPage(model, associateResultWithType, id, activeMilestone);
		model.addAttribute(SEARCH_MODEL, searchModel);
		model.addAttribute(SEARCH_DOMAIN, CAMPAIGN);

		populateMetadata(model);

		return "campaign-search-result.html";

	}

	@RequestMapping(value = RESULTS, params = TESTCASE_VIA_REQUIREMENT)
	public String getTestCaseThroughRequirementSearchResultPage(Model model, @RequestParam String searchModel,
			@RequestParam(required = false) String associateResultWithType, @RequestParam(required = false) Long id,
			@CurrentMilestone Milestone activeMilestone) {

		initModelForPage(model, associateResultWithType, id, activeMilestone);
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

	@RequestMapping(value = TABLE, method = RequestMethod.POST, params = { RequestParams.MODEL,
			TESTCASE_VIA_REQUIREMENT, RequestParams.S_ECHO_PARAM })
	@ResponseBody
	public DataTableModel getTestCaseThroughRequirementTableModel(final DataTableDrawParameters params,
			final Locale locale, @RequestParam(value = RequestParams.MODEL) String model,
			@RequestParam(required = false) String associateResultWithType, @RequestParam(required = false) Long id,
			@CurrentMilestone Milestone activeMilestone)
					throws JsonParseException, JsonMappingException, IOException {

		AdvancedSearchModel searchModel = new ObjectMapper().readValue(model, AdvancedSearchModel.class);

		if (activeMilestone != null && featureManager.isEnabled(Feature.MILESTONE) ) {
			addMilestoneToSearchModel(searchModel, activeMilestone);
		}

		PagingAndMultiSorting paging = new DataTableMultiSorting(params, testCaseSearchResultMapper);

		PagedCollectionHolder<List<TestCase>> holder = testCaseAdvancedSearchService
				.searchForTestCasesThroughRequirementModel(searchModel, paging, locale);

		boolean isInAssociationContext = isInAssociationContext(associateResultWithType);

		Set<Long> ids = null;

		if (isInAssociationContext) {
			ids = getIdsOfTestCasesAssociatedWithObjects(associateResultWithType, id);
		}

		return new TestCaseSearchResultDataTableModelHelper(locale, messageSource, permissionService, iterationService,
				isInAssociationContext, ids).buildDataModel(holder, params.getsEcho());
	}

	@RequestMapping(value = TABLE, method = RequestMethod.POST, params = { RequestParams.MODEL, TESTCASE,
			RequestParams.S_ECHO_PARAM })
	@ResponseBody
	public DataTableModel getTestCaseTableModel(final DataTableDrawParameters params, final Locale locale,
			@RequestParam(value = RequestParams.MODEL) String model,
			@RequestParam(required = false) String associateResultWithType, @RequestParam(required = false) Long id,
			@CurrentMilestone Milestone activeMilestone)
					throws JsonParseException, JsonMappingException, IOException {

		AdvancedSearchModel searchModel = new ObjectMapper().readValue(model, AdvancedSearchModel.class);
		if (activeMilestone != null) {
			addMilestoneToSearchModel(searchModel, activeMilestone);
		}
		PagingAndMultiSorting paging = new DataTableMultiSorting(params, testCaseSearchResultMapper);

		PagedCollectionHolder<List<TestCase>> holder = testCaseAdvancedSearchService.searchForTestCases(searchModel,
				paging, locale);

		boolean isInAssociationContext = isInAssociationContext(associateResultWithType);

		Set<Long> ids = null;

		if (isInAssociationContext) {
			ids = getIdsOfTestCasesAssociatedWithObjects(associateResultWithType, id);
		}

		return new TestCaseSearchResultDataTableModelHelper(locale, messageSource, permissionService, iterationService,
				isInAssociationContext, ids).buildDataModel(holder, params.getsEcho());
	}

	@RequestMapping(value = TABLE, method = RequestMethod.POST, params = { RequestParams.MODEL, REQUIREMENT,
			RequestParams.S_ECHO_PARAM })
	@ResponseBody
	public DataTableModel getRequirementTableModel(final DataTableDrawParameters params, final Locale locale,
			@RequestParam(value = RequestParams.MODEL) String model,
			@RequestParam(required = false) String associateResultWithType, @RequestParam(required = false) Long id,
			@CurrentMilestone Milestone activeMilestone)
					throws JsonParseException, JsonMappingException, IOException {

		AdvancedSearchModel searchModel = new ObjectMapper().readValue(model, AdvancedSearchModel.class);

		if (activeMilestone != null) {
			addMilestoneToSearchModel(searchModel, activeMilestone);
		}

		PagingAndMultiSorting paging = new DataTableMultiSorting(params, requirementSearchResultMapper);

		PagedCollectionHolder<List<RequirementVersion>> holder = requirementVersionAdvancedSearchService
				.searchForRequirementVersions(searchModel, paging, messageSource, locale);

		boolean isInAssociationContext = isInAssociationContext(associateResultWithType);

		Set<Long> ids = null;

		if (isInAssociationContext) {
			ids = getIdsOfRequirementsAssociatedWithObjects(associateResultWithType, id);
		}

		return new RequirementSearchResultDataTableModelHelper(locale, messageSource, permissionService,
				isInAssociationContext, ids).buildDataModel(holder, params.getsEcho());
	}

	// TODO

	@RequestMapping(value = TABLE, method = RequestMethod.POST, params = { RequestParams.MODEL, CAMPAIGN,
			RequestParams.S_ECHO_PARAM })
	@ResponseBody
	public DataTableModel getCampaignTableModel(final DataTableDrawParameters params, final Locale locale,
			@RequestParam(value = RequestParams.MODEL) String model,
			@RequestParam(required = false) String associateResultWithType, @RequestParam(required = false) Long id,
			@CurrentMilestone Milestone activeMilestone) throws JsonParseException, JsonMappingException, IOException {

		AdvancedSearchModel searchModel = new ObjectMapper().readValue(model, AdvancedSearchModel.class);
		if (activeMilestone != null) {
			addMilestoneToSearchModel(searchModel, activeMilestone);
		}
		PagingAndMultiSorting paging = new DataTableMultiSorting(params, campaignSearchResultMapper);

		PagedCollectionHolder<List<Execution>> holder = campaignAdvancedSearchService.searchForCampaign(searchModel,
				paging, locale);

		boolean isInAssociationContext = isInAssociationContext(associateResultWithType);

		// TODO wtf ids is null ?!
		Set<Long> ids = null;

		if (isInAssociationContext) {
			ids = getIdsOfTestCasesAssociatedWithObjects(associateResultWithType, id);
		}

		return new CampaignSearchResultDataTableModelHelper(locale, messageSource, permissionService, iterationService,
				isInAssociationContext, ids).buildDataModel(holder, params.getsEcho());
	}

	/*
	 *
	 */

	private void addMilestoneToSearchModel(AdvancedSearchModel searchModel, Milestone activeMilestone) {
		// yes this is a list field for only one value ! But this allow us to handle milestone mode same as reference
		// mode
		AdvancedSearchListFieldModel model = new AdvancedSearchListFieldModel();
		List<String> milestones = new ArrayList<String>();
		milestones.add(activeMilestone.getId().toString());
		model.setValues(milestones);
		searchModel.addField("milestones.id", model);
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

	@RequestMapping(value = INPUT, method = RequestMethod.GET, headers = AcceptHeaders.CONTENT_JSON, params = TESTCASE_VIA_REQUIREMENT)
	@ResponseBody
	public SearchInputInterfaceModel getTestCaseViaRequirementSearchInputInterfaceModel(Locale locale,
			boolean isMilestoneMode) {
		// TODO should no longer be called through HTTP, put it private
		return getRequirementSearchInputInterfaceModel(locale, isMilestoneMode);
	}

	@RequestMapping(value = INPUT, method = RequestMethod.GET, headers = AcceptHeaders.CONTENT_JSON, params = REQUIREMENT)
	@ResponseBody
	public SearchInputInterfaceModel getRequirementSearchInputInterfaceModel(Locale locale, boolean isMilestoneMode) {
		// TODO should no longer be called through HTTP, put it private
		SearchInputInterfaceModel model = new SearchInputInterfaceModel();

		// Perimeter
		model.addPanel(requirementVersionSearchInterfaceDescription.createRequirementPerimeterPanel(locale));

		// Information
		model.addPanel(requirementVersionSearchInterfaceDescription.createRequirementInformationPanel(locale));

		// History
		model.addPanel(requirementVersionSearchInterfaceDescription.createRequirementHistoryPanel(locale));

		// Attributes
		model.addPanel(requirementVersionSearchInterfaceDescription.createRequirementAttributePanel(locale));

		// Milestones
		if (!isMilestoneMode && featureManager.isEnabled(Feature.MILESTONE)) {
			model.addPanel(requirementVersionSearchInterfaceDescription.createMilestonePanel(locale));
		}

		// Version
		model.addPanel(requirementVersionSearchInterfaceDescription.createRequirementVersionPanel(locale));

		// Content
		model.addPanel(requirementVersionSearchInterfaceDescription.createRequirementContentPanel(locale));

		// Associations
		model.addPanel(requirementVersionSearchInterfaceDescription.createRequirementAssociationPanel(locale));

		// CUFs
		model.addPanel(createCUFPanel(locale, BindableEntity.REQUIREMENT_VERSION));

		return model;
	}

	@RequestMapping(value = INPUT, method = RequestMethod.GET, headers = AcceptHeaders.CONTENT_JSON, params = TESTCASE)
	@ResponseBody
	public SearchInputInterfaceModel getTestCaseSearchInputInterfaceModel(Locale locale, boolean isMilestoneMode) {
		// TODO should no longer be called through HTTP, put it private
		SearchInputInterfaceModel model = new SearchInputInterfaceModel();

		// Perimeter
		model.addPanel(testcaseVersionSearchInterfaceDescription.createPerimeterPanel(locale));

		// Information
		model.addPanel(testcaseVersionSearchInterfaceDescription.createGeneralInfoPanel(locale));

		// History
		model.addPanel(testcaseVersionSearchInterfaceDescription.createTestCaseHistoryPanel(locale));

		// Attributes
		model.addPanel(testcaseVersionSearchInterfaceDescription.createAttributePanel(locale));

		// Milestones
		if (!isMilestoneMode && featureManager.isEnabled(Feature.MILESTONE)) {
			model.addPanel(testcaseVersionSearchInterfaceDescription.createMilestonePanel(locale));
		}

		// Content
		model.addPanel(testcaseVersionSearchInterfaceDescription.createContentPanel(locale));

		// Associations
		model.addPanel(testcaseVersionSearchInterfaceDescription.createAssociationPanel(locale));

		// CUF
		model.addPanel(createCUFPanel(locale, BindableEntity.TEST_CASE));

		return model;
	}


	@RequestMapping(value = INPUT, method = RequestMethod.GET, headers = AcceptHeaders.CONTENT_JSON, params = CAMPAIGN)
	@ResponseBody
	public SearchInputInterfaceModel getCampaignSearchInputInterfaceModel(Locale locale, boolean isMilestoneMode) {

		// TODO should no longer be called through HTTP, put it private
		SearchInputInterfaceModel model = new SearchInputInterfaceModel();

		// Information
		model.addPanel(campaignSearchInterfaceDescription.createGeneralInfoPanel(locale));

		// Attributes
		model.addPanel(campaignSearchInterfaceDescription.createAttributePanel(locale));

		// Milestones
		if (!isMilestoneMode && featureManager.isEnabled(Feature.MILESTONE)) {
			model.addPanel(requirementVersionSearchInterfaceDescription.createMilestonePanel(locale));
		}

		// Content
		model.addPanel(campaignSearchInterfaceDescription.createContentPanel(locale));

		// TODO : executions there
		model.addPanel(campaignSearchInterfaceDescription.createExecutionPanel(locale));

		// CUF
		model.addPanel(createCUFPanel(locale, BindableEntity.CAMPAIGN));

		return model;
	}


	@RequestMapping(value = "/milestones/tc-mass-modif-associables/{testCaseIds}", method = RequestMethod.GET)
	@ResponseBody
	public DataTableModel getMilestonesForMassTcModif(@PathVariable List<Long> testCaseIds,
			DataTableDrawParameters params, Locale locale) {
		Collection<Milestone> milestones = testCaseModificationService
				.findAssociableMilestonesForMassModif(testCaseIds);
		return buildMilestoneTableModelForMassModif(milestones, locale);
	}

	@RequestMapping(value = "/milestones/reqV-mass-modif-associables/{reqVersionIds}", method = RequestMethod.GET)
	@ResponseBody
	public DataTableModel getMilestonesForMassReqVersionModif(@PathVariable List<Long> reqVersionIds,
			DataTableDrawParameters params, Locale locale) {
		Collection<Milestone> milestones = versionService.findAssociableMilestonesForMassModif(reqVersionIds);
		return buildMilestoneTableModelForMassModif(milestones, locale);
	}


	@RequestMapping(value = "/milestones/tc-mass-modif-data/{testCaseIds}", method = RequestMethod.GET)
	@ResponseBody
	public MilestoneMassModifData getMilestoneMassModifDataForTc(@PathVariable List<Long> testCaseIds){

		MilestoneMassModifData data = new MilestoneMassModifData();
		data.setCheckedIds(testCaseModificationService.findBindedMilestonesIdForMassModif(testCaseIds));
		boolean hasData = testCaseModificationService.findAssociableMilestonesForMassModif(testCaseIds).size() != 0 ? true : false;
		data.setHasData(hasData);
		data.setSamePerimeter(testCaseModificationService.haveSamePerimeter(testCaseIds));
		return data;
	}



	@RequestMapping(value = "/milestones/reqV-mass-modif-data/{reqVersionIds}", method = RequestMethod.GET)
	@ResponseBody
	public MilestoneMassModifData getMilestoneMassModifDataForReqVersion(@PathVariable List<Long> reqVersionIds,
			Locale locale) {

		MilestoneMassModifData data = new MilestoneMassModifData();
		data.setCheckedIds(versionService.findBindedMilestonesIdForMassModif(reqVersionIds));
		boolean hasData = versionService.findAssociableMilestonesForMassModif(reqVersionIds).size() != 0 ? true : false;
		data.setHasData(hasData);
		data.setSamePerimeter(versionService.haveSamePerimeter(reqVersionIds));
		return data;
	}




	@RequestMapping(value = "/tcs/{testCaseIds}/milestones", method = RequestMethod.POST, params = IDS)
	@ResponseBody
	public void bindMilestonesToTcs(@PathVariable List<Long> testCaseIds, @RequestParam(IDS) List<Long> milestoneIds) {

		Collection<Long> bindedBefore = testCaseModificationService.findBindedMilestonesIdForMassModif(testCaseIds);
		bindedBefore.removeAll(milestoneIds);

		for (Long testCaseId : testCaseIds) {
			testCaseModificationService.bindMilestones(testCaseId, milestoneIds);
			testCaseModificationService.unbindMilestones(testCaseId, bindedBefore);
		}
	}


	@RequestMapping(value = "/reqVersions/{reqVIds}/milestones", method = RequestMethod.POST, params = IDS)
	@ResponseBody
	public boolean bindMilestonesToReqV(@PathVariable List<Long> reqVIds, @RequestParam(IDS) List<Long> milestoneIds) {
		Collection<Long> bindedBefore = versionService.findBindedMilestonesIdForMassModif(reqVIds);
		//was binded before but is not now so need to unbind
		bindedBefore.removeAll(milestoneIds);

		boolean isOneVersionAlreadyBind = milestoneIds.isEmpty() ? false : versionService.isOneMilestoneAlreadyBindToAnotherRequirementVersion(reqVIds, milestoneIds);

		for (Long reqVId : reqVIds) {
			versionService.bindMilestones(reqVId, milestoneIds);
			versionService.unbindMilestones(reqVId, bindedBefore);
		}

		return isOneVersionAlreadyBind;
	}



	private DataTableModel buildMilestoneTableModelForMassModif(Collection<Milestone> data, Locale locale) {
		MilestoneDataTableModelHelper helper = new MilestoneDataTableModelHelper(internationalizationHelper, locale);
		Collection<Object> aaData = helper.buildRawModel(data);
		DataTableModel model = new DataTableModel("");
		model.setAaData((List<Object>) aaData);
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
			res.put("requirement-version-id", item.getId());
			res.put("requirement-reference", item.getReference());
			res.put("requirement-label", item.getName());
			res.put("editable", isRequirementVersionEditable(item));
			res.put("requirement-criticality", formatCriticality(item.getCriticality(), locale));
			res.put("requirement-category", formatInfoItem(item.getCategory(), locale));
			res.put("requirement-status", formatStatus(item.getStatus(), locale));
			res.put("requirement-milestone-nb", item.getMilestones().size());
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

		private boolean isTestCaseEditable(TestCase item) {
			if (item.isModifiable()) {
				return permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "WRITE", item);
			} else {
				return false;
			}
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
			res.put("test-case-milestone-nb", item.getMilestones().size());
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

	/* Execution */

	private static final class CampaignSearchResultDataTableModelHelper extends DataTableModelBuilder<Execution> {
		private InternationalizationHelper messageSource;
		private Locale locale;
		private PermissionEvaluationService permissionService;
		private IterationModificationService iterationService;
		private boolean isInAssociationContext;
		private Set<Long> associatedTestCaseIds;

		private CampaignSearchResultDataTableModelHelper(Locale locale, InternationalizationHelper messageSource,
				PermissionEvaluationService permissionService, IterationModificationService iterationService,
				boolean isInAssociationContext, Set<Long> associatedTestCaseIds) {
			this.locale = locale;
			this.messageSource = messageSource;
			this.permissionService = permissionService;
			this.iterationService = iterationService;
			this.isInAssociationContext = isInAssociationContext;
			this.associatedTestCaseIds = associatedTestCaseIds;
		}

		private boolean isInAssociationContext() {
			return this.isInAssociationContext;
		}

		private boolean isExecutionEditable(Execution item) {
			// Milestone dependent ? Not for now.
			return permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "WRITE", item);
		}

		@Override
		public Map<String, Object> buildItemData(Execution item) {
			final AuditableMixin auditable = (AuditableMixin) item;
			Map<String, Object> res = new HashMap<String, Object>();
			res.put(DataTableModelConstants.PROJECT_NAME_KEY, item.getProject().getName());
			res.put("project-id", item.getProject().getId());
			if (isInAssociationContext()) {
				res.put("empty-is-associated-holder", " ");
				res.put("is-associated", associatedTestCaseIds.contains(item.getId()));
			}
			res.put(DataTableModelConstants.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex());
			res.put("campaign-name", item.getCampaign().getName().toString());
			res.put("iteration-name", item.getIteration().getName().toString());
			res.put("editable", isExecutionEditable(item));
			res.put("execution-id", item.getId().toString());
			res.put("execution-mode", formatMode(item.getExecutionMode(), locale));
			res.put("execution-milestone-nb", item.getCampaign().getMilestones().toString());
			res.put("testsuite-execution", item.getTestPlan().getLabel());
			res.put("execution-status", formatExecutionStatus(item.getExecutionStatus(), locale));
			res.put("execution-executed-by", formatUsername(item.getLastExecutedBy()));
			res.put("execution-executed-on", formatDateItem(item));
			res.put("execution-datasets", formatDatasetsItem(item));
			res.put("execution-suiteId", item.getTestPlan().getId());
			res.put("empty-openinterface2-holder", " ");
			res.put("empty-opentree-holder", " ");
			return res;
		}

		private String formatExecutionStatus(ExecutionStatus status, Locale locale) {
			return status.getLevel() + "-" + messageSource.internationalize(status, locale);
		}

		private String formatMode(TestCaseExecutionMode mode, Locale locale) {
			return messageSource.internationalize(mode, locale);
		}

		private String formatType(TestCaseType type, Locale locale) {
			return messageSource.internationalize(type, locale);
		}

		private String formatInfoItem(InfoListItem item, Locale locale) {
			return messageSource.getMessage(item.getLabel(), null, item.getLabel(), locale);
		}

		private String formatDatasetsItem(Execution item) {
			String dataset = "-";
			if (item.getDatasetLabel() != null) {
				dataset = item.getDatasetLabel();
			}
			return dataset;
		}

		private String formatDateItem(Execution item) {
			String reportDate = "-";
			// Get the i18n thing
			DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			if (item.getLastExecutedOn() != null) {
				reportDate = df.format(item.getLastExecutedOn());
			}
			return reportDate;
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

			switch (customField.getInputType()) {
			case DROPDOWN_LIST:
				SingleSelectField selectField = (SingleSelectField) customField;
				model.getFields().add(convertToSearchInputFieldModel(selectField, locale));
				break;

			case PLAIN_TEXT:
				model.getFields().add(convertToSearchInputFieldModel(customField));
				break;

			case CHECKBOX:
				model.getFields().add(createCheckBoxField(customField, locale));
				break;

			case DATE_PICKER:
				model.getFields().add(createDatePickerField(customField));
				break;

			case TAG:
				model.getFields().add(convertToSearchInputFieldModel((MultiSelectField) customField));
				break;

			case RICH_TEXT:
				break; // not supported for now
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

	private SearchInputFieldModel convertToSearchInputFieldModel(MultiSelectField multifield) {
		List<SearchInputPossibleValueModel> possibleValues = new ArrayList<SearchInputPossibleValueModel>(multifield
				.getOptions().size());

		for (CustomFieldOption option : multifield.getOptions()) {
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

	private void populateMetadata(Model model) {
		model.addAttribute("projects", readableJsonProjects());
	}

	private void populateMetadata(SearchInputInterfaceModel model) {

		model.addMetadata(PROJECTS_META, readableJsonProjects());

	}

	private List<JsonProject> readableJsonProjects() {
		List<Project> readableProjects = projectFinder.findAllReadable();
		List<JsonProject> jsonified = new ArrayList<JsonProject>(readableProjects.size());

		for (Project p : readableProjects) {
			jsonified.add(jsProjectBuilder.toExtendedProject(p));
		}

		return jsonified;
	}

}
