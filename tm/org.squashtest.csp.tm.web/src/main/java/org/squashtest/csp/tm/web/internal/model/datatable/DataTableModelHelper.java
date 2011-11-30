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

import java.util.Collection;
import java.util.List;

import org.squashtest.csp.core.infrastructure.collection.PagedCollectionHolder;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;

public abstract class DataTableModelHelper<X> {

	private long currentIndex = 0;

	public DataTableModel buildDataModel(FilteredCollectionHolder<List<X>> holder, int startIndex, String sEcho) {

		currentIndex = startIndex;

		Collection<X> collectionX = holder.getFilteredCollection();

		DataTableModel model = createModelFromItems(sEcho, collectionX);

		model.displayRowsFromTotalOf(holder.getUnfilteredResultCount());

		return model;

	}

	public DataTableModel buildDataModel(PagedCollectionHolder<List<X>> holder, String sEcho) {

		currentIndex = holder.getFirstItemIndex() + 1;

		Collection<X> pagedItems = holder.getPagedItems();

		DataTableModel model = createModelFromItems(sEcho, pagedItems);

		model.displayRowsFromTotalOf(holder.getTotalNumberOfItems());

		return model;

	}

	private DataTableModel createModelFromItems(String sEcho, Collection<X> pagedItems) {
		DataTableModel model = new DataTableModel(sEcho);

		for (X item : pagedItems) {

			Object[] itemData = buildItemData(item);

			model.addRow(itemData);

			currentIndex++;
		}
		return model;
	}

	public long getCurrentIndex() {
		return currentIndex;
	}

	protected abstract Object[] buildItemData(X item);
}
