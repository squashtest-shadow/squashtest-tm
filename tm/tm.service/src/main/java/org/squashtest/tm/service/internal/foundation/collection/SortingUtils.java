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
package org.squashtest.tm.service.internal.foundation.collection;

import java.util.Collection;
import java.util.Iterator;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.squashtest.tm.core.foundation.collection.Sorting;

/**
 * Utility class to apply sorting to a Hibernate query.
 * 
 * @author Gregory Fouquet
 * 
 */
public final class SortingUtils {
	private SortingUtils() {
		super();
	}

	/**
	 * Adds sorting to a Criteria query.
	 * 
	 * @param criteria
	 * @param sorting
	 */
	public static void addOrder(Criteria criteria, Sorting sorting) {
		switch (sorting.getSortOrder()) {
		case ASCENDING:
			criteria.addOrder(Order.asc(sorting.getSortedAttribute()));
			break;
		case DESCENDING:
			criteria.addOrder(Order.desc(sorting.getSortedAttribute()));
			break;
		}
	}
	
	
	public static void addOrders(Criteria criteria, Collection<Sorting> sortings){
		Iterator<Sorting> iterator = sortings.iterator();
		while(iterator.hasNext()){
			Sorting next = iterator.next();
			addOrder(criteria, next);			
		}
	}
}
