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

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;

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
import org.squashtest.csp.core.infrastructure.collection.PagedCollectionHolder;
import org.squashtest.csp.core.infrastructure.collection.PagingAndSorting;
import org.squashtest.csp.tm.domain.Internationalizable;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.requirement.RequirementCriticality;
import org.squashtest.csp.tm.domain.requirement.RequirementStatus;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseExecutionMode;
import org.squashtest.csp.tm.service.RequirementModificationService;
import org.squashtest.csp.tm.web.internal.helper.JsonHelper;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableMapperPagingAndSortingAdapter;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModel;
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
	private final DataTableMapper verifyingTcMapper = new DataTableMapper("verifying-test-cases", TestCase.class,
			Project.class).initMapping(5).mapAttribute(Project.class, 2, "name", String.class)
			.mapAttribute(TestCase.class, 3, "name", String.class)
			.mapAttribute(TestCase.class, 4, "executionMode", TestCaseExecutionMode.class);

	// will return the Requirement in a full page
	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public ModelAndView showRequirementInfo(@PathVariable long requirementId, Locale locale) {

		Requirement requirement = requirementModService.findById(requirementId);

		ModelAndView mav = new ModelAndView("page/requirement-libraries/show-requirement");

		mav.addObject("requirement", requirement);

		// build criticality list
		SortedMap<String, String> criticalities = initCriticitySelectionList(locale, requirement.getCriticality());
		mav.addObject("criticalityList", jsonify(criticalities));

		return mav;
	}

	// will return the fragment only
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView showRequirement(@PathVariable long requirementId, Locale locale) {
		Requirement requirement = requirementModService.findById(requirementId);

		ModelAndView mav = new ModelAndView("fragment/requirements/edit-requirement");
		mav.addObject("requirement", requirement);

		// build criticality list
		SortedMap<String, String> criticalities = initCriticitySelectionList(locale, requirement.getCriticality());
		mav.addObject("criticalityList", jsonify(criticalities));

		return mav;
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=requirement-description", "value" })
	public @ResponseBody
	String updateDescription(@RequestParam("value") String newDescription, @PathVariable long requirementId) {

		requirementModService.changeDescription(requirementId, newDescription);
		LOGGER.trace("requirement " + requirementId + ": updated description to " + newDescription);
		return newDescription;

	}

	@RequestMapping(method = RequestMethod.POST, params = { "newName" })
	public @ResponseBody
	Object rename(HttpServletResponse response, @RequestParam("newName") String newName,
			@PathVariable long requirementId) {

		requirementModService.rename(requirementId, newName);
		LOGGER.info("RequirementModificationController : renaming " + requirementId + " as " + newName);
		return new Object() {
		};

	}

	@RequestMapping(value = "/general", method = RequestMethod.GET)
	public ModelAndView refreshGeneralInfos(@PathVariable long requirementId) {

		ModelAndView mav = new ModelAndView("fragment/generics/general-information-fragment");

		Requirement requirement = requirementModService.findById(requirementId);

		mav.addObject("auditableEntity", requirement);
		mav.addObject("entityContextUrl", "/requirements/" + requirementId);

		return mav;
	}

	private DataTableModel buildVerifyingTestCasesTableModel(PagedCollectionHolder<List<TestCase>> holder,
			String sEcho, Locale locale) {
		DataTableModel model = new DataTableModel(sEcho);
		String type = "";
		List<TestCase> testCases = holder.getPagedItems();

		for (int i = 0; i < testCases.size(); i++) {
			TestCase tc = testCases.get(i);

			type = formatExecutionMode(tc.getExecutionMode(), locale);

			model.addRow(new Object[] { tc.getId(), holder.getFirstItemIndex() + i + 1, tc.getProject().getName(),
					tc.getName(), type, "" });
		}

		model.displayRowsFromTotalOf(holder.getTotalNumberOfItems());
		return model;
	}

	@RequestMapping(value = "/verifying-test-cases-table", params = "sEcho")
	public @ResponseBody
	DataTableModel getVerifiedTestCasesTableModel(@PathVariable("requirementId") long requirementId,
			final DataTableDrawParameters params, Locale locale) {
		PagingAndSorting filter = new DataTableMapperPagingAndSortingAdapter(params, verifyingTcMapper);

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("ReqModController : filterin " + params.getsSortDir_0() + " for "
					+ verifyingTcMapper.pathAt(params.getiSortCol_0()));
		}

		PagedCollectionHolder<List<TestCase>> holder = requirementModService.findVerifyingTestCasesByRequirementId(
				requirementId, filter);

		return buildVerifyingTestCasesTableModel(holder, params.getsEcho(), locale);
	}

	private String formatExecutionMode(TestCaseExecutionMode mode, Locale locale) {
		return internationalize(mode, locale);
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=requirement-criticality", "value" })
	@ResponseBody
	public String updateCriticality(@RequestParam("value") String value, @PathVariable long requirementId, Locale locale) {
		RequirementCriticality criticality = RequirementCriticality.valueOf(value);
		requirementModService.changeCriticality(requirementId, criticality);
		LOGGER.debug("Requirement {} : requirement criticality changed, new value : {}", requirementId,
				criticality.name());
		return HtmlUtils.htmlEscape(formatCriticality(criticality, locale));
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=requirement-status", "value" })
	@ResponseBody
	public String updateStatus(@RequestParam("value") String value, @PathVariable long requirementId, Locale locale) {
		RequirementStatus status = RequirementStatus.valueOf(value);
		requirementModService.changeStatus(requirementId, status);
		LOGGER.debug("Requirement {} : requirement status changed, new value : {}", requirementId, status.name());
		return HtmlUtils.htmlEscape(internationalize(status, locale));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/next-status")
	@ResponseBody
	public SortedMap<String, String> getNextStatusList(Locale locale, @PathVariable long requirementId) {
		Requirement requirement = requirementModService.findById(requirementId);
		RequirementStatus status = requirement.getStatus();
		return initStatusSelectionList(locale, status);
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=requirement-reference", "value" })
	@ResponseBody
	String updateReference(@RequestParam("value") String requirementReference, @PathVariable long requirementId)
			throws UnsupportedEncodingException {
		requirementModService.changeReference(requirementId, requirementReference.trim());
		LOGGER.debug("Requirement {} : requirement reference changed, new value : {}", requirementId,
				requirementReference);
		return HtmlUtils.htmlEscape(requirementReference);
	}

	@RequestMapping(value = "/versions/new", method = RequestMethod.POST)
	@ResponseBody
	public void createNewVersion(@PathVariable long requirementId) {
		requirementModService.createNewVersion(requirementId);
	}

	/***
	 * Method which returns the criticality select options in the chosen language. The output is formatted in json and
	 * meant to be used for a select input. That list is sorted according to the RequirementCriticality level. @param
	 * locale the Locale
	 * 
	 * @return a map representing the available options.
	 */
	private SortedMap<String, String> initCriticitySelectionList(Locale locale, RequirementCriticality selected) {

		SortedMap<String, String> map = new TreeMap<String, String>(RequirementCriticality.stringComparator());

		for (RequirementCriticality criticality : RequirementCriticality.values()) {
			String translated = formatCriticality(criticality, locale);
			map.put(criticality.toString(), translated);
		}

		// we don't want to use the attribute 'selected' here because it induces wrong behaviors under FF or IE when
		// editing the same combobox multiple times.

		return map;

	}

	/**
	 * The change status combobox is filtered and only proposes the status to which it is legal to switch to. That
	 * method will generate a map for that purpose. Pretty much like
	 * {@link #initCriticitySelectionList(Locale, RequirementCriticality)};
	 * 
	 * @param locale
	 * @param status
	 * @return
	 */
	private SortedMap<String, String> initStatusSelectionList(Locale locale, RequirementStatus status) {
		SortedMap<String, String> map = new TreeMap<String, String>(RequirementStatus.stringComparator());

		for (RequirementStatus iterStatus : status.getAvailableNextStatus()) {
			map.put(iterStatus.toString(), internationalize(iterStatus, locale));
		}
		// here other status are added with the value "disabled."+"iterStatus
		for (RequirementStatus disabledStatus : status.getDisabledStatus()) {
			map.put("disabled." + disabledStatus.toString(), internationalize(disabledStatus, locale));
		}
		// here we use the 'selected' attribute since it's reloaded for each use of the combobox anyway.
		map.put("selected", status.toString());

		return map;

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

	private String jsonify(Object toSerialize) {
		return JsonHelper.serialize(toSerialize);
	}

}
