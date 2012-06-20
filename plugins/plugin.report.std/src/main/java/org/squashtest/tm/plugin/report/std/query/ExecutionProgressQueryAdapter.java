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
package org.squashtest.tm.plugin.report.std.query;

import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.api.report.criteria.Criteria;
import org.squashtest.tm.api.report.query.ReportQuery;
import org.squashtest.tm.internal.domain.report.common.hibernate.HibernateExecutionProgressQuery;
import org.squashtest.tm.plugin.report.std.service.ReportService;

/**
 * @author Gregory Fouquet
 * 
 */
public class ExecutionProgressQueryAdapter implements ReportQuery {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionProgressQueryAdapter.class);
	/**
	 * 
	 */
	private static final String CAMPAIGN_IDS = "campaignIds";
	/**
	 * 
	 */
	private static final String LEGACY_CAMPAIGN_IDS = "campaignIds[]";
	/**
	 * 
	 */
	private static final String CAMPAIGN_SELECTION_MODE = "campaignSelectionMode";
	@Inject
	private Provider<HibernateExecutionProgressQuery> legacyQueryProvider;
	@Inject
	private ReportService reportService;

	/**
	 * @see org.squashtest.tm.api.report.query.ReportQuery#executeQuery(java.util.Map, java.util.Map)
	 */
	@Override
	public void executeQuery(Map<String, Criteria> criteria, Map<String, Object> model) {
		LOGGER.warn(criteria.toString());
		HibernateExecutionProgressQuery legacyQuery = legacyQueryProvider.get();
		
		Criteria selMode = criteria.get(CAMPAIGN_SELECTION_MODE);
		if ("EVERYTHING".equals(selMode.getValue())) {
			LOGGER.warn("EVERY");
			legacyQuery.setCriterion(LEGACY_CAMPAIGN_IDS, (Object[]) null);
		} else  {
			LOGGER.warn("NODES");
			Criteria campaignIds = criteria.get(CAMPAIGN_IDS);
			legacyQuery.setCriterion(LEGACY_CAMPAIGN_IDS, campaignIds.getValue());
		}

		for (Map.Entry<String, Criteria> entry : criteria.entrySet()) {
			if (noAdaptationNeeded(entry.getKey())) {
				LOGGER.warn(entry.getKey());
				legacyQuery.setCriterion(entry.getKey(), entry.getValue().getValue());
			}
		}

		Collection<?> data = reportService.executeQuery(legacyQuery);

		model.put("data", data);

	}

	/**
	 * @param criterionName
	 * @return
	 */
	private boolean noAdaptationNeeded(String criterionName) {
		return !(CAMPAIGN_IDS.equals(criterionName) || CAMPAIGN_SELECTION_MODE.equals(criterionName));
	}

	/**
	 * @param legacyQueryProvider
	 *            the legacyQuery to set
	 */
	public void setLegacyQueryProvider(Provider<HibernateExecutionProgressQuery> legacyQueryProvider) {
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
