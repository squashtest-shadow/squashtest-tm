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
package org.squashtest.csp.tm.infrastructure.filter;

import org.squashtest.tm.core.foundation.collection.Paging;

/**
 * Defines a filter to apply when querying for a collection.
 * 
 * Consider using the {@link Paging} api define in core module instead.
 * 
 * @author Gregory Fouquet
 * @deprecated use {@link Paging} instead 
 */

public interface CollectionFilter extends Paging {

	/**
	 * The 0-based index of the first returned item.
	 * 
	 * @return
	 */
	int getFirstItemIndex();

	/**
	 * The max number of items in the returned collection.
	 * 
	 * @return
	 */
	int getMaxNumberOfItems();

}