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
package org.squashtest.tm.web.internal.controller.testcase;

import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.springframework.context.MessageSource;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.RequirementCategory;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.exception.VerifiedRequirementException;
import org.squashtest.tm.service.requirement.RequirementLibraryFinderService;
import org.squashtest.tm.service.requirement.VerifiedRequirementsManagerService;
import org.squashtest.tm.service.testcase.TestCaseModificationService;
import org.squashtest.tm.web.internal.helper.VerifiedRequirementActionSummaryBuilder;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableMapperPagingAndSortingAdapter;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelHelper;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.IndexBasedMapper;

/**
 * Controller for verified requirements management page.
 * 
 * @author Gregory Fouquet
 * 
 */
@Controller
public class VerifiedRequirementsManagerController {
	/**
	 * 
	 */
	private static final String REQUIREMENT_VERSIONS_IDS = "requirementVersionsIds[]";

	private static final String REQUIREMENTS_IDS = "requirementsIds[]";

	private final DatatableMapper verifiedReqMapper = new IndexBasedMapper(9)
														  .mapAttribute(Project.class, "name", String.class, 1)
														  .mapAttribute(RequirementVersion.class, "id", Long.class, 2)
														  .mapAttribute(RequirementVersion.class, "reference", String.class, 3)
														  .mapAttribute(RequirementVersion.class, "name", String.class, 4)
														  .mapAttribute(RequirementVersion.class, "versionNumber", Integer.class, 5)
														  .mapAttribute(RequirementVersion.class, "criticality", RequirementCriticality.class, 6)
														  .mapAttribute(RequirementVersion.class, "category", RequirementCategory.class, 7);
	
	@Inject
	private Provider<DriveNodeBuilder> driveNodeBuilder;
	@Inject
	private MessageSource messageSource;

	private VerifiedRequirementsManagerService verifiedRequirementsManagerService;
	private RequirementLibraryFinderService requirementLibraryFinder;
	private TestCaseModificationService testCaseFinder;

	@ServiceReference
	public void setVerifiedRequirementsManagerService(
			VerifiedRequirementsManagerService verifiedRequirementsManagerService) {
		this.verifiedRequirementsManagerService = verifiedRequirementsManagerService;
	}

	/**
	 * @param requirementLibraryFinder
	 *            the requirementLibraryFinder to set
	 */
	@ServiceReference
	public void setRequirementLibraryFinder(RequirementLibraryFinderService requirementLibraryFinder) {
		this.requirementLibraryFinder = requirementLibraryFinder;
	}

	@RequestMapping(value = "/test-cases/{testCaseId}/verified-requirement-versions/manager", method = RequestMethod.GET)
	public String showManager(@PathVariable long testCaseId, Model model) {
		TestCase testCase = testCaseFinder.findById(testCaseId);
		List<RequirementLibrary> linkableLibraries = requirementLibraryFinder
				.findLinkableRequirementLibraries();

		List<JsTreeNode> linkableLibrariesModel = createLinkableLibrariesModel(linkableLibraries);

		model.addAttribute("testCase", testCase);
		model.addAttribute("linkableLibrariesModel", linkableLibrariesModel);

		return "page/test-cases/show-verified-requirements-manager";
	}

	private List<JsTreeNode> createLinkableLibrariesModel(List<RequirementLibrary> linkableLibraries) {
		DriveNodeBuilder builder = driveNodeBuilder.get();
		List<JsTreeNode> linkableLibrariesModel = new ArrayList<JsTreeNode>();

		for (RequirementLibrary library : linkableLibraries) {
			JsTreeNode libraryNode = builder.setModel(library).build();
			linkableLibrariesModel.add(libraryNode);
		}
		return linkableLibrariesModel;
	}

