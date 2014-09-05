/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.web.internal.controller.testcase;

import java.util.TimeZone;

import javax.inject.Provider
import javax.servlet.http.HttpServletRequest

import org.springframework.web.servlet.ModelAndView
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder
import org.squashtest.tm.core.foundation.collection.PagingAndSorting
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.testcase.ActionTestStep
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseImportance
import org.squashtest.tm.service.customfield.CustomFieldHelperService
import org.squashtest.tm.service.testcase.TestCaseModificationService
import org.squashtest.tm.web.internal.controller.generic.ServiceAwareAttachmentTableModelHelper
import org.squashtest.tm.web.internal.helper.InternationalizableLabelFormatter;
import org.squashtest.tm.web.internal.helper.LevelLabelFormatter
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters
import org.squashtest.tm.web.internal.model.datatable.DataTableModel
import org.squashtest.tm.web.internal.model.json.JsonGeneralInfo;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper

import spock.lang.Specification

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

	TestCaseStatusJeditableComboDataBuilder statusComboBuilder = Mock()
	Provider<TestCaseStatusJeditableComboDataBuilder> statusComboBuilderProvider = Mock()

	LevelLabelFormatter levelLabelFormatter = Mock()
	Provider<LevelLabelFormatter> levelLabelFormatterProvider = Mock()

	InternationalizableLabelFormatter levelLabelFormatterWithoutOrder = Mock()
	Provider<InternationalizableLabelFormatter> levelLabelFormatterWithoutOrderProvider = Mock()
	ServiceAwareAttachmentTableModelHelper attachmHelper = Mock()

	CustomFieldHelperService cufHelperService = Mock()

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

		setupStatusComboBuilder()
		controller.statusComboBuilderProvider = statusComboBuilderProvider

		setupLevelLabelFormatter()
		controller.levelLabelFormatterProvider = levelLabelFormatterProvider


		setupLevelLabelFormatterWithoutOrder()
		controller.labelFormatter = levelLabelFormatterWithoutOrderProvider

		controller.cufHelperService = cufHelperService

		controller.attachmentHelper = attachmHelper;

		mockCallingTestCaseService()
	}

	def mockCallingTestCaseService(){
		PagedCollectionHolder holder = Mock()
		holder.getFirstItemIndex() >> 0
		holder.getPagedItems() >> []
		holder.getTotalNumberOfItems() >> 0
		testCaseModificationService.findCallingTestSteps(_,_) >> holder
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

	def setupStatusComboBuilder() {
		statusComboBuilder.useLocale(_) >> statusComboBuilder
		statusComboBuilder.selectItem(_) >> statusComboBuilder

		statusComboBuilderProvider.get() >> statusComboBuilder
	}

	def setupLevelLabelFormatter() {
		levelLabelFormatter.useLocale(_) >> levelLabelFormatter

		levelLabelFormatterProvider.get() >> levelLabelFormatter
	}

	def setupLevelLabelFormatterWithoutOrder() {
		levelLabelFormatterWithoutOrder.useLocale(_) >> levelLabelFormatterWithoutOrder

		levelLabelFormatterWithoutOrderProvider.get() >> levelLabelFormatterWithoutOrder
	}

	def setupAttachmentHelper(){
		attachmHelper.findPagedAttachments(_) >> Mock(DataTableModel)
	}


	def "should return test case page fragment"() {
		given:
		TestCase tc = Mock()
		tc.getId() >> 15l
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



	def "should adapt table draw parameters to pagingandsorting"() {
		given:
		DataTableDrawParameters	params = Mock()
		params.getiDisplayLength() >> 10
		params.getiDisplayStart() >> 5
		params.getsSortedAttribute_0() >> 2
		params.getsSortDir_0() >> "asc"

		DatatableMapper dtMapper = Mock()
		dtMapper.getMapping(2) >> "name"

		when:
		PagingAndSorting filter = controller.createPaging(params,dtMapper)

		then:
		filter.firstItemIndex == 5
		filter.pageSize == 10
		filter.sortedAttribute == "name"
		filter.sortOrder.code == "asc"
	}


	def "should return general info fragment"() {
		given:
		TestCase testCase = Mock()
		AuditableMixin mixin = (AuditableMixin) testCase
		mixin.getCreatedOn() >> new Date(1385488000402);
		mixin.getCreatedBy() >> "robert"
		testCaseModificationService.findById(10) >> testCase

		and:
		def oldTz = TimeZone.getDefault();
		TimeZone.setDefault(new SimpleTimeZone(0, "GMT"));

		when:
		JsonGeneralInfo infos = controller.refreshGeneralInfos(10)

		then:
		infos.createdOn == "2013-11-26T17:46:40.402+0000"
		infos.createdBy == "robert"
		infos.modifiedOn == null
		infos.modifiedBy == null

		cleanup:
		TimeZone.setDefault(oldTz);
	}

	def "when showing a test case, should put importance data in the model"() {
		given:
		TestCase testCase = Mock()
		testCase.getId() >> 10l
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
		testCase.getId() >> 10l
		testCase.importance >> TestCaseImportance.HIGH
		testCaseModificationService.findTestCaseWithSteps(10) >> testCase

		and:
		levelLabelFormatter.formatLabel(TestCaseImportance.HIGH) >> "takai"

		when:
		ModelAndView mav = controller.showTestCaseInfo(10, Locale.JAPANESE)

		then:
		2 * levelLabelFormatter.useLocale(Locale.JAPANESE) >> levelLabelFormatter
		mav.modelMap['testCaseImportanceLabel'] == "takai"
	}

}
