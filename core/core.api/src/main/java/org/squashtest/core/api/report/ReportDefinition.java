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

package org.squashtest.core.api.report;

import javax.annotation.PostConstruct;

import org.squashtest.core.api.report.form.FormDefinition;
import org.squashtest.core.api.report.form.InputDefinition;

/**
 * @author bsiri
 * @author Gregory Fouquet
 * 
 */
public class ReportDefinition {
	private ReportCategory category = ReportCategory.VARIOUS;
	private ReportType type = ReportType.GENERIC;

	private String nameKey;
	private String descriptionKey;
	private ReportView[] views = {};
	private int defaultViewIndex = 0;
	private InputDefinition[] form = {};

	/**
	 * @return the category
	 */
	public ReportCategory getCategory() {
		return category;
	}

	/**
	 * @param category
	 *            the category to set
	 */
	public void setCategory(ReportCategory category) {
		this.category = category;
	}

	/**
	 * @return the type
	 */
	public ReportType getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(ReportType type) {
		this.type = type;
	}

	/**
	 * @return the nameKey
	 */
	public String getNameKey() {
		return nameKey;
	}

	/**
	 * @param nameKey
	 *            the nameKey to set
	 */
	public void setNameKey(String nameKey) {
		this.nameKey = nameKey;
	}

	/**
	 * @return the descriptionKey
	 */
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
	 * @return the views
	 */
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
}
