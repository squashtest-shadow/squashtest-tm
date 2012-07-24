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
package org.squashtest.csp.tm.web.internal.controller.requirement;

import static org.squashtest.csp.tm.web.internal.helper.JEditablePostParams.VALUE;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
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
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;
import org.squashtest.csp.tm.domain.Level;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.requirement.RequirementCategory;
import org.squashtest.csp.tm.domain.requirement.RequirementCriticality;
import org.squashtest.csp.tm.domain.requirement.RequirementStatus;
import org.squashtest.csp.tm.domain.requirement.RequirementVersion;
import org.squashtest.csp.tm.service.RequirementModificationService;
import org.squashtest.csp.tm.service.RequirementVersionManagerService;
import org.squashtest.csp.tm.web.internal.helper.InternationalisableLabelFormatter;
import org.squashtest.csp.tm.web.internal.helper.LevelLabelFormatter;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableMapperPagingAndSortingAdapter;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModelHelper;
import org.squashtest.csp.tm.web.internal.model.viewmapper.DataTableMapper;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;

@Controller
@RequestMapping("/requirements/{requirementId}")
public class RequirementModificationController {

	private static final Logger LOGGER = LoggerFactory.getLogger(RequirementModificationController.class);

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

	private RequirementModificationService requirementModService;
	private RequirementVersionManagerService versionFinder;

	private final DataTableMapper versionMapper = new DataTableMapper("requirement-version", RequirementVersion.class)
			.initMapping(6).mapAttribute(RequirementVersion.class, 1, "versionNumber", int.class)
			.mapAttribute(RequirementVersion.class, 2, "reference", String.class)
			.mapAttribute(RequirementVersion.class, 3, "name", String.class)
			.mapAttribute(RequirementVersion.class, 4, "criticality", RequirementCriticality.class);

	@ServiceReference
	public void setRequirementModificationService(RequirementModificationService service) {
		requirementModService = service;
	}

	@ServiceReference
	public void setRequirementVersionManagerService(RequirementVersionManagerService service) {
		versionFinder = service;
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
		String categories = buildMarshalledCategories(locale);
		mav.addObject("categoryList", categories);

		return mav;
	}

