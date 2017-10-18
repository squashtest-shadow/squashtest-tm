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

import org.apache.commons.collections.MultiMap;
import org.jooq.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryPluginBinding;
import org.squashtest.tm.jooq.domain.tables.*;
import org.squashtest.tm.jooq.domain.tables.records.ProjectRecord;
import org.squashtest.tm.service.internal.dto.UserDto;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;
import org.squashtest.tm.service.internal.dto.json.JsonMilestone;
import org.squashtest.tm.service.internal.workspace.AbstractWorkspaceDisplayService;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.squashtest.tm.domain.project.Project.PROJECT_TYPE;
import static org.squashtest.tm.jooq.domain.Tables.*;

@Service("testCaseWorkspaceDisplayService")
@Transactional(readOnly = true)
public class TestCaseWorkspaceDisplayService extends AbstractWorkspaceDisplayService {

	@Inject
	DSLContext DSL;

	private TestCaseLibraryContent TCLC = TEST_CASE_LIBRARY_CONTENT.as("TCLC");
	private TestCaseLibraryNode TCLN = TEST_CASE_LIBRARY_NODE.as("TCLN");
	private TestCaseLibraryNode TCLN_CHILD = TEST_CASE_LIBRARY_NODE.as("TCLN_CHILD");
	private TestCaseFolder TCF = TEST_CASE_FOLDER.as("TCF");
	private TestCase TC = TEST_CASE.as("TC");
	private RequirementVersionCoverage RVC = REQUIREMENT_VERSION_COVERAGE.as("RVC");
	private TestCaseSteps TCS = TEST_CASE_STEPS.as("TCS");
	private TclnRelationship TCLNR = TCLN_RELATIONSHIP.as("TCLNR");

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
		return TestCaseLibrary.class.getSimpleName();
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
	protected List<Long> getOpenedEntityIds(MultiMap expansionCandidates) {
		return (List<Long>) expansionCandidates.get("TestCaseFolder");
	}

	@Override
	protected TableField<ProjectRecord, Long> getProjectLibraryColumn() {
		return PROJECT.TCL_ID;
	}

