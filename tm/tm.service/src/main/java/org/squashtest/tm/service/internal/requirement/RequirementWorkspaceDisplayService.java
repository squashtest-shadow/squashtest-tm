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
package org.squashtest.tm.service.internal.requirement;

import org.jooq.Field;
import org.jooq.TableLike;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.requirement.RequirementLibraryPluginBinding;
import org.squashtest.tm.service.internal.workspace.AbstractWorkspaceDisplayService;

import static org.squashtest.tm.jooq.domain.Tables.*;

@Service("requirementWorkspaceDisplayService")
@Transactional(readOnly = true)
public class RequirementWorkspaceDisplayService extends AbstractWorkspaceDisplayService {

	@Override
	protected Field<Long> getProjectLibraryColumn() {
		return PROJECT.RL_ID;
	}

	@Override
	protected String getRel() {
		return "drive";
	}

	@Override
	protected Field<Long> selectLibraryId() {
		return REQUIREMENT_LIBRARY.RL_ID;
	}

	@Override
	protected TableLike<?> getLibraryTable() {
		return REQUIREMENT_LIBRARY;
	}

	@Override
	protected String getClassName() {
		return RequirementLibrary.class.getSimpleName();
	}

	@Override
	protected String getLibraryClassName() {
		return RequirementLibrary.class.getName();
	}

	@Override
	protected String getLibraryPluginType() {
		return RequirementLibraryPluginBinding.RL_TYPE;
	}
}
