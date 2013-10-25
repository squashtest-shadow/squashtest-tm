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
package org.squashtest.tm.web.internal.model.datatable;

import java.util.Collection;
import org.apache.commons.lang.StringUtils;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;

public class DataTableColumnFiltering implements ColumnFiltering{

	private final DataTableDrawParameters params;
	
	public DataTableColumnFiltering(DataTableDrawParameters params) {
		super();
		this.params = params;
	}
	
	@Override
	public boolean isDefined() {
		Collection<String> values = params.getsSearches().values();
		for(String value : values){
			if(!StringUtils.isBlank(value)){
				return true;
			}
		}
		return false;
	}

	@Override
	public String getFilter(Integer index) {
		return params.getsSearches(index);
	}

	@Override
	public boolean hasFilter(Integer index) {
		return !StringUtils.isBlank(getFilter(index));
	}
}
