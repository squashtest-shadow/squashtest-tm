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

import org.squashtest.csp.tm.domain.event.RequirementCreation;
import org.squashtest.csp.tm.service.audit.RequirementAuditTrailService;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModel;

import spock.lang.Specification;

/**
 * @author Gregory Fouquet
 *
 */
class RequirementAuditTrailControllerTest extends Specification {
	RequirementAuditTrailController controller = new RequirementAuditTrailController()
	RequirementAuditTrailService requirementAuditTrailService = Mock()

	def setup() {
		controller.auditTrailService = requirementAuditTrailService
	}

	def ""() {
		given:
		Locale locale = Locale.JAPANESE
		DataTableDrawParameters drawParams = Mock()

		and:
		RequirementCreation event = Mock()
		requirementAuditTrailService.findAllByRequirementIdOrderedByDate(10L, _) >> [event]
		
		when:
		DataTableModel model =  controller.getEventsTableModel(10L, drawParams, locale)

		then:
		model.aaData.size() == 1
	}
}
