package org.squashtest.csp.core.infrastructure.hibernate;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.squashtest.csp.core.infrastructure.collection.Paging;

/**
 * Utility clas for paging queries.
 * 
 * @author Gregory Fouquet
 * 
 */
public final class PagingUtils {
	private PagingUtils() {
		super();
	}

	public static void addPaging(Query query, Paging paging) {
		query.setMaxResults(paging.getPageSize());
		query.setFirstResult(paging.getFirstItemIndex());
	}

	public static void addPaging(Criteria criteria, Paging paging) {
		criteria.setMaxResults(paging.getPageSize());
		criteria.setFirstResult(paging.getFirstItemIndex());
	}
}
