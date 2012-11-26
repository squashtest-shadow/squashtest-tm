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
package org.squashtest.csp.tm.web.internal.controller.requirement



import java.util.Locale

import javax.inject.Provider

import org.springframework.context.MessageSource
import org.springframework.ui.ExtendedModelMap
import org.springframework.ui.Model
import org.springframework.web.servlet.ModelAndView
import org.squashtest.csp.tm.domain.requirement.Requirement
import org.squashtest.csp.tm.domain.requirement.RequirementCategory
import org.squashtest.csp.tm.domain.requirement.RequirementCriticality
import org.squashtest.csp.tm.domain.requirement.RequirementStatus
import org.squashtest.csp.tm.domain.requirement.RequirementVersion
import org.squashtest.csp.tm.service.RequirementModificationService
import org.squashtest.csp.tm.service.customfield.CustomFieldValueFinderService;
import org.squashtest.csp.tm.web.internal.helper.InternationalisableLabelFormatter;
import org.squashtest.csp.tm.web.internal.helper.LabelFormatter
import org.squashtest.csp.tm.web.internal.helper.LevelLabelFormatter

import spock.lang.Specification


class RequirementModificationControllerTest extends Specification {
	RequirementModificationController controller = new RequirementModificationController()
	RequirementModificationService requirementModificationService= Mock()
	MessageSource messageSource = Mock()
	LabelFormatter formatter = new LevelLabelFormatter(messageSource)
	LabelFormatter internationalformatter = new InternationalisableLabelFormatter(messageSource)
	Provider criticalityBuilderProvider = criticalityBuilderProvider()
	Provider categoryBuilderProvider = categoryBuilderProvider()
	Provider statusBuilderProvider = statusBuilderProvider()
	Provider levelFormatterProvider = levelFormatterProvider()	
	Provider internationalFormatterProvider = internationalFormatterProvider()

	def setup() {
		controller.requirementModService = requirementModificationService
		controller.criticalityComboBuilderProvider = criticalityBuilderProvider
		controller.categoryComboBuilderProvider = categoryBuilderProvider
		controller.statusComboDataBuilderProvider = statusBuilderProvider
		controller.levelFormatterProvider = levelFormatterProvider
		controller.internationalizableFormatterProvider = internationalFormatterProvider
		controller.cufValueService = Mock(CustomFieldValueFinderService)
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
		Requirement req = Mock(Requirement.class)
		req.getCriticality() >> RequirementCriticality.UNDEFINED
		req.getStatus() >> RequirementStatus.WORK_IN_PROGRESS
		req.getCategory() >> RequirementCategory.UNDEFINED
		long reqId=15
		requirementModificationService.findById(15) >> req

		when:
		ModelAndView res = controller.showRequirement(reqId, null)

		then:
		res.viewName == "fragment/requirements/edit-requirement"
		res.modelMap['requirement'] == req
	}

	def "should ask to create new version"() {
		when:
		controller.createNewVersion(10L)

		then:
		requirementModificationService.createNewVersion(10L)
	}

	def "should return versions manager view"() {
		given:
		Requirement req = Mock()
		requirementModificationService.findById(0) >> req
		
		when:
		String viewName = controller.showRequirementVersionsManager(0, Mock(Model), Locale.JAPANESE)

		then:
		viewName == "page/requirements/versions-manager"
	}

	def "should populate versions manager model"() {
		given:
		Requirement req = Mock()
		req.unmodifiableVersions >> []
		req.currentVersion >> Mock(RequirementVersion)
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
}
