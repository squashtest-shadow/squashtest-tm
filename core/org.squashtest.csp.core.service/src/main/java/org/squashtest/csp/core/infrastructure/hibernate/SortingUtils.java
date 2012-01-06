package org.squashtest.csp.core.infrastructure.hibernate;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.squashtest.csp.core.infrastructure.collection.Sorting;

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
		switch (sorting.getSortingOrder()) {
		case ASCENDING:
			criteria.addOrder(Order.asc(sorting.getSortedAttribute()));
			break;
		case DESCENDING:
			criteria.addOrder(Order.desc(sorting.getSortedAttribute()));
			break;
		}
	}
}
