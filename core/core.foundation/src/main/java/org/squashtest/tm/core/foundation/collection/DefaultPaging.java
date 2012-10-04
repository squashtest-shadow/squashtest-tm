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
package org.squashtest.tm.core.foundation.collection;

/**
 * {@link Paging} of Squash TM default size.
 * 
 * @author Gregory Fouquet
 * 
 */
public class DefaultPaging implements Paging {
	/**
	 * First page of default size.
	 */
	public static final Paging FIRST_PAGE = new DefaultPaging(0);

	private final int firstItemIndex;

	/**
	 * @param firstItemIndex
	 */
	public DefaultPaging(int firstItemIndex) {
		this.firstItemIndex = firstItemIndex;
	}

	/**
	 * @see org.squashtest.tm.core.foundation.collection.Paging#getFirstItemIndex()
	 */
	@Override
	public int getFirstItemIndex() {
		return firstItemIndex;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.squashtest.tm.core.foundation.collection.Paging#getPageSize()
	 */
	@Override
	public int getPageSize() {
		return 50;
	}

}
