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
package org.squashtest.tm.web.internal.controller.report;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.api.report.Report;
import org.squashtest.tm.api.report.criteria.Criteria;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.web.internal.helper.JsonHelper;
import org.squashtest.tm.web.internal.model.jquery.FilterModel;
import org.squashtest.tm.web.internal.report.ReportsRegistry;
import org.squashtest.tm.web.internal.report.criteria.ConciseFormToCriteriaConverter;
import org.squashtest.tm.web.internal.report.criteria.FormToCriteriaConverter;

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

	@Inject
	private ProjectFinder projectFinder;

	@Inject
	@Value("${report.criteria.project.multiselect:false}")
	private boolean projectMultiselect;

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
		// XXX shouldnt these 2 lines go in populateMWR ? check if "report viewer" works as expected (see
		// showReportViexwer)
		model.addAttribute("projectMultiselect", projectMultiselect);
		model.addAttribute("projectFilterModel", findProjectsModels());
		return "report-panel.html";
	}

	private FilterModel findProjectsModels() {
		List<Project> projects = projectFinder.findAllOrderedByName();
		return new FilterModel(projects);
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

	/**
	 * Generates report view from a standard post with a data attribute containing a serialized JSON form.
	 * 
	 * @param namespace
	 * @param index
	 * @param viewIndex
	 * @param format
	 * @param data
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @deprecated since #3762 #getReportView should be called by gui
	 */
	@Deprecated
	@RequestMapping(value = "/views/{viewIndex}/formats/{format}", method = RequestMethod.GET, params = { "parameters" })
	public ModelAndView generateReportViewUsingGet(@PathVariable String namespace, @PathVariable int index,
			@PathVariable int viewIndex, @PathVariable String format, @RequestParam("parameters") String parameters)
					throws JsonParseException, JsonMappingException, IOException {
		Map<String, Object> form = JsonHelper.deserialize(parameters);
		Map<String, Criteria> crit = (new FormToCriteriaConverter()).convert(form);

		Report report = reportsRegistry.findReport(namespace, index);
		return report.buildModelAndView(viewIndex, format, crit);

	}

	@RequestMapping(value = "/views/{viewIndex}/formats/{format}", method = RequestMethod.GET, params = { "json" })
	public ModelAndView getReportView(@PathVariable String namespace, @PathVariable int index,
			@PathVariable int viewIndex, @PathVariable String format, @RequestParam("json") String parameters)
					throws JsonParseException, JsonMappingException, IOException {
		Map<String, Object> form = JsonHelper.deserialize(parameters);
		Report report = reportsRegistry.findReport(namespace, index);
		List<Project> projects = projectFinder.findAllOrderedByName();
		Map<String, Criteria> crit = (new ConciseFormToCriteriaConverter(report, projects)).convert(form);

		return report.buildModelAndView(viewIndex, format, crit);

	}

}
