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

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import org.springframework.context.MessageSource;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.csp.core.infrastructure.collection.PagedCollectionHolder;
import org.squashtest.csp.tm.domain.event.ChangedProperty;
import org.squashtest.csp.tm.domain.event.RequirementAuditEvent;
import org.squashtest.csp.tm.domain.event.RequirementLargePropertyChange;
import org.squashtest.csp.tm.service.audit.RequirementAuditTrailService;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableDrawParametersPagingAdapter;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModel;

/**
 * This controller handles requests related to a requirement's audit trail (ie its collection of
 * {@link RequirementAuditEvent})
 * 
 * @author Gregory Fouquet
 * 
 */
@Controller
@RequestMapping("/audit-trail/requirement-versions")
public class RequirementAuditTrailController {
	private RequirementAuditTrailService auditTrailService;
	@Inject
	private MessageSource messageSource;

	/**
	 * @param auditTrailService
	 *            the auditTrailService to set
	 */
	@ServiceReference
	public void setAuditTrailService(RequirementAuditTrailService auditTrailService) {
		this.auditTrailService = auditTrailService;
	}

	@RequestMapping(value = "{requirementVersionId}/events-table", params = "sEcho")
	@ResponseBody
	public DataTableModel getEventsTableModel(@PathVariable long requirementVersionId, DataTableDrawParameters drawParams,
			Locale locale) {
		PagedCollectionHolder<List<RequirementAuditEvent>> auditTrail = auditTrailService
				.findAllByRequirementVersionIdOrderedByDate(requirementVersionId,
						new DataTableDrawParametersPagingAdapter(drawParams));

		RequirementAuditEventTableModelBuilder builder = new RequirementAuditEventTableModelBuilder(locale,
				messageSource);

		return builder.buildDataModel(auditTrail, drawParams.getsEcho());
	}
	
	@RequestMapping(value="fat-prop-change-events/{eventId}") @ResponseBody
	public ChangedProperty getLargePropertyChangeEvent(@PathVariable long eventId) {
		final RequirementLargePropertyChange event = auditTrailService.findLargePropertyChangeById(eventId);
		
		return new ChangedPropertyJsonDecorator(event);
	}
}
