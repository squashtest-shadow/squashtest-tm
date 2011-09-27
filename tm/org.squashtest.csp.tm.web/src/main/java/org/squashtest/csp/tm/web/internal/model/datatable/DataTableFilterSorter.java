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
package org.squashtest.csp.tm.web.internal.model.datatable;

import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.web.internal.model.viewmapper.DataTableMapper;

public class DataTableFilterSorter implements CollectionSorting {
	
	private DataTableDrawParameters params;
	private DataTableMapper mapper;

	public DataTableFilterSorter(DataTableDrawParameters params, DataTableMapper mapper){
		this.params=params;
		this.mapper=mapper;
	}
	
	@Override
	public int getMaxNumberOfItems() {
		return params.getiDisplayLength();
	}

	@Override
	public int getFirstItemIndex() {
		return params.getiDisplayStart();
	}

	@Override
	public String getSortedAttribute() {
		return mapper.pathAt(params.getiSortCol_0());
	}

	@Override
	public String getSortingOrder() {
		return params.getsSortDir_0();
	}

}
