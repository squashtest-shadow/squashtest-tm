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
package org.squashtest.tm.web.internal.controller.testcase;

import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.NullArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.core.bugtracker.core.BugTrackerNoCredentialsException;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor;
import org.squashtest.tm.core.foundation.collection.DefaultPagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.SortOrder;
import org.squashtest.tm.domain.IdentifiedUtil;
import org.squashtest.tm.domain.bugtracker.BugTrackerStatus;
import org.squashtest.tm.domain.bugtracker.IssueOwnership;
import org.squashtest.tm.domain.bugtracker.RemoteIssueDecorator;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.RenderingLocation;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.DatasetParamValue;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseExecutionMode;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseNature;
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.domain.testcase.TestCaseType;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.exception.UnknownEntityException;
import org.squashtest.tm.service.bugtracker.BugTrackersLocalService;
import org.squashtest.tm.service.customfield.CustomFieldHelper;
import org.squashtest.tm.service.customfield.CustomFieldHelperService;
import org.squashtest.tm.service.execution.ExecutionFinder;
import org.squashtest.tm.service.requirement.VerifiedRequirement;
import org.squashtest.tm.service.requirement.VerifiedRequirementsManagerService;
import org.squashtest.tm.service.testcase.ParameterFinder;
import org.squashtest.tm.service.testcase.TestCaseModificationService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.bugtracker.BugTrackerControllerHelper;
import org.squashtest.tm.web.internal.controller.generic.ServiceAwareAttachmentTableModelHelper;
import org.squashtest.tm.web.internal.controller.testcase.parameters.ParametersDataTableModelHelper;
import org.squashtest.tm.web.internal.controller.testcase.parameters.TestCaseDatasetsController;
import org.squashtest.tm.web.internal.controller.testcase.parameters.TestCaseParametersController.ParameterNameComparator;
import org.squashtest.tm.web.internal.controller.testcase.steps.TestStepsTableModelBuilder;
import org.squashtest.tm.web.internal.helper.LevelLabelFormatter;
import org.squashtest.tm.web.internal.helper.LevelLabelFormatterWithoutOrder;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.combo.OptionTag;
import org.squashtest.tm.web.internal.model.customfield.CustomFieldJsonConverter;
import org.squashtest.tm.web.internal.model.customfield.CustomFieldModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableSorting;
import org.squashtest.tm.web.internal.model.jquery.RenameModel;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

@Controller
@RequestMapping("/test-cases/{testCaseId}")
public class TestCaseModificationController {
	/**
	 * 
	 */
	private static final String TEST_CASE = "testCase";

	private static final Logger LOGGER = LoggerFactory.getLogger(TestCaseModificationController.class);

	/**
	 * 
	 */
	private static final String NAME = "name";

	private static final String TEST_CASE_ = "test case ";

	private final DatatableMapper<String> referencingTestCaseMapper = new NameBasedMapper(6)
			.mapAttribute("project-name", NAME, Project.class)
			.mapAttribute("tc-reference", "reference", TestCase.class)
			.mapAttribute("tc-name", NAME, TestCase.class)
			.mapAttribute("tc-mode", "executionMode", TestCase.class);

	
	@Inject
	private TestCaseModificationService testCaseModificationService;

	@Inject
	private ExecutionFinder executionFinder;

	@Inject
	private ParameterFinder parameterFinder;

	@Inject
	private VerifiedRequirementsManagerService verifiedRequirementsManagerService;

	@Inject
	private MessageSource messageSource;

	@Inject
	private InternationalizationHelper internationalizationHelper;

	@Inject
	private Provider<TestCaseImportanceJeditableComboDataBuilder> importanceComboBuilderProvider;

	@Inject
	private Provider<TestCaseNatureJeditableComboDataBuilder> natureComboBuilderProvider;

	@Inject
	private Provider<TestCaseTypeJeditableComboDataBuilder> typeComboBuilderProvider;

	// ****** custom field services ******************

	@Inject
	private CustomFieldHelperService cufHelperService;

	@Inject
	private CustomFieldJsonConverter converter;

	// ****** /custom field services ******************

	@Inject
	private Provider<TestCaseStatusJeditableComboDataBuilder> statusComboBuilderProvider;

	@Inject
	private Provider<LevelLabelFormatter> levelLabelFormatterProvider;

	@Inject
	private Provider<LevelLabelFormatterWithoutOrder> levelLabelFormatterWithoutOrderProvider;

	@Inject
	private BugTrackersLocalService bugTrackersLocalService;
	
	@Inject
	private ServiceAwareAttachmentTableModelHelper attachmentHelper;
	


