/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.springframework.context.i18n.LocaleContextHolder;
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
import org.squashtest.tm.core.foundation.collection.DefaultPagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.domain.Level;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.event.RequirementAuditEvent;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementCategory;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.exception.UnknownEntityException;
import org.squashtest.tm.service.audit.RequirementAuditTrailService;
import org.squashtest.tm.service.customfield.CustomFieldHelperService;
import org.squashtest.tm.service.customfield.CustomFieldValueFinderService;
import org.squashtest.tm.service.requirement.RequirementVersionManagerService;
import org.squashtest.tm.service.testcase.VerifyingTestCaseManagerService;
import org.squashtest.tm.web.internal.controller.audittrail.RequirementAuditEventTableModelBuilder;
import org.squashtest.tm.web.internal.controller.generic.ServiceAwareAttachmentTableModelHelper;
import org.squashtest.tm.web.internal.helper.InternationalisableLabelFormatter;
import org.squashtest.tm.web.internal.helper.LevelLabelFormatter;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.jquery.RenameModel;

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
	@Inject
	private InternationalizationHelper i18nHelper;
	
	@Inject
	private VerifyingTestCaseManagerService verifyingTestCaseManager;

	@Inject
	private ServiceAwareAttachmentTableModelHelper attachmentsHelper;

	@Inject
	private CustomFieldHelperService cufHelperService;
	
	@Inject
	private RequirementVersionManagerService requirementVersionManager;

	@Inject
	private CustomFieldValueFinderService cufValueService;

	public RequirementVersionManagerController() {
		super();
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

	@RequestMapping(method = RequestMethod.POST, params = { "id=requirement-reference", VALUE }, produces = "text/plain;charset=UTF-8")
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

	@RequestMapping(value = "/editor-fragment", method = RequestMethod.GET)
	public String getRequirementEditor(@PathVariable long requirementVersionId, Model model, Locale locale) {
		populateRequirementEditorModel(requirementVersionId, model, locale);
		return "fragment/requirements/requirement-version-editor";
	}
	

	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public String showRequirementVersionEditor(@PathVariable long requirementVersionId, Model model, Locale locale) {
		populateRequirementEditorModel(requirementVersionId, model, locale);
		return "page/requirements/requirement-version-editor";
	}

	private void populateRequirementEditorModel(long requirementVersionId, Model model, Locale locale) {
		
		RequirementVersion requirementVersion = requirementVersionManager.findById(requirementVersionId);
		String criticalities = buildMarshalledCriticalities(locale);
		boolean hasCUF = cufValueService.hasCustomFields(requirementVersion);
		String categories = buildMarshalledCategories(locale);		
		DataTableModel verifyingTCModel = getVerifyingTCModel(requirementVersion);
		DataTableModel attachmentsModel = attachmentsHelper.findPagedAttachments(requirementVersion);
		DataTableModel auditTrailModel = getEventsTableModel(requirementVersion);

		model.addAttribute("requirementVersion", requirementVersion);
		model.addAttribute("jsonCriticalities", criticalities);
		model.addAttribute("jsonCategories", categories);
		model.addAttribute("hasCUF", hasCUF);
		model.addAttribute("verifyingTestCaseModel", verifyingTCModel);
		model.addAttribute("attachmentsModel", attachmentsModel);
		model.addAttribute("auditTrailModel", auditTrailModel);
	}
	
	private DataTableModel getVerifyingTCModel(RequirementVersion version){
		PagedCollectionHolder<List<TestCase>> holder = verifyingTestCaseManager.findAllByRequirementVersion(
				version.getId(), new DefaultPagingAndSorting("Project.name"));
		
		return new VerifyingTestCasesTableModelHelper(i18nHelper).buildDataModel(holder, "0");		
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
		return new  RenameModel(newName);
	}

	
	private DataTableModel getEventsTableModel(RequirementVersion requirementVersion){
		PagedCollectionHolder<List<RequirementAuditEvent>> auditTrail = auditTrailService
				.findAllByRequirementVersionIdOrderedByDate(requirementVersion.getId(), new DefaultPagingAndSorting());

		RequirementAuditEventTableModelBuilder builder = new RequirementAuditEventTableModelBuilder(LocaleContextHolder.getLocale(), i18nHelper);

		return builder.buildDataModel(auditTrail, "");

	}
	
	

	/**
	 * Returns a map of all requirement version's siblings, including itself. The map will be filled with strings:<br>
	 * -the key being the id of the version <br>
	 * -and the value being "versionNumber (versionStatus)"<br>
	 * <br>
	 * Versions having an {@link RequirementStatus#OBSOLETE} status are not included in the result.<br>
	 * Last map entry is key= "selected", value = id of concerned requirement.
	 * 
	 * @param locale
	 * @param requirementVersionId
	 *            : the id of the concerned requirement version.
	 * @return a {@link Map} with key = "id" and value ="versionNumber (versionStatus)", <br>
	 *         obsolete versions excluded, <br>
	 *         last entry is key="selected", value= id of concerned requirement.
	 */
	@RequestMapping(value = "/version-numbers", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, String> showAllVersions(Locale locale, @PathVariable long requirementVersionId) {
		Map<String, String> versionsNumbersById = new LinkedHashMap<String, String>();

		RequirementVersion requirementVersion = requirementVersionManager.findById(requirementVersionId);

		Requirement requirement = requirementVersion.getRequirement();

		// Retrieve all versions of the requirement
		List<RequirementVersion> requirementVersions = requirement.getRequirementVersions();

		// We duplicate the list before we sort it
		List<RequirementVersion> cloneRequirementVersions = new ArrayList<RequirementVersion>();

		for (RequirementVersion rv : requirementVersions) {
			cloneRequirementVersions.add(rv);
		}

		Collections.sort(cloneRequirementVersions, new MyRequirementVersionsDecOrder());

		String status = "";

		for (RequirementVersion version : cloneRequirementVersions) {
			if (version.getStatus() != RequirementStatus.OBSOLETE) {
				status = i18nHelper.getMessage("requirement.status." + version.getStatus().name(), null, locale);
				versionsNumbersById.put("" + version.getId(), "" + version.getVersionNumber() + " (" + status + ")");
			}
		}
		versionsNumbersById.put("selected", "" + requirementVersionId);

		return versionsNumbersById;
	}

	/**
	 * Comparator for RequieredVersions
	 * 
	 * @author FOG
	 * 
	 */
	public static class MyRequirementVersionsDecOrder implements Comparator<RequirementVersion> {

		@Override
		public int compare(RequirementVersion rV1, RequirementVersion rV2) {
			return (rV1.getVersionNumber() > rV2.getVersionNumber() ? -1 : (rV1.getVersionNumber() == rV2
					.getVersionNumber() ? 0 : 1));
		}
	}

	private RequirementAuditTrailService auditTrailService;

	/**
	 * @param auditTrailService
	 *            the auditTrailService to set
	 */
	@ServiceReference
	public void setAuditTrailService(RequirementAuditTrailService auditTrailService) {
		this.auditTrailService = auditTrailService;
	}

	@RequestMapping(method = RequestMethod.GET, params = "format=printable")
	public ModelAndView printRequirementVersion(@PathVariable long requirementVersionId, Locale locale) {
		ModelAndView mav = new ModelAndView("print-requirement-version.html");
		RequirementVersion version = requirementVersionManager.findById(requirementVersionId);
		if (version == null) {
			throw new UnknownEntityException(requirementVersionId, RequirementVersion.class);
		}
		mav.addObject("requirementVersion", version);
		// =================CUFS
		List<CustomFieldValue> customFieldValues = cufHelperService.newHelper(version).getCustomFieldValues();
		mav.addObject("requirementVersionCufValues", customFieldValues);
		// ==================TEST CASES
		List<TestCase> verifyingTC = verifyingTestCaseManager.findAllByRequirementVersion(requirementVersionId);
		mav.addObject("verifyingTestCases", verifyingTC);
		// =================VERSIONS
		List<RequirementVersion> versions = requirementVersionManager.findAllByRequirement(version.getRequirement()
				.getId());
		mav.addObject("siblingVersions", versions);
		// =================AUDIT TRAIL
		PagedCollectionHolder<List<RequirementAuditEvent>> auditTrail = auditTrailService
				.findAllByRequirementVersionIdOrderedByDate(requirementVersionId);

		RequirementAuditEventTableModelBuilder builder = new RequirementAuditEventTableModelBuilder(locale,	i18nHelper);

		DataTableModel auditTrailModel = builder.buildDataModel(auditTrail, "1");
		mav.addObject("auditTrailDatas", auditTrailModel.getAaData());
		return mav;
	}
}
