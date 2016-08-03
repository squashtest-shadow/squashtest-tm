/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.io.IOUtils;
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
import org.squashtest.tm.api.report.form.Input;
import org.squashtest.tm.api.report.form.InputType;
import org.squashtest.tm.api.report.form.OptionInput;
import org.squashtest.tm.api.report.form.RadioButtonsGroup;
import org.squashtest.tm.api.report.form.composite.TagPickerOption;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.service.customfield.CustomFieldManagerService;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.web.internal.helper.JsonHelper;
import org.squashtest.tm.web.internal.model.jquery.FilterModel;
import org.squashtest.tm.web.internal.report.ReportsRegistry;
import org.squashtest.tm.web.internal.report.criteria.ConciseFormToCriteriaConverter;
import org.squashtest.tm.web.internal.report.criteria.FormToCriteriaConverter;

import com.lowagie.text.pdf.codec.Base64;


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
	private CustomFieldManagerService customFieldFinder;

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

		// used for tag picker prefilling.
		populateTagsIfRequired(report, model);

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
		Map<String, Criteria> crit = new FormToCriteriaConverter().convert(form);

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
		Map<String, Criteria> crit = new ConciseFormToCriteriaConverter(report, projects).convert(form);


		ModelAndView mav;
		if ("docx".equals(format)){
			Map<String,Object> model = report.buildModelAndView(viewIndex, format, crit).getModel();
			mav = new ModelAndView("docx.html");
			mav.addObject("model", model.get("data"));
			mav.addObject("html", model.get("html"));
			mav.addObject("fileName", model.get("fileName"));
			mav.addObject("namespace",namespace);
			mav.addObject("index",index);
			mav.addObject("viewIndx", viewIndex);
		} else {
			mav = report.buildModelAndView(viewIndex, format, crit);
		}

		return mav;

	}


	@RequestMapping(value = "/views/{viewIndex}/docxtemplate", method = RequestMethod.GET)
	public void getTemplate(@PathVariable String namespace, @PathVariable int index, @PathVariable int viewIndex, HttpServletRequest request, HttpServletResponse response) throws Exception{

		Report report = reportsRegistry.findReport(namespace, index);
		report.getViews()[viewIndex].getSpringView().render(null, request, response);

	}




	public static byte[] decompress(byte[] str) throws IOException {
		byte[] buf = new byte[str.length*10];
		GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(str));
		int len;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		while ((len = gis.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		return out.toByteArray();
	}

	public static byte[] base64Decoding(String str) {
		return Base64.decode(str);
	}



	@RequestMapping(value = "/ie9", method = RequestMethod.POST)
	public void ie9Sucks(HttpServletResponse response, @RequestParam("b64") String b64, @RequestParam("fileName") String fileName) throws IOException{

		final File tempFile = File.createTempFile(fileName, ".tmp");
		tempFile.deleteOnExit();
		byte decoded[] = base64Decoding(b64);

		FileOutputStream fos = new FileOutputStream(tempFile);
		fos.write(decoded);
		fos.close();


		InputStream in = new BufferedInputStream(new FileInputStream(tempFile));

		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename="+fileName+".docx");
		IOUtils.copy(in, response.getOutputStream());
		response.flushBuffer();
		in.close();

	}

	// TODO : make it work for the regular tagpicker. Currently it only
	// works for radiobutton-grouped-pickers.
	// Also lots of wtf because on the rush
	private void populateTagsIfRequired(Report report, Model model){
		// also supply the list of available tags if the tag picker is used
		for (Input input : report.getForm()){
			if (input.getType() == InputType.RADIO_BUTTONS_GROUP){
				RadioButtonsGroup container = (RadioButtonsGroup)input;
				for (OptionInput opt : container.getOptions()){
					if (opt instanceof TagPickerOption){
						String entityType = ((TagPickerOption)opt).getPickerBoundEntity();
						List<String> availableTags = getPossibleTagValues(entityType);
						if (availableTags.isEmpty()){
							availableTags = null;
						}
						model.addAttribute("availableTags", availableTags);
						return; 	// OOOOh !
					}
				}
			}
		}
	}

	public List<String> getPossibleTagValues(String boundEntity){

		List<Long> projectIds = (List<Long>) CollectionUtils.collect(projectFinder.findAllOrderedByName(), new Transformer() {

			@Override
			public Object transform(Object input) {
				return 	((GenericProject) input).getId();
			}
		});

		return customFieldFinder.getAvailableTagsForEntity(boundEntity, projectIds);
	}

}
