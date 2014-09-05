/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.plugin.report.std.query;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.squashtest.tm.api.report.criteria.Criteria;
import org.squashtest.tm.internal.domain.report.common.hibernate.HibernateExecutionProgressQuery;
import org.squashtest.tm.internal.domain.report.query.hibernate.HibernateReportQuery;

/**
 * @author Gregory Fouquet
 * 
 */
public class ExecutionProgressQueryAdapter extends LegacyQueryAdapter<HibernateExecutionProgressQuery> {

	@Inject
	private Provider<HibernateExecutionProgressQuery> legacyQueryProvider;
	/**
	 * 
	 */
	static final String CAMPAIGN_IDS = "campaignIds";
	/**
	 * 
	 */
	static final String LEGACY_CAMPAIGN_IDS = "campaignIds[]";
	/**
	 * 
	 */
	static final String CAMPAIGN_SELECTION_MODE = "campaignSelectionMode";

	@SuppressWarnings({ "rawtypes" })
	protected void processNonStandardCriteria(Map<String, Criteria> criteria, HibernateReportQuery legacyQuery) {
		Criteria selMode = criteria.get(CAMPAIGN_SELECTION_MODE);
		if ("EVERYTHING".equals(selMode.getValue())) {
			setNoCampaignIds(legacyQuery);
		} else {
			Criteria idsCrit = criteria.get(CAMPAIGN_IDS);
			Collection nodesIds = new HashSet<Object>();
			addCampaignIds(idsCrit, nodesIds, "campaigns");
			addCampaignIds(idsCrit, nodesIds, "campaign-folders");
			legacyQuery.setCriterion(LEGACY_CAMPAIGN_IDS, nodesIds.toArray());
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addCampaignIds(Criteria idsCrit, Collection nodesIds, String nodeType) {
		Collection<?> campaignIds = ((Map<String, Collection<?>>) idsCrit.getValue()).get(nodeType);
		if (campaignIds != null) {
			nodesIds.addAll(campaignIds);
		}
	}

	private void setNoCampaignIds(HibernateReportQuery legacyQuery) {
		legacyQuery.setCriterion(LEGACY_CAMPAIGN_IDS, (Object[]) null);
	}

	/**
	 * @param criterionName
	 * @return
	 */
	@Override
	protected boolean isStandardCriteria(String criterionName) {
		return !(CAMPAIGN_IDS.equals(criterionName) || CAMPAIGN_SELECTION_MODE.equals(criterionName));
	}

	/**
	 * @return the legacyQueryProvider
	 */
	protected Provider<HibernateExecutionProgressQuery> getLegacyQueryProvider() {
		return legacyQueryProvider;
	}
}
