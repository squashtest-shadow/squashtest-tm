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
package org.squashtest.tm.service.internal.workspace;


import org.apache.commons.collections.MultiMap;
import org.jooq.*;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.squashtest.tm.jooq.domain.tables.*;
import org.squashtest.tm.service.customfield.CustomFieldModelService;
import org.squashtest.tm.service.infolist.InfoListModelService;
import org.squashtest.tm.service.internal.dto.CustomFieldBindingModel;
import org.squashtest.tm.service.internal.dto.PermissionWithMask;
import org.squashtest.tm.service.internal.dto.UserDto;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;
import org.squashtest.tm.service.internal.dto.json.JsonInfoList;
import org.squashtest.tm.service.internal.dto.json.JsonMilestone;
import org.squashtest.tm.service.internal.dto.json.JsonProject;
import org.squashtest.tm.service.internal.helper.HyphenedStringHelper;
import org.squashtest.tm.service.milestone.MilestoneModelService;
import org.squashtest.tm.service.workspace.WorkspaceDisplayService;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;
import static org.squashtest.tm.domain.project.Project.PROJECT_TYPE;
import static org.squashtest.tm.jooq.domain.Tables.*;
import static org.squashtest.tm.service.internal.dto.PermissionWithMask.findByMask;
import static org.squashtest.tm.service.internal.dto.json.JsTreeNode.State;

public abstract class AbstractWorkspaceDisplayService implements WorkspaceDisplayService {

	@Inject
	private MessageSource messageSource;

	@Inject
	DSLContext DSL;

	@Inject
	private MilestoneModelService milestoneModelService;

	@Inject
	private CustomFieldModelService customFieldModelService;

	@Inject
	private InfoListModelService infoListModelService;

	@Override
	public Collection<JsTreeNode> findAllLibraries(List<Long> readableProjectIds, UserDto currentUser, MultiMap expansionCandidates) {
		List<Long> openedLibraryIds = (List<Long>) expansionCandidates.get("TestCaseLibrary");
		List<Long> openedFolderIds = (List<Long>) expansionCandidates.get("TestCaseFolder");
		Map<Long, JsTreeNode> expandedJsTreeNodes = new HashMap<>();
		if (openedFolderIds != null) {
			expandedJsTreeNodes = FindExpandedJsTreeNodes(currentUser, openedFolderIds);
		}

		Map<Long, JsTreeNode> jsTreeNodes = doFindLibraries(readableProjectIds, currentUser, openedLibraryIds, expandedJsTreeNodes);
		findWizards(readableProjectIds, jsTreeNodes);

		if (currentUser.isNotAdmin()) {
			findPermissionMap(currentUser, jsTreeNodes);
		}

		return jsTreeNodes.values();
	}

	@Override
	public Collection<JsonProject> findAllProjects(List<Long> readableProjectIds, UserDto currentUser) {
		Map<Long, JsonProject> jsonProjects = doFindAllProjects(readableProjectIds);
		return jsonProjects.values();
	}

	protected Map<Long, JsonProject> doFindAllProjects(List<Long> readableProjectIds) {
		// As projects are objects with complex relationship we pre fetch some of the relation to avoid unnecessary joins or requests, and unnecessary conversion in DTO after fetch
		// We do that only on collaborators witch should not be too numerous versus the number of projects
		// good candidate for this pre fetch are infolists, custom fields (not bindings), milestones...
		Map<Long, JsonInfoList> infoListMap = infoListModelService.findUsedInfoList(readableProjectIds);

		Map<Long, JsonProject> jsonProjectMap = findJsonProjects(readableProjectIds, infoListMap);

		// Now we retrieve the bindings for projects, injecting cuf inside
		Map<Long, Map<String, List<CustomFieldBindingModel>>> customFieldsBindingsByProject = customFieldModelService.findCustomFieldsBindingsByProject(readableProjectIds);

		// We find the milestone bindings and provide projects with them
		Map<Long, List<JsonMilestone>> milestoneByProjectId = milestoneModelService.findMilestoneByProject(readableProjectIds);

		// We provide the projects with their bindings and milestones
		jsonProjectMap.forEach((projectId, jsonProject) -> {
			if (customFieldsBindingsByProject.containsKey(projectId)) {
				Map<String, List<CustomFieldBindingModel>> bindingsByEntityType = customFieldsBindingsByProject.get(projectId);
				jsonProject.setCustomFieldBindings(bindingsByEntityType);
			}

			if (milestoneByProjectId.containsKey(projectId)) {
				List<JsonMilestone> jsonMilestone = milestoneByProjectId.get(projectId);
				jsonProject.setMilestones(new HashSet<>(jsonMilestone));
			}
		});

		return jsonProjectMap;
	}

