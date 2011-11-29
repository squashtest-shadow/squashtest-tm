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

import org.squashtest.csp.tm.domain.event.RequirementAuditEvent;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModelHelper;

/**
 * Builder for datatable model showing {@link RequirementAuditEvent} objects.
 * 
 * @author Gregory Fouquet
 * 
 */
public class RequirementAuditEventTableModelBuilder extends DataTableModelHelper<RequirementAuditEvent> {

	/**
	 * @see org.squashtest.csp.tm.web.internal.model.datatable.DataTableModelHelper#buildItemData(java.lang.Object)
	 */
	@Override
	protected Object[] buildItemData(RequirementAuditEvent item) {
		// TODO Auto-generated method stub
		return null;
	}

}
