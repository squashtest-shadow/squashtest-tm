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
package org.squashtest.tm.service.internal.repository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.squashtest.tm.core.foundation.collection.DefaultSorting;
import org.squashtest.tm.core.foundation.collection.DelegatePagingAndMultiSorting;
import org.squashtest.tm.core.foundation.collection.MultiSorting;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.core.foundation.collection.Pagings;
import org.squashtest.tm.core.foundation.collection.Sorting;

/**
 * 
 * @author Frederic
 * Enables sorting of objects according to test case importance.
 */
public final class ImportanceSortHelper {

	private class DummyMultiSorting implements MultiSorting{

		private List<Sorting> sortings;
		
		public DummyMultiSorting(List<Sorting> sortings){
			this.sortings = sortings;
		}
		
		@Override
		public List<Sorting> getSortings() {
			return this.sortings;
		}
		
	}
	
	public PagingAndMultiSorting modifyImportanceSortInformation(PagingAndMultiSorting sortings){
				
		List<Sorting> newSortingList = new ArrayList<Sorting>();
		
		for (Iterator<Sorting> iter = sortings.getSortings().iterator(); iter.hasNext();) {
			Sorting sorting = iter.next();
			if("TestCase.importance".equals(sorting.getSortedAttribute())){
				
				StringBuilder builder = new StringBuilder();
				
				builder.append("CASE TestCase.importance ")
				   .append("when 'LOW' then 3 ")
				   .append("when 'MEDIUM' then 2 ")
				   .append("when 'HIGH' then 1 ")
				   .append("when 'VERY_HIGH' then 0 ")
				   .append("else 4 ")
				   .append("END ");
				
				DefaultSorting importance = new DefaultSorting();
				importance.setSortedAttribute(builder.toString());
				importance.setSortOrder(sorting.getSortOrder());
				newSortingList.add(importance);
			} 
			else {
				newSortingList.add(sorting);
			}
		}
				
		PagingAndMultiSorting newSortings = new DelegatePagingAndMultiSorting(Pagings.createNew(sortings.getFirstItemIndex()), new DummyMultiSorting(newSortingList));
		
		return newSortings;
	}
}