	@Override
	protected Map<Long, JsTreeNode> doFindLibraries(List<Long> readableProjectIds, UserDto currentUser, List<Long> openedLibraryIds, Map<Long, JsTreeNode> expandedJsTreeNodes, JsonMilestone activeMilestone) {
		{
			List<Long> filteredProjectIds;
			if (hasActiveFilter(currentUser.getUsername())) {
				filteredProjectIds = findFilteredProjectIds(readableProjectIds, currentUser.getUsername());
			} else {
				filteredProjectIds = readableProjectIds;
			}

			if (openedLibraryIds == null)
				openedLibraryIds = Collections.singletonList(-1L);

			Select<Record1<Long>> groupedTestCaseStep = getGroupedTestCaseStep();
			Select<Record1<Long>> groupedTCLNR = getGroupedTCLNR();

			Table<Record10<Long, String, String, String, String, String, String, String, String, String>> childrenInfo = DSL
				.select(selectLibraryId(),
					org.jooq.impl.DSL.groupConcat(org.jooq.impl.DSL.concat(TCLC.CONTENT_ID.cast(String.class), "=Sep="))
						.orderBy(TCLC.CONTENT_ORDER).as("CHILDREN_ID"),
					org.jooq.impl.DSL.groupConcat(org.jooq.impl.DSL.concat(org.jooq.impl.DSL.decode()
						.when(TCF.TCLN_ID.isNotNull(), "test-case-folders")
						.otherwise("test-cases"), "=Sep="))
						.orderBy(TCLC.CONTENT_ORDER).as("CHILDREN_CLASS"),
					org.jooq.impl.DSL.groupConcat(org.jooq.impl.DSL.concat(TCLN.NAME, "=Sep="))
						.orderBy(TCLC.CONTENT_ORDER).as("CHILDREN_NAME"),
					org.jooq.impl.DSL.groupConcat(org.jooq.impl.DSL.concat(org.jooq.impl.DSL.coalesce(TC.IMPORTANCE, " "), "=Sep="))
						.orderBy(TCLC.CONTENT_ORDER).as("CHILDREN_IMPORTANCE"),
					org.jooq.impl.DSL.groupConcat(org.jooq.impl.DSL.concat(org.jooq.impl.DSL.decode()
						.when(TC.REFERENCE.eq(""), " ").otherwise(org.jooq.impl.DSL.coalesce(TC.REFERENCE, " ")), "=Sep="))
						.orderBy(TCLC.CONTENT_ORDER).as("CHILDREN_REFERENCE"),
					org.jooq.impl.DSL.groupConcat(org.jooq.impl.DSL.concat(org.jooq.impl.DSL.coalesce(TC.TC_STATUS, " "), "=Sep="))
						.orderBy(TCLC.CONTENT_ORDER).as("CHILDREN_STATUS"),
					org.jooq.impl.DSL.groupConcat(org.jooq.impl.DSL.concat(org.jooq.impl.DSL.decode()
						.when(groupedTestCaseStep.field("TEST_CASE_ID").isNull(), "false")
						.otherwise("true"), "=Sep="))
						.orderBy(TCLC.CONTENT_ORDER).as("CHILDREN_HAS_STEP"),
					org.jooq.impl.DSL.groupConcat(org.jooq.impl.DSL.concat(org.jooq.impl.DSL.decode()
						.when(RVC.VERIFYING_TEST_CASE_ID.isNull(), "false")
						.otherwise("true"), "=Sep="))
						.orderBy(TCLC.CONTENT_ORDER).as("CHILDREN_IS_REQ_COVERED"),
					org.jooq.impl.DSL.groupConcat(org.jooq.impl.DSL.concat(org.jooq.impl.DSL.decode()
						.when(groupedTCLNR.field("ANCESTOR_ID").isNull(), "false")
						.otherwise("true"), "=Sep="))
						.orderBy(TCLC.CONTENT_ORDER).as("CHILDREN_HAS_CONTENT")
				)
				.from(getLibraryTable())
				.join(PROJECT).using(selectLibraryId())
				.leftJoin(TCLC).on(selectLibraryId().eq(TCLC.LIBRARY_ID))
				.leftJoin(TCLN).on(TCLN.TCLN_ID.eq(TCLC.CONTENT_ID))
				.leftJoin(TCF).on(TCF.TCLN_ID.eq(TCLC.CONTENT_ID))
				.leftJoin(TC).on(TCLC.CONTENT_ID.eq(TC.TCLN_ID))
				.leftJoin(groupedTestCaseStep).on(TC.TCLN_ID.eq(groupedTestCaseStep.field("TEST_CASE_ID", Long.class)))
				.leftJoin(RVC).on(TCLC.CONTENT_ID.eq(RVC.VERIFYING_TEST_CASE_ID))
				.leftJoin(groupedTCLNR).on(TCLC.CONTENT_ID.eq(groupedTCLNR.field("ANCESTOR_ID", Long.class)))
				.where(PROJECT.PROJECT_ID.in(openedLibraryIds))
				.and(PROJECT.PROJECT_TYPE.eq(PROJECT_TYPE))
				.groupBy(selectLibraryId())
				.asTable("CHILDREN_INFO");

			Map<Long, JsTreeNode> jsTreeNodes = DSL
				.select(selectLibraryId(),
					PROJECT.PROJECT_ID,
					PROJECT.NAME,
					PROJECT.LABEL,
					org.jooq.impl.DSL.decode()
						.when(TCLC.LIBRARY_ID.isNull(), false)
						.otherwise(true).as("HAS_CONTENT"),
					childrenInfo.field("CHILDREN_ID"),
					childrenInfo.field("CHILDREN_CLASS"),
					childrenInfo.field("CHILDREN_NAME"),
					childrenInfo.field("CHILDREN_IMPORTANCE"),
					childrenInfo.field("CHILDREN_REFERENCE"),
					childrenInfo.field("CHILDREN_STATUS"),
					childrenInfo.field("CHILDREN_NAME"),
					childrenInfo.field("CHILDREN_HAS_STEP"),
					childrenInfo.field("CHILDREN_IS_REQ_COVERED"),
					childrenInfo.field("CHILDREN_HAS_CONTENT")
				)
				.from(getLibraryTable())
				.join(PROJECT).using(selectLibraryId())
				.leftJoin(TCLC).on(selectLibraryId().eq(TCLC.LIBRARY_ID))
				.leftJoin(childrenInfo).using(selectLibraryId())
				.where(PROJECT.PROJECT_ID.in(filteredProjectIds))
				.and(PROJECT.PROJECT_TYPE.eq(PROJECT_TYPE))
				.groupBy(selectLibraryId())
				.fetch()
				.stream()
				.map(r -> {
					Map<String, Object> attr = new HashMap<>();
					Long libraryId = r.get(selectLibraryId(), Long.class);
					attr.put("resId", libraryId);
					attr.put("resType", getResType());
					attr.put("rel", getRel());
					attr.put("name", getClassName());
					attr.put("id", getClassName() + '-' + libraryId);
					attr.put("title", r.get(PROJECT.LABEL));
					attr.put("project", r.get(PROJECT.PROJECT_ID));

					JsTreeNode node = buildNode(r.get(PROJECT.NAME), null, attr, currentUser);

					if (!(boolean) r.get("HAS_CONTENT")) {
						node.setState(JsTreeNode.State.leaf);
					} else if (r.get("CHILDREN_ID") == null || ((String) r.get("CHILDREN_ID")).isEmpty()) {
						node.setState(JsTreeNode.State.closed);
					} else {
						node.setState(JsTreeNode.State.open);
						node.setChildren(buildDirectChildren((String) r.get("CHILDREN_ID"), (String) r.get("CHILDREN_NAME"),
							(String) r.get("CHILDREN_CLASS"), (String) r.get("CHILDREN_IMPORTANCE"), (String) r.get("CHILDREN_REFERENCE"),
							(String) r.get("CHILDREN_STATUS"), (String) r.get("CHILDREN_HAS_STEP"), (String) r.get("CHILDREN_IS_REQ_COVERED"),
							(String) r.get("CHILDREN_HAS_CONTENT"), currentUser, expandedJsTreeNodes, activeMilestone));
					}
					return node;
				}) // We collect the data in a LinkedHashMap to keep the positionnal order
				.collect(Collectors.toMap(node -> (Long) node.getAttr().get("resId"), Function.identity(),
					(u, v) -> {
						throw new IllegalStateException(String.format("Duplicate key %s", u));
					},
					LinkedHashMap::new));

			return jsTreeNodes;
		}
	}

