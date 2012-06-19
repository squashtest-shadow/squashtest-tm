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

import java.util.Map;

import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.api.report.criteria.Criteria;
import org.squashtest.tm.api.report.form.Input;

/**
 * @author Gregory Fouquet
 * 
 */
public interface Report {

	/**
	 * @return the category
	 */
	StandardReportCategory getCategory();

	/**
	 * @return the type
	 */
	StandardReportType getType();

	/**
	 * @return the descriptionKey
	 */
	String getDescriptionKey();

	/**
	 * @return the views
	 */
	ReportView[] getViews();

	String getDescription();

	/**
	 * @return the form
	 */
	Input[] getForm();

	String getLabelKey();

	String getLabel();

	/**
	 * @param viewIndex
	 * @param format
	 * @param criteria
	 * @return 
	 */
	ModelAndView buildModelAndView(int viewIndex, String format, Map<String, Criteria> criteria);

}