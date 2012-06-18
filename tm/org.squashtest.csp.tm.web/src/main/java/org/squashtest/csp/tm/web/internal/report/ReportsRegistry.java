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
	
	private static final String NAMESPACE_KEY = "osgi.service.blueprint.compname";

	private MultiValueMap reports = new MultiValueMap();

	/**
	 * OSGi context should be configured to call this method when a {@link ReportPlugin} service is started.
	 * @param plugin
	 * @param properties
	 */
	public void pluginRegistered(ReportPlugin plugin, Map<?, ?> properties) {
		ReportDefinition report = plugin.getReport();
		StandardReportCategory category = report.getCategory();
		IdentifiedReportDecorator identifiedReport = createIdentifiedReport(report, properties);

		synchronized (reports) {
			reports.put(category, identifiedReport);
		}

		LOGGER.info("Registered report [{}] under Category [{}]", report.getLabelKey(), category.getI18nKey());
		LOGGER.debug("{}", properties);
	}

	private IdentifiedReportDecorator createIdentifiedReport(ReportDefinition report, Map<?, ?> properties) {
		String pluginNamespace = (String) properties.get(NAMESPACE_KEY);
		IdentifiedReportDecorator identifiedReport = new IdentifiedReportDecorator(report, pluginNamespace, 0);
		return identifiedReport;
	}

	/**
	 * OSGi context should be configured to call this method when a {@link ReportPlugin} service is stopped.
	 * @param plugin
	 * @param properties
	 */
	public void pluginUnregistered(ReportPlugin plugin, Map<?, ?> properties) {
		ReportDefinition report = plugin.getReport();
		StandardReportCategory category = report.getCategory();
		IdentifiedReportDecorator identifiedReport = createIdentifiedReport(report, properties);

		synchronized (reports) {
			reports.remove(category, identifiedReport);
		}

		LOGGER.info("Unregistered report [{}] from Category [{}]", report.getLabelKey(), category.getI18nKey());
		LOGGER.debug("{}", properties);
	}

	@SuppressWarnings("unchecked")
	public Set<StandardReportCategory> getCategories() {
		return reports.keySet();
	}

	@SuppressWarnings("unchecked")
	public Collection<IdentifiedReportDecorator> getReports(StandardReportCategory category) {
		Collection<IdentifiedReportDecorator> res = (Collection<IdentifiedReportDecorator>) reports.get(category);
		return res == null ? Collections.<IdentifiedReportDecorator> emptyList() : res;
	}
	
	@SuppressWarnings("unchecked")
	public Map<StandardReportCategory, Collection<ReportDefinition>> getReportsByCategory() {
		return reports;
	}
}
