/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;
import org.squashtest.tm.core.foundation.collection.DefaultPagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.Level;
import org.squashtest.tm.domain.event.RequirementAuditEvent;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementCategory;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.audit.RequirementAuditTrailService;
import org.squashtest.tm.service.customfield.CustomFieldValueFinderService;
import org.squashtest.tm.service.requirement.RequirementModificationService;
import org.squashtest.tm.service.requirement.RequirementVersionManagerService;
import org.squashtest.tm.service.testcase.VerifyingTestCaseManagerService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.audittrail.RequirementAuditEventTableModelBuilder;
import org.squashtest.tm.web.internal.controller.generic.ServiceAwareAttachmentTableModelHelper;
import org.squashtest.tm.web.internal.helper.InternationalizableLabelFormatter;
import org.squashtest.tm.web.internal.helper.LevelLabelFormatter;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.model.datatable.DataTableSorting;
import org.squashtest.tm.web.internal.model.jquery.RenameModel;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

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
	private Provider<InternationalizableLabelFormatter> internationalizableFormatterProvider;

	@Inject
	private CustomFieldValueFinderService cufValueService;

	@Inject
	private RequirementModificationService requirementModService;

	@Inject
	private RequirementVersionManagerService versionFinder;

	@Inject
	private VerifyingTestCaseManagerService verifyingTestCaseManager;

	@Inject
	private InternationalizationHelper i18nHelper;

	@Inject
	private ServiceAwareAttachmentTableModelHelper attachmentsHelper;

	@Inject
	private RequirementAuditTrailService auditTrailService;

	private final DatatableMapper<String> versionMapper = new NameBasedMapper()
	.mapAttribute("version-number", "versionNumber", RequirementVersion.class)
	.mapAttribute("reference", "reference", RequirementVersion.class)
	.mapAttribute(DataTableModelConstants.DEFAULT_ENTITY_NAME_KEY, "name", RequirementVersion.class)
	.mapAttribute("status", "status", RequirementVersion.class)
	.mapAttribute("criticality", "criticality", RequirementVersion.class)
	.mapAttribute("category", "category", RequirementVersion.class);




	// will return the Requirement in a full page
	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public String showRequirementInfo(Model model, @PathVariable(RequestParams.REQUIREMENT_ID) long requirementId, Locale locale) {
		populateRequirementModel(model, requirementId, locale);
		return "page/requirement-workspace/show-requirement";

	}

	// will return the fragment only
	@RequestMapping(method = RequestMethod.GET)
	public String showRequirement(Model model, @PathVariable(RequestParams.REQUIREMENT_ID) long requirementId, Locale locale) {
		populateRequirementModel(model, requirementId, locale);
		return "fragment/requirements/requirement";
	}


	private void populateRequirementModel(Model model, long requirementId, Locale locale){

		Requirement requirement = requirementModService.findById(requirementId);
		String criticalities = buildMarshalledCriticalities(locale);
		String categories = buildMarshalledCategories(locale);
		boolean hasCUF = cufValueService.hasCustomFields(requirement.getCurrentVersion());
		DataTableModel verifyingTCModel = getVerifyingTCModel(requirement.getCurrentVersion());
		DataTableModel attachmentsModel = attachmentsHelper.findPagedAttachments(requirement);
		DataTableModel auditTrailModel = getEventsTableModel(requirement);

		model.addAttribute("requirement", requirement);
		model.addAttribute("criticalityList", criticalities);
		model.addAttribute("categoryList", categories);
		model.addAttribute("hasCUF", hasCUF);
		model.addAttribute("verifyingTestCasesModel", verifyingTCModel);
		model.addAttribute("attachmentsModel", attachmentsModel);
		model.addAttribute("auditTrailModel", auditTrailModel);

	}

	private String buildMarshalledCriticalities(Locale locale) {
		return criticalityComboBuilderProvider.get().useLocale(locale).buildMarshalled();
	}

	private String buildMarshalledCategories(Locale locale) {
		return categoryComboBuilderProvider.get().useLocale(locale).buildMarshalled();
	}

	private DataTableModel getVerifyingTCModel(RequirementVersion version){
		PagedCollectionHolder<List<TestCase>> holder = verifyingTestCaseManager.findAllByRequirementVersion(
				version.getId(), new DefaultPagingAndSorting("Project.name"));

		return new VerifyingTestCasesTableModelHelper(i18nHelper).buildDataModel(holder, "0");
	}



	@RequestMapping(method = RequestMethod.POST, params = { "id=requirement-description", VALUE }, produces = "text/plain;charset=UTF-8")
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

		return new RenameModel(newName);
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
		LOGGER.debug("Requirement {} : requirement criticality changed, new value : {}", requirementId, category.name());
		return formatCategory(category, locale, internationalizableFormatterProvider);
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=requirement-status", VALUE })
	@ResponseBody
	public String changeStatus(@RequestParam(VALUE) String value, @PathVariable long requirementId, Locale locale) {
		RequirementStatus status = RequirementStatus.valueOf(value);
		requirementModService.changeStatus(requirementId, status);
		LOGGER.debug("Requirement {} : requirement status changed, new value : {}", requirementId, status.name());
		return internationalize(status, locale, levelFormatterProvider);
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=requirement-name", VALUE }, produces = "text/plain;charset=UTF-8")
	@ResponseBody
	public String changeName(@RequestParam(VALUE) String value, @PathVariable long requirementId, Locale locale) {
		requirementModService.rename(requirementId, value);
		LOGGER.info("RequirementModificationController : renaming " + requirementId + " as " + value);
		return value;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/next-status")
	@ResponseBody
	public Map<String, String> getNextStatusList(Locale locale, @PathVariable long requirementId) {
		Requirement requirement = requirementModService.findById(requirementId);
		RequirementStatus status = requirement.getStatus();
		return initStatusSelectionList(locale, status);
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=requirement-reference", VALUE }, produces = "text/plain;charset=UTF-8")
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


	private DataTableModel getEventsTableModel(Requirement requirement){
		PagedCollectionHolder<List<RequirementAuditEvent>> auditTrail = auditTrailService
				.findAllByRequirementVersionIdOrderedByDate(requirement.getCurrentVersion().getId(), new DefaultPagingAndSorting());

		RequirementAuditEventTableModelBuilder builder = new RequirementAuditEventTableModelBuilder(LocaleContextHolder.getLocale(), i18nHelper);

		return builder.buildDataModel(auditTrail, "");

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
		return internationalize(criticality, locale, levelFormatterProvider);
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
	private static String formatCategory(RequirementCategory category, Locale locale, Provider<InternationalizableLabelFormatter> internationalizableFormatterProvider) {
		return internationalizableFormatterProvider.get().useLocale(locale).formatLabel(category);
	}

	private static String internationalize(Level level, Locale locale,
			Provider<LevelLabelFormatter> levelFormatterProvider) {
		return levelFormatterProvider.get().useLocale(locale).formatLabel(level);
	}

	@RequestMapping(value = "/versions/manager")
	public String showRequirementVersionsManager(@PathVariable long requirementId, Model model, Locale locale) {
		Requirement req = requirementModService.findById(requirementId);

		model.addAttribute("requirement", req);
		model.addAttribute("versions", req.getUnmodifiableVersions());
		model.addAttribute("selectedVersion", req.getCurrentVersion());
		model.addAttribute("criticalityList", buildMarshalledCriticalities(locale));
		model.addAttribute("categoryList", buildMarshalledCategories(locale));
		model.addAttribute("verifyingTestCasesModel", getVerifyingTCModel(req.getCurrentVersion()));
		model.addAttribute("auditTrailModel", getEventsTableModel(req));
		boolean hasCUF = cufValueService.hasCustomFields(req.getCurrentVersion());

		model.addAttribute("hasCUF", hasCUF);
		return "page/requirement-workspace/versions-manager";
	}

	@RequestMapping(value = "/versions/table", params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getRequirementVersionsTableModel(@PathVariable long requirementId,
			DataTableDrawParameters params, final Locale locale) {
		PagingAndSorting pas = new DataTableSorting(params, versionMapper);

		PagedCollectionHolder<List<RequirementVersion>> holder = versionFinder.findAllByRequirement(requirementId, pas);

		return new RequirementVersionDataTableModel(locale, levelFormatterProvider, internationalizableFormatterProvider).buildDataModel(holder,
				params.getsEcho());
	}

	private static final class RequirementVersionDataTableModel extends DataTableModelBuilder<RequirementVersion> {
		private Locale locale;
		private Provider<LevelLabelFormatter> levelFormatterProvider;
		private Provider<InternationalizableLabelFormatter> internationalizableFormatterProvider;

		private RequirementVersionDataTableModel(Locale locale, Provider<LevelLabelFormatter> levelFormatterProvider, Provider<InternationalizableLabelFormatter> internationalizableFormatterProvider) {
			this.locale = locale;
			this.levelFormatterProvider = levelFormatterProvider;
			this.internationalizableFormatterProvider = internationalizableFormatterProvider;
		}

		@Override
		public Map<String, Object> buildItemData(RequirementVersion version) {

			Map<String, Object> row = new HashMap<String, Object>(7);

			row.put(DataTableModelConstants.DEFAULT_ENTITY_ID_KEY, version.getId());
			row.put("version-number", version.getVersionNumber());
			row.put("reference", version.getReference());
			row.put(DataTableModelConstants.DEFAULT_ENTITY_NAME_KEY, version.getName());
			row.put("status", internationalize(version.getStatus(), locale, levelFormatterProvider));
			row.put("criticality", internationalize(version.getCriticality(), locale, levelFormatterProvider));
			row.put("category", formatCategory(version.getCategory(), locale, internationalizableFormatterProvider));

			return row;

		}

	}

}
