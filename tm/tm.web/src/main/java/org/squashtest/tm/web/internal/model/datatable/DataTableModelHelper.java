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
package org.squashtest.tm.web.internal.model.datatable;

import java.util.Collection;
import java.util.List;

import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.service.foundation.collection.FilteredCollectionHolder;

public abstract class DataTableModelHelper<X> {
	
	public final static String DEFAULT_ENTITY_ID_KEY = "entity-id";
	public final static String DEFAULT_ENTITY_INDEX_KEY = "entity-index";
	public final static String DEFAULT_NB_ATTACH_KEY = "nb-attachments";
	public final static String DEFAULT_ATTACH_LIST_ID_KEY = "attach-list-id";
	public final static String DEFAULT_EMPTY_DELETE_HOLDER_KEY = "empty-delete-holder";
	public final static String DEFAULT_EMPTY_EXECUTE_HOLDER_KEY = "empty-execute-holder";
	
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

			Object itemData = buildItemData(item);

			model.addRow(itemData);

			currentIndex++;
		}
		return model;
	}

	protected final long getCurrentIndex() {
		return currentIndex;
	}

	protected abstract Object buildItemData(X item);
	
	protected void incrementIndex(){
		currentIndex++;
	}

}