	/**
	 * Returns the fragment html view of test case
	 * 
	 * @param testCaseId
	 * @param locale
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	public final ModelAndView showTestCase(@PathVariable long testCaseId, Locale locale) {
		ModelAndView mav = new ModelAndView("fragment/test-cases/edit-test-case");

		TestCase testCase = testCaseModificationService.findById(testCaseId);
		populateModelWithTestCaseEditionData(mav, testCase, locale);

		return mav;
	}

	/**
	 * Returns the full-page html view of test case
	 * 
	 * @param testCaseId
	 * @param locale
	 * @return
	 */
	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public ModelAndView showTestCaseInfo(@PathVariable long testCaseId, Locale locale) {

		LOGGER.trace("TestCaseModificationController : getting infos");

		ModelAndView mav = new ModelAndView("page/test-case-libraries/show-test-case");

		TestCase testCase = testCaseModificationService.findTestCaseWithSteps(testCaseId);
		populateModelWithTestCaseEditionData(mav, testCase, locale);

		return mav;
	}

	private void populateModelWithTestCaseEditionData(ModelAndView mav, TestCase testCase, Locale locale) {

		boolean hasCUF = cufHelperService.hasCustomFields(testCase);

		// Convert execution mode with local parameter
		List<OptionTag> executionModes = new ArrayList<OptionTag>();
		for (TestCaseExecutionMode executionMode : TestCaseExecutionMode.values()) {
			OptionTag ot = new OptionTag();
			ot.setLabel(formatExecutionMode(executionMode, locale));
			ot.setValue(executionMode.toString());
			executionModes.add(ot);
		}
		mav.addObject(TEST_CASE, testCase);
		mav.addObject("executionModes", executionModes);
		mav.addObject("testCaseImportanceComboJson", buildImportanceComboData(locale));
		mav.addObject("testCaseImportanceLabel", formatImportance(testCase.getImportance(), locale));
		mav.addObject("testCaseNatureComboJson", buildNatureComboData(locale));
		mav.addObject("testCaseNatureLabel", formatNature(testCase.getNature(), locale));
		mav.addObject("testCaseTypeComboJson", buildTypeComboData(locale));
		mav.addObject("testCaseTypeLabel", formatType(testCase.getType(), locale));
		mav.addObject("testCaseStatusComboJson", buildStatusComboData(locale));
		mav.addObject("testCaseStatusLabel", formatStatus(testCase.getStatus(), locale));
		mav.addObject("attachmentsModel", attachmentHelper.findPagedAttachments(testCase));
		mav.addObject("callingTestCasesModel", _getCallingTestCaseTableModel(testCase.getId(), new DefaultPagingAndSorting("TestCase.name"), ""));
		mav.addObject("hasCUF", hasCUF);
	}

	@RequestMapping(value = "/importance-combo-data", method = RequestMethod.GET)
	@ResponseBody
	public String buildImportanceComboData(Locale locale) {
		return importanceComboBuilderProvider.get().useLocale(locale).buildMarshalled();
	}

	private String buildNatureComboData(Locale locale) {
		return natureComboBuilderProvider.get().useLocale(locale).buildMarshalled();
	}

	private String buildTypeComboData(Locale locale) {
		return typeComboBuilderProvider.get().useLocale(locale).buildMarshalled();
	}

	private String buildStatusComboData(Locale locale) {
		return statusComboBuilderProvider.get().useLocale(locale).buildMarshalled();
	}

