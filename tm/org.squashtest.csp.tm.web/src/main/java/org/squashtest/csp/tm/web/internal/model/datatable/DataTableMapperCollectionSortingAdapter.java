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

import org.squashtest.csp.core.infrastructure.collection.PagingAndSorting;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.web.internal.model.viewmapper.DataTableMapper;
/**
 * 
 * @author Gregory Fouquet
 *
 */
public class DataTableMapperCollectionSortingAdapter implements CollectionSorting {
	private final PagingAndSorting delegate;  

	public DataTableMapperCollectionSortingAdapter(DataTableDrawParameters drawParams, DataTableMapper mapper) {
		delegate = new DataTableMapperPagingAndSortingAdapter(drawParams, mapper);
	}

	@Override
	public int getMaxNumberOfItems() {
		return delegate.getPageSize();
	}

	public String getSortedAttribute() {
		return delegate.getSortedAttribute();
	}

	public String getSortingOrder() {
		return delegate.getSortingOrder().getCode();
	}

	public int getFirstItemIndex() {
		return delegate.getFirstItemIndex();
	}

	public int getPageSize() {
		return delegate.getPageSize();
	}

}
