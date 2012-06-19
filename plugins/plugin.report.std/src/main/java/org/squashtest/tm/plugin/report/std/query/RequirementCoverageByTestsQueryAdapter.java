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

package org.squashtest.tm.plugin.report.std.query;

import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.squashtest.tm.api.report.criteria.Criteria;
import org.squashtest.tm.api.report.query.ReportQuery;
import org.squashtest.tm.internal.domain.report.common.hibernate.HibernateRequirementCoverageByTestsQuery;
import org.squashtest.tm.plugin.report.std.service.ReportService;

/**
 * @author Gregory
 * 
 */
public class RequirementCoverageByTestsQueryAdapter implements ReportQuery {
	@Inject
	private Provider<HibernateRequirementCoverageByTestsQuery> legacyQueryProvider;
	@Inject
	private ReportService reportService;

	/**
	 * @see org.squashtest.tm.api.report.query.ReportQuery#executeQuery(java.util.Map, java.util.Map)
	 */
	@Override
	public void executeQuery(Map<String, Criteria> criteria, Map<String, Object> model) {
		// mode
		// projectIDs[]
		HibernateRequirementCoverageByTestsQuery legacyQuery = legacyQueryProvider.get();

		for (Map.Entry<String, Criteria> entry : criteria.entrySet()) {
			legacyQuery.setCriterion(entry.getKey(), entry.getValue().getValue());
		}

		Collection<?> data = reportService.executeQuery(legacyQuery);

		model.put("data", data);
	}

	/**
	 * @param legacyQueryProvider
	 *            the legacyQueryProvider to set
	 */
	public void setLegacyQueryProvider(Provider<HibernateRequirementCoverageByTestsQuery> legacyQueryProvider) {
		this.legacyQueryProvider = legacyQueryProvider;
	}

	/**
	 * @param reportService
	 *            the reportService to set
	 */
	public void setReportService(ReportService reportService) {
		this.reportService = reportService;
	}
}
