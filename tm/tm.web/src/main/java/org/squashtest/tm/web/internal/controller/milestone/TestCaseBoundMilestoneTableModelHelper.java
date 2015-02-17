/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.milestone;

import java.util.HashMap;
import java.util.Map;

import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;

public class TestCaseBoundMilestoneTableModelHelper extends DataTableModelBuilder<MetaMilestone> {

	@Override
	protected Map<String, Object> buildItemData(MetaMilestone item) {

		Map<String, Object> row = new HashMap<>();

		row.put(DataTableModelConstants.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex());
		row.put(DataTableModelConstants.DEFAULT_ENTITY_ID_KEY, item.getId());
		row.put("directMember", item.isDirectMembership());
		row.put("status", item.getStatus());
		row.put("label", item.getLabel());
		row.put("date", item.getEndDate());
		row.put("description", item.getDescription());
		row.put(DataTableModelConstants.DEFAULT_EMPTY_DELETE_HOLDER_KEY, null);

		return row;
	}


}
