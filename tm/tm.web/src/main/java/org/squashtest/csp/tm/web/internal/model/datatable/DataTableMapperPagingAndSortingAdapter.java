/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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

import org.squashtest.csp.tm.web.internal.model.viewmapper.DataTableMapper;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.SortOrder;

/**
 * PagingAndSortingAdapter backed by a DataTableDrawParameters and a DataTableMapper (for sorting purposes).
 * 
 * @author Gregory Fouquet
 * 
 */
public class DataTableMapperPagingAndSortingAdapter extends DataTableDrawParametersPagingAdapter implements
		PagingAndSorting {
	public enum SortedAttributeSource {
		SINGLE_ENTITY {
			/**
			 * @return the sorted attribute simple name.
			 */
			@Override
			String sortedAttributeName(DataTableMapper mapper, DataTableDrawParameters params) {
				return mapper.attrAt(params.getiSortCol_0());
			}
		},
		MULTIPLE_ENTITIES {
			/**
			 * @return the full sorted attribute path.
			 */
			@Override
			String sortedAttributeName(DataTableMapper mapper, DataTableDrawParameters params) {
				return mapper.pathAt(params.getiSortCol_0());
			}
		};
		/**
		 * Returns the sorted attribute name, according to this style of attributes source.
		 * 
		 * @param mapper
		 * @param params
		 * @return
		 */
		abstract String sortedAttributeName(DataTableMapper mapper, DataTableDrawParameters params);
	}

	private final DataTableDrawParameters params;
	private final DataTableMapper mapper;
	private final SortedAttributeSource sortedAttributeNameStrategy;

	public DataTableMapperPagingAndSortingAdapter(DataTableDrawParameters drawParams, DataTableMapper mapper) {
		super(drawParams);
		this.params = drawParams;
		this.mapper = mapper;
		this.sortedAttributeNameStrategy = SortedAttributeSource.MULTIPLE_ENTITIES;
	}

	public DataTableMapperPagingAndSortingAdapter(DataTableDrawParameters params,
			DataTableMapper mapper, SortedAttributeSource sortedAttributeSource) {
		super(params);
		this.params = params;
		this.mapper = mapper;
		this.sortedAttributeNameStrategy = sortedAttributeSource;
	}

	@Override
	public String getSortedAttribute() {
		return sortedAttributeNameStrategy.sortedAttributeName(mapper, params);
	}

	@Override
	public SortOrder getSortOrder() {
		return SortOrder.coerceFromCode(params.getsSortDir_0());
	}

}
