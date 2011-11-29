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

package org.squashtest.csp.tm.web.internal.controller.administration;

import java.util.List;
import java.util.Locale;

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.csp.tm.domain.event.RequirementAuditEvent;
import org.squashtest.csp.tm.service.audit.RequirementAuditTrailService;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableDrawParametersPagingAdapter;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModel;

/**
 * @author Gregory Fouquet
 * 
 */
@Controller
@RequestMapping("/audit-trail/requirement/{requirementId}")
public class RequirementAuditTrailController {
	private RequirementAuditTrailService auditTrailService;

	/**
	 * @param auditTrailService
	 *            the auditTrailService to set
	 */
	@ServiceReference
	public void setAuditTrailService(RequirementAuditTrailService auditTrailService) {
		this.auditTrailService = auditTrailService;
	}

	@RequestMapping(value = "events-table", params = "sEcho")
	@ResponseBody
	public DataTableModel getEventsTableModel(@PathVariable long requirementId, DataTableDrawParameters drawParams,
			Locale locale) {
		List<RequirementAuditEvent> auditTrail = auditTrailService.findAllByRequirementIdOrderedByDate(requirementId,
				new DataTableDrawParametersPagingAdapter(drawParams));

		return null;
	}
}
