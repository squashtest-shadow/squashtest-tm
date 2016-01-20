/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.core.foundation.collection.DefaultPagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.SinglePageCollectionHolder;
import org.squashtest.tm.domain.Level;
import org.squashtest.tm.domain.event.RequirementAuditEvent;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.audit.RequirementAuditTrailService;
import org.squashtest.tm.service.customfield.CustomFieldValueFinderService;
import org.squashtest.tm.service.requirement.RequirementVersionManagerService;
import org.squashtest.tm.service.testcase.VerifyingTestCaseManagerService;
import org.squashtest.tm.web.internal.argumentresolver.MilestoneConfigResolver.CurrentMilestone;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.audittrail.RequirementAuditEventTableModelBuilder;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneFeatureConfiguration;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneModelUtils;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneUIConfigurationService;
import org.squashtest.tm.web.internal.helper.LevelLabelFormatter;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.builder.JsonInfoListBuilder;
import org.squashtest.tm.web.internal.model.datatable.*;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.*;


@Controller
@RequestMapping("/requirements/{requirementId}/versions")
public class RequirementVersionManagerController {


	@Inject
	private RequirementVersionManagerService versionService;


	@Inject
	private InternationalizationHelper i18nHelper;

	@Inject
	private Provider<LevelLabelFormatter> levelFormatterProvider;

	@Inject
	private CustomFieldValueFinderService cufValueService;

	@Inject
	private JsonInfoListBuilder infoListBuilder;

	@Inject
	private Provider<RequirementCriticalityComboDataBuilder> criticalityComboBuilderProvider;

	@Inject
	private VerifyingTestCaseManagerService verifyingTestCaseManager;

	@Inject
	private RequirementAuditTrailService auditTrailService;

	@Inject
	private MilestoneUIConfigurationService milestoneConfService;


	private final DatatableMapper<String> versionMapper = new NameBasedMapper()
	.mapAttribute("version-number", "versionNumber", RequirementVersion.class)
	.mapAttribute("reference", "reference", RequirementVersion.class)
	.mapAttribute(DataTableModelConstants.DEFAULT_ENTITY_NAME_KEY, "name", RequirementVersion.class)
	.mapAttribute("status", "status", RequirementVersion.class)
	.mapAttribute("criticality", "criticality", RequirementVersion.class)
	.mapAttribute("category", "category", RequirementVersion.class);




	@RequestMapping(value = "/new", method = RequestMethod.POST)
	@ResponseBody
	public void createNewVersion(@PathVariable long requirementId, @CurrentMilestone Milestone activeMilestone) {

		if (activeMilestone == null){
			versionService.createNewVersion(requirementId);
		}else{
			ArrayList<Long> milestoneIds = new ArrayList<>();
			milestoneIds.add(activeMilestone.getId());
			versionService.createNewVersion(requirementId, milestoneIds);
		}
	}




	@RequestMapping(value = "/manager")
	public String showRequirementVersionsManager(@PathVariable long requirementId, Model model, Locale locale,
			@CurrentMilestone Milestone activeMilestone) {

		Requirement req = versionService.findRequirementById(requirementId);

		PagedCollectionHolder<List<RequirementVersion>> holder = new SinglePageCollectionHolder<>(req.getUnmodifiableVersions());

		DataTableModel tableModel = new RequirementVersionDataTableModel(locale, levelFormatterProvider, i18nHelper).buildDataModel(holder,
				"0");

		MilestoneFeatureConfiguration milestoneConf = milestoneConfService.configure(activeMilestone, req.getCurrentVersion());

		model.addAttribute("requirement", req);
		model.addAttribute("versions", req.getUnmodifiableVersions());
		model.addAttribute("versionsTableModel", tableModel);
		model.addAttribute("selectedVersion", req.getCurrentVersion());
		model.addAttribute("criticalityList", buildMarshalledCriticalities(locale));
		model.addAttribute("categoryList", infoListBuilder.toJson(req.getProject().getRequirementCategories()));
		model.addAttribute("verifyingTestCasesModel", getVerifyingTCModel(req.getCurrentVersion()));
		model.addAttribute("auditTrailModel", getEventsTableModel(req));
		model.addAttribute("milestoneConf", milestoneConf);
		boolean hasCUF = cufValueService.hasCustomFields(req.getCurrentVersion());

		model.addAttribute("hasCUF", hasCUF);
		return "page/requirement-workspace/versions-manager";
	}


