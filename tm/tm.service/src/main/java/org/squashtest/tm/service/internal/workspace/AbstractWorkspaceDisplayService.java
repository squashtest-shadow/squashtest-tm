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
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.TableLike;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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

	private static final String END_SEPARATOR_PLACEHOLDER = "=Sep=";
	private static final String SEPARATOR_PLACEHOLDER = END_SEPARATOR_PLACEHOLDER + ",";

	@Override
	public Collection<JsTreeNode> findAllLibraries(List<Long> readableProjectIds, UserDto currentUser, MultiMap expansionCandidates, JsonMilestone activeMilestone) {

		List<Long> openedLibraryIds = (List<Long>) expansionCandidates.get(getClassName());
		List<Long> openedEntityIds = getOpenedEntityIds(expansionCandidates);
		Map<Long, JsTreeNode> expandedJsTreeNodes = new HashMap<>();

		if (openedEntityIds != null) {
			expandedJsTreeNodes = FindExpandedJsTreeNodes(currentUser, openedEntityIds, activeMilestone);
		}

		Map<Long, JsTreeNode> jsTreeNodes = doFindLibraries(readableProjectIds, currentUser, openedLibraryIds, expandedJsTreeNodes, activeMilestone);
		findWizards(readableProjectIds, jsTreeNodes);

		if (currentUser.isNotAdmin()) {
			findPermissionMap(currentUser, jsTreeNodes);
		}

		return jsTreeNodes.values();
	}

	protected abstract List<Long> getOpenedEntityIds(MultiMap expansionCandidates);

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

	protected  abstract Map<Long, JsTreeNode> doFindLibraries(List<Long> readableProjectIds, UserDto currentUser, List<Long> openedLibraryIds,
												 Map<Long, JsTreeNode> expandedJsTreeNodes, JsonMilestone activeMilestone);

	protected abstract Map<Long, JsTreeNode> FindExpandedJsTreeNodes(UserDto currentUser, List<Long> openedEntityIds, JsonMilestone activeMilestone);

	protected JsTreeNode buildNode(String title, State state, Map<String, Object> attr, UserDto currentUser) {
		JsTreeNode node = new JsTreeNode();
		node.setTitle(title);
		if (state != null) {
			node.setState(state);
		}
		node.setAttr(attr);

		//permissions set to false by default except for admin which have rights by definition
		EnumSet<PermissionWithMask> permissions = EnumSet.allOf(PermissionWithMask.class);
		for (PermissionWithMask permission : permissions) {
			node.addAttr(permission.getQuality(), String.valueOf(currentUser.isAdmin()));
		}

		// milestone attributes : libraries are yes-men
		node.addAttr("milestone-creatable-deletable", "true");
		node.addAttr("milestone-editable", "true");
		node.addAttr("wizards", new HashSet<String>());
		return node;
	}

	// TODO factorise or make it abstract
	protected List<JsTreeNode> buildDirectChildren(String childrenId, String childrenName, String childrenClass, String childrenImportance,
												 String childrenReference, String childrenStatus, String childrenHasStep,
												 String childrenIsReqCovered, String childrenHasContent, UserDto currentUser, Map<Long, JsTreeNode> expandedJsTreeNodes, JsonMilestone activeMilestone) {

		List<Long> testCaseIdsLinkedToActiveMilestone = findTCIdsLinkedToActiveMilestone(activeMilestone);

		List<JsTreeNode> children = new ArrayList<>();
		String[] childrenIdArray = childrenId.substring(0, childrenId.length() - END_SEPARATOR_PLACEHOLDER.length()).split(SEPARATOR_PLACEHOLDER);
		String[] childrenNameArray = childrenName.substring(0, childrenName.length() - END_SEPARATOR_PLACEHOLDER.length()).split(SEPARATOR_PLACEHOLDER);
		String[] childrenClassArray = childrenClass.substring(0, childrenClass.length() - END_SEPARATOR_PLACEHOLDER.length()).split(SEPARATOR_PLACEHOLDER);
		String[] childrenImportanceArray = childrenImportance.substring(0, childrenImportance.length() - END_SEPARATOR_PLACEHOLDER.length()).split(SEPARATOR_PLACEHOLDER);
		String[] childrenReferenceArray = childrenReference.substring(0, childrenReference.length() - END_SEPARATOR_PLACEHOLDER.length()).split(SEPARATOR_PLACEHOLDER);
		String[] childrenStatusArray = childrenStatus.substring(0, childrenStatus.length() - END_SEPARATOR_PLACEHOLDER.length()).split(SEPARATOR_PLACEHOLDER);
		String[] childrenHasStepArray = childrenHasStep.substring(0, childrenHasStep.length() - END_SEPARATOR_PLACEHOLDER.length()).split(SEPARATOR_PLACEHOLDER);
		String[] childrenIsReqCoveredArray = childrenIsReqCovered.substring(0, childrenIsReqCovered.length() - END_SEPARATOR_PLACEHOLDER.length()).split(SEPARATOR_PLACEHOLDER);
		String[] childrenHasContentArray = childrenHasContent.substring(0, childrenHasContent.length() - END_SEPARATOR_PLACEHOLDER.length()).split(SEPARATOR_PLACEHOLDER);

		for (int i = 0; i < childrenIdArray.length; i++) {
			Long childId = Long.parseLong(childrenIdArray[i]);
			if (passesMilestoneFilter(activeMilestone, childrenClassArray[i], testCaseIdsLinkedToActiveMilestone, childId)) {
				// we check if we must replace the current child by an expanded JsTreeNode, otherwise we build the node
				if (expandedJsTreeNodes.containsKey(childId)) {
					children.add(expandedJsTreeNodes.get(childId));
				} else {
					Map<String, Object> attr = new HashMap<>();
					State state;

					attr.put("resId", childId);
					attr.put("resType", childrenClassArray[i]);
					attr.put("name", childrenNameArray[i]);

					if (childrenClassArray[i].equals("test-case-folders")) {
						attr.put("id", "TestCaseFolder-" + childId);
						attr.put("rel", "folder");
						if (Boolean.parseBoolean(childrenHasContentArray[i])) {
							state = State.closed;
						} else {
							state = State.leaf;
						}
					} else {
						attr.put("id", "TestCase-" + childId);
						attr.put("rel", "test-case");
						attr.put("reference", childrenReferenceArray[i]);
						attr.put("importance", childrenImportanceArray[i].toLowerCase());
						attr.put("status", childrenStatusArray[i].toLowerCase());
						attr.put("hassteps", childrenHasStepArray[i]);
						attr.put("isreqcovered", childrenIsReqCoveredArray[i]);

						//build tooltip
						String[] args = {getMessage("test-case.status." + childrenStatusArray[i]), getMessage("test-case.importance." + childrenImportanceArray[i]),
							getMessage("squashtm.yesno." + childrenIsReqCoveredArray[i]), getMessage("tooltip.tree.testCase.hasSteps." + childrenHasStepArray[i])};
						attr.put("title", getMessage("label.tree.testCase.tooltip", args));

						state = State.leaf;
					}
					children.add(buildNode(childrenNameArray[i], state, attr, currentUser));
				}
			}
		}
		return children;
	}

	// TODO factorise or make it abstract
	private boolean passesMilestoneFilter(JsonMilestone activeMilestone, String childrenClass, List<Long> testCaseIdsLinkedToActiveMilestone, Long childId) {
		return (activeMilestone == null || childrenClass.equals("test-case-folders") || testCaseHasActiveMilestone(testCaseIdsLinkedToActiveMilestone, childId));
	}

	// TODO factorise or make it abstract
	private boolean testCaseHasActiveMilestone(List<Long> testCaseIdsLinkedToActiveMilestone, Long libraryId) {
		for (Long testCaseId : testCaseIdsLinkedToActiveMilestone) {
			if (libraryId.equals(testCaseId)) {
				return true;
			}
		}
		return false;
	}

	// TODO factorise or make it abstract
	public List<Long> findNodesByMilestoneId(Long milestoneId) {
		return DSL.select(MILESTONE_TEST_CASE.TEST_CASE_ID)
			.from(MILESTONE_TEST_CASE)
			.where(MILESTONE_TEST_CASE.MILESTONE_ID.eq(milestoneId))
			.fetch(MILESTONE_TEST_CASE.TEST_CASE_ID, Long.class);
	}

	// TODO factorise or make it abstract
	private List<Long> findTCIdsLinkedToActiveMilestone(JsonMilestone activeMilestone) {
		List<Long> testCaseIdsLinkedToActiveMilestone = new ArrayList<>();
		if (activeMilestone != null) {
			testCaseIdsLinkedToActiveMilestone = findNodesByMilestoneId(activeMilestone.getId());
		}
		return testCaseIdsLinkedToActiveMilestone;
	}

	protected void buildHierarchy(Map<Long, JsTreeNode> jsTreeNodes, List<Long> openedEntityIds) {
		//TODO Make it work for all Workspaces
		List<Long> directLibraryChildren = DSL
			.select()
			.from(TEST_CASE_LIBRARY_CONTENT)
			.where(TEST_CASE_LIBRARY_CONTENT.CONTENT_ID.in(openedEntityIds))
			.fetch(TEST_CASE_LIBRARY_CONTENT.CONTENT_ID, Long.class);

		for (JsTreeNode jsTreeNode : jsTreeNodes.values()) {
			if (directLibraryChildren.contains(jsTreeNode.getAttr().get("resId"))) {
				buildSubHierarchy(jsTreeNodes, jsTreeNode.getChildren(), openedEntityIds);
			}
		}
	}

	private void buildSubHierarchy(Map<Long, JsTreeNode> jsTreeNodes, List<JsTreeNode> children, List<Long> openedEntityIds) {
		for (JsTreeNode jsTreeNodeChild : children) {
			if (openedEntityIds.contains((Long) jsTreeNodeChild.getAttr().get("resId"))) {
				jsTreeNodeChild.setState(State.open);
				jsTreeNodeChild.setChildren(jsTreeNodes.get((Long) jsTreeNodeChild.getAttr().get("resId")).getChildren());
				if (jsTreeNodeChild.getChildren().size() != 0) {
					buildSubHierarchy(jsTreeNodes, jsTreeNodeChild.getChildren(), openedEntityIds);
				}
			}
		}
	}

	protected List<Long> findFilteredProjectIds(List<Long> readableProjectIds, String username) {
		return DSL.select(PROJECT_FILTER_ENTRY.PROJECT_ID)
			.from(PROJECT_FILTER)
			.join(PROJECT_FILTER_ENTRY).on(PROJECT_FILTER.PROJECT_FILTER_ID.eq(PROJECT_FILTER_ENTRY.FILTER_ID))
			.where(PROJECT_FILTER.USER_LOGIN.eq(username)).and(PROJECT_FILTER_ENTRY.PROJECT_ID.in(readableProjectIds))
			.fetch(PROJECT_FILTER_ENTRY.PROJECT_ID, Long.class);
	}

	protected boolean hasActiveFilter(String userName) {
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


// When we will factorize the code
/*	protected abstract Field<Long> getMilestoneLibraryNodeId();

	protected abstract Field<Long> getMilestoneId();

	protected abstract TableLike<?> getMilestoneLibraryNodeTable();*/

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
