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

package org.squashtest.csp.tm.web.internal.report;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.MultiValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.plugin.api.report.ReportDefinition;
import org.squashtest.plugin.api.report.ReportPlugin;
import org.squashtest.plugin.api.report.StandardReportCategory;

/**
 * This class registers / unregisters {@link ReportDefinition} and their {@link StandardReportCategory} when
 * {@link ReportPlugin} services are started / stopped.
 * 
 * @author Gregory Fouquet
 * 
 */
public class ReportsRegistry {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReportsRegistry.class);

	private MultiValueMap reports = new MultiValueMap();

	/**
	 * OSGi context should be configured to call this method when a {@link ReportPlugin} service is started.
	 * @param plugin
	 * @param properties
	 */
	public void pluginRegistered(ReportPlugin plugin, Map<?, ?> properties) {
		ReportDefinition report = plugin.getReport();
		StandardReportCategory category = report.getCategory();

		synchronized (reports) {
			reports.put(category, report);
		}

		LOGGER.info("Registered report [{}] under Category [{}]", report.getLabelKey(), category.getI18nKey());
		LOGGER.debug("{}", properties);
	}

	/**
	 * OSGi context should be configured to call this method when a {@link ReportPlugin} service is stopped.
	 * @param plugin
	 * @param properties
	 */
	public void pluginUnregistered(ReportPlugin plugin, Map<?, ?> properties) {
		ReportDefinition report = plugin.getReport();
		StandardReportCategory category = report.getCategory();

		synchronized (reports) {
			reports.remove(category, report);
		}

		LOGGER.info("Unregistered report [{}] from Category [{}]", report.getLabelKey(), category.getI18nKey());
		LOGGER.debug("{}", properties);
	}

	@SuppressWarnings("unchecked")
	public Set<StandardReportCategory> getCategories() {
		return reports.keySet();
	}

	@SuppressWarnings("unchecked")
	public Collection<ReportDefinition> getReports(StandardReportCategory category) {
		Collection<ReportDefinition> res = (Collection<ReportDefinition>) reports.get(category);
		return res == null ? Collections.<ReportDefinition> emptyList() : res;
	}
	
	@SuppressWarnings("unchecked")
	public Map<StandardReportCategory, Collection<ReportDefinition>> getReportsByCategory() {
		return reports;
	}
}
