
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

package org.squashtest.csp.tm.web.internal.controller.audittrail;

import org.apache.poi.hssf.record.formula.functions.T
import org.springframework.context.MessageSource
import org.squashtest.csp.core.infrastructure.collection.PagedCollectionHolder
import org.squashtest.csp.tm.domain.event.RequirementCreation
import org.squashtest.csp.tm.domain.event.RequirementLargePropertyChange;
import org.squashtest.csp.tm.service.audit.RequirementAuditTrailService
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableDrawParameters
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModel

import spock.lang.Specification

/**
 * @author Gregory Fouquet
 *
 */
class RequirementAuditTrailControllerTest extends Specification {
	RequirementAuditTrailController controller = new RequirementAuditTrailController()
	RequirementAuditTrailService requirementAuditTrailService = Mock()
	MessageSource messageSource = Mock()

	def setup() {
		controller.auditTrailService = requirementAuditTrailService
		controller.messageSource = messageSource
	}

	def "should return an audit event table model for the requested requirement"() {
		given:
		Locale locale = Locale.JAPANESE
		DataTableDrawParameters drawParams = Mock()

		and:
		RequirementCreation event = Mock()
		PagedCollectionHolder holder = Mock()
		holder.pagedItems >> [event]
		requirementAuditTrailService.findAllByRequirementIdOrderedByDate(10L, _) >> holder

		when:
		DataTableModel model =  controller.getEventsTableModel(10L, drawParams, locale)

		then:
		model.aaData.size() == 1
	}
	def "should return an audit event"() {
		given:
		RequirementLargePropertyChange event = Mock()
		event.propertyName >> "shoe size"
		event.oldValue >> "10.5"
		event.newValue >> "13"
		requirementAuditTrailService.findLargePropertyChangeById(10L) >> event
		
		when:
		def res =  controller.getLargePropertyChangeEvent(10L)

		then:
		res
		res.propertyName == event.propertyName
		res.oldValue == event.oldValue
		res.newValue == event.newValue
	}
}
