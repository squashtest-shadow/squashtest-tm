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
package org.squashtest.tm.web.internal.controller.requirement;

import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
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
import org.squashtest.tm.core.foundation.collection.SinglePageCollectionHolder;
import org.squashtest.tm.core.foundation.collection.SpringPaginationUtils;
import org.squashtest.tm.domain.Level;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.event.RequirementAuditEvent;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.exception.UnknownEntityException;
import org.squashtest.tm.service.audit.RequirementAuditTrailService;
import org.squashtest.tm.service.customfield.CustomFieldHelperService;
import org.squashtest.tm.service.customfield.CustomFieldValueFinderService;
import org.squashtest.tm.service.infolist.InfoListItemFinderService;
import org.squashtest.tm.service.requirement.RequirementVersionManagerService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.testcase.VerifyingTestCaseManagerService;
import org.squashtest.tm.web.internal.controller.audittrail.RequirementAuditEventTableModelBuilder;
import org.squashtest.tm.web.internal.controller.generic.ServiceAwareAttachmentTableModelHelper;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneFeatureConfiguration;
import org.squashtest.tm.web.internal.controller.milestone.MilestonePanelConfiguration;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneTableModelHelper;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneUIConfigurationService;
import org.squashtest.tm.web.internal.helper.LevelLabelFormatter;
import org.squashtest.tm.web.internal.http.ContentTypes;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.builder.JsonInfoListBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.jquery.RenameModel;
import org.squashtest.tm.web.internal.model.json.JsonGeneralInfo;
import org.squashtest.tm.web.internal.model.json.JsonInfoList;

/**
 * Controller which receives requirement version management related requests.
 *
 * @author Gregory Fouquet
 *
 */
@Controller
@RequestMapping("/requirement-versions/{requirementVersionId}")
public class RequirementVersionModificationController {
	@Inject
	private Provider<RequirementCriticalityComboDataBuilder> criticalityComboBuilderProvider;

	@Inject
	private Provider<RequirementStatusComboDataBuilder> statusComboDataBuilderProvider;

	@Inject
	private Provider<LevelLabelFormatter> levelFormatterProvider;

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

	@Inject
	private InfoListItemFinderService infoListItemService;

	@Inject
	private JsonInfoListBuilder infoListBuilder;

	@Inject
	private MilestoneUIConfigurationService milestoneConfService;

	@Inject
	private PermissionEvaluationService permissionService;


	public RequirementVersionModificationController() {
		super();
	}


	@RequestMapping(value = "/editor-fragment", method = RequestMethod.GET)
	public String getRequirementEditor(@PathVariable long requirementVersionId, Model model, Locale locale) {
		populateRequirementEditorModel(requirementVersionId, model, locale);
		return "fragment/requirements/requirement-version-editor";
	}