	private Map<Long, JsonProject> findJsonProjects(List<Long> readableProjectIds, Map<Long, JsonInfoList> infoListMap) {
		return DSL.select(PROJECT.PROJECT_ID, PROJECT.NAME, PROJECT.REQ_CATEGORIES_LIST, PROJECT.TC_NATURES_LIST, PROJECT.TC_TYPES_LIST)
			.from(PROJECT)
			.where(PROJECT.PROJECT_ID.in(readableProjectIds)).and(PROJECT.PROJECT_TYPE.eq(PROJECT_TYPE))
			.orderBy(PROJECT.PROJECT_ID)
			.stream()
			.map(r -> {
				Long projectId = r.get(PROJECT.PROJECT_ID);
				JsonProject jsonProject = new JsonProject(projectId, r.get(PROJECT.NAME));
				jsonProject.setRequirementCategories(infoListMap.get(r.get(PROJECT.REQ_CATEGORIES_LIST)));
				jsonProject.setTestCaseNatures(infoListMap.get(r.get(PROJECT.TC_NATURES_LIST)));
				jsonProject.setTestCaseTypes(infoListMap.get(r.get(PROJECT.TC_TYPES_LIST)));
				return jsonProject;

			}).collect(Collectors.toMap(JsonProject::getId, Function.identity(),
				(u, v) -> {
					throw new IllegalStateException(String.format("Duplicate key %s", u));
				},
				LinkedHashMap::new));
	}

	public void findPermissionMap(UserDto currentUser, Map<Long, JsTreeNode> jsTreeNodes) {
		Set<Long> libraryIds = jsTreeNodes.keySet();

		DSL
			.selectDistinct(selectLibraryId(), ACL_GROUP_PERMISSION.PERMISSION_MASK)
			.from(getLibraryTable())
			.join(PROJECT).on(getProjectLibraryColumn().eq(selectLibraryId()))
			.join(ACL_OBJECT_IDENTITY).on(ACL_OBJECT_IDENTITY.IDENTITY.eq(selectLibraryId()))
			.join(ACL_RESPONSIBILITY_SCOPE_ENTRY).on(ACL_OBJECT_IDENTITY.ID.eq(ACL_RESPONSIBILITY_SCOPE_ENTRY.OBJECT_IDENTITY_ID))
			.join(ACL_GROUP_PERMISSION).on(ACL_RESPONSIBILITY_SCOPE_ENTRY.ACL_GROUP_ID.eq(ACL_GROUP_PERMISSION.ACL_GROUP_ID))
			.join(ACL_CLASS).on(ACL_GROUP_PERMISSION.CLASS_ID.eq(ACL_CLASS.ID).and(ACL_CLASS.CLASSNAME.eq(getLibraryClassName())))
			.where(ACL_RESPONSIBILITY_SCOPE_ENTRY.PARTY_ID.in(currentUser.getPartyIds())).and(PROJECT.PROJECT_TYPE.eq(PROJECT_TYPE)).and(getProjectLibraryColumn().in(libraryIds))
			.fetch()
			.stream()
			.collect(groupingBy(
				r -> r.getValue(selectLibraryId()),
				mapping(
					r -> r.getValue(ACL_GROUP_PERMISSION.PERMISSION_MASK),
					toList()
				)
			)).forEach((Long nodeId, List<Integer> masks) -> {
			JsTreeNode node = jsTreeNodes.get(nodeId);

			for (Integer mask : masks) {
				PermissionWithMask permission = findByMask(mask);
				if (permission != null) {
					node.addAttr(permission.getQuality(), String.valueOf(true));
				}
			}
		});
	}