	private String buildMarshalledCriticalities(Locale locale) {
		return criticalityComboBuilderProvider.get().useLocale(locale).buildMarshalled();
	}
	private String buildMarshalledCategories(Locale locale) {
		return categoryComboBuilderProvider.get().useLocale(locale).buildMarshalled();
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
		String categories = buildMarshalledCategories(locale);
		mav.addObject("categoryList", categories);
		return mav;
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=requirement-description", VALUE })
	public @ResponseBody
	String changeDescription(@RequestParam(VALUE) String newDescription, @PathVariable long requirementId) {

		requirementModService.changeDescription(requirementId, newDescription);
		LOGGER.trace("requirement " + requirementId + ": updated description to " + newDescription);
		return newDescription;

	}

	@RequestMapping(method = RequestMethod.POST, params = { "newName" })
	public @ResponseBody
	Object rename(@RequestParam("newName") String newName, @PathVariable long requirementId) {
		requirementModService.rename(requirementId, newName);
		LOGGER.info("RequirementModificationController : renaming " + requirementId + " as " + newName);
		final String newNameJson = newName;
		return new Object(){ public String newName=newNameJson; };
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=requirement-criticality", VALUE })
	@ResponseBody
	public String changeCriticality(@RequestParam(VALUE) String value, @PathVariable long requirementId, Locale locale) {
		RequirementCriticality criticality = RequirementCriticality.valueOf(value);
		requirementModService.changeCriticality(requirementId, criticality);
		LOGGER.debug("Requirement {} : requirement criticality changed, new value : {}", requirementId,
				criticality.name());
		return formatCriticality(criticality, locale);
	}
	
	@RequestMapping(method = RequestMethod.POST, params = { "id=requirement-category", VALUE })
	@ResponseBody
	public String changeCategory(@RequestParam(VALUE) String value, @PathVariable long requirementId, Locale locale) {
		RequirementCategory category = RequirementCategory.valueOf(value);
		requirementModService.changeCategory(requirementId, category);
		LOGGER.debug("Requirement {} : requirement criticality changed, new value : {}", requirementId,
				category.name());
		return formatCategory(category, locale);
	}
	@RequestMapping(method = RequestMethod.POST, params = { "id=requirement-status", VALUE })
	@ResponseBody
	public String changeStatus(@RequestParam(VALUE) String value, @PathVariable long requirementId, Locale locale) {
		RequirementStatus status = RequirementStatus.valueOf(value);
		requirementModService.changeStatus(requirementId, status);
		LOGGER.debug("Requirement {} : requirement status changed, new value : {}", requirementId, status.name());
		return internationalize(status, locale);
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
	public String changeReference(@RequestParam(VALUE) String requirementReference, @PathVariable long requirementId)
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
	
	@RequestMapping(value = "/versions/version-number", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, String> showAllVersions(Locale locale, @PathVariable long requirementId) {
		Map<String, String> versionsNumbersById = new LinkedHashMap<String, String>();
		
		RequirementVersion requirementVersion = versionFinder.findById(requirementId);
		
		Requirement requirement = requirementVersion.getRequirement();
		
		//Retrieve all versions of the requirement
		List<RequirementVersion> requirementVersions = requirement.getRequirementVersions();
		
		//We duplicate the list before we sort it
		List <RequirementVersion> cloneRequirementVersions = new ArrayList<RequirementVersion>();
		
		for (RequirementVersion rv : requirementVersions) {
			cloneRequirementVersions.add(rv);
		}
		
		Collections.sort(cloneRequirementVersions, new MyRequirementVersionsDecOrder());

		String status = "";
		
		for (RequirementVersion version : cloneRequirementVersions) {
			if (version.getStatus() != RequirementStatus.OBSOLETE){
				status = messageSource.getMessage("requirement.status." + version.getStatus().name(), null, locale);
				versionsNumbersById.put("" + version.getId(), "" + version.getVersionNumber() + " (" + status + ")");
			}
		}
		versionsNumbersById.put("selected", "" + requirementId);
		
		return versionsNumbersById;
	}
	
	/**
	 * Comparator for RequieredVersions
	 * @author FOG
	 *
	 */
	public class MyRequirementVersionsDecOrder implements Comparator<RequirementVersion>{
		 
	    @Override
	    public int compare(RequirementVersion rV1, RequirementVersion rV2) {
	        return (rV1.getVersionNumber()>rV2.getVersionNumber() ? -1 : (rV1.getVersionNumber()==rV2.getVersionNumber() ? 0 : 1));
	    }
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
	
	/***
	 * Method which returns category in the chosen language
	 *
	 * @param criticality
	 *            the category
	 * @param locale
	 *            the locale with the chosen language
	 * @return the category in the chosen language
	 */
	private String formatCategory(RequirementCategory category, Locale locale) {
		return internationalizableFormatterProvider.get().useLocale(locale).formatLabel(category);
	}

	private String internationalize(Level level, Locale locale) {
		return levelFormatterProvider.get().useLocale(locale).formatLabel(level);
	}

	@RequestMapping(value = "/versions/manager")
	public String showRequirementVersionsManager(@PathVariable long requirementId, Model model, Locale locale) {
		Requirement req = requirementModService.findById(requirementId);

		model.addAttribute("requirement", req);
		model.addAttribute("versions", req.getUnmodifiableVersions());
		model.addAttribute("selectedVersion", req.getCurrentVersion());
		model.addAttribute("jsonCriticalities", buildMarshalledCriticalities(locale));
		model.addAttribute("jsonCategories", buildMarshalledCategories(locale));
		return "page/requirements/versions-manager";
	}

	@RequestMapping(value = "/versions/table", params = "sEcho")
	@ResponseBody
	public DataTableModel getRequirementVersionsTableModel(@PathVariable long requirementId,
			DataTableDrawParameters params, final Locale locale) {
		PagingAndSorting pas = new DataTableMapperPagingAndSortingAdapter(params, versionMapper);

		PagedCollectionHolder<List<RequirementVersion>> holder = versionFinder.findAllByRequirement(requirementId, pas);

		return new DataTableModelHelper<RequirementVersion>() {
			@Override
			public Object[] buildItemData(RequirementVersion version) {
				return new Object[] { version.getId(), version.getVersionNumber(), version.getReference(),
						version.getName(), internationalize(version.getStatus(), locale), "" };
			}
		}.buildDataModel(holder, params.getsEcho());
	}

	@ServiceReference
	public void setVersionFinder(RequirementVersionManagerService versionFinder) {
		this.versionFinder = versionFinder;
	}
}
