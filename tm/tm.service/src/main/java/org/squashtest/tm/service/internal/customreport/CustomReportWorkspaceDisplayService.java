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
package org.squashtest.tm.service.internal.customreport;

import org.apache.commons.collections.MultiMap;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.TableLike;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.customreport.CustomReportLibrary;
import org.squashtest.tm.service.internal.dto.PermissionWithMask;
import org.squashtest.tm.service.internal.dto.UserDto;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;
import org.squashtest.tm.service.internal.dto.json.JsonMilestone;
import org.squashtest.tm.service.internal.workspace.AbstractWorkspaceDisplayService;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.squashtest.tm.domain.project.Project.PROJECT_TYPE;
import static org.squashtest.tm.jooq.domain.Tables.*;

@Service("customReportWorkspaceDisplayService")
@Transactional(readOnly = true)
public class CustomReportWorkspaceDisplayService extends AbstractWorkspaceDisplayService {

	@Inject
	DSLContext DSL;

	@Override
	protected List<Long> getOpenedEntityIds(MultiMap expansionCandidates) {
		return null;
	}

	@Override
	protected Field<Long> getProjectLibraryColumn() {
		return PROJECT.CRL_ID;
	}

	@Override
	protected Map<Long, JsTreeNode> doFindLibraries(List<Long> readableProjectIds, UserDto currentUser, List<Long> openedLibraryIds, Map<Long, JsTreeNode> expandedJsTreeNodes, JsonMilestone activeMilestone) {
		List<Long> filteredProjectIds;
		if (hasActiveFilter(currentUser.getUsername())) {
			filteredProjectIds = findFilteredProjectIds(readableProjectIds, currentUser.getUsername());
		} else {
			filteredProjectIds = readableProjectIds;
		}


		Map<Long, JsTreeNode> jsTreeNodes = DSL
			.select(selectLibraryId(), PROJECT.PROJECT_ID, PROJECT.NAME, PROJECT.LABEL)
			.from(getLibraryTable())
			.join(PROJECT).using(selectLibraryId())
			.where(PROJECT.PROJECT_ID.in(filteredProjectIds))
			.and(PROJECT.PROJECT_TYPE.eq(PROJECT_TYPE))
			.fetch()
			.stream()
			.map(r -> {
				JsTreeNode node = new JsTreeNode();
				Long libraryId = r.get(selectLibraryId(), Long.class);
				node.addAttr("resId", libraryId);
				node.setTitle(r.get(PROJECT.NAME));
				node.addAttr("resType", getResType());
				node.addAttr("rel", getRel());
				node.addAttr("name", getClassName());
				node.addAttr("id", getClassName() + '-' + libraryId);
				node.addAttr("title", r.get(PROJECT.LABEL));
				node.addAttr("project", r.get(PROJECT.PROJECT_ID));

				//permissions set to false by default except for admin witch have rights by definition
				EnumSet<PermissionWithMask> permissions = EnumSet.allOf(PermissionWithMask.class);
				for (PermissionWithMask permission : permissions) {
					node.addAttr(permission.getQuality(), String.valueOf(currentUser.isAdmin()));
				}

				// milestone attributes : libraries are yes-men
				node.addAttr("milestone-creatable-deletable", "true");
				node.addAttr("milestone-editable", "true");
				node.addAttr("wizards", new HashSet<String>());
				node.setState(JsTreeNode.State.closed);
				return node;
			})
			.collect(Collectors.toMap(node -> (Long) node.getAttr().get("resId"), Function.identity()));

		//TODO opened nodes and content
		return jsTreeNodes;
	}

	@Override
	protected Map<Long, JsTreeNode> FindExpandedJsTreeNodes(UserDto currentUser, List<Long> openedEntityIds, JsonMilestone activeMilestone) {
		return null;
	}

	@Override
	protected String getRel() {
		return "drive";
	}

	@Override
	protected Field<Long> selectLibraryId() {
		return CUSTOM_REPORT_LIBRARY.CRL_ID;
	}

	@Override
	protected TableLike<?> getLibraryTable() {
		return CUSTOM_REPORT_LIBRARY;
	}

	@Override
	protected String getClassName() {
		return CustomReportLibrary.class.getSimpleName();
	}

	@Override
	protected String getLibraryClassName() {
		return CustomReportLibrary.class.getName();
	}

	@Override
	protected String getLibraryPluginType() {
		throw new RuntimeException("No plugin library of type Custom Report exists in squash tm");
	}
}
