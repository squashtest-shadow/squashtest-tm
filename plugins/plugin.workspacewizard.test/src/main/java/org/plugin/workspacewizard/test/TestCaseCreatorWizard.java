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
package org.plugin.workspacewizard.test;

import org.plugin.workspacewizard.test.utils.DefaultMenuItem;
import org.squashtest.tm.api.widget.MenuItem;
import org.squashtest.tm.api.wizard.WorkspaceWizard;
import org.squashtest.tm.api.workspace.WorkspaceType;

public class TestCaseCreatorWizard implements WorkspaceWizard{

	@Override
	public String getName() {
		return "test case creator";
	}

	@Override
	public String getId() {
		return "squashtm.test.creator.testcase";
	}

	@Override
	public WorkspaceType getDisplayWorkspace() {
		return WorkspaceType.TEST_CASE_WORKSPACE;
	}

	@Override
	public MenuItem getWizardMenu() {
		return new DefaultMenuItem("test case creator", "create smart test cases", "api/creator/test-case");
	}

}