	@RequestMapping(value = "/table", params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getRequirementVersionsTableModel(@PathVariable long requirementId,
			DataTableDrawParameters params, final Locale locale) {
		PagingAndSorting pas = new DataTableSorting(params, versionMapper);

		PagedCollectionHolder<List<RequirementVersion>> holder = versionService.findAllByRequirement(requirementId, pas);

		return new RequirementVersionDataTableModel(locale, levelFormatterProvider, i18nHelper).buildDataModel(holder,
				params.getsEcho());
	}




	private String buildMarshalledCriticalities(Locale locale) {
		return criticalityComboBuilderProvider.get().useLocale(locale).buildMarshalled();
	}

	private DataTableModel getVerifyingTCModel(RequirementVersion version){
		PagedCollectionHolder<List<TestCase>> holder = verifyingTestCaseManager.findAllByRequirementVersion(
				version.getId(), new DefaultPagingAndSorting("Project.name"));

		return new VerifyingTestCasesTableModelHelper(i18nHelper).buildDataModel(holder, "0");
	}

	private DataTableModel getEventsTableModel(Requirement requirement){
		PagedCollectionHolder<List<RequirementAuditEvent>> auditTrail = auditTrailService
				.findAllByRequirementVersionIdOrderedByDate(requirement.getCurrentVersion().getId(), new DefaultPagingAndSorting());

		RequirementAuditEventTableModelBuilder builder = new RequirementAuditEventTableModelBuilder(LocaleContextHolder.getLocale(), i18nHelper);

		return builder.buildDataModel(auditTrail, "");

	}



	private static String internationalize(Level level, Locale locale,
			Provider<LevelLabelFormatter> levelFormatterProvider) {
		return levelFormatterProvider.get().useLocale(locale).formatLabel(level);
	}


	private static final class RequirementVersionDataTableModel extends DataTableModelBuilder<RequirementVersion> {
		private Locale locale;
		private Provider<LevelLabelFormatter> levelFormatterProvider;
		private InternationalizationHelper i18nHelper;

		private RequirementVersionDataTableModel(Locale locale, Provider<LevelLabelFormatter> levelFormatterProvider, InternationalizationHelper helper) {
			this.locale = locale;
			this.levelFormatterProvider = levelFormatterProvider;
			this.i18nHelper = helper;
		}

		@Override
		public Map<String, Object> buildItemData(RequirementVersion version) {

			Map<String, Object> row = new HashMap<>(7);

			row.put(DataTableModelConstants.DEFAULT_ENTITY_ID_KEY, version.getId());
			row.put("version-number", version.getVersionNumber());
			row.put("reference", version.getReference());
			row.put(DataTableModelConstants.DEFAULT_ENTITY_NAME_KEY, version.getName());
			row.put("status", internationalize(version.getStatus(), locale, levelFormatterProvider));
			row.put("criticality", internationalize(version.getCriticality(), locale, levelFormatterProvider));
			row.put("category", i18nHelper.getMessage(version.getCategory().getLabel(), null, version.getCategory().getLabel(), locale)  );
			row.put("milestone-dates", MilestoneModelUtils.timeIntervalToString(version.getMilestones(),i18nHelper, locale));
			row.put("milestone", MilestoneModelUtils.milestoneLabelsOrderByDate(version.getMilestones()));
			return row;

		}

	}

}
