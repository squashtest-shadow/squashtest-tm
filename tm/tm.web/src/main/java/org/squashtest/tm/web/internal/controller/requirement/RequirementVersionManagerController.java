/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.requirement;

import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;
import org.squashtest.tm.domain.Level;
import org.squashtest.tm.domain.requirement.RequirementCategory;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.service.customfield.CustomFieldValueFinderService;
import org.squashtest.tm.service.requirement.RequirementVersionManagerService;
import org.squashtest.tm.web.internal.helper.InternationalisableLabelFormatter;
import org.squashtest.tm.web.internal.helper.LevelLabelFormatter;

/**
 * Controller which receives requirement version management related requests.
 *
 * @author Gregory Fouquet
 *
 */
@Controller
@RequestMapping("/requirement-versions/{requirementVersionId}")
public class RequirementVersionManagerController {
	@Inject
	private Provider<RequirementCriticalityComboDataBuilder> criticalityComboBuilderProvider;
	@Inject
	private Provider<RequirementCategoryComboDataBuilder> categoryComboBuilderProvider;
	@Inject
	private Provider<RequirementStatusComboDataBuilder> statusComboDataBuilderProvider;
	@Inject
	private Provider<LevelLabelFormatter> levelFormatterProvider;
	@Inject
	private Provider<InternationalisableLabelFormatter> internationalizableFormatterProvider;

	private RequirementVersionManagerService requirementVersionManager;
	
	@Inject
	private CustomFieldValueFinderService cufValueService;

	public RequirementVersionManagerController() {
		super();
	}

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
	String changeDescription(@PathVariable long requirementVersionId, @RequestParam(VALUE) String newDescription) {
		requirementVersionManager.changeDescription(requirementVersionId, newDescription);
		return newDescription;

	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=requirement-criticality", VALUE })
	@ResponseBody
	public String changeCriticality(@PathVariable long requirementVersionId,
			@RequestParam(VALUE) RequirementCriticality criticality, Locale locale) {
		requirementVersionManager.changeCriticality(requirementVersionId, criticality);
		return internationalize(criticality, locale);
	}
	@RequestMapping(method = RequestMethod.POST, params = { "id=requirement-category", VALUE })
	@ResponseBody
	public String changeCategory(@PathVariable long requirementVersionId,
			@RequestParam(VALUE) RequirementCategory category, Locale locale) {
		requirementVersionManager.changeCategory(requirementVersionId, category);
		return internationalizableFormatterProvider.get().useLocale(locale).formatLabel(category);

	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=requirement-status", VALUE })
	@ResponseBody
	public String changeStatus(@PathVariable long requirementVersionId, @RequestParam(VALUE) String value, Locale locale) {
		RequirementStatus status = RequirementStatus.valueOf(value);
		requirementVersionManager.changeStatus(requirementVersionId, status);
		return internationalize(status, locale);	
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=requirement-reference", VALUE })
	@ResponseBody
	String changeReference(@PathVariable long requirementVersionId, @RequestParam(VALUE) String requirementReference) {
		requirementVersionManager.changeReference(requirementVersionId, requirementReference.trim());
		return HtmlUtils.htmlEscape(requirementReference);
	}

	/**
	 * @param level
	 * @param locale
	 * @return
	 */
	private String internationalize(Level level, Locale locale) {
		return levelFormatterProvider.get().useLocale(locale).formatLabel(level);
	}

	@RequestMapping(value="/editor-fragment", method = RequestMethod.GET)
	public String getRequirementEditor(@PathVariable long requirementVersionId, Model model, Locale locale) {
		populateRequirementEditorModel(requirementVersionId, model, locale);
		return "fragment/requirements/requirement-version-editor";
	}

	private void populateRequirementEditorModel(long requirementVersionId, Model model, Locale locale) {
		RequirementVersion requirementVersion = requirementVersionManager.findById(requirementVersionId);
		boolean hasCUF = cufValueService.hasCustomFields(requirementVersion);
		
		model.addAttribute("requirementVersion", requirementVersion);

		String criticalities = buildMarshalledCriticalities(locale);
		model.addAttribute("jsonCriticalities", criticalities);
		String categories = buildMarshalledCategories(locale);
		model.addAttribute("jsonCategories", categories);
		model.addAttribute("hasCUF", hasCUF);
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

	private String buildMarshalledCriticalities(Locale locale) {
		return criticalityComboBuilderProvider.get().useLocale(locale).buildMarshalled();
	}
	private String buildMarshalledCategories(Locale locale) {
		return categoryComboBuilderProvider.get().useLocale(locale).buildMarshalled();
	}
	@RequestMapping(method = RequestMethod.POST, params = { "newName" })
	public @ResponseBody
	Object rename(@PathVariable long requirementVersionId, @RequestParam("newName") String newName) {
		requirementVersionManager.changeName(requirementVersionId, newName);
		return new Object();
	}
	
	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public String showRequirementVersionEditor(@PathVariable long requirementVersionId, Model model, Locale locale) {
		populateRequirementEditorModel(requirementVersionId, model, locale);
		return "page/requirements/requirement-version-editor";
	}	
}
