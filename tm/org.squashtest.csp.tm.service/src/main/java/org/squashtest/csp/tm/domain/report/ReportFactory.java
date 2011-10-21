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
package org.squashtest.csp.tm.domain.report;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * 
 * This class is the front-end to which a service needs to talk to register or find a report category.
 * 
 * You can basically think of it like for a repository, except that it doesn't read the ReportCategories and Reports
 * directly, something else must load them then register them against the ReportFactory. Each ReportCategory and Report,
 * once registered, are assigned an id. The user can query for a specific ReportCategory or Report based on its id, or
 * get the whole list on demand. More about the id later.
 * 
 * This singleton factory manages ReportCategories just like the ReportCategories manage the Reports : like beans. Like
 * the bean they are, each ReportCategory and Report should be instanciated and registered only once. By default this is
 * ensured by Spring IoC using @Component and @Resource annotations. A third party program can use the method
 * addCategory() to register a new ReportCategory instance, however it falls under its responsibilities to ensure that
 * each ReportCategory and Report are instanciated only once.
 * 
 * When a ReportCategory is registered against the ReportFactory, the ReportFactory assigns it an id. As expected this
 * id is meant to uniquely identify the said ReportCategory or Report, within the same application run. However the
 * implementation doesn't garantee that the ReportCategories or Reports will have exactly the same id between different
 * application run.
 * 
 * When a ReportCategory is unregistered the id doesn't become available again.
 * 
 * 
 * @author bsiri
 * 
 */

public final class ReportFactory {

	private static ReportFactory instance = null;

	private static List<ReportCategory> categories = new LinkedList<ReportCategory>();

	private static Integer idCategoryCounter = 0;

	private ReportFactory() {

	}

	/**
	 * @return the runnning instance of the ReportFactory
	 */
	public static synchronized ReportFactory getInstance() {
		if (instance == null) {
			instance = new ReportFactory();
		}

		return instance;
	}

	/**
	 * @return the list of all registered ReportCategory
	 */
	/*
	 * FIXME : make that immutable !
	 */
	public List<ReportCategory> getAllReportCategories() {
		return categories;
	}

	/**
	 * register a new ReportCategory
	 * 
	 * @param category
	 *            : a newly created instance of ReportCategory
	 */
	public static void addCategory(ReportCategory category) {
		registerCategory(category);
		categories.add(category);
	}

	/**
	 * remove a ReportCategory from the list of registered ReportCategory
	 * 
	 * @param category
	 *            : a previously registered ReportCategory to unregister.
	 */
	public static void removeCategory(ReportCategory category) {
		removeCategory(category.getId());
	}

	/**
	 * remove a ReportCategory from the list of registered ReportCategory
	 * 
	 * @param categoryId
	 *            : the id of the said category.
	 */
	public static void removeCategory(Integer categoryId) {
		ListIterator<ReportCategory> iterator = categories.listIterator();
		while (iterator.hasNext()) {
			ReportCategory category = iterator.next();
			if (category.getId().equals(categoryId)) {
				iterator.remove();
				return;
			}
		}
	}

	/**
	 * Will unregister a Report from its ReportCategory
	 * 
	 * @param report
	 *            : a previously registered Report to unregister
	 */
	public static void removeReport(Report report) {
		removeReport(report.getId());
	}

	/**
	 * Will unregister a Report from its ReportCategory
	 * 
	 * @param reportId
	 *            : the id of the said Report
	 */
	public static void removeReport(Integer reportId) {
		for (ReportCategory category : categories) {
			category.removeReport(reportId);
		}
	}

	/*
	 * this method attach an id to the category being registered
	 */
	private static void registerCategory(ReportCategory category) {
		category.setId(idCategoryCounter++);
	}

	/**
	 * Will return the bean instance of the ReportCategory referred to with this id.
	 * 
	 * @param id
	 *            the numeric id of the bean
	 * @return the bean if the id have been found, null otherwise.
	 */
	public ReportCategory findCategoryById(Integer id) {
		for (ReportCategory category : categories) {
			if (category.getId().equals(id)) {
				return category;
			}
		}
		return null;
	}

	/**
	 * Will return the bean instance of the Report referred to with this id.
	 * 
	 * @param reportId
	 *            the numeric id of the bean
	 * @return the bean if the id have been found, null otherwise.
	 */
	public Report findReportById(Integer reportId) {
		Report report = null;
		for (ReportCategory category : categories) {
			report = category.findReportById(reportId);
			if (report != null) {
				return report;
			}
		}
		return null;
	}
}
