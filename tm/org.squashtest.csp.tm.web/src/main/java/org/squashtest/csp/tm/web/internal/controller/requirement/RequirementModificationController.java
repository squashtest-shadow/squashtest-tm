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

import static org.squashtest.csp.tm.web.internal.helper.JEditablePostParams.VALUE;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;
import org.squashtest.csp.tm.domain.Internationalizable;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.requirement.RequirementCriticality;
import org.squashtest.csp.tm.domain.requirement.RequirementStatus;
import org.squashtest.csp.tm.service.RequirementModificationService;

@Controller
@RequestMapping("/requirements/{requirementId}")
public class RequirementModificationController {

	private static final Logger LOGGER = LoggerFactory.getLogger(RequirementModificationController.class);

	@Inject
	private Provider<RequirementCriticalityComboDataBuilder> criticalityComboBuilderProvider;
	@Inject
	private Provider<RequirementStatusComboDataBuilder> statusComboDataBuilderProvider;

	private RequirementModificationService requirementModService;

	@ServiceReference
	public void setRequirementModificationService(RequirementModificationService service) {
		requirementModService = service;
	}

	@Inject
	private MessageSource messageSource;

	// will return the Requirement in a full page
	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public ModelAndView showRequirementInfo(@PathVariable long requirementId, Locale locale) {

		Requirement requirement = requirementModService.findById(requirementId);

		ModelAndView mav = new ModelAndView("page/requirement-libraries/show-requirement");

		mav.addObject("requirement", requirement);

		String criticalities = buildMarshalledCriticalities(locale);
		mav.addObject("criticalityList", criticalities);

		return mav;
	}

	private String buildMarshalledCriticalities(Locale locale) {
		String criticalities = criticalityComboBuilderProvider.get().useLocale(locale).buildMarshalled();
		return criticalities;
	}

	// will return the fragment only
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView showRequirement(@PathVariable long requirementId, Locale locale) {
		Requirement requirement = requirementModService.findById(requirementId);

		ModelAndView mav = new ModelAndView("fragment/requirements/edit-requirement");
		mav.addObject("requirement", requirement);

		// build criticality list
		String criticalities = buildMarshalledCriticalities(locale);
		mav.addObject("criticalityList", criticalities);

		return mav;
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=requirement-description", VALUE })
	public @ResponseBody
	String updateDescription(@RequestParam(VALUE) String newDescription, @PathVariable long requirementId) {

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

	@RequestMapping(method = RequestMethod.POST, params = { "id=requirement-criticality", VALUE })
	@ResponseBody
	public String updateCriticality(@RequestParam(VALUE) String value, @PathVariable long requirementId, Locale locale) {
		RequirementCriticality criticality = RequirementCriticality.valueOf(value);
		requirementModService.changeCriticality(requirementId, criticality);
		LOGGER.debug("Requirement {} : requirement criticality changed, new value : {}", requirementId,
				criticality.name());
		return HtmlUtils.htmlEscape(formatCriticality(criticality, locale));
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=requirement-status", VALUE })
	@ResponseBody
	public String updateStatus(@RequestParam(VALUE) String value, @PathVariable long requirementId, Locale locale) {
		RequirementStatus status = RequirementStatus.valueOf(value);
		requirementModService.changeStatus(requirementId, status);
		LOGGER.debug("Requirement {} : requirement status changed, new value : {}", requirementId, status.name());
		return HtmlUtils.htmlEscape(internationalize(status, locale));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/next-status")
	@ResponseBody
	public Map<String, String> getNextStatusList(Locale locale, @PathVariable long requirementId) {
		Requirement requirement = requirementModService.findById(requirementId);
		RequirementStatus status = requirement.getStatus();
		return initStatusSelectionList(locale, status);
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=requirement-reference", VALUE })
	@ResponseBody
	public String updateReference(@RequestParam(VALUE) String requirementReference, @PathVariable long requirementId)
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

	/**
	 * The change status combobox is filtered and only proposes the status to which it is legal to switch to. That
	 * method will generate a map for that purpose. Pretty much like
	 * {@link #initCriticitySelectionList(Locale, RequirementCriticality)};
	 *
	 * @param locale
	 * @param status
	 * @return
	 */
	private Map<String, String> initStatusSelectionList(Locale locale, RequirementStatus status) {
		return statusComboDataBuilderProvider.get().useLocale(locale).selectItem(status).buildMap();

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

	@RequestMapping(value = "/versions/manager", method = RequestMethod.GET)
	public String showRequirementVersionsManager(@PathVariable long requirementId, Model model, Locale locale) {
		Requirement req = requirementModService.findById(requirementId);

		model.addAttribute("requirement", req);
		model.addAttribute("versions", req.getUnmodifiableVersions());
		model.addAttribute("selectedVersion", req.getCurrentVersion());
		model.addAttribute("jsonCriticalities", buildMarshalledCriticalities(locale));

		return "page/requirements/versions-manager";
	}

}
