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

import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

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
import org.springframework.web.util.HtmlUtils;
import org.squashtest.csp.tm.domain.Internationalizable;
import org.squashtest.csp.tm.domain.Level;
import org.squashtest.csp.tm.domain.requirement.RequirementCriticality;
import org.squashtest.csp.tm.domain.requirement.RequirementStatus;
import org.squashtest.csp.tm.domain.requirement.RequirementVersion;
import org.squashtest.csp.tm.service.RequirementVersionManagerService;
import org.squashtest.csp.tm.web.internal.helper.LevelLabelFormatter;

/**
 * Controller which receives requirement version management related requests.
 * 
 * @author Gregory Fouquet
 * 
 */
@Controller
@RequestMapping("/requirement-versions/{requirementVersionId}")
public class RequirementVersionManagerController {
	private static final Logger LOGGER = LoggerFactory.getLogger(RequirementModificationController.class);

	@Inject
	private Provider<RequirementCriticalityComboDataBuilder> criticalityComboBuilderProvider;
	@Inject
	private Provider<RequirementStatusComboDataBuilder> statusComboDataBuilderProvider;
	@Inject
	private Provider<LevelLabelFormatter> levelFormatterProvider;

	private RequirementVersionManagerService requirementVersionManager;

	/**
	 * @param requirementVersionManagerService
	 *            the requirementVersionManagerService to set
	 */
	@ServiceReference
	public void setRequirementVersionManager(RequirementVersionManagerService requirementVersionManagerService) {
		this.requirementVersionManager = requirementVersionManagerService;
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=requirement-description", VALUE })
	public @ResponseBody
	String updateDescription(@PathVariable long requirementVersionId, @RequestParam(VALUE) String newDescription) {
		requirementVersionManager.changeDescription(requirementVersionId, newDescription);
		return newDescription;

	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=requirement-criticality", VALUE })
	@ResponseBody
	public String updateCriticality(@PathVariable long requirementVersionId,
			@RequestParam(VALUE) RequirementCriticality criticality, Locale locale) {
		requirementVersionManager.changeCriticality(requirementVersionId, criticality);
		return HtmlUtils.htmlEscape(internationalize(criticality, locale));
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=requirement-status", VALUE })
	@ResponseBody
	public String updateStatus(@PathVariable long requirementVersionId, @RequestParam(VALUE) String value, Locale locale) {
		RequirementStatus status = RequirementStatus.valueOf(value);
		requirementVersionManager.changeStatus(requirementVersionId, status);
		return HtmlUtils.htmlEscape(internationalize(status, locale));
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=requirement-reference", VALUE })
	@ResponseBody
	String updateReference(@PathVariable long requirementVersionId, @RequestParam(VALUE) String requirementReference) {
		requirementVersionManager.changeReference(requirementVersionId, requirementReference.trim());
		return HtmlUtils.htmlEscape(requirementReference);
	}

	/**
	 * @param criticality
	 * @param locale
	 * @return
	 */
	private String internationalize(Level level, Locale locale) {
		return levelFormatterProvider.get().useLocale(locale).formatLabel(level);
	}

	@RequestMapping(value="/editor-fragment", method = RequestMethod.GET)
	public String getRequirementEditor(@PathVariable long requirementVersionId, Model model, Locale locale) {
		RequirementVersion requirementVersion = requirementVersionManager.findById(requirementVersionId);
		model.addAttribute("requirementVersion", requirementVersion);

		String criticalities = buildMarshalledCriticalities(locale);
		model.addAttribute("jsonCriticalities", criticalities);

		return "fragment/requirements/requirement-version-editor";
	}

	@RequestMapping(method = RequestMethod.GET, value = "/next-status")
	@ResponseBody
	public Map<String, String> getNextStatusList(Locale locale, @PathVariable long requirementVersionId) {
		RequirementVersion requirementVersion = requirementVersionManager.findById(requirementVersionId);
		RequirementStatus status = requirementVersion.getStatus();
		
		return statusComboDataBuilderProvider.get().useLocale(locale).selectItem(status).buildMap();

	}
	
	@RequestMapping(value = "/general", method = RequestMethod.GET)
	public String showGeneralInfos(@PathVariable long requirementVersionId, Model model) {
		RequirementVersion version = requirementVersionManager.findById(requirementVersionId);

		model.addAttribute("auditableEntity", version);
		model.addAttribute("entityContextUrl", "/requirement-versions/" + requirementVersionId);

		return "fragment/generics/general-information-fragment";
	}

//	@RequestMapping(value = "/editor", method = RequestMethod.GET)
//	public String showInlinedEditor(@PathVariable long requirementVersionId, Model model, Locale locale) {
//		RequirementVersion version = requirementVersionManager.findById(requirementVersionId);
//
//		model.addAttribute("requirementVersion", version);
//		model.addAttribute("jsonCriticalities", buildMarshalledCriticalities(locale));
//
//		return "page/requirements/requirement-version-editor";
//	}

	private String buildMarshalledCriticalities(Locale locale) {
		return criticalityComboBuilderProvider.get().useLocale(locale).buildMarshalled();
	}
}
