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
package org.squashtest.csp.tm.web.internal.controller.testcase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.core.infrastructure.collection.PagedCollectionHolder;
import org.squashtest.csp.core.infrastructure.collection.PagingAndSorting;
import org.squashtest.csp.tm.domain.Internationalizable;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.requirement.RequirementCriticality;
import org.squashtest.csp.tm.domain.requirement.RequirementVersion;
import org.squashtest.csp.tm.domain.testcase.ActionTestStep;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseExecutionMode;
import org.squashtest.csp.tm.domain.testcase.TestCaseImportance;
import org.squashtest.csp.tm.domain.testcase.TestStep;
import org.squashtest.csp.tm.infrastructure.filter.CollectionFilter;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.csp.tm.service.CallStepManagerService;
import org.squashtest.csp.tm.service.TestCaseModificationService;
import org.squashtest.csp.tm.service.VerifiedRequirement;
import org.squashtest.csp.tm.web.internal.combo.OptionTag;
import org.squashtest.csp.tm.web.internal.helper.LevelLabelFormatter;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableMapperCollectionSortingAdapter;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableMapperPagingAndSortingAdapter;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModelHelper;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTablePagedFilter;
import org.squashtest.csp.tm.web.internal.model.viewmapper.DataTableMapper;

@Controller
@RequestMapping("/test-cases/{testCaseId}")
public class TestCaseModificationController {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestCaseModificationController.class);

	private final DataTableMapper verifiedReqMapper = new DataTableMapper("verified-requirement",
			RequirementVersion.class, Project.class).initMapping(7)
			.mapAttribute(Project.class, 2, "name", String.class)
			.mapAttribute(RequirementVersion.class, 3, "reference", String.class)
			.mapAttribute(RequirementVersion.class, 4, "name", String.class)
			.mapAttribute(RequirementVersion.class, 5, "criticality", RequirementCriticality.class);

	private final DataTableMapper referencingTestCaseMapper = new DataTableMapper("referencing-test-cases",
			TestCase.class, Project.class).initMapping(5).mapAttribute(Project.class, 2, "name", String.class)
			.mapAttribute(TestCase.class, 3, "name", String.class)
			.mapAttribute(TestCase.class, 4, "executionMode", TestCaseExecutionMode.class);

	private TestCaseModificationService testCaseModificationService;

	@Inject
	private MessageSource messageSource;

	@Inject
	private Provider<TestCaseImportanceJeditableComboDataBuilder> importanceComboBuilderProvider;

	@Inject
	private Provider<LevelLabelFormatter> importanceLabelFormatterProvider;

	@Inject
	private CallStepManagerService callStepManager;

	@ServiceReference
	public void setTestCaseModificationService(TestCaseModificationService testCaseModificationService) {
		this.testCaseModificationService = testCaseModificationService;
	}

	@RequestMapping(method = RequestMethod.GET)
	public final ModelAndView showTestCase(@PathVariable long testCaseId,
			@RequestParam(required = false, value = "edit-mode") Boolean editable, Locale locale) {
		ModelAndView mav = new ModelAndView("fragment/test-cases/edit-test-case");

		TestCase testCase = testCaseModificationService.findById(testCaseId);
		populateModelWithTestCaseEditionData(mav, testCase, locale);

		if (editable != null) {
			mav.addObject("editable", editable);
		}

		return mav;
	}

	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public ModelAndView showTestCaseInfo(@PathVariable long testCaseId, Locale locale) {

		LOGGER.trace("TestCaseModificationController : getting infos");

		ModelAndView mav = new ModelAndView("page/test-case-libraries/show-test-case");

		TestCase testCase = testCaseModificationService.findTestCaseWithSteps(testCaseId);
		populateModelWithTestCaseEditionData(mav, testCase, locale);

		return mav;
	}

	private void populateModelWithTestCaseEditionData(ModelAndView mav, TestCase testCase, Locale locale) {
		// Convert execution mode with local parameter
		List<OptionTag> executionModes = new ArrayList<OptionTag>();
		for (TestCaseExecutionMode executionMode : TestCaseExecutionMode.values()) {
			OptionTag ot = new OptionTag();
			ot.setLabel(formatExecutionMode(executionMode, locale));
			ot.setValue(executionMode.toString());
			executionModes.add(ot);
		}
		mav.addObject("testCase", testCase);
		mav.addObject("executionModes", executionModes);
		mav.addObject("testCaseImportanceComboJson", buildImportanceComboData(testCase, locale));
		mav.addObject("testCaseImportanceLabel", formatImportance(testCase.getImportance(), locale));

	}

	private String buildImportanceComboData(TestCase testCase, Locale locale) {
		return importanceComboBuilderProvider.get().useLocale(locale).buildMarshalled();
	}

	private String formatExecutionMode(TestCaseExecutionMode mode, Locale locale) {
		return internationalize(mode, locale);
	}

	@RequestMapping(value = "/steps-table", params = "sEcho")
	@ResponseBody
	public DataTableModel getStepsTableModel(@PathVariable long testCaseId, DataTableDrawParameters params,
			Locale locale) {

		LOGGER.trace("TestCaseModificationController: getStepsTableModel called ");

		CollectionFilter filter = createCollectionFilter(params);

		FilteredCollectionHolder<List<TestStep>> holder = testCaseModificationService.findStepsByTestCaseIdFiltered(
				testCaseId, filter);

		return new TestStepsTableModelBuilder(messageSource, locale).buildDataModel(holder,
				filter.getFirstItemIndex() + 1, params.getsEcho());

	}

	@RequestMapping(method = RequestMethod.POST, params = "executionMode")
	@ResponseBody
	public void changeExecutionMode(@RequestParam String executionMode, @PathVariable long testCaseId) {
		TestCaseExecutionMode mode = TestCaseExecutionMode.valueOf(executionMode);
		testCaseModificationService.changeExecutionMode(testCaseId, mode);
		LOGGER.trace("test case {} : execution mode changed, new mode is {}", testCaseId, mode.name());
	}

	@RequestMapping(value = "/steps/add", method = RequestMethod.POST, params = { "action", "expectedResult" })
	@ResponseBody
	public void addActionTestStep(@ModelAttribute("add-test-step") @Valid ActionTestStep step,
			@PathVariable long testCaseId) {

		testCaseModificationService.addActionTestStep(testCaseId, step);

		LOGGER.trace("test case " + testCaseId + ": step added, action : " + step.getAction() + ", expected result : "
				+ step.getExpectedResult());
	}

	@RequestMapping(value = "/steps/paste", method = RequestMethod.POST, params = { "copiedStepId[]" })
	@ResponseBody
	public void pasteStep(@RequestParam("copiedStepId[]") String[] copiedStepId,
			@RequestParam(value = "indexToCopy", required = false) Long positionId, @PathVariable long testCaseId) {

		callStepManager.checkForCyclicStepCallBeforePaste(testCaseId, copiedStepId);

		for (int i = copiedStepId.length - 1; i >= 0; i--) {
			String id = copiedStepId[i];
			testCaseModificationService.pasteCopiedTestStep(testCaseId, positionId, Long.parseLong(id));
		}
		LOGGER.trace("test case copied some Steps");
	}

	@RequestMapping(value = "/steps/{stepId}", method = RequestMethod.POST, params = "newIndex")
	@ResponseBody
	public void changeStepIndex(@PathVariable long stepId, @RequestParam int newIndex, @PathVariable long testCaseId) {

		testCaseModificationService.changeTestStepPosition(testCaseId, stepId, newIndex);
		LOGGER.trace("test case " + testCaseId + ": step " + stepId + " moved to " + newIndex);

	}

	@RequestMapping(value = "/steps/move", method = RequestMethod.POST, params = { "newIndex", "stepIds[]" })
	@ResponseBody
	public void changeStepsIndex(@RequestParam("stepIds[]") List<Long> stepIds, @RequestParam int newIndex,
			@PathVariable long testCaseId) {

		testCaseModificationService.changeTestStepsPosition(testCaseId, newIndex, stepIds);

	}

	@RequestMapping(value = "/steps/{stepId}", method = RequestMethod.DELETE)
	@ResponseBody
	public void deleteStep(@PathVariable long stepId, @PathVariable long testCaseId) {

		testCaseModificationService.removeStepFromTestCase(testCaseId, stepId);
		LOGGER.trace("test case " + testCaseId + ": removed step " + stepId);
	}

	@RequestMapping(value = "/steps/{stepId}/action", method = RequestMethod.POST, params = { "id", "value" })
	@ResponseBody
	public String changeStepAction(@PathVariable long stepId, @RequestParam("value") String newAction) {
		testCaseModificationService.updateTestStepAction(stepId, newAction);
		LOGGER.trace("TestCaseModificationController : updated action for step {}", stepId);
		return newAction;
	}

	@RequestMapping(value = "/steps/{stepId}/result", method = RequestMethod.POST, params = { "id", "value" })
	@ResponseBody
	public String changeStepDescription(@PathVariable long stepId, @RequestParam("value") String newResult) {
		testCaseModificationService.updateTestStepExpectedResult(stepId, newResult);
		LOGGER.trace("TestCaseModificationController : updated action for step {}", stepId);
		return newResult;
	}

	@RequestMapping(value = "/removed-steps", params = "removedStepIds[]", method = RequestMethod.POST)
	@ResponseBody
	public void deleteListStep(@PathVariable long testCaseId,
			@RequestParam("removedStepIds[]") List<Long> removedStepIds) {
		testCaseModificationService.removeListOfSteps(testCaseId, removedStepIds);
		LOGGER.trace("TestCaseModificationController : removed a list of steps");
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=test-case-description", "value" })
	@ResponseBody
	public String changeDescription(@RequestParam("value") String testCaseDescription, @PathVariable long testCaseId) {

		testCaseModificationService.changeDescription(testCaseId, testCaseDescription);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("test case " + testCaseId + ": updated description to " + testCaseDescription);
		}

		return testCaseDescription;
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, params = { "id=test-case-importance", "value" })
	public String changeImportance(@PathVariable long testCaseId, @RequestParam("value") TestCaseImportance importance,
			Locale locale) {
		testCaseModificationService.changeImportance(testCaseId, importance);

		return formatImportance(importance, locale);
	}

	@RequestMapping(value = "/importanceAuto", method = RequestMethod.POST, params = { "importanceAuto" })
	@ResponseBody
	public String changeImportanceAuto(@PathVariable long testCaseId,
			@RequestParam(value = "importanceAuto") boolean auto, Locale locale) {
		testCaseModificationService.changeImportanceAuto(testCaseId, auto);
		TestCase testCase = testCaseModificationService.findById(testCaseId);
		return formatImportance(testCase.getImportance(), locale);
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=test-case-prerequisite", "value" })
	@ResponseBody
	public String changePrerequisite(@RequestParam("value") String testCasePrerequisite, @PathVariable long testCaseId) {

		testCaseModificationService.changePrerequisite(testCaseId, testCasePrerequisite);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("test case " + testCaseId + ": updated prerequisite to " + testCasePrerequisite);
		}

		return testCasePrerequisite;
	}

	@RequestMapping(method = RequestMethod.POST, params = { "newName" })
	@ResponseBody
	public Object rename(HttpServletResponse response, @PathVariable long testCaseId, @RequestParam String newName) {

		testCaseModificationService.rename(testCaseId, newName);
		LOGGER.info("TestCaseModificationController : renaming {} as {}", testCaseId, newName);
		final String reNewName = newName;
		return new Object() {
			public String newName = reNewName; // NOSONAR unreadable field actually read by JSON marshaller.
		};

	}

	@ResponseBody
	@RequestMapping(value = "/importance", method = RequestMethod.GET)
	public String getImportance(@PathVariable long testCaseId, Locale locale) {
		TestCase testCase = testCaseModificationService.findById(testCaseId);
		TestCaseImportance importance = testCase.getImportance();
		return formatImportance(importance, locale);
	}

	private String formatImportance(TestCaseImportance importance, Locale locale) {
		return importanceLabelFormatterProvider.get().useLocale(locale).formatLabel(importance);
	}

	@RequestMapping(value = "/general", method = RequestMethod.GET)
	public ModelAndView refreshGeneralInfos(@PathVariable long testCaseId) {

		ModelAndView mav = new ModelAndView("fragment/generics/general-information-fragment");

		TestCase testCase = testCaseModificationService.findById(testCaseId);

		if (testCase == null) {
			testCase = createNotFoundTestCase();
		}
		mav.addObject("auditableEntity", testCase);
		// context-absolute url of this entity
		mav.addObject("entityContextUrl", "/test-cases/" + testCaseId);

		return mav;
	}

	// FIXME : a not found test case is an exception, now that we have a decent Exception manager we should remove that
	// workaround.
	@Deprecated
	private TestCase createNotFoundTestCase() {
		TestCase testCase;
		testCase = new TestCase();
		testCase.setName("NotFound");
		testCase.setDescription("This requirement either do not exists, or was removed");
		return testCase;
	}

	@RequestMapping(value = "/all-verified-requirements-table", params = "sEcho")
	@ResponseBody
	public DataTableModel getAllVerifiedRequirementsTableModel(@PathVariable long testCaseId,
			final DataTableDrawParameters params, final Locale locale) {

		PagingAndSorting pas = createPagingAndSorting(params, verifiedReqMapper);

		PagedCollectionHolder<List<VerifiedRequirement>> holder = testCaseModificationService
				.findAllVerifiedRequirementsByTestCaseId(testCaseId, pas);

		return new DataTableModelHelper<VerifiedRequirement>() {
			@Override
			public Object[] buildItemData(VerifiedRequirement item) {
				return new Object[] { item.getId(), getCurrentIndex(), item.getProject().getName(),
						item.getReference(), item.getName(), item.getDecoratedRequirement().getVersionNumber(), internationalize(item.getCriticality(), locale), "",
						item.getDecoratedRequirement().getStatus().name(), item.isDirectVerification() };
			}
		}.buildDataModel(holder, params.getsEcho());

	}

	private PagingAndSorting createPagingAndSorting(DataTableDrawParameters params, DataTableMapper mapper) {
		return new DataTableMapperPagingAndSortingAdapter(params, mapper);
	}

	@RequestMapping(value = "/calling-test-case-table", params = "sEcho")
	@ResponseBody
	public DataTableModel getCallingTestCaseTableModel(@PathVariable long testCaseId, DataTableDrawParameters params,
			final Locale locale) {

		LOGGER.trace("TestCaseModificationController: getCallingTestCaseTableModel called ");

		CollectionSorting filter = createCollectionFilter(params, referencingTestCaseMapper);

		FilteredCollectionHolder<List<TestCase>> holder = testCaseModificationService.findCallingTestCases(testCaseId,
				filter);

		return new DataTableModelHelper<TestCase>() {
			@Override
			public Object[] buildItemData(TestCase item) {
				return new Object[] { item.getId(), getCurrentIndex(), item.getProject().getName(), item.getName(),
						internationalize(item.getExecutionMode(), locale) };
			}
		}.buildDataModel(holder, filter.getFirstItemIndex() + 1, params.getsEcho());

	}

	private CollectionSorting createCollectionFilter(final DataTableDrawParameters params,
			final DataTableMapper dtMapper) {
		return new DataTableMapperCollectionSortingAdapter(params, dtMapper);
	}

	private CollectionFilter createCollectionFilter(final DataTableDrawParameters params) {
		return new DataTablePagedFilter(params);
	}

	/* ********************************** localization stuffs ****************************** */

	private String internationalize(Internationalizable internationalizable, Locale locale) {
		return messageSource.getMessage(internationalizable.getI18nKey(), null, locale);
	}

}
