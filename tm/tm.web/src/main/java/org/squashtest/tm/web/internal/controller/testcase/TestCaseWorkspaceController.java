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
package org.squashtest.tm.web.internal.controller.testcase;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.squashtest.tm.api.workspace.WorkspaceType;
import org.squashtest.tm.domain.dto.JsonProject;
import org.squashtest.tm.domain.dto.ProjectDto;
import org.squashtest.tm.domain.dto.TestCaseLibraryDto;
import org.squashtest.tm.domain.dto.jstree.JsTreeNode;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.service.customreport.CustomReportDashboardService;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.library.WorkspaceService;
import org.squashtest.tm.service.optimized.OptimizedService;
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService;
import org.squashtest.tm.web.internal.controller.generic.WorkspaceController;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.SQLException;
import java.util.*;

@Controller
@RequestMapping("/test-case-workspace")
public class TestCaseWorkspaceController extends WorkspaceController<TestCaseLibraryNode> {

	@Inject
	private TestCaseLibraryNavigationService testCaseLibraryNavigationService;

	@Inject
	@Named("squashtest.tm.service.TestCasesWorkspaceService")
	private WorkspaceService<TestCaseLibrary> workspaceService;

	@Inject
	@Named("testCase.driveNodeBuilder")
	private Provider<DriveNodeBuilder<TestCaseLibraryNode>> driveNodeBuilderProvider;

	@Inject
	private CustomReportDashboardService customReportDashboardService;

	@Inject
	private OptimizedService optimizedService;

	@Inject
	private ProjectDao projectDao;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	protected WorkspaceService<TestCaseLibrary> getWorkspaceService() {
		return workspaceService;
	}

	@Override
	protected String getWorkspaceViewName() {
		return "test-case-workspace.html";
	}

	/**
	 * @see org.squashtest.tm.web.internal.controller.generic.WorkspaceController#getWorkspaceType()
	 */
	@Override
	protected WorkspaceType getWorkspaceType() {
		return null;
	}

	@Override
	protected void populateModel(Model model, Locale locale) {
		List<TestCaseLibrary> libraries = workspaceService.findAllImportableLibraries();
		Collections.sort(libraries, new Comparator<TestCaseLibrary>() {
			@Override
			public int compare(TestCaseLibrary o1, TestCaseLibrary o2) {
				String name1 = o1.getProject().getName();
				String name2 = o2.getProject().getName();
				return  name1.compareTo(name2);
			}
		});
		model.addAttribute("editableLibraries", libraries);
	}

	/**
	 * @see org.squashtest.tm.web.internal.controller.generic.WorkspaceController#driveNodeBuilderProvider()
	 */
	@Override
	protected Provider<DriveNodeBuilder<TestCaseLibraryNode>> driveNodeBuilderProvider() {
		return driveNodeBuilderProvider;
	}

	@Override
	protected String[] getNodeParentsInWorkspace(Long elementId) {
		List<String> parents = testCaseLibraryNavigationService.getParentNodesAsStringList(elementId);
		return parents.toArray(new String[parents.size()]);
	}

	@Override
	protected String getTreeElementIdInWorkspace(Long elementId) {
		return "TestCase-" + elementId;
	}

	@RequestMapping(path = "/optimized",method = RequestMethod.GET)
	public String getOptimizedWorkspace(Model model, Locale locale,
										@CookieValue(value = "jstree_open", required = false, defaultValue = "") String[] openedNodes,
										@CookieValue(value = "workspace-prefs", required = false, defaultValue = "") String elementId) throws SQLException {

		//1 get the projects i can read
		//1.1 get the projects ids without Spring security involved (500 request for that... i don't want that)
		List<Long> readableProjectIds = optimizedService.findReadableProjectIds();
		model.addAttribute("projectIds", readableProjectIds);

		//1.2 get the js projects fully hydrated (Infolist, Milestones, Cuf bindings)
		List<JsonProject> jsonProjects = optimizedService.findJsonProjects(readableProjectIds);
		model.addAttribute("projects", jsonProjects);

		//2 get the root model nodes ie just test case libraries for the poc
		Collection<JsTreeNode> jsTreeNodes = optimizedService.findLibraries(readableProjectIds);
		model.addAttribute("rootModel", jsTreeNodes);

		//3 Degenerated model that need the same infos in a different shape
		// Love it but no time to redo the html view and the js that need that so...
		List<TestCaseLibraryDto> libraries = new ArrayList<>();
		for (JsTreeNode jsTreeNode : jsTreeNodes) {
			Object importable = jsTreeNode.getAttr().get("importable");
			if (importable != null && importable.equals("true")) {
				TestCaseLibraryDto libraryDto = new TestCaseLibraryDto();
				Object resId = jsTreeNode.getAttr().get("resId");
				libraryDto.setId(Long.parseLong(resId.toString()));
				ProjectDto projectDto = new ProjectDto();
				Object project = jsTreeNode.getAttr().get("project");
				projectDto.setId(Long.parseLong(project.toString()));
				projectDto.setName(jsTreeNode.getTitle());
				libraryDto.setProject(projectDto);
				libraries.add(libraryDto);
			}
		}

		model.addAttribute("editableLibraries", libraries);

		//defaultInfolist
		model.addAttribute("userPrefs", getWorkspaceUserPref());
		model.addAttribute("defaultInfoLists", i18nLevelEnumInfolistHelper.getInternationalizedDefaultList(locale));

		//enums, nothing to optimize
		model.addAttribute("testCaseImportance", i18nLevelEnumInfolistHelper.getI18nLevelEnum(TestCaseImportance.class,locale));
		model.addAttribute("testCaseStatus", i18nLevelEnumInfolistHelper.getI18nLevelEnum(TestCaseStatus.class,locale));
		model.addAttribute("requirementStatus", i18nLevelEnumInfolistHelper.getI18nLevelEnum(RequirementStatus.class,locale));
		model.addAttribute("requirementCriticality", i18nLevelEnumInfolistHelper.getI18nLevelEnum(RequirementCriticality.class,locale));
		model.addAttribute("executionStatus", i18nLevelEnumInfolistHelper.getI18nLevelEnum(ExecutionStatus.class, locale));

		return "test-case-workspace.html";
	}

	@RequestMapping(path = "/testId",method = RequestMethod.GET)
	public String getOptimizedWorkspaceToto(Model model, Locale locale,
										@CookieValue(value = "jstree_open", required = false, defaultValue = "") String[] openedNodes,
										@CookieValue(value = "workspace-prefs", required = false, defaultValue = "") String elementId) throws SQLException {

		Project project = entityManager.find(Project.class, 6L);
		Project project1 = entityManager.find(Project.class, 6L);

		Project project2 = projectDao.findById(6L);
		Project project3 = projectDao.findById(6L);

		assert project == project1;
		assert project == project2;
		assert project == project3;

		return "toto";
	}
}