	@RequestMapping(method = RequestMethod.GET)
	public String showRequirementVersion(@PathVariable long requirementVersionId, Model model, Locale locale) {
		populateRequirementEditorModel(requirementVersionId, model, locale);
		return "fragment/requirements/requirement-version";
	}

	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public String showRequirementVersionInfos(@PathVariable long requirementVersionId, Model model, Locale locale) {
		populateRequirementEditorModel(requirementVersionId, model, locale);
		return "page/requirement-workspace/show-requirement-version";
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, params = {"id=requirement-description", VALUE}, produces = "text/plain;charset=UTF-8")
	public
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
			@RequestParam(VALUE) String categoryCode, Locale locale) {
		requirementVersionManager.changeCategory(requirementVersionId, categoryCode);
		InfoListItem category = infoListItemService.findByCode(categoryCode);
		return formatInfoItem(category, locale);

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

	private String internationalize(Level level, Locale locale) {
		return levelFormatterProvider.get().useLocale(locale).formatLabel(level);
	}


	private void populateRequirementEditorModel(long requirementVersionId, Model model, Locale locale) {

		RequirementVersion requirementVersion = requirementVersionManager.findById(requirementVersionId);
		String criticalities = buildMarshalledCriticalities(locale);

		boolean hasCUF = cufValueService.hasCustomFields(requirementVersion);
		JsonInfoList categories = infoListBuilder.toJson(requirementVersion.getProject().getRequirementCategories());

		DataTableModel verifyingTCModel = getVerifyingTCModel(requirementVersion);
		DataTableModel attachmentsModel = attachmentsHelper.findPagedAttachments(requirementVersion);
		DataTableModel auditTrailModel = getEventsTableModel(requirementVersion);

		model.addAttribute("requirementVersion", requirementVersion);
		model.addAttribute("criticalityList", criticalities);
		model.addAttribute("categoryList", categories);
		model.addAttribute("hasCUF", hasCUF);
		model.addAttribute("verifyingTestCasesModel", verifyingTCModel);
		model.addAttribute("attachmentsModel", attachmentsModel);
		model.addAttribute("auditTrailModel", auditTrailModel);

		MilestoneFeatureConfiguration milestoneConf = milestoneConfService.configure(requirementVersion);

		model.addAttribute("milestoneConf", milestoneConf);

		if (requirementVersion.getRequirement().isSynchronized()){
			model.addAttribute("requirementURL", requirementVersion.getRequirement().getSyncExtender().getRemoteUrl());
		}

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

	@RequestMapping(value = "/general", method = RequestMethod.GET, produces=ContentTypes.APPLICATION_JSON)
	@ResponseBody
	public JsonGeneralInfo refreshGeneralInfos(@PathVariable long requirementVersionId){
		RequirementVersion version = requirementVersionManager.findById(requirementVersionId);
		return new JsonGeneralInfo((AuditableMixin)version);

	}

	private String buildMarshalledCriticalities(Locale locale) {
		return criticalityComboBuilderProvider.get().useLocale(locale).buildMarshalled();
	}


	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, params = {"newName"})
	public
	Object rename(@PathVariable long requirementVersionId, @RequestParam("newName") String newName) {
		requirementVersionManager.rename(requirementVersionId, newName);
		return new  RenameModel(newName);
	}

	// that one performs the same operation than #rename, but some jeditables like it more that way
	@RequestMapping(method = RequestMethod.POST, params = { "id=requirement-name", VALUE })
	@ResponseBody
	public String changeName(@PathVariable long requirementVersionId, @RequestParam(VALUE) String value, Locale locale) {
		requirementVersionManager.rename(requirementVersionId, value);
		return value;
	}


	private DataTableModel getEventsTableModel(RequirementVersion requirementVersion){
            Pageable pageable = new PageRequest(0, SpringPaginationUtils.DEFAULT_SIZE, Direction.DESC, "date");
            Page<RequirementAuditEvent> auditTrail = auditTrailService
                            .findAllByRequirementVersionIdOrderedByDate(requirementVersion.getId(), pageable);

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
	 * @param requirementVersionId
	 *            : the id of the concerned requirement version.
	 * @return a {@link Map} with key = "id" and value ="versionNumber (versionStatus)", <br>
	 *         obsolete versions excluded, <br>
	 *         last entry is key="selected", value= id of concerned requirement.
	 */
	@RequestMapping(value = "/version-numbers", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, String> showAllVersions(Locale locale, @PathVariable long requirementVersionId) {
		Map<String, String> versionsNumbersById = new LinkedHashMap<>();

		RequirementVersion requirementVersion = requirementVersionManager.findById(requirementVersionId);

		Requirement requirement = requirementVersion.getRequirement();

		// Retrieve all versions of the requirement
		List<RequirementVersion> requirementVersions = requirement.getRequirementVersions();

		// We duplicate the list before we sort it
		List<RequirementVersion> cloneRequirementVersions = new ArrayList<>();

		for (RequirementVersion rv : requirementVersions) {
			cloneRequirementVersions.add(rv);
		}

		Collections.sort(cloneRequirementVersions, new MyRequirementVersionsDecOrder());

		String status;

		for (RequirementVersion version : cloneRequirementVersions) {
			if (version.getStatus() != RequirementStatus.OBSOLETE) {
				status = i18nHelper.internationalize("requirement.status." + version.getStatus().name(), locale);
				versionsNumbersById.put(String.valueOf(version.getId()), version.getVersionNumber() + " (" + status + ")");
			}
		}
		versionsNumbersById.put("selected", String.valueOf(requirementVersionId));

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
			return rV1.getVersionNumber() > rV2.getVersionNumber() ? -1 : rV1.getVersionNumber() == rV2
					.getVersionNumber() ? 0 : 1;
		}
	}

	private RequirementAuditTrailService auditTrailService;

	/**
	 * @param auditTrailService
	 *            the auditTrailService to set
	 */
	@Inject
	public void setAuditTrailService(RequirementAuditTrailService auditTrailService) {
		this.auditTrailService = auditTrailService;
	}

	private String formatInfoItem(InfoListItem nature, Locale locale) {
		return  i18nHelper.getMessage(nature.getLabel(), null, nature.getLabel(), locale);
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
		Page<RequirementAuditEvent> auditTrail = auditTrailService.findAllByRequirementVersionIdOrderedByDate(requirementVersionId);

		RequirementAuditEventTableModelBuilder builder = new RequirementAuditEventTableModelBuilder(locale,	i18nHelper);

		DataTableModel auditTrailModel = builder.buildDataModel(auditTrail, "1");
		mav.addObject("auditTrailDatas", auditTrailModel.getAaData());
		return mav;
	}

	/* **********************************************************************
	 *
	 * Milestones section
	 *
	 ********************************************************************** */

	@RequestMapping(value = "/milestones", method=RequestMethod.GET)
	@ResponseBody
	public DataTableModel getBoundMilestones(@PathVariable("requirementVersionId") long requirementVersionId, DataTableDrawParameters params){

		Collection<Milestone> allMilestones = requirementVersionManager.findAllMilestones(requirementVersionId);

		return buildMilestoneModel(new ArrayList<>(allMilestones), params.getsEcho());
	}

	@RequestMapping(value = "/milestones/{milestoneIds}", method=RequestMethod.POST)
	@ResponseBody
	public void bindMilestones(@PathVariable("requirementVersionId") long requirementVersionId, @PathVariable("milestoneIds") List<Long> milestoneIds){

		requirementVersionManager.bindMilestones(requirementVersionId, milestoneIds);
	}

	@RequestMapping(value = "/milestones/{milestoneIds}", method=RequestMethod.DELETE)
	@ResponseBody
	public void unbindMilestones(@PathVariable("requirementVersionId") long requirementVersionId, @PathVariable("milestoneIds") List<Long> milestoneIds){

		requirementVersionManager.unbindMilestones(requirementVersionId, milestoneIds);
	}

	@RequestMapping(value = "/milestones/associables", method=RequestMethod.GET)
	@ResponseBody
	public DataTableModel getNotYetBoundMilestones(@PathVariable("requirementVersionId") long requirementVersionId, DataTableDrawParameters params){
		Collection<Milestone> notBoundMilestones = requirementVersionManager.findAssociableMilestones(requirementVersionId);
		return buildMilestoneModel(new ArrayList<>(notBoundMilestones),params.getsEcho());
	}


	@RequestMapping(value = "/milestones/panel", method=RequestMethod.GET)
	public String getMilestonesPanel(@PathVariable("requirementVersionId") Long requirementVersionId, Model model){

		MilestonePanelConfiguration conf = new MilestonePanelConfiguration();
		RequirementVersion version = requirementVersionManager.findById(requirementVersionId);

		// build the needed data
		Collection<Milestone> allMilestones = requirementVersionManager.findAllMilestones(requirementVersionId);
		List<?> currentModel = buildMilestoneModel(new ArrayList<>(allMilestones),  "0").getAaData();

		Map<String, String> identity = new HashMap<>();
		identity.put("restype", "requirements");
		identity.put("resid", version.getRequirement().getId().toString());

		String rootPath = "/requirement-versions/"+requirementVersionId.toString();

		Boolean editable = permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "LINK", version);

		List<Milestone> mil = version.getProject().getMilestones();
		CollectionUtils.filter(mil, new Predicate() {
			@Override
			public boolean evaluate(Object milestone) {
				return ((Milestone)milestone).getStatus().isBindableToObject();
			}
		});
		Boolean isMilestoneInProject = !mil.isEmpty();


		// add them to the model
		conf.setNodeType("requirement-version");
		conf.setRootPath(rootPath);
		conf.setIdentity(identity);
		conf.setCurrentModel(currentModel);
		conf.setEditable(editable);
		conf.setIsMilestoneInProject(isMilestoneInProject);
		model.addAttribute("conf", conf);

		return "milestones/milestones-tab.html";

	}

	private DataTableModel buildMilestoneModel(List<Milestone> milestones, String sEcho){


		PagedCollectionHolder<List<Milestone>> collectionHolder =
			new SinglePageCollectionHolder<>(milestones);

		Locale locale = LocaleContextHolder.getLocale();
		return new MilestoneTableModelHelper(i18nHelper, locale).buildDataModel(collectionHolder, sEcho);

	}
}
