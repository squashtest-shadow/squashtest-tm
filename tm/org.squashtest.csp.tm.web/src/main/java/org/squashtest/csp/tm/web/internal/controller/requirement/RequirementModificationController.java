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
package org.squashtest.csp.tm.web.internal.controller.requirement;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;
import org.squashtest.csp.tm.domain.Internationalizable;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.requirement.RequirementCriticality;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseExecutionMode;
import org.squashtest.csp.tm.infrastructure.filter.CollectionFilter;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.csp.tm.service.RequirementModificationService;
import org.squashtest.csp.tm.web.internal.combo.OptionTag;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.csp.tm.web.internal.model.jquery.JsonSimpleData;
import org.squashtest.csp.tm.web.internal.model.viewmapper.DataTableMapper;

@Controller
@RequestMapping("/requirements/{requirementId}")
public class RequirementModificationController {
	private static final Logger LOGGER = LoggerFactory.getLogger(RequirementModificationController.class);

	private RequirementModificationService requirementModService;



	@ServiceReference
	public void setRequirementModificationService(RequirementModificationService service) {
		requirementModService = service;
	}

	@Inject
	private MessageSource messageSource;

	/*
	 * in case the advanced one fails, uncomment and use this mapper instead
	 */
	private final DataTableMapper verifyingTcMapper = new DataTableMapper("verifying-test-cases", TestCase.class, Project.class)
													.initMapping(5)
													.mapAttribute(Project.class, 2, "name", String.class)
													.mapAttribute(TestCase.class, 3, "name", String.class)
													.mapAttribute(TestCase.class, 4, "executionMode", TestCaseExecutionMode.class);

	// will return the Requirement in a full page
	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public ModelAndView showRequirementInfo(@PathVariable long requirementId, Locale locale) {

		Requirement requirement = requirementModService.find(requirementId);

		ModelAndView mav = new ModelAndView("page/requirement-libraries/show-requirement");

		mav.addObject("requirement", requirement);

		mav.addObject("criticalityList", initCriticitySelectionList(locale));
		return mav;
	}

	// will return the fragment only
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView showRequirement(@PathVariable long requirementId, Locale locale) {
		Requirement requirement = requirementModService.find(requirementId);

		ModelAndView mav = new ModelAndView("fragment/requirements/edit-requirement");
		mav.addObject("requirement", requirement);
		//build criticality list
		mav.addObject("criticalityList", initCriticitySelectionList(locale));
		return mav;
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=requirement-description", "value" })
	public @ResponseBody
	String updateDescription(@RequestParam("value") String newDescription, @PathVariable long requirementId) {

		requirementModService.updateDescription(requirementId, newDescription);
		LOGGER.trace("requirement " + requirementId + ": updated description to " + newDescription);
		return newDescription;

	}

	@RequestMapping(method = RequestMethod.POST, params = { "newName" })
	public @ResponseBody
	String rename(HttpServletResponse response, @RequestParam("newName") String newName,
			@PathVariable long requirementId) {

		requirementModService.rename(requirementId, newName);
		LOGGER.info("RequirementModificationController : renaming " + requirementId + " as " + newName);
		return new JsonSimpleData().addAttr("newName", HtmlUtils.htmlEscape(newName)).toString();

	}


	@RequestMapping(value = "/general", method = RequestMethod.GET)
	public ModelAndView refreshGeneralInfos(@PathVariable long requirementId) {

		ModelAndView mav = new ModelAndView("fragment/generics/general-information-fragment");

		Requirement requirement = requirementModService.find(requirementId);

		mav.addObject("auditableEntity", requirement);
		mav.addObject("entityContextUrl", "/requirements/" + requirementId);

		return mav;
	}

	private DataTableModel buildVerifyingTestCasesTableModel(FilteredCollectionHolder<List<TestCase>> holder,
			CollectionFilter filter,
			String sEcho,
			Locale locale) {
		DataTableModel model = new DataTableModel(sEcho);
		String type = "";
		List<TestCase> testCases = holder.getFilteredCollection();

		for (int i = 0; i < testCases.size(); i++) {
			TestCase tc = testCases.get(i);

			type=formatExecutionMode(tc.getExecutionMode(), locale);

			model.addRow(new Object[] { tc.getId(), filter.getFirstItemIndex()+ i+1,  tc.getProject().getName(), tc.getName(), type, "" });
		}

		model.displayRowsFromTotalOf(holder.getUnfilteredResultCount());
		return model;
	}

	@RequestMapping(value = "/verifying-test-cases-table", params = "sEcho")
	public @ResponseBody
	DataTableModel getVerifiedTestCasesTableModel(@PathVariable("requirementId") long requirementId,
			final DataTableDrawParameters params, Locale locale) {
		CollectionSorting filter = createCollectionFilter(params, verifyingTcMapper);

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("ReqModController : filterin " + params.getsSortDir_0() + " for "
					+ verifyingTcMapper.pathAt(params.getiSortCol_0()));
		}

		FilteredCollectionHolder<List<TestCase>> holder = requirementModService.findVerifyingTestCasesByRequirementId(
				requirementId, filter);

		return buildVerifyingTestCasesTableModel(holder, filter, params.getsEcho(), locale);
	}