	@RequestMapping(value = "/test-cases/{testCaseId}/verified-requirements", method = RequestMethod.POST, params = REQUIREMENTS_IDS)
	public @ResponseBody
	Map<String, Object> addVerifiedRequirementsToTestCase(@RequestParam(REQUIREMENTS_IDS) List<Long> requirementsIds,
			@PathVariable long testCaseId) {
		Collection<VerifiedRequirementException> rejections = verifiedRequirementsManagerService
				.addVerifiedRequirementsToTestCase(requirementsIds, testCaseId);

		return buildSummary(rejections);

	}
	
	@RequestMapping(value = "/test-cases/{testCaseId}/verified-requirement-versions/{oldVersionId}", method = RequestMethod.POST)
	@ResponseBody
	public int changeVersion(@PathVariable long testCaseId, @PathVariable long oldVersionId, @RequestParam(VALUE) long newVersionId) {
		
		List<Long> oldVersion = new ArrayList<Long>();
		oldVersion.add(oldVersionId);
		List<Long> newVersion = new ArrayList<Long>();
		newVersion.add(newVersionId);
		
		int newVersionNumber = verifiedRequirementsManagerService.changeVerifiedRequirementVersionOnTestCase(oldVersionId, newVersionId, testCaseId);
		
		return newVersionNumber;
	}
	

	private Map<String, Object> buildSummary(Collection<VerifiedRequirementException> rejections) {
		return VerifiedRequirementActionSummaryBuilder.buildAddActionSummary(rejections);
	}

	@RequestMapping(value = "/test-cases/{testCaseId}/non-verified-requirement-versions", method = RequestMethod.POST, params = REQUIREMENT_VERSIONS_IDS)
	public @ResponseBody
	void removeVerifiedRequirementVersionsFromTestCase(
			@RequestParam(REQUIREMENT_VERSIONS_IDS) List<Long> requirementVersionsIds, @PathVariable long testCaseId) {
		verifiedRequirementsManagerService.removeVerifiedRequirementVersionsFromTestCase(requirementVersionsIds, testCaseId);

	}

	@RequestMapping(value = "/test-cases/{testCaseId}/verified-requirement-versions/{requirementVersionId}", method = RequestMethod.DELETE)
	public @ResponseBody
	void removeVerifiedRequirementVersionFromTestCase(@PathVariable long requirementVersionId,
			@PathVariable long testCaseId) {
		verifiedRequirementsManagerService.removeVerifiedRequirementVersionFromTestCase(requirementVersionId, testCaseId);

	}

	@RequestMapping(value = "/test-cases/{testCaseId}/verified-requirement-versions/table", params = "sEcho")
	@ResponseBody
	public DataTableModel getVerifiedRequirementsTableModel(@PathVariable long testCaseId,
			final DataTableDrawParameters params, final Locale locale) {

		PagingAndSorting filter = new DataTableMapperPagingAndSortingAdapter(params, verifiedReqMapper);

		PagedCollectionHolder<List<RequirementVersion>> holder = verifiedRequirementsManagerService
				.findAllDirectlyVerifiedRequirementsByTestCaseId(testCaseId, filter);

		return new DataTableModelHelper<RequirementVersion>() {
			@Override
			public Object[] buildItemData(RequirementVersion item) {
				return new Object[] { getCurrentIndex(), item.getRequirement().getProject().getName(), item.getId(), 
						item.getReference(), item.getName(), item.getVersionNumber(), internationalizeCriticality(item.getCriticality(), locale), internationalizeCategory(item.getCategory(), locale), "", item.getStatus().name(), true // the
				};
			}

		}.buildDataModel(holder, params.getsEcho());

	}

	private Object internationalizeCriticality(RequirementCriticality criticality, Locale locale) {
		return messageSource.getMessage(criticality.getI18nKey(), null, locale);
	}

	private Object internationalizeCategory(RequirementCategory category, Locale locale) {
		return messageSource.getMessage(category.getI18nKey(), null, locale);
	}

	/**
	 * @param testCaseFinder the testCaseFinder to set
	 */
	@ServiceReference
	public void setTestCaseFinder(TestCaseModificationService testCaseFinder) {
		this.testCaseFinder = testCaseFinder;
	}
}