	protected abstract Field<Long> getProjectLibraryColumn();

	public Map<Long, JsTreeNode> doFindLibraries(List<Long> readableProjectIds, UserDto currentUser, List<Long> openedLibraryIds, Map<Long, JsTreeNode> expandedJsTreeNodes) {
		List<Long> filteredProjectIds;
		if (hasActiveFilter(currentUser.getUsername())) {
			filteredProjectIds = findFilteredProjectIds(readableProjectIds, currentUser.getUsername());
		} else {
			filteredProjectIds = readableProjectIds;
		}

		if (openedLibraryIds == null)
			openedLibraryIds = Collections.singletonList(-1L);

		TestCaseLibraryContent TCLC = TEST_CASE_LIBRARY_CONTENT.as("TCLC");
		TestCaseLibraryNode TCLN = TEST_CASE_LIBRARY_NODE.as("TCLN");
		TestCaseFolder TCF = TEST_CASE_FOLDER.as("TCF");
		TestCase TC = TEST_CASE.as("TC");
		TestCaseSteps TCS = TEST_CASE_STEPS.as("TCS");
		RequirementVersionCoverage RVC = REQUIREMENT_VERSION_COVERAGE.as("RVC");

		Select<Record1<Long>> groupedTestCaseStep = DSL
			.select(TCS.TEST_CASE_ID)
			.from(TCS)
			.groupBy(TCS.TEST_CASE_ID);

		Table<Record9<Long, String, String, String, String, String, String, String, String>> childrenInfo = DSL
			.select(selectLibraryId(),
				org.jooq.impl.DSL.groupConcat(TCLC.CONTENT_ID)
					.orderBy(TCLC.CONTENT_ORDER).as("CHILDREN_ID"),
				org.jooq.impl.DSL.groupConcat(org.jooq.impl.DSL.decode()
					.when(TCF.TCLN_ID.isNotNull(), "test-case-folder")
					.otherwise("test-case"))
					.orderBy(TCLC.CONTENT_ORDER).as("CHILDREN_CLASS"),
				org.jooq.impl.DSL.groupConcat(TCLN.NAME)
					.orderBy(TCLC.CONTENT_ORDER).as("CHILDREN_NAME"),

				org.jooq.impl.DSL.groupConcat(org.jooq.impl.DSL.coalesce(TC.IMPORTANCE, " "))
					.orderBy(TCLC.CONTENT_ORDER).as("CHILDREN_IMPORTANCE"),
				org.jooq.impl.DSL.groupConcat(org.jooq.impl.DSL.decode()
					.when(TC.REFERENCE.eq(""), " ").otherwise(org.jooq.impl.DSL.coalesce(TC.REFERENCE, " ")))
					.orderBy(TCLC.CONTENT_ORDER).as("CHILDREN_REFERENCE"),
				org.jooq.impl.DSL.groupConcat(org.jooq.impl.DSL.coalesce(TC.TC_STATUS, " "))
					.orderBy(TCLC.CONTENT_ORDER).as("CHILDREN_STATUS"),
				org.jooq.impl.DSL.groupConcat(org.jooq.impl.DSL.decode()
					.when(groupedTestCaseStep.field("TEST_CASE_ID").isNull(), "false")
					.otherwise("true"))
					.orderBy(TCLC.CONTENT_ORDER).as("CHILDREN_HAS_STEP"),
				org.jooq.impl.DSL.groupConcat(org.jooq.impl.DSL.decode()
					.when(RVC.VERIFYING_TEST_CASE_ID.isNull(), "false")
					.otherwise("true"))
					.orderBy(TCLC.CONTENT_ORDER).as("CHILDREN_IS_REQ_COVERED")
			)
			.from(getLibraryTable())
			.join(PROJECT).using(selectLibraryId())
			.leftJoin(TCLC).on(selectLibraryId().eq(TCLC.LIBRARY_ID))
			.leftJoin(TCLN).on(TCLN.TCLN_ID.eq(TCLC.CONTENT_ID))
			.leftJoin(TCF).on(TCF.TCLN_ID.eq(TCLC.CONTENT_ID))
			.leftJoin(TC).on(TCLC.CONTENT_ID.eq(TC.TCLN_ID))
			.leftJoin(groupedTestCaseStep).on(TC.TCLN_ID.eq(groupedTestCaseStep.field("TEST_CASE_ID", Long.class)))
			.leftJoin(RVC).on(TCLC.CONTENT_ID.eq(RVC.VERIFYING_TEST_CASE_ID))
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
				childrenInfo.field("CHILDREN_IS_REQ_COVERED")
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
				if (!(boolean) r.get("HAS_CONTENT")) {
					node.setState(State.leaf);
				} else if (r.get("CHILDREN_ID") == null || ((String) r.get("CHILDREN_ID")).isEmpty()) {
					node.setState(State.closed);
				} else {
					node.setState(State.open);
					node.setChildren(buildDirectChildren((String) r.get("CHILDREN_ID"), (String) r.get("CHILDREN_NAME"),
						(String) r.get("CHILDREN_CLASS"), (String) r.get("CHILDREN_IMPORTANCE"), (String) r.get("CHILDREN_REFERENCE"),
						(String) r.get("CHILDREN_STATUS"), (String) r.get("CHILDREN_HAS_STEP"), (String) r.get("CHILDREN_IS_REQ_COVERED"), currentUser, expandedJsTreeNodes));
				}
				return node;
			}) // We collect the data in a LinkedHashMap to keep the positionnal order
			.collect(Collectors.toMap(node -> (Long) node.getAttr().get("resId"), Function.identity(),
				(u, v) -> {
					throw new IllegalStateException(String.format("Duplicate key %s", u));
				},
				LinkedHashMap::new));

		//TODO opened nodes and content
		return jsTreeNodes;
	}

	public Map<Long, JsTreeNode> FindExpandedJsTreeNodes(UserDto currentUser, List<Long> openedFolderIds) {
		//TODO Make it work for all Workspaces?
		TestCaseLibraryNode TCLN = TEST_CASE_LIBRARY_NODE.as("TCLN");
		TestCaseLibraryNode TCLN_CHILD = TEST_CASE_LIBRARY_NODE.as("TCLN_CHILD");
		TclnRelationship TCLNR = TCLN_RELATIONSHIP.as("TCLNR");
		TestCaseFolder TCF = TEST_CASE_FOLDER.as("TCF");
		TestCase TC = TEST_CASE.as("TC");
		RequirementVersionCoverage RVC = REQUIREMENT_VERSION_COVERAGE.as("RVC");

		Table<Record> groupedTestCaseStep = DSL
			.select()
			.from(TEST_CASE_STEPS)
			.groupBy(TEST_CASE_STEPS.TEST_CASE_ID)
			.asTable("TCS");

		Map<Long, JsTreeNode> jsTreeNodes = DSL
			.select(
				TCLN.TCLN_ID.as("PARENT_FOLDER_ID"),
				TCLN.NAME.as("PARENT_FOLDER_NAME"),
				org.jooq.impl.DSL.groupConcat(TCLNR.DESCENDANT_ID)
					.orderBy(TCLNR.CONTENT_ORDER).as("CHILDREN_ID"),
				org.jooq.impl.DSL.groupConcat(TCLN_CHILD.NAME)
					.orderBy(TCLNR.CONTENT_ORDER).as("CHILDREN_NAME"),
				org.jooq.impl.DSL.groupConcat(org.jooq.impl.DSL.decode()
					.when(TCF.TCLN_ID.isNotNull(), "test-case-folder")
					.otherwise("test-cases"))
					.orderBy(TCLNR.CONTENT_ORDER).as("CHILDREN_CLASS"),
				org.jooq.impl.DSL.groupConcat(org.jooq.impl.DSL.coalesce(TC.IMPORTANCE, " "))
					.orderBy(TCLNR.CONTENT_ORDER).as("CHILDREN_IMPORTANCE"),
				org.jooq.impl.DSL.groupConcat(org.jooq.impl.DSL.decode()
					.when(TC.REFERENCE.eq(""), " ").otherwise(org.jooq.impl.DSL.coalesce(TC.REFERENCE, " ")))
					.orderBy(TCLNR.CONTENT_ORDER).as("CHILDREN_REFERENCE"),
				org.jooq.impl.DSL.groupConcat(org.jooq.impl.DSL.coalesce(TC.TC_STATUS, " "))
					.orderBy(TCLNR.CONTENT_ORDER).as("CHILDREN_STATUS"),
				org.jooq.impl.DSL.groupConcat(org.jooq.impl.DSL.decode()
					.when(groupedTestCaseStep.field("TEST_CASE_ID").isNull(), "false")
					.otherwise("true"))
					.orderBy(TCLNR.CONTENT_ORDER).as("CHILDREN_HAS_STEP"),
				org.jooq.impl.DSL.groupConcat(org.jooq.impl.DSL.decode()
					.when(RVC.VERIFYING_TEST_CASE_ID.isNull(), "false")
					.otherwise("true"))
					.orderBy(TCLNR.CONTENT_ORDER).as("CHILDREN_IS_REQ_COVERED")
			)
			.from(TCLN
					.join(TCLNR).on(TCLN.TCLN_ID.eq(TCLNR.ANCESTOR_ID))
					.join(TCLN_CHILD).on(TCLNR.DESCENDANT_ID.eq(TCLN_CHILD.TCLN_ID))
					.leftJoin(TCF).on(TCLNR.DESCENDANT_ID.eq(TCF.TCLN_ID))
					.leftJoin(TC).on(TCLNR.DESCENDANT_ID.eq(TC.TCLN_ID))
					.leftJoin(groupedTestCaseStep).on(TC.TCLN_ID.eq(groupedTestCaseStep.field("TEST_CASE_ID", Long.class)))
					.leftJoin(RVC).on(TCLNR.DESCENDANT_ID.eq(RVC.VERIFYING_TEST_CASE_ID))
			)
			.where(TCLN.TCLN_ID.in(openedFolderIds))
			.groupBy(TCLN.TCLN_ID)
			.fetch()
			.stream()
			.map(r -> {
				JsTreeNode parent = buildParent((Long) r.get("PARENT_FOLDER_ID"), (String) r.get("PARENT_FOLDER_NAME"), currentUser);
				parent.setChildren(buildDirectChildren((String) r.get("CHILDREN_ID"), (String) r.get("CHILDREN_NAME"),
					(String) r.get("CHILDREN_CLASS"), (String) r.get("CHILDREN_IMPORTANCE"), (String) r.get("CHILDREN_REFERENCE"),
					(String) r.get("CHILDREN_STATUS"), (String) r.get("CHILDREN_HAS_STEP"), (String) r.get("CHILDREN_IS_REQ_COVERED"), currentUser, new HashMap<>()));
				return parent;
			})
			.collect(Collectors.toMap(node -> (Long) node.getAttr().get("resId"), Function.identity()));

		buildHierarchy(jsTreeNodes, openedFolderIds);
		return jsTreeNodes;
	}

	//TODO factorise
	private JsTreeNode buildParent(Long parentId, String parentName, UserDto currentUser) {
		JsTreeNode node = new JsTreeNode();
		node.addAttr("resId", parentId);
		node.setTitle(parentName);
		node.addAttr("resType", "test-case-folders");
		node.addAttr("rel", "folder");
		node.addAttr("name", parentName);
		node.addAttr("id", "TestCaseFolder-" + parentId);

		//permissions set to false by default except for admin which have rights by definition
		EnumSet<PermissionWithMask> permissions = EnumSet.allOf(PermissionWithMask.class);
		for (PermissionWithMask permission : permissions) {
			node.addAttr(permission.getQuality(), String.valueOf(currentUser.isAdmin()));
		}

		// milestone attributes : libraries are yes-men
		node.addAttr("milestone-creatable-deletable", "true");
		node.addAttr("milestone-editable", "true");
		node.addAttr("wizards", new HashSet<String>());
		node.setState(State.open);

		return node;
	}

	private List<JsTreeNode> buildDirectChildren(String childrenId, String childrenName, String childrenClass, String childrenImportance,
												 String childrenReference, String childrenStatus, String childrenHasStep,
												 String childrenIsReqCovered, UserDto currentUser, Map<Long, JsTreeNode> expandedJsTreeNodes) {
		List<JsTreeNode> children = new ArrayList<>();
		String[] childrenIds = childrenId.split(",");
		String[] childrenNames = childrenName.split(",");
		String[] childrenClasses = childrenClass.split(",");
		String[] childrenImportances = childrenImportance.split(",");
		String[] childrenReferences = childrenReference.split(",");
		String[] childrenStatuses = childrenStatus.split(",");
		String[] childrenHasSteps = childrenHasStep.split(",");
		String[] childrenIsReqCovereds = childrenIsReqCovered.split(",");

		for (int i = 0; i < childrenIds.length; i++) {
			if (expandedJsTreeNodes.containsKey(Long.parseLong(childrenIds[i]))) {
				children.add(expandedJsTreeNodes.get(Long.parseLong(childrenIds[i])));
			} else {
				JsTreeNode childNode = new JsTreeNode();
				childNode.addAttr("resId", childrenIds[i]);
				childNode.setTitle(childrenNames[i]);
				childNode.addAttr("resType", childrenClasses[i]);
				childNode.addAttr("name", childrenNames[i]);

				//permissions set to false by default except for admin which have rights by definition
				EnumSet<PermissionWithMask> permissions = EnumSet.allOf(PermissionWithMask.class);
				for (PermissionWithMask permission : permissions) {
					childNode.addAttr(permission.getQuality(), String.valueOf(currentUser.isAdmin()));
				}

				// milestone attributes : libraries are yes-men
				childNode.addAttr("milestone-creatable-deletable", "true");
				childNode.addAttr("milestone-editable", "true");
				childNode.addAttr("wizards", new HashSet<String>());
				if (childrenClasses[i].equals("test-case-folder")) {
					childNode.addAttr("id", "TestCaseFolder-" + childrenIds[i]);
					childNode.addAttr("rel", "folder");
					childNode.setState(State.closed);
				} else {
					boolean bool = false;
					childNode.addAttr("id", "TestCase-" + childrenIds[i]);
					childNode.addAttr("rel", "test-case");
					childNode.addAttr("reference", childrenReferences[i]);
					childNode.addAttr("importance", childrenImportances[i].toLowerCase());
					childNode.addAttr("status", childrenStatuses[i].toLowerCase());
					childNode.addAttr("hassteps", childrenHasSteps[i]);
					childNode.addAttr("isreqcovered", childrenIsReqCovereds[i]);
					//build tooltip
					String[] args = {getMessage("test-case.status." + childrenStatuses[i]), getMessage("test-case.importance." + childrenImportances[i]),
						getMessage("squashtm.yesno." + childrenIsReqCovereds[i]), getMessage("tooltip.tree.testCase.hasSteps." + childrenHasSteps[i])};
					String tooltip = getMessage("label.tree.testCase.tooltip", args);
					childNode.addAttr("title", tooltip);
					childNode.setState(State.leaf);
				}
				children.add(childNode);
			}
		}

		return children;
	}

	private void buildHierarchy(Map<Long, JsTreeNode> jsTreeNodes, List<Long> openedFolderIds) {
		//TODO Make it work for all Workspaces
		List<Long> directLibraryChildren = DSL
			.select()
			.from(TEST_CASE_LIBRARY_CONTENT)
			.where(TEST_CASE_LIBRARY_CONTENT.CONTENT_ID.in(openedFolderIds))
			.fetch(TEST_CASE_LIBRARY_CONTENT.CONTENT_ID, Long.class);

		// we will iterate over the copy and modify the original, otherwise we have a ConcurrentModificationException
		List<JsTreeNode> jsTreeNodesCopy = new ArrayList<>();
		jsTreeNodesCopy.addAll(jsTreeNodes.values());

		for (JsTreeNode jsTreeNode : jsTreeNodes.values()) {
			if (directLibraryChildren.contains(jsTreeNode.getAttr().get("resId"))) {
				buildSubHierarchy(jsTreeNodes, jsTreeNode.getChildren(), openedFolderIds);
			}
		}
	}


	private void buildSubHierarchy(Map<Long, JsTreeNode> jsTreeNodes, List<JsTreeNode> children, List<Long> openedFolderIds) {
		for (JsTreeNode jsTreeNodeChild : children) {
			Long id = (Long.parseLong((String) jsTreeNodeChild.getAttr().get("resId")));
			if (openedFolderIds.contains(id)) {
				jsTreeNodeChild.setState(State.open);
				jsTreeNodeChild.setChildren(jsTreeNodes.get(id).getChildren());
				jsTreeNodes.remove(id);
				buildSubHierarchy(jsTreeNodes, jsTreeNodeChild.getChildren(), openedFolderIds);
			}
		}
	}

	private List<Long> findFilteredProjectIds(List<Long> readableProjectIds, String username) {
		return DSL.select(PROJECT_FILTER_ENTRY.PROJECT_ID)
			.from(PROJECT_FILTER)
			.join(PROJECT_FILTER_ENTRY).on(PROJECT_FILTER.PROJECT_FILTER_ID.eq(PROJECT_FILTER_ENTRY.FILTER_ID))
			.where(PROJECT_FILTER.USER_LOGIN.eq(username)).and(PROJECT_FILTER_ENTRY.PROJECT_ID.in(readableProjectIds))
			.fetch(PROJECT_FILTER_ENTRY.PROJECT_ID, Long.class);
	}

	private boolean hasActiveFilter(String userName) {
		//first we must filter by global filter
		Record1<Boolean> record1 = DSL.select(PROJECT_FILTER.ACTIVATED)
			.from(PROJECT_FILTER)
			.where(PROJECT_FILTER.USER_LOGIN.eq(userName))
			.fetchOne();

		if (record1 == null) {
			return false;
		}
		return record1.get(PROJECT_FILTER.ACTIVATED);
	}

	protected abstract String getRel();

	protected abstract Field<Long> selectLibraryId();

	protected abstract TableLike<?> getLibraryTable();

	protected abstract String getClassName();

	protected abstract String getLibraryClassName();

	protected String getResType() {
		return buildResourceType(getClassName());
	}

	private String buildResourceType(String classSimpleName) {
		String singleResourceType = HyphenedStringHelper.camelCaseToHyphened(classSimpleName);
		return singleResourceType.replaceAll("y$", "ies");
	}

	public void findWizards(List<Long> readableProjectIds, Map<Long, JsTreeNode> jsTreeNodes) {

		Map<Long, Set<String>> pluginByLibraryId = DSL.select(getProjectLibraryColumn(), LIBRARY_PLUGIN_BINDING.PLUGIN_ID)
			.from(PROJECT)
			.join(getLibraryTable()).using(getProjectLibraryColumn())
			.join(LIBRARY_PLUGIN_BINDING).on(LIBRARY_PLUGIN_BINDING.LIBRARY_ID.eq(getProjectLibraryColumn()).and(LIBRARY_PLUGIN_BINDING.LIBRARY_TYPE.eq(getLibraryPluginType())))
			.where(PROJECT.PROJECT_ID.in(readableProjectIds).and((PROJECT.PROJECT_TYPE).eq(PROJECT_TYPE)))
			.fetch()
			.stream()
			.collect(Collectors.groupingBy(r -> r.get(getProjectLibraryColumn()), mapping(r -> r.get(LIBRARY_PLUGIN_BINDING.PLUGIN_ID), toSet())));

		pluginByLibraryId.forEach((libId, pluginIds) -> {
			jsTreeNodes.get(libId).addAttr("wizards", pluginIds);
		});
	}

	protected abstract String getLibraryPluginType();

	private String getMessage(String key) {
		Locale locale = LocaleContextHolder.getLocale();
		return messageSource.getMessage(key, null, locale);
	}

	private String getMessage(String code, Object[] args) {
		Locale locale = LocaleContextHolder.getLocale();
		return messageSource.getMessage(code, args, locale);
	}


}
