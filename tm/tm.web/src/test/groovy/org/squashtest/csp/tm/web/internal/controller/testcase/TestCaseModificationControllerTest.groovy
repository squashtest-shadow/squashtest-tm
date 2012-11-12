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
package org.squashtest.csp.tm.web.internal.controller.testcase;

import org.springframework.context.MessageSource;
import org.springframework.web.servlet.ModelAndView;

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;

import org.squashtest.csp.tm.service.TestCaseModificationService;
import org.squashtest.csp.tm.web.internal.helper.LevelLabelFormatter;
import org.squashtest.csp.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModelHelper;
import org.squashtest.csp.tm.web.internal.model.viewmapper.DataTableMapper;
import org.squashtest.csp.tm.domain.attachment.AttachmentList;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.testcase.TestCase
import org.squashtest.csp.tm.domain.testcase.TestCaseImportance;
import org.squashtest.csp.tm.domain.testcase.TestStep;

import spock.lang.Specification;
import org.squashtest.csp.tm.domain.testcase.ActionTestStep;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.csp.tools.unittest.reflection.ReflectionCategory;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;

class TestCaseModificationControllerTest extends Specification {
	TestCaseModificationController controller = new TestCaseModificationController()
	
	TestCaseModificationService testCaseModificationService = Mock()
	HttpServletRequest request = Mock()
	InternationalizationHelper messageSource = Mock()
	
	TestCaseImportanceJeditableComboDataBuilder importanceComboBuilder = Mock()
	Provider<TestCaseImportanceJeditableComboDataBuilder> importanceComboBuilderProvider = Mock()

	TestCaseNatureJeditableComboDataBuilder natureComboBuilder = Mock()
	Provider<TestCaseNatureJeditableComboDataBuilder> natureComboBuilderProvider = Mock()
	
	TestCaseTypeJeditableComboDataBuilder typeComboBuilder = Mock()
	Provider<TestCaseTypeJeditableComboDataBuilder> typeComboBuilderProvider = Mock()
	
	LevelLabelFormatter levelLabelFormatter = Mock()
	Provider<LevelLabelFormatter> levelLabelFormatterProvider = Mock()

		def setup() {
		controller.testCaseModificationService = testCaseModificationService
		request.getCharacterEncoding() >> "ISO-8859-1"
		controller.internationalizationHelper = messageSource

		setupImportanceComboBuilder()
		controller.importanceComboBuilderProvider = importanceComboBuilderProvider

		setupNatureComboBuilder()
		controller.natureComboBuilderProvider = natureComboBuilderProvider

		setupTypeComboBuilder()
		controller.typeComboBuilderProvider = typeComboBuilderProvider
		
		setupLevelLabelFormatter()		
		controller.levelLabelFormatterProvider = levelLabelFormatterProvider
	
	}

	def setupImportanceComboBuilder() {
		importanceComboBuilder.useLocale(_) >> importanceComboBuilder
		importanceComboBuilder.selectItem(_) >> importanceComboBuilder

		importanceComboBuilderProvider.get() >> importanceComboBuilder
	}

	def setupNatureComboBuilder() {
		natureComboBuilder.useLocale(_) >> natureComboBuilder
		natureComboBuilder.selectItem(_) >> natureComboBuilder

		natureComboBuilderProvider.get() >> natureComboBuilder
	}

	def setupTypeComboBuilder() {
		typeComboBuilder.useLocale(_) >> typeComboBuilder
		typeComboBuilder.selectItem(_) >> typeComboBuilder

		typeComboBuilderProvider.get() >> typeComboBuilder
	}
	
	def setupLevelLabelFormatter() {
		levelLabelFormatter.useLocale(_) >> levelLabelFormatter

		levelLabelFormatterProvider.get() >> levelLabelFormatter
	}

		def "should build table model for test case steps"() {
		given:
		AttachmentList al = Mock()
		al.size() >> 1
		al.getId() >> 5

		and:
		ActionTestStep step1 = new ActionTestStep(action: "a1", expectedResult: "r1")
		use(ReflectionCategory) {
			TestStep.set field: "id", of: step1, to: 1L
			ActionTestStep.set field: "attachmentList", of: step1, to: al
		}

		and:
		ActionTestStep step2 = new ActionTestStep(action: "a2", expectedResult: "r2")
		use(ReflectionCategory) {
			TestStep.set field: "id", of: step2, to: 2L
			ActionTestStep.set field: "attachmentList", of: step2, to: al
		}


		and:
		FilteredCollectionHolder<List<ActionTestStep>> holder = new FilteredCollectionHolder<List<ActionTestStep>>(2, [step1, step2])
		testCaseModificationService.findStepsByTestCaseIdFiltered(10, _) >> holder


		and:
		DataTableDrawParameters params = new DataTableDrawParameters();
		params.setiDisplayLength(10);
		params.setiDisplayStart(0)
		params.setsEcho("echo");


		when:
		def res = controller.getStepsTableModel(10, params, Locale.FRENCH)

		then:
		res.sEcho == "echo"
		res.aaData == [
			[ "", 1, 1, 5, "a1", "r1", "", "", 1, "action", null ],
			[ "", 2, 2, 5, "a2", "r2", "", "", 1, "action", null ]
		]
	}
	def "should change step index"() {
		given:


		when:
		controller.changeStepIndex(10, 1, 20)

		then:
		1 * testCaseModificationService.changeTestStepPosition(20, 10, 1)
	}

