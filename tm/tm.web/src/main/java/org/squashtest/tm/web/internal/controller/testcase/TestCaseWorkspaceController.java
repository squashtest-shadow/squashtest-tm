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

import java.util.List;

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.api.workspace.WorkspaceType;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.service.library.WorkspaceService;
import org.squashtest.tm.web.internal.controller.generic.WorkspaceController;

@Controller
@RequestMapping("/test-case-workspace")
public class TestCaseWorkspaceController extends WorkspaceController<TestCaseLibrary> {
	private WorkspaceService<TestCaseLibrary> workspaceService;

	@ServiceReference(serviceBeanName = "squashtest.tm.service.TestCasesWorkspaceService")
	public final void setWorkspaceService(WorkspaceService<TestCaseLibrary> testCaseWorkspaceService) {
		this.workspaceService = testCaseWorkspaceService;
	}

	@Override
	protected WorkspaceService<TestCaseLibrary> getWorkspaceService() {
		return workspaceService;
	}

	@Override
	protected String getWorkspaceViewName() {
		return "page/test-case-workspace";
	}

	@Override
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView showWorkspace() {

		ModelAndView mav = super.showWorkspace();
		List<TestCaseLibrary> libraries = workspaceService.findAllImportableLibraries();
		mav.addObject("editableLibraries", libraries);

		return mav;
	}

	/**
	 * @see org.squashtest.tm.web.internal.controller.generic.WorkspaceController#getWorkspaceType()
	 */
	protected WorkspaceType getWorkspaceType() {
		return null;
	}

}
