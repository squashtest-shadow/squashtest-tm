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

package org.squashtest.tm.api.report;

import javax.annotation.PostConstruct;

import org.squashtest.tm.api.report.form.Input;
import org.squashtest.tm.core.i18n.Labelled;

/**
 * @author bsiri
 * @author Gregory Fouquet
 * 
 */
public class BasicReport extends Labelled implements Report {
	private StandardReportCategory category = StandardReportCategory.VARIOUS;
	private StandardReportType type = StandardReportType.GENERIC;

	private String descriptionKey;
	private ReportView[] views = {};
	private int defaultViewIndex = 0;
	private Input[] form = {};

	/**
	 * @see org.squashtest.tm.api.report.Report#getCategory()
	 */
	@Override
	public StandardReportCategory getCategory() {
		return category;
	}

	/**
	 * @param category
	 *            the category to set
	 */
	public void setCategory(StandardReportCategory category) {
		this.category = category;
	}

	/**
	 * @see org.squashtest.tm.api.report.Report#getType()
	 */
	@Override
	public StandardReportType getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(StandardReportType type) {
		this.type = type;
	}

	/**
	 * @see org.squashtest.tm.api.report.Report#getDescriptionKey()
	 */
	@Override
	public String getDescriptionKey() {
		return descriptionKey;
	}

	/**
	 * @param descriptionKey
	 *            the descriptionKey to set
	 */
	public void setDescriptionKey(String descriptionKey) {
		this.descriptionKey = descriptionKey;
	}

	@PostConstruct
	protected void checkState() {
	}

	/**
	 * @param views
	 *            the views to set
	 */
	public void setViews(ReportView[] views) {
		this.views = views;
	}

	/**
	 * @see org.squashtest.tm.api.report.Report#getViews()
	 */
	@Override
	public ReportView[] getViews() {
		return views;
	}

	/**
	 * @param defaultViewIndex
	 *            the defaultViewIndex to set
	 */
	public void setDefaultViewIndex(int defaultViewIndex) {
		this.defaultViewIndex = defaultViewIndex;
	}

	/**
	 * @see org.squashtest.tm.api.report.Report#getDescription()
	 */
	@Override
	public String getDescription() {
		return getMessage(descriptionKey);
	}

	/**
	 * @param form
	 *            the form to set
	 */
	public void setForm(Input[] form) {
		this.form = form;
	}

	/**
	 * @see org.squashtest.tm.api.report.Report#getForm()
	 */
	@Override
	public Input[] getForm() {
		return form;
	}

	/**
	 * @return the defaultViewIndex
	 */
	public int getDefaultViewIndex() {
		return defaultViewIndex;
	}
}
