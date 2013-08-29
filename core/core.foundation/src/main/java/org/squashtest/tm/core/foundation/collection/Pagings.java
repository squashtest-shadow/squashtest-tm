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
package org.squashtest.tm.core.foundation.collection;

/**
 * {@link Paging} of Squash TM default size.
 * 
 * @author Gregory Fouquet
 * 
 */
public final class Pagings {
	
	private Pagings(){};
	
	public static final Paging DEFAULT_PAGING = new PagingImpl();
	
	public static final Paging NO_PAGING = new PagingImpl(0,0,true);
	
	public static final Paging createNew (int firstItemIndex){
		return new PagingImpl(firstItemIndex);
	}
	
	public static final Paging createNew (int firstItemIndex, int pageSize){
		return new PagingImpl(firstItemIndex, pageSize);
	}
	
	public static final Paging createNew (int firstItemIndex, int pageSize, boolean shouldDisplayAll){
		return new PagingImpl(firstItemIndex, pageSize, shouldDisplayAll);
	}
	
	
	private static final class PagingImpl implements Paging{

		private int firstIndex = 0;
		private int pageSize = 50;
		private boolean displayAll = false;
		
		
		
		
		public PagingImpl() {
			super();
		}

		public PagingImpl(int firstIndex) {
			super();
			this.firstIndex = firstIndex;
		}

		public PagingImpl(int firstIndex, int pageSize) {
			super();
			this.firstIndex = firstIndex;
			this.pageSize = pageSize;
		}

		public PagingImpl(int firstIndex, int pageSize, boolean displayAll) {
			super();
			this.firstIndex = firstIndex;
			this.pageSize = pageSize;
			this.displayAll = displayAll;
		}

		@Override
		public int getFirstItemIndex() {
			return firstIndex;
		}

		@Override
		public int getPageSize() {
			return pageSize;
		}

		@Override
		public boolean shouldDisplayAll() {
			return displayAll;
		}
		
	}

}