	@Override
	protected Map<Long, JsTreeNode> FindExpandedJsTreeNodes(UserDto currentUser, List<Long> openedEntityIds, JsonMilestone activeMilestone) {

		Select<Record1<Long>> groupedTestCaseStep = getGroupedTestCaseStep();
		Select<Record1<Long>> groupedTCLNR = getGroupedTCLNR();

		Map<Long, JsTreeNode> jsTreeNodes = DSL
			.select(
				TCLN.TCLN_ID.as("PARENT_FOLDER_ID"),
				TCLN.NAME.as("PARENT_FOLDER_NAME"),
				org.jooq.impl.DSL.groupConcat(org.jooq.impl.DSL.concat(TCLNR.DESCENDANT_ID.cast(String.class), "=Sep="))
					.orderBy(TCLNR.CONTENT_ORDER).as("CHILDREN_ID"),
				org.jooq.impl.DSL.groupConcat(org.jooq.impl.DSL.concat(TCLN_CHILD.NAME, "=Sep="))
					.orderBy(TCLNR.CONTENT_ORDER).as("CHILDREN_NAME"),
				org.jooq.impl.DSL.groupConcat(org.jooq.impl.DSL.concat(org.jooq.impl.DSL.decode()
					.when(TCF.TCLN_ID.isNotNull(), "test-case-folders")
					.otherwise("test-cases"), "=Sep="))
					.orderBy(TCLNR.CONTENT_ORDER).as("CHILDREN_CLASS"),
				org.jooq.impl.DSL.groupConcat(org.jooq.impl.DSL.concat(org.jooq.impl.DSL.coalesce(TC.IMPORTANCE, " "), "=Sep="))
					.orderBy(TCLNR.CONTENT_ORDER).as("CHILDREN_IMPORTANCE"),
				org.jooq.impl.DSL.groupConcat(org.jooq.impl.DSL.concat(org.jooq.impl.DSL.decode()
					.when(TC.REFERENCE.eq(""), " ").otherwise(org.jooq.impl.DSL.coalesce(TC.REFERENCE, " ")), "=Sep="))
					.orderBy(TCLNR.CONTENT_ORDER).as("CHILDREN_REFERENCE"),
				org.jooq.impl.DSL.groupConcat(org.jooq.impl.DSL.concat(org.jooq.impl.DSL.coalesce(TC.TC_STATUS, " "), "=Sep="))
					.orderBy(TCLNR.CONTENT_ORDER).as("CHILDREN_STATUS"),
				org.jooq.impl.DSL.groupConcat(org.jooq.impl.DSL.concat(org.jooq.impl.DSL.decode()
					.when(groupedTestCaseStep.field("TEST_CASE_ID").isNull(), "false")
					.otherwise("true"), "=Sep="))
					.orderBy(TCLNR.CONTENT_ORDER).as("CHILDREN_HAS_STEP"),
				org.jooq.impl.DSL.groupConcat(org.jooq.impl.DSL.concat(org.jooq.impl.DSL.decode()
					.when(RVC.VERIFYING_TEST_CASE_ID.isNull(), "false")
					.otherwise("true"), "=Sep="))
					.orderBy(TCLNR.CONTENT_ORDER).as("CHILDREN_IS_REQ_COVERED"),
				org.jooq.impl.DSL.groupConcat(org.jooq.impl.DSL.concat(org.jooq.impl.DSL.decode()
					.when(groupedTCLNR.field("ANCESTOR_ID").isNull(), "false")
					.otherwise("true"), "=Sep="))
					.orderBy(TCLNR.CONTENT_ORDER).as("CHILDREN_HAS_CONTENT")
			)
			.from(TCLN
					.join(TCLNR).on(TCLN.TCLN_ID.eq(TCLNR.ANCESTOR_ID))
					.join(TCLN_CHILD).on(TCLNR.DESCENDANT_ID.eq(TCLN_CHILD.TCLN_ID))
					.leftJoin(TCF).on(TCLNR.DESCENDANT_ID.eq(TCF.TCLN_ID))
					.leftJoin(TC).on(TCLNR.DESCENDANT_ID.eq(TC.TCLN_ID))
					.leftJoin(groupedTestCaseStep).on(TC.TCLN_ID.eq(groupedTestCaseStep.field("TEST_CASE_ID", Long.class)))
					.leftJoin(RVC).on(TCLNR.DESCENDANT_ID.eq(RVC.VERIFYING_TEST_CASE_ID))
					.leftJoin(groupedTCLNR).on(TCLNR.DESCENDANT_ID.eq(groupedTCLNR.field("ANCESTOR_ID", Long.class)))
			)
			.where(TCLN.TCLN_ID.in(openedEntityIds))
			.groupBy(TCLN.TCLN_ID)
			.fetch()
			.stream()
			.map(r -> {
				Map<String, Object> attr = new HashMap<>();
				Long folderId = (Long) r.get("PARENT_FOLDER_ID");
				attr.put("resId", folderId);
				attr.put("resType", "test-case-folders");
				attr.put("rel", "folder");
				attr.put("name", (String) r.get("PARENT_FOLDER_NAME"));
				attr.put("id", "TestCaseFolder-" + folderId);
				attr.put("title", (String) r.get("PARENT_FOLDER_NAME"));

				JsTreeNode parent = buildNode((String) r.get("PARENT_FOLDER_NAME"), JsTreeNode.State.open, attr, currentUser);
				parent.setChildren(buildDirectChildren((String) r.get("CHILDREN_ID"), (String) r.get("CHILDREN_NAME"),
					(String) r.get("CHILDREN_CLASS"), (String) r.get("CHILDREN_IMPORTANCE"), (String) r.get("CHILDREN_REFERENCE"),
					(String) r.get("CHILDREN_STATUS"), (String) r.get("CHILDREN_HAS_STEP"), (String) r.get("CHILDREN_IS_REQ_COVERED"),
					(String) r.get("CHILDREN_HAS_CONTENT"), currentUser, new HashMap<>(), activeMilestone));
				return parent;
			})
			.collect(Collectors.toMap(node -> (Long) node.getAttr().get("resId"), Function.identity()));

		buildHierarchy(jsTreeNodes, openedEntityIds);
		return jsTreeNodes;
	}

	private Select<Record1<Long>> getGroupedTestCaseStep() {
		return DSL
			.select(TCS.TEST_CASE_ID)
			.from(TCS)
			.groupBy(TCS.TEST_CASE_ID);
	}

	private Select<Record1<Long>> getGroupedTCLNR() {
		return DSL
			.select(TCLNR.ANCESTOR_ID)
			.from(TCLNR)
			.groupBy(TCLNR.ANCESTOR_ID);
	}
}

