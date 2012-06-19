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

import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.tm.web.internal.report.ReportsRegistry;

@Controller
@RequestMapping("/report-workspace")
public class ReportWorkspaceController {
	@Inject
	private ReportsRegistry reportsRegistry;

	@RequestMapping(method = RequestMethod.GET)
	public String showReportWorkspace(Model model) {
		model.addAttribute("categories", reportsRegistry.getCategories());
		model.addAttribute("reports", reportsRegistry.getReportsByCategory());

		return "report-workspace.html";
	}

	@RequestMapping(value = "/report", method = RequestMethod.GET, params = { "report" })
	public ModelAndView showReport(@RequestParam("report") Integer reportId) {

		// Report report = reportService.findReportById(reportId);

		ModelAndView mav = new ModelAndView("fragment/reports/edit-report");
		// mav.addObject("report", report);

		return mav;

	}

	// debug page
	@RequestMapping(value = "/report/info", method = RequestMethod.GET, params = { "report" })
	public ModelAndView showReportInfo(@RequestParam("report") Integer reportId) {

		// Report report = reportService.findReportById(reportId);

		ModelAndView mav = new ModelAndView("page/report-libraries/show-report");
		// mav.addObject("report", report);

		return mav;

	}

	/*
	 * will return an array containing the supported formats when exporting a given report for a given view
	 */

	@RequestMapping(value = "/report/export-options")
	public @ResponseBody
	String[] getExportFormats(@RequestParam("report") Integer reportId,
			@RequestParam(value = "view", required = false) Integer viewId) {
		// Report report = reportService.findReportById(reportId);

		String formats[];
		// if (viewId != null) {
		// formats = report.getViewCatalog().findView(viewId).getFormats();
		// } else {
		// formats = report.getViewCatalog().getDefaultView().getFormats();
		// }

		// return formats;
		return null;
	}

	/*
	 * 
	 * FIXME : find a way to make the reports completely independent of tm.web. That is, no resource bundle, no
	 * JasperReportsMultiFormatView etc.
	 * 
	 * for more informations about the localization of the report, see
	 * org.springframework.web.servlet.view.jasperreports.AbstractJasperReportsView:exposeLocalizationContext
	 */

	@RequestMapping(value = "/report/generate", method = RequestMethod.GET, params = { "report" })
	public ModelAndView generateReport(@RequestParam("report") Integer reportId,
			@RequestParam(value = "view", required = false) Integer viewId,
			@RequestParam(value = "format", required = false) String format, HttpServletRequest httpRequest,
			Locale locale) {

//		Report report = reportService.findReportById(reportId);
//
//		ReportQuery reportQuery = report.createReportQuery();
//
//		reportQuery = parseParams(reportQuery, httpRequest);
//
//		Collection<?> data = reportService.executeQuery(reportQuery);
//
//		String viewName;
//		if (viewId != null) {
//			viewName = report.getViewCatalog().findView(viewId).getModel();
//		} else {
//			viewName = report.getViewCatalog().getDefaultView().getModel();
//		}
//
//		String formatName;
//		if (format != null) {
//			formatName = format;
//		} else {
//			formatName = "html";
//		}
//
//		// test
//		ModelAndView mav = new ModelAndView(viewName);
//
//		// TODO to remove later
//		mav.getModel().put(JRParameter.REPORT_RESOURCE_BUNDLE,
//				new MessageSourceResourceBundle(reportMessageSource, locale));
//
//		mav.addObject("data", data);
//		mav.addObject("format", formatName);
//
//		return mav;
		return null;
	}


}
