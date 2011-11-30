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

package org.squashtest.csp.tm.web.internal.controller.audittrail

import org.springframework.context.MessageSource;
import org.squashtest.csp.core.infrastructure.collection.PagedCollectionHolder
import org.squashtest.csp.tm.domain.event.RequirementAuditEvent;
import org.squashtest.csp.tm.domain.event.RequirementCreation
import org.squashtest.csp.tm.domain.event.RequirementLargePropertyChange;
import org.squashtest.csp.tm.domain.event.RequirementPropertyChange;
import org.squashtest.csp.tm.domain.requirement.Requirement
import org.squashtest.csp.tm.domain.requirement.RequirementStatus;
import org.squashtest.csp.tm.web.internal.helper.LabelFormatter;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.csp.tools.unittest.reflection.ReflectionCategory;

import spock.lang.Specification
/**
 * @author Gregory
 *
 */
class RequirementAuditEventTableModelBuilderTest extends Specification {
	MessageSource messageSource = Mock()
	Locale locale = Locale.JAPANESE
	LabelFormatter statusFormatter = Mock()
	RequirementAuditEventTableModelBuilder builder = new RequirementAuditEventTableModelBuilder(locale, messageSource)

	def "should build item for RequirementCreation event"() {
		given:
		Requirement req = Mock()
		RequirementCreation event = new RequirementCreation(req, "chris jericho")
		setId(event)

		and:
		PagedCollectionHolder paged = pagedCollection(event)

		and:
		messageSource.getMessage(_,null,locale) >> "Création"

		when:
		DataTableModel model = builder.buildDataModel(paged, "wooo")

		then:
		model.getAaData() == [
			[
				"Cr&eacute;ation",
				"creation",
				"10"
			]
		]
	}

	def "should build item for Requirement small property change event"() {
		given:
		Requirement req = Mock()
		RequirementPropertyChange event = RequirementPropertyChange.builder()
				.setModifiedProperty("reference")
				.setSource(req)
				.setAuthor("peter parker")
				.setOldValue("amazing")
				.setNewValue("astonishing")
				.build()

		setId(event)

		and:
		PagedCollectionHolder paged = pagedCollection(event)

		and:
		messageSource.getMessage(_,["amazing", "astonishing"],locale) >> "Modification de reference : 'amazing' -> 'astonishing'"

		when:
		DataTableModel model = builder.buildDataModel(paged, "wooo")

		then:
		model.getAaData() == [
			[
				"Modification de reference : 'amazing' -&gt; 'astonishing'",
				"simple-prop",
				"10"
			]
		]
	}

	def "should build item for Requirement fat property change event"() {
		given:
		Requirement req = Mock()
		RequirementLargePropertyChange event = RequirementLargePropertyChange.builder()
				.setModifiedProperty("description")
				.setSource(req)
				.setAuthor("peter parker")
				.setOldValue("amazing")
				.setNewValue("astonishing")
				.build()

		setId(event)

		and:
		PagedCollectionHolder paged = pagedCollection(event)


		and:
		messageSource.getMessage(_,null,locale) >> "Modification de la description"

		when:
		DataTableModel model = builder.buildDataModel(paged, "wooo")

		then:
		model.getAaData() == [
			[
				"Modification de la description",
				"fat-prop",
				"10"
			]
		]
	}

	def setId(def event) {
		use(ReflectionCategory) {
			RequirementAuditEvent.set field: "id", of: event, to: 10L
		}
	}

	def pagedCollection(def event) {
		PagedCollectionHolder paged = Mock()
		paged.pagedItems >> [event]
		paged.firstItemIndex >> 1
		paged.totalNumberOfItems >> 10

		return paged
	}

	def "should build item for Requirement status property change event"() {
		given:
		Requirement req = Mock()
		RequirementPropertyChange event = RequirementPropertyChange.builder()
				.setModifiedProperty("status")
				.setSource(req)
				.setAuthor("peter parker")
				.setOldValue("OBSOLETE")
				.setNewValue("APPROVED")
				.build()

		setId(event)

		and:
		PagedCollectionHolder paged = Mock()
		paged.pagedItems >> [event]
		paged.firstItemIndex >> 1
		paged.totalNumberOfItems >> 10

		and:
		messageSource.getMessage(RequirementStatus.OBSOLETE.i18nKey, null, locale) >> "Obs"
		messageSource.getMessage(RequirementStatus.APPROVED.i18nKey, null, locale) >> "App"
		messageSource.getMessage(_, ["Obs", "App"], locale) >> "Modification du status : 'Obsolète' -> 'Approuvé'"

		when:
		DataTableModel model = builder.buildDataModel(paged, "wooo")

		then:
		model.getAaData() == [
			[
				"Modification du status : 'Obsol&egrave;te' -&gt; 'Approuv&eacute;'",
				"simple-prop",
				"10"
			]
		]
	}	
}