	private CollectionSorting createCollectionFilter(final DataTableDrawParameters params,
			final DataTableMapper dtMapper) {
		CollectionSorting filter = new CollectionSorting() {
			@Override
			public int getMaxNumberOfItems() {
				return params.getiDisplayLength();
			}

			@Override
			public int getFirstItemIndex() {
				return params.getiDisplayStart();
			}

			@Override
			public String getSortedAttribute() {
				return dtMapper.pathAt(params.getiSortCol_0());
			}

			@Override
			public String getSortingOrder() {
				return params.getsSortDir_0();
			}
		};
		return filter;
	}

	private String formatExecutionMode(TestCaseExecutionMode mode, Locale locale){
		return internationalize(mode, locale);
	}

	@RequestMapping(method = RequestMethod.POST, params = "requirementCriticality")
	@ResponseBody
	public void updateCriticality(@RequestParam String requirementCriticality, @PathVariable long requirementId){
		RequirementCriticality criticality = RequirementCriticality.valueOf(requirementCriticality);
		requirementModService.updateRequirementCriticality(requirementId, criticality);
		LOGGER.debug("Requirement {} : requirement criticality changed, new value : {}", requirementId, criticality.name());
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=requirement-reference", "value" })
	@ResponseBody
	String updateReference(@RequestParam("value") String requirementReference, @PathVariable long requirementId) {
		requirementModService.updateRequirementReference(requirementId, requirementReference.trim());
		LOGGER.debug("Requirement {} : requirement reference changed, new value : {}", requirementId,
				requirementReference);
		return requirementReference;
	}

	/***
	 * Method which returns the criticality select options in the chosen language
	 * @param locale the Locale
	 * @return an optionTag list with label and value
	 */
	private List<OptionTag> initCriticitySelectionList(Locale locale)
	{
		List<OptionTag> toReturn = new ArrayList<OptionTag>();
		for (RequirementCriticality criticality : RequirementCriticality.values()) {
			toReturn.add(new OptionTag(formatCriticality(criticality, locale), criticality.toString()));
		}
		return toReturn;
	}

	/***
	 * Method which returns criticality in the chosen language
	 *
	 * @param criticality
	 *            the criticality
	 * @param locale
	 *            the locale with the chosen language
	 * @return the criticality in the chosen language
	 */
	private String formatCriticality(RequirementCriticality criticality, Locale locale) {
		return internationalize(criticality, locale);
	}

	private String internationalize(Internationalizable internationalizable, Locale locale) {
		return messageSource.getMessage(internationalizable.getI18nKey(), null, locale);
	}

}