	private String formatExecutionMode(TestCaseExecutionMode mode, Locale locale) {
		return internationalizationHelper.internationalize(mode, locale);
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=test-case-description", VALUE })
	@ResponseBody
	public String changeDescription(@RequestParam(VALUE) String testCaseDescription, @PathVariable long testCaseId) {

		testCaseModificationService.changeDescription(testCaseId, testCaseDescription);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace(TEST_CASE_ + testCaseId + ": updated description to " + testCaseDescription);
		}

		return testCaseDescription;
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=test-case-reference", VALUE })
	@ResponseBody
	public String changeReference(@RequestParam(VALUE) String testCaseReference, @PathVariable long testCaseId) {

		testCaseModificationService.changeReference(testCaseId, testCaseReference);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace(TEST_CASE_ + testCaseId + ": updated reference to " + testCaseReference);
		}

		return testCaseReference;
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, params = { "id=test-case-importance", VALUE })
	public String changeImportance(@PathVariable long testCaseId, @RequestParam(VALUE) TestCaseImportance importance,
			Locale locale) {
		testCaseModificationService.changeImportance(testCaseId, importance);

		return formatImportance(importance, locale);
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, params = { "id=test-case-nature", VALUE })
	public String changeNature(@PathVariable long testCaseId, @RequestParam(VALUE) TestCaseNature nature, Locale locale) {
		testCaseModificationService.changeNature(testCaseId, nature);

		return formatNature(nature, locale);
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=test-case-newname", VALUE})
	@ResponseBody
	public Object changeName(@PathVariable long testCaseId, @RequestParam(VALUE) String newName) {

		testCaseModificationService.rename(testCaseId, newName);
		LOGGER.info("TestCaseModificationController : renaming {} as {}", testCaseId, newName);

		return newName;
	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, params = { "id=test-case-type", VALUE })
	public String changeType(@PathVariable long testCaseId, @RequestParam(VALUE) TestCaseType type, Locale locale) {
		testCaseModificationService.changeType(testCaseId, type);

		return formatType(type, locale);
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, params = { "id=test-case-status", VALUE })
	public String changeStatus(@PathVariable long testCaseId, @RequestParam(VALUE) TestCaseStatus status, Locale locale) {
		testCaseModificationService.changeStatus(testCaseId, status);

		return formatStatus(status, locale);
	}

	@RequestMapping(value = "/importanceAuto", method = RequestMethod.POST, params = { "importanceAuto" })
	@ResponseBody
	public String changeImportanceAuto(@PathVariable long testCaseId,
			@RequestParam(value = "importanceAuto") boolean auto, Locale locale) {
		testCaseModificationService.changeImportanceAuto(testCaseId, auto);
		TestCase testCase = testCaseModificationService.findById(testCaseId);
		return formatImportance(testCase.getImportance(), locale);
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=test-case-prerequisite", VALUE })
	@ResponseBody
	public String changePrerequisite(@RequestParam(VALUE) String testCasePrerequisite, @PathVariable long testCaseId) {

		testCaseModificationService.changePrerequisite(testCaseId, testCasePrerequisite);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace(TEST_CASE_ + testCaseId + ": updated prerequisite to " + testCasePrerequisite);
		}

		return testCasePrerequisite;
	}

	@RequestMapping(method = RequestMethod.POST, params = { "newName" })
	@ResponseBody
	public Object changeName(HttpServletResponse response, @PathVariable long testCaseId, @RequestParam String newName) {

		testCaseModificationService.rename(testCaseId, newName);
		LOGGER.info("TestCaseModificationController : renaming {} as {}", testCaseId, newName);

		return new RenameModel(newName);

	}

	@ResponseBody
	@RequestMapping(value = "/importance", method = RequestMethod.GET)
	public String getImportance(@PathVariable long testCaseId, Locale locale) {
		TestCase testCase = testCaseModificationService.findById(testCaseId);
		TestCaseImportance importance = testCase.getImportance();
		return formatImportance(importance, locale);
	}

	@ResponseBody
	@RequestMapping(value = "/nature", method = RequestMethod.GET)
	public String getNature(@PathVariable long testCaseId, Locale locale) {
		TestCase testCase = testCaseModificationService.findById(testCaseId);
		TestCaseNature nature = testCase.getNature();
		return formatNature(nature, locale);
	}

	@ResponseBody
	@RequestMapping(value = "/type", method = RequestMethod.GET)
	public String getType(@PathVariable long testCaseId, Locale locale) {
		TestCase testCase = testCaseModificationService.findById(testCaseId);
		TestCaseType type = testCase.getType();
		return formatType(type, locale);
	}

	@ResponseBody
	@RequestMapping(value = "/status", method = RequestMethod.GET)
	public String getStatus(@PathVariable long testCaseId, Locale locale) {
		TestCase testCase = testCaseModificationService.findById(testCaseId);
		TestCaseStatus status = testCase.getStatus();
		return formatStatus(status, locale);
	}

	private String formatImportance(TestCaseImportance importance, Locale locale) {
		return levelLabelFormatterProvider.get().useLocale(locale).formatLabel(importance);
	}

	private String formatNature(TestCaseNature nature, Locale locale) {
		return levelLabelFormatterWithoutOrderProvider.get().useLocale(locale).formatLabel(nature);
	}

	private String formatType(TestCaseType type, Locale locale) {
		return levelLabelFormatterWithoutOrderProvider.get().useLocale(locale).formatLabel(type);
	}

	private String formatStatus(TestCaseStatus status, Locale locale) {
		return levelLabelFormatterProvider.get().useLocale(locale).formatLabel(status);
	}

	@RequestMapping(value = "/general", method = RequestMethod.GET)
	public ModelAndView refreshGeneralInfos(@PathVariable long testCaseId) {

		ModelAndView mav = new ModelAndView("fragment/generics/general-information-fragment");

		TestCase testCase = testCaseModificationService.findById(testCaseId);

		
		mav.addObject("auditableEntity", testCase);
		// context-absolute url of this entity
		mav.addObject("entityContextUrl", "/test-cases/" + testCaseId);

		return mav;
	}



	@RequestMapping(value = "/calling-test-cases/table", params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getCallingTestCaseTableModel(@PathVariable long testCaseId, DataTableDrawParameters params,
			final Locale locale) {

		LOGGER.trace("TestCaseModificationController: getCallingTestCaseTableModel called ");

		PagingAndSorting paging = createPaging(params, referencingTestCaseMapper);

		return _getCallingTestCaseTableModel(testCaseId, paging, params.getsEcho());

	}
	
	private DataTableModel _getCallingTestCaseTableModel(long testCaseId, PagingAndSorting paging, String sEcho){
		
		PagedCollectionHolder<List<TestCase>> holder = testCaseModificationService.findCallingTestCases(testCaseId,
				paging);

		return new CallingTestCasesTableModelBuilder(internationalizationHelper).buildDataModel(holder, sEcho);		
	}

	private PagingAndSorting createPaging(final DataTableDrawParameters params, final DatatableMapper<?> dtMapper) {
		return new DataTableSorting(params, dtMapper);
	}

	/* ********************************** localization stuffs ****************************** */

	/**
	 * Return view for Printable test case
	 * 
	 * @param testCaseId
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, params = "format=printable")
	public ModelAndView showPrintableTestCase(@PathVariable long testCaseId, Locale locale) {
		// TODO smells like copy-pasta
		LOGGER.debug("get printable test case");
		TestCase testCase = testCaseModificationService.findById(testCaseId);
		if (testCase == null) {
			throw new UnknownEntityException(testCaseId, TestCase.class);
		}
		ModelAndView mav = new ModelAndView("print-test-case.html");
		mav.addObject(TEST_CASE, testCase);

		// ============================BUGTRACKER
		if (testCase.getProject().isBugtrackerConnected()) {
			Project project = testCase.getProject();
			BugTrackerStatus status = bugTrackersLocalService.checkBugTrackerStatus(project.getId());
			BugTrackerInterfaceDescriptor descriptor = bugTrackersLocalService.getInterfaceDescriptor(project
					.findBugTracker());
			descriptor.setLocale(locale);

			mav.addObject("interfaceDescriptor", descriptor);
			mav.addObject("bugTrackerStatus", status);

			List<DecoratedIssueOwnership> decoratedIssues = Collections.emptyList();
			if (status.equals(BugTrackerStatus.BUGTRACKER_READY)) {
				try {
					List<IssueOwnership<RemoteIssueDecorator>> issuesOwnerShipList = Collections.emptyList();
					issuesOwnerShipList = bugTrackersLocalService.findIssueOwnershipForTestCase(testCaseId);
					decoratedIssues = new ArrayList<TestCaseModificationController.DecoratedIssueOwnership>(
							issuesOwnerShipList.size());
					for (IssueOwnership<RemoteIssueDecorator> ownerShip : issuesOwnerShipList) {
						decoratedIssues.add(new DecoratedIssueOwnership(ownerShip, locale));
					}

				}
				// no credentials exception are okay, the rest is to be treated as usual
				catch (BugTrackerNoCredentialsException noCrdsException) {
				} catch (NullArgumentException npException) {
				}
			}
			mav.addObject("issuesOwnerShipList", decoratedIssues);

		}

		mav.addObject(TEST_CASE, testCase);

		// =================CUFS
		List<CustomFieldValue> customFieldValues = cufHelperService.newHelper(testCase).getCustomFieldValues();
		mav.addObject("testCaseCufValues", customFieldValues);

		// ================= EXECUTIONS
		Paging paging = createSinglePagePaging();
		List<Execution> executions = executionFinder.findAllByTestCaseIdOrderByRunDate(testCaseId, paging);
		mav.addObject("execs", executions);

		// =================STEPS
		// FIXME loads all steps colletion, fetch paged sublist instead
		// FIXME souldn't we load all the steps BTW ?
		List<TestStep> steps = testCase.getSteps().subList(0, Math.min(10, testCase.getSteps().size()));

		// the custom fields definitions
		CustomFieldHelper<ActionTestStep> helper = cufHelperService.newStepsHelper(steps, testCase.getProject())
				.setRenderingLocations(RenderingLocation.STEP_TABLE).restrictToCommonFields();

		List<CustomFieldModel> cufDefinitions = convertToJsonCustomField(helper.getCustomFieldConfiguration());
		List<CustomFieldValue> stepCufValues = helper.getCustomFieldValues();

		TestStepsTableModelBuilder builder = new TestStepsTableModelBuilder(internationalizationHelper, locale);
		builder.setCurrentIndex(1l);
		builder.usingCustomFields(stepCufValues, cufDefinitions.size());
		Collection<Object> stepsData = builder.buildRawModel(steps);
		mav.addObject("stepsData", stepsData);
		mav.addObject("cufDefinitions", cufDefinitions);

		// ================PARAMETERS
		List<Parameter> parameters = parameterFinder.findAllforTestCase(testCaseId);
		Collections.sort(parameters, new ParameterNameComparator(SortOrder.ASCENDING));

		ParametersDataTableModelHelper paramHelper = new ParametersDataTableModelHelper(testCaseId, messageSource,
				locale);
		Collection<Object> parameterDatas = paramHelper.buildRawModel(parameters);
		mav.addObject("paramDatas", parameterDatas);

		// ================DATASETS
		Map<String, String> paramHeadersByParamId = TestCaseDatasetsController.findDatasetParamHeadersByParamId(
				testCaseId, locale, parameters, messageSource);
		List<Object[]> datasetsparamValuesById = getParamValuesById(testCase.getDatasets());
		mav.addObject("paramIds", IdentifiedUtil.extractIds(parameters));
		mav.addObject("paramHeadersById", paramHeadersByParamId);
		mav.addObject("datasetsparamValuesById", datasetsparamValuesById);

		// =====================CALLING TC
		List<TestCase> callingTCs = testCaseModificationService.findAllCallingTestCases(testCaseId);
		mav.addObject("callingTCs", callingTCs);

		// ========================VERIFIED REQUIREMENTS
		List<VerifiedRequirement> verifReq = verifiedRequirementsManagerService
				.findAllVerifiedRequirementsByTestCaseId(testCaseId);
		mav.addObject("verifiedRequirements", verifReq);

		return mav;
	}

	/**
	 * Return a list of dataset's organized infos as an object with : <br>
	 * object[0] = dataset's name object[1] = the dataset's paramValues as a map, mapping the paramValue.parameter.id to
	 * the paramValue.value
	 * 
	 * @param datasets
	 * @return a list of Object[] with each object representing a dataset's information
	 */
	private List<Object[]> getParamValuesById(Set<Dataset> datasets) {
		List<Object[]> result = new ArrayList<Object[]>(datasets.size());

		for (Dataset dataset : datasets) {
			Set<DatasetParamValue> datasetParamValues = dataset.getParameterValues();
			Map<String, String> datasetParamValuesById = new HashMap<String, String>(datasetParamValues.size());

			for (DatasetParamValue datasetParamValue : datasetParamValues) {
				datasetParamValuesById.put(datasetParamValue.getParameter().getId().toString(),
						datasetParamValue.getParamValue());
			}
			String datasetName = dataset.getName();
			Object[] datasetView = new Object[2];
			datasetView[0] = datasetName;
			datasetView[1] = datasetParamValuesById;
			result.add(datasetView);
		}
		return result;
	}

	/**
	 * Creates a paging which shows all entries on a single page.
	 * 
	 * @return
	 */
	private Paging createSinglePagePaging() {
		return new Paging() {

			@Override
			public boolean shouldDisplayAll() {
				return true;
			}

			@Override
			public int getPageSize() {
				return 0;
			}

			@Override
			public int getFirstItemIndex() {
				return 0;
			}
		};
	}

	public class DecoratedIssueOwnership {
		private IssueOwnership<RemoteIssueDecorator> ownership;
		private String ownerDesc;

		public DecoratedIssueOwnership(IssueOwnership<RemoteIssueDecorator> ownership, Locale locale) {
			this.ownership = ownership;
			this.ownerDesc = BugTrackerControllerHelper.findOwnerDescForTestCase(ownership.getOwner(), messageSource,
					locale);
		}

		public String getOwnerDesc() {
			return ownerDesc;
		}

		public IssueOwnership<RemoteIssueDecorator> getOwnership() {
			return ownership;
		}

	}

	private List<CustomFieldModel> convertToJsonCustomField(Collection<CustomField> customFields) {
		List<CustomFieldModel> models = new ArrayList<CustomFieldModel>(customFields.size());
		for (CustomField field : customFields) {
			models.add(converter.toJson(field));
		}
		return models;
	}

}
