/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.web.internal.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import org.apache.commons.collections.map.MultiValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.squashtest.tm.api.report.BasicReport;
import org.squashtest.tm.api.report.Report;
import org.squashtest.tm.api.report.ReportPlugin;
import org.squashtest.tm.api.report.StandardReportCategory;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;

/**
 * This class registers / unregisters {@link BasicReport} and their {@link StandardReportCategory} when
 * {@link ReportPlugin} services are started / stopped.
 * 
 * @author Gregory Fouquet
 * 
 */
public class ReportsRegistry {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReportsRegistry.class);



	private final MultiValueMap reportsByCategory = new MultiValueMap();
	private final Map<ReportIdentifier, IdentifiedReportDecorator> reportByIdentifier = new ConcurrentHashMap<ReportIdentifier, IdentifiedReportDecorator>();


	@Inject
	private MessageSource i18nHelper;

	/**
	 * OSGi context should be configured to call this method when a {@link ReportPlugin} service is started.
	 * @param plugin
	 * @param properties
	 */
	public synchronized void registerReports(ReportPlugin plugin, Map<?, ?> properties) {
		Report[] reports = plugin.getReports();

		for (int i = 0; i < reports.length; i++) {
			Report report =  reports[i];
			StandardReportCategory category = report.getCategory();
			IdentifiedReportDecorator identifiedReport = createIdentifiedReport(report, properties, i);

			reportsByCategory.put(category, identifiedReport);
			reportByIdentifier.put(identifiedReport.getIdentifier(), identifiedReport);

			LOGGER.info("Registered report [{}] under Category [{}]", report, category.getI18nKey());
			LOGGER.debug("Report plugin registered along with properties [{}]", properties);
		}
	}

	private IdentifiedReportDecorator createIdentifiedReport(Report report, Map<?, ?> properties, int index) {
		String pluginNamespace = (String) properties.get(OsgiServiceConstants.SERVICE_ID_KEY);
		return new IdentifiedReportDecorator(report, pluginNamespace, index);
	}

	/**
	 * OSGi context should be configured to call this method when a {@link ReportPlugin} service is stopped.
	 * @param plugin
	 * @param properties
	 */
	public synchronized void unregisterReports(ReportPlugin plugin, Map<?, ?> properties) {
		// this sometimes happen
		if (plugin == null) {
			LOGGER.warn("Unregistered null plugin with properties {}", properties);
			return;
		}

		Report[] reports = plugin.getReports();

		for (int i = 0; i < reports.length; i++) {
			Report report =  reports[i];

			StandardReportCategory category = report.getCategory();
			IdentifiedReportDecorator identifiedReport = createIdentifiedReport(report, properties, i);

			reportsByCategory.remove(category, identifiedReport);
			reportByIdentifier.remove(identifiedReport.getIdentifier());

			LOGGER.info("Unregistered report [{}] from Category [{}]", report, category.getI18nKey());
			LOGGER.debug("Report plugin unregistered along with properties [{}]", properties);
		}
	}

	@SuppressWarnings("unchecked")
	public Set<StandardReportCategory> getCategories() {
		return reportsByCategory.keySet();
	}


	public List<StandardReportCategory> getSortedCategories() {
		List<StandardReportCategory> sortedCategories = new ArrayList<StandardReportCategory>(getCategories());
		Collections.sort(sortedCategories, new CategorySorter(i18nHelper));
		return sortedCategories;
	}

	@SuppressWarnings("unchecked")
	public Collection<IdentifiedReportDecorator> findReports(StandardReportCategory category) {
		Collection<IdentifiedReportDecorator> res = (Collection<IdentifiedReportDecorator>) reportsByCategory.get(category);
		return res == null ? Collections.<IdentifiedReportDecorator> emptyList() : res;
	}

	@SuppressWarnings("unchecked")
	public Map<StandardReportCategory, Collection<BasicReport>> getReportsByCategory() {
		return reportsByCategory;
	}

	@SuppressWarnings("unchecked")
	public Map<StandardReportCategory, Collection<BasicReport>> getSortedReportsByCategory() {

		Map<StandardReportCategory, Collection<BasicReport>> sortedMap = new HashMap<StandardReportCategory, Collection<BasicReport>>(reportsByCategory.size());
		Iterator<StandardReportCategory> categIterator = reportsByCategory.keySet().iterator();

		while (categIterator.hasNext()){
			StandardReportCategory categ = categIterator.next();
			List<BasicReport> sortedReports = new ArrayList<BasicReport>(reportsByCategory.getCollection(categ));
			Collections.sort(sortedReports, new ReportSorter());
			sortedMap.put(categ, sortedReports);
		}

		return sortedMap;
	}

	/**
	 * @param namespace
	 * @param index
	 * @return
	 */
	public Report findReport(String namespace, int index) {
		return reportByIdentifier.get(new ReportIdentifier(namespace, index));
	}



	// ****************************** boilerplate *****************************

	private static class CategorySorter implements Comparator<StandardReportCategory>{

		private MessageSource i18nHelper;

		CategorySorter(MessageSource helper){
			this.i18nHelper = helper;
		}

		@Override
		public int compare(StandardReportCategory category1, StandardReportCategory category2) {
			Locale locale = LocaleContextHolder.getLocale();
			String name1 = i18nHelper.getMessage(category1.getI18nKey(), null, locale);
			String name2 = i18nHelper.getMessage(category2.getI18nKey(), null, locale);
			return name1.compareTo(name2);
		}

	}

	private static class ReportSorter implements Comparator<Report>{
		@Override
		public int compare(Report report1, Report report2) {
			String name1 = report1.getLabel();
			String name2 = report2.getLabel();
			return name1.compareTo(name2);
		}

	}
}
