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
package org.squashtest.tm.web.internal.controller.requirement



import javax.inject.Provider

import org.springframework.context.MessageSource
import org.springframework.ui.ExtendedModelMap
import org.springframework.ui.Model
import org.springframework.web.servlet.ModelAndView
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.requirement.Requirement
import org.squashtest.tm.domain.requirement.RequirementCategory
import org.squashtest.tm.domain.requirement.RequirementCriticality
import org.squashtest.tm.domain.requirement.RequirementStatus
import org.squashtest.tm.domain.requirement.RequirementVersion
import org.squashtest.tm.service.audit.RequirementAuditTrailService;
import org.squashtest.tm.service.customfield.CustomFieldValueFinderService
import org.squashtest.tm.service.requirement.RequirementModificationService
import org.squashtest.tm.service.testcase.VerifyingTestCaseManagerService;
import org.squashtest.tm.web.internal.controller.generic.ServiceAwareAttachmentTableModelHelper;
import org.squashtest.tm.web.internal.helper.InternationalisableLabelFormatter
import org.squashtest.tm.web.internal.helper.LabelFormatter
import org.squashtest.tm.web.internal.helper.LevelLabelFormatter
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;

import spock.lang.Specification


class RequirementModificationControllerTest extends Specification {
	RequirementModificationController controller = new RequirementModificationController()
	RequirementModificationService requirementModificationService= Mock()
	InternationalizationHelper i18nHelper = Mock()
	LabelFormatter formatter = new LevelLabelFormatter(i18nHelper)
	LabelFormatter internationalformatter = new InternationalisableLabelFormatter(i18nHelper)
	Provider criticalityBuilderProvider = criticalityBuilderProvider()
	Provider categoryBuilderProvider = categoryBuilderProvider()
	Provider statusBuilderProvider = statusBuilderProvider()
	Provider levelFormatterProvider = levelFormatterProvider()	
	Provider internationalFormatterProvider = internationalFormatterProvider()
	VerifyingTestCaseManagerService verifTCService = Mock()
	ServiceAwareAttachmentTableModelHelper attachmentsHelper = Mock()
	RequirementAuditTrailService auditTrailService = Mock()

	def setup() {
		controller.requirementModService = requirementModificationService
		controller.criticalityComboBuilderProvider = criticalityBuilderProvider
		controller.categoryComboBuilderProvider = categoryBuilderProvider
		controller.statusComboDataBuilderProvider = statusBuilderProvider
		controller.levelFormatterProvider = levelFormatterProvider
		controller.internationalizableFormatterProvider = internationalFormatterProvider
		controller.cufValueService = Mock(CustomFieldValueFinderService)
		controller.verifyingTestCaseManager = verifTCService
		controller.attachmentsHelper = attachmentsHelper
		controller.auditTrailService = auditTrailService;
		
		mockAuditTrailService()
	}
	
	def mockAuditTrailService(){
		PagedCollectionHolder holder = Mock()
		holder.getFirstItemIndex() >> 0
		holder.getPagedItems() >> []
		holder.getTotalNumberOfItems() >> 0
		auditTrailService.findAllByRequirementVersionIdOrderedByDate(_,_)>> holder
	}

	def criticalityBuilderProvider() {
		RequirementCriticalityComboDataBuilder builder = new RequirementCriticalityComboDataBuilder()
		builder.labelFormatter = formatter

		Provider provider = Mock()
		provider.get() >> builder

		return provider
	}
	def categoryBuilderProvider() {
		RequirementCategoryComboDataBuilder builder = new RequirementCategoryComboDataBuilder()
		builder.labelFormatter = internationalformatter

		i18nHelper.internationalize(_, _) >> "--"
		builder.setInternationalizationHelper(i18nHelper)
		
		Provider provider = Mock()
		provider.get() >> builder

		return provider
	}

	def statusBuilderProvider() {
		RequirementStatusComboDataBuilder builder = new RequirementStatusComboDataBuilder()
		builder.labelFormatter = formatter
		
		Provider provider = Mock()
		provider.get() >> builder

		return provider
	}

	def levelFormatterProvider() {
		Provider provider = Mock()
		provider.get() >> formatter

		return provider
	}
	def internationalFormatterProvider(){
		Provider provider = Mock()
		provider.get() >> internationalformatter
		
		return provider	
	}

	def "should return requirement page fragment"() {
		given:
		Requirement req = mockRequirementAmongOtherThings()
		req.getCriticality() >> RequirementCriticality.UNDEFINED
		req.getStatus() >> RequirementStatus.WORK_IN_PROGRESS
		req.getCategory() >> RequirementCategory.UNDEFINED
		long reqId=15
		requirementModificationService.findById(15) >> req
		Model model = Mock()
		attachmentsHelper.findPagedAttachments(_) >> Mock(DataTableModel)

		when:
		String res = controller.showRequirement(model, reqId, null)

		then:
		res == "fragment/requirements/edit-requirement"
		1 * model.addAttribute('requirement', req)
	}

	def "should ask to create new version"() {
		when:
		controller.createNewVersion(10L)

		then:
		requirementModificationService.createNewVersion(10L)
	}

	def "should return versions manager view"() {
		given:
		Requirement req = mockRequirementAmongOtherThings()
		requirementModificationService.findById(0) >> req
		
		when:
		String viewName = controller.showRequirementVersionsManager(0, Mock(Model), Locale.JAPANESE)

		then:
		viewName == "page/requirements/versions-manager"
	}

	def "should populate versions manager model"() {
		given:
		Requirement req = mockRequirementAmongOtherThings()
		requirementModificationService.findById(0) >> req
		
		when:
		Model model = new ExtendedModelMap()
		String viewName = controller.showRequirementVersionsManager(0, model, Locale.JAPANESE)

		then:
		model.asMap()["requirement"] != null
		model.asMap()["versions"] != null
		model.asMap()["selectedVersion"] != null
		model.asMap()["jsonCriticalities"] != null
		model.asMap()["jsonCategories"] != null
	}
	
	def mockRequirementAmongOtherThings(){
		
		Requirement r = Mock()
		RequirementVersion v = Mock()
		r.getCurrentVersion() >> v
		v.getId() >> 0
		r.getUnmodifiableVersions() >> [v]
		
		PagedCollectionHolder<?> ch = Mock()
		ch.getFirstItemIndex() >> 0
		ch.getPagedItems() >> []
		
		verifTCService.findAllByRequirementVersion(_,_)>> ch
		
		return r
	}
}