	def "should return test case page fragment"() {
		given:
		TestCase tc = Mock()
		List<ActionTestStep> steps = new ArrayList<ActionTestStep>();
		tc.getSteps() >> steps
		long tcId=15
		testCaseModificationService.findById(tcId) >> tc

		when:
		ModelAndView res = controller.showTestCase (tcId, null)

		then:
		res.viewName == "fragment/test-cases/edit-test-case"
		res.modelMap['testCase'] == tc
	}

	def "should build table model for verified requirements"() {
		given:
		DataTableDrawParameters request = new DataTableDrawParameters(sEcho: "echo", iDisplayStart: 0, iDisplayLength: 100)

		and:
		PagedCollectionHolder holder = Mock()
		holder.pagedItems >> []
		testCaseModificationService.findAllVerifiedRequirementsByTestCaseId(10, _) >> holder

		when:
		def res = controller.getAllVerifiedRequirementsTableModel(10, request, Locale.getDefault())

		then:
		res.sEcho == "echo"
		res.iTotalDisplayRecords == 0
		res.iTotalRecords == 0
	}

	def "should build verified requirements model from 1 row of 5"() {
		given:
		Requirement req = Mock()
		req.name >> "foo"
		req.id >> 15

		Project project = Mock()
		req.project >> project
		project.name >> "bar"

		and:
		FilteredCollectionHolder<List<Requirement>> holder = Mock()
		holder.filteredCollection >> [req]
		holder.unfilteredResultCount >> 5



		when:

		//well, groovy

		def helper = [
			buildItemData: { item ->
				[
					item.getId(),
					1,
					item.getProject().getName(),
					item.getName(),
					"" ] as Object[];
			}

		] as DataTableModelHelper<Requirement>;


		def res = helper.buildDataModel(holder, 1,"echo");

		then:
		res.sEcho == "echo"
		res.iTotalDisplayRecords == 5
		res.iTotalRecords == 5
		res.aaData == [
			[
				15,
				1,
				"bar",
				"foo",
				""
			]
		]
	}

	def "should adapt table draw parameters to collection filter"() {
		given:
		DataTableDrawParameters	params = Mock()
		params.getiDisplayLength() >> 10
		params.getiDisplayStart() >> 5
		params.getiSortCol_0() >> 2
		params.getsSortDir_0() >> "asc"

		DataTableMapper dtMapper = Mock()
		dtMapper.pathAt(2) >> "name"

		when:
		def filter = controller.createPaging(params,dtMapper)

		then:
		filter.firstItemIndex == 5
		filter.pageSize == 10
		filter.sortedAttribute == "name"
		filter.sortingOrder == "asc"
	}

	def "should return general info fragment"() {
		given:
		TestCase testCase = Mock()
		testCaseModificationService.findById(10) >> testCase

		when:
		ModelAndView mav = controller.refreshGeneralInfos(10)

		then:
		mav.viewName == "fragment/generics/general-information-fragment"
		mav.modelMap['entityContextUrl'] == "/test-cases/10"
		mav.modelMap['auditableEntity'] == testCase
	}

	def "when showing a test case, should put importance data in the model"() {
		given:
		TestCase testCase = Mock()
		testCase.importance >> TestCaseImportance.HIGH
		testCaseModificationService.findTestCaseWithSteps(10) >> testCase
		
		and:
		importanceComboBuilder.buildMarshalled() >> "akemashite omedet�"

		when:
		ModelAndView mav = controller.showTestCaseInfo(10, Locale.JAPANESE)

		then:
		1 * importanceComboBuilder.useLocale(Locale.JAPANESE) >> importanceComboBuilder
		0 * importanceComboBuilder.selectItem(TestCaseImportance.HIGH) >> importanceComboBuilder
		mav.modelMap['testCaseImportanceComboJson'] == "akemashite omedet�"
	}
	
	def "when showing a test case, should put test case importance label in the model"() {
		given:
		TestCase testCase = Mock()
		testCase.importance >> TestCaseImportance.HIGH
		testCaseModificationService.findTestCaseWithSteps(10) >> testCase
		
		and:
		levelLabelFormatter.formatLabel(TestCaseImportance.HIGH) >> "takai"

		when:
		ModelAndView mav = controller.showTestCaseInfo(10, Locale.JAPANESE)

		then:
		3 * levelLabelFormatter.useLocale(Locale.JAPANESE) >> levelLabelFormatter
		mav.modelMap['testCaseImportanceLabel'] == "takai"
	}

}
