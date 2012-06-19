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

package org.squashtest.csp.tm.web.internal.controller.report;

import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.tm.web.internal.report.ReportsRegistry;
import org.squashtest.csp.tm.web.internal.report.criteria.FormToCriteriaConverter;
import org.squashtest.tm.api.report.Report;
import org.squashtest.tm.api.report.criteria.Criteria;

/**
 * @author Gregory Fouquet
 * 
 */
@Controller
@RequestMapping("/reports/{namespace}/{index}")
public class ReportController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ReportController.class);
	@Inject
	private ReportsRegistry reportsRegistry;

	/**
	 * Populates model and returns the fragment panel showing a report.
	 * 
	 * @param namespace
	 *            namespace of the report
	 * @param index
	 *            0-based index of the report in its namespace
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/panel", method = RequestMethod.GET)
	public String showReportPanel(@PathVariable String namespace, @PathVariable int index, Model model) {
		populateModelWithReport(namespace, index, model);
		return "contextual-report.html";
	}

	private void populateModelWithReport(String namespace, int index, Model model) {
		Report report = reportsRegistry.findReport(namespace, index);
		model.addAttribute("report", report);
	}

	/**
	 * Populates model and returns the full page showing a report.
	 * 
	 * @param namespace
	 *            namespace of the report
	 * @param index
	 *            0-based index of the report in its namespace
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/viewer", method = RequestMethod.GET)
	public String showReportViewer(@PathVariable String namespace, @PathVariable int index, Model model) {
		populateModelWithReport(namespace, index, model);
		model.addAttribute("hasBackButton", Boolean.TRUE);
		return "report-viewer.html";
	}
	
	@RequestMapping(value="/views/{viewIndex}/formats/{format}", method = RequestMethod.POST)
	public ModelAndView generateReportView(@PathVariable String namespace, @PathVariable int index, @PathVariable int viewIndex, @PathVariable String format, @RequestBody Map<String, Object> form) {
		LOGGER.debug(form.toString());
		Map<String, Criteria> crit = (new FormToCriteriaConverter()).convert(form);
		LOGGER.debug(crit.toString());
		
		Report report = reportsRegistry.findReport(namespace, index);
		return report.buildModelAndView(viewIndex, format, crit);
	}
}
