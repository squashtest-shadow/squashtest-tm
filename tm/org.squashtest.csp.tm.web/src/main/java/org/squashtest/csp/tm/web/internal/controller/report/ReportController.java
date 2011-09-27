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

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import net.sf.jasperreports.engine.JRParameter;

import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceResourceBundle;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.tm.domain.report.Report;
import org.squashtest.csp.tm.domain.report.ReportCategory;
import org.squashtest.csp.tm.domain.report.query.ReportQuery;
import org.squashtest.csp.tm.service.ReportService;

@Controller
@RequestMapping("/report-workspace")
public class ReportController {

	private ReportService reportService;
	
	@Inject
	private MessageSource reportMessageSource;

	@ServiceReference
	public void setReportService(ReportService reportService) {
		this.reportService = reportService;
	}

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView showReportWorkspace() {
		List<ReportCategory> categoryList = reportService.findAllReportCategories();

		ModelAndView mav = new ModelAndView("page/report-workspace");
		mav.addObject("categories", categoryList);

		return mav;
	}

	@RequestMapping(value = "/report", method = RequestMethod.GET, params = { "report" })
	public ModelAndView showReport(@RequestParam("report") Integer reportId) {

		Report report = reportService.findReportById(reportId);

		ModelAndView mav = new ModelAndView("fragment/reports/edit-report");
		mav.addObject("report", report);

		return mav;

	}

	//debug page
	@RequestMapping(value = "/report/info", method = RequestMethod.GET, params = { "report" })
	public ModelAndView showReportInfo(@RequestParam("report") Integer reportId) {

		Report report = reportService.findReportById(reportId);

		ModelAndView mav = new ModelAndView("page/report-libraries/show-report");
		mav.addObject("report", report);

		return mav;

	}
	
	
	/*
	 * will return an array containing the supported formats when exporting a given report for a given view 
	 * 
	 */
	
	@RequestMapping(value="/report/export-options")
	public @ResponseBody String[] getExportFormats(@RequestParam("report") Integer reportId, 
			@RequestParam(value="view", required=false) Integer viewId){
		Report report = reportService.findReportById(reportId);
		
		String formats[];
		if (viewId!=null){
			formats = report.getViewCatalog().findView(viewId).getFormats();
		}else{
			formats = report.getViewCatalog().getDefaultView().getFormats();
		}
		
		return formats;
	}
	


	/*
	 * 
	 * FIXME : find a way to make the reports completely independent of tm.web. That is, no resource bundle, 
	 * no JasperReportsMultiFormatView etc.
	 * 
	 * for more informations about the localization of the report, see 
	 * org.springframework.web.servlet.view.jasperreports.AbstractJasperReportsView:exposeLocalizationContext 
	 *
	 */

	@RequestMapping(value = "/report/generate", method = RequestMethod.GET, params = { "report" })
	public ModelAndView generateReport(@RequestParam("report")Integer reportId, 
										@RequestParam(value="view", required=false) Integer viewId, 
										@RequestParam(value="format", required=false) String format,
										HttpServletRequest httpRequest, 
										Locale locale) {

		Report report = reportService.findReportById(reportId);

		ReportQuery reportQuery = report.createReportQuery();

		reportQuery = parseParams(reportQuery, httpRequest);

		Collection<?> data = reportService.executeQuery(reportQuery);

		String viewName;
		if (viewId!=null){
			viewName = report.getViewCatalog().findView(viewId).getModel();
		}else{
			viewName = report.getViewCatalog().getDefaultView().getModel();
		}
		
		String formatName;
		if (format!=null){
			formatName=format;
		}
		else{
			formatName="html";
		}

		//test
		ModelAndView mav = new ModelAndView(viewName);
		
		//TODO to remove later
		mav.getModel().put(JRParameter.REPORT_RESOURCE_BUNDLE, new MessageSourceResourceBundle(reportMessageSource, locale));

		
		
		mav.addObject("data",data);
		mav.addObject("format",formatName);
		
	
		return mav;
	}

	
	
	/* **************************** private stuffs **************************************** */
	


	private ReportQuery parseParams(ReportQuery reportQuery, HttpServletRequest httpRequest){

		Map<String, String[]> paramMap = httpRequest.getParameterMap();

		Set<Entry<String, String[]>> entries = paramMap.entrySet();

		for (Entry<String, String[]> entry : entries){
			if (reportQuery.isCriterionExists(entry.getKey())){
				reportQuery.setCriterion(entry.getKey(), (Object[]) entry.getValue());
			}
		}

		return reportQuery;
	}
	
	



	
}
