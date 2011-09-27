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
package org.squashtest.csp.tm.internal.repository.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.springframework.stereotype.Repository;
import org.squashtest.csp.tm.domain.report.query.ReportQuery;
import org.squashtest.csp.tm.domain.report.query.ReportQueryFlavor;
import org.squashtest.csp.tm.domain.report.query.UnsupportedFlavorException;
import org.squashtest.csp.tm.domain.report.query.hibernate.HibernateQueryFlavor;
import org.squashtest.csp.tm.domain.report.query.hibernate.HibernateReportQuery;
import org.squashtest.csp.tm.internal.repository.ReportQueryDao;

@Repository
public class HibernateReportQueryDao extends HibernateDao<Object> implements ReportQueryDao {

	private final ReportQueryFlavor flavor = new HibernateQueryFlavor();



	@Override
	public boolean doesSupportFlavor(ReportQueryFlavor flavor) {
		return (this.flavor.getClass() == flavor.getClass());
	}

	@Override
	public List<?> executeQuery(ReportQuery query) throws UnsupportedFlavorException {
		if (! doesSupportFlavor(query.getFlavor())){
			throw new UnsupportedFlavorException("Error : ReportQueryDao implementation does not support queries of class "+query.getClass().getName());
		}
		HibernateReportQuery hibQuery = (HibernateReportQuery)query;

		DetachedCriteria dCriteria = hibQuery.createHibernateQuery();

		List<?> result;

		if (dCriteria != null) {

			result = executeDetachedCriteria(dCriteria);

		} else {

			result = hibQuery.doInSession(currentSession());

		}


		return hibQuery.convertToDto(result);

	}

	private List<?> executeDetachedCriteria(DetachedCriteria dCriteria) {

		Session session = currentSession();
		Criteria criteria = dCriteria.getExecutableCriteria(session);
		return criteria.list();
	}

	@Override
	public ReportQueryFlavor[] getSupportedFlavors() {
		ReportQueryFlavor[] flavorArray = {flavor};
		return flavorArray;
	}

}
