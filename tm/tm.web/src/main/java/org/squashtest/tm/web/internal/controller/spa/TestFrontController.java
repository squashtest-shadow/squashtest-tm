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
package org.squashtest.tm.web.internal.controller.spa;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.domain.dto.JsonProject;
import org.squashtest.tm.domain.dto.ProjectDto;
import org.squashtest.tm.domain.dto.TestCaseLibraryDto;
import org.squashtest.tm.domain.dto.jstree.JsTreeNode;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.service.optimized.OptimizedService;
import org.squashtest.tm.service.user.PartyPreferenceService;
import org.squashtest.tm.web.internal.helper.I18nLevelEnumInfolistHelper;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.*;

@Controller
@RequestMapping("/test-front")
public class TestFrontController {

	@Inject
	private I18nLevelEnumInfolistHelper i18nLevelEnumInfolistHelper;

	@Inject
	private OptimizedService optimizedService;

	@Inject
	private PartyPreferenceService partyPreferenceService;


	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public Map<String,Object> getOptimizedWorkspace(Model model, Locale locale,
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
		// Love it but no time to redo the html view and the js that need that so ... just reshape them...
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

		return new HashMap<String,Object>(model.asMap());
	}

	private Map<String,String> getWorkspaceUserPref(){
		return partyPreferenceService.findPreferencesForCurrentUser();
	};
}
