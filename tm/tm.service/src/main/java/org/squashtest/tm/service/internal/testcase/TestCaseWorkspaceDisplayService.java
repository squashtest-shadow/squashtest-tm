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
package org.squashtest.tm.service.internal.testcase;

import org.jooq.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryPluginBinding;
import org.squashtest.tm.jooq.domain.tables.records.ProjectRecord;
import org.squashtest.tm.service.internal.workspace.AbstractWorkspaceDisplayService;

import static org.squashtest.tm.jooq.domain.Tables.PROJECT;
import static org.squashtest.tm.jooq.domain.Tables.TEST_CASE_LIBRARY;

@Service
@Transactional(readOnly = true)
public class TestCaseWorkspaceDisplayService extends AbstractWorkspaceDisplayService {

	@Override
	protected String getRel() {
		return "drive";
	}

	@Override
	protected Field<Long> selectLibraryId() {
		return TEST_CASE_LIBRARY.TCL_ID;
	}

	@Override
	protected TableLike<?> getLibraryTable() {
		return org.squashtest.tm.jooq.domain.tables.TestCaseLibrary.TEST_CASE_LIBRARY;
	}

	@Override
	protected String getClassName() {
		return TestCaseLibrary.class.getSimpleName() ;
	}

	@Override
	protected String getLibraryClassName() {
		return TestCaseLibrary.class.getName();
	}

	@Override
	protected String getLibraryPluginType() {
		return TestCaseLibraryPluginBinding.TCL_TYPE;
	}

	@Override
	protected TableField<ProjectRecord, Long> getProjectLibraryColumn() {
		return PROJECT.TCL_ID;
	}
}
