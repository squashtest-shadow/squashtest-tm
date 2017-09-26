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


import org.apache.commons.lang3.EnumUtils;
import org.jooq.*;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.InputType;
import org.squashtest.tm.domain.customfield.MultiSelectField;
import org.squashtest.tm.domain.customfield.RenderingLocation;
import org.squashtest.tm.domain.milestone.MilestoneRange;
import org.squashtest.tm.domain.milestone.MilestoneStatus;
import org.squashtest.tm.service.customfield.CustomFieldModelService;
import org.squashtest.tm.service.infolist.InfoListModelService;
import org.squashtest.tm.service.internal.dto.*;
import org.squashtest.tm.service.internal.dto.CustomFieldModelFactory.DatePickerFieldModel;
import org.squashtest.tm.service.internal.dto.CustomFieldModelFactory.SingleSelectFieldModel;
import org.squashtest.tm.service.internal.dto.CustomFieldModelFactory.SingleValuedCustomFieldModel;
import org.squashtest.tm.service.internal.dto.json.*;
import org.squashtest.tm.service.internal.helper.HyphenedStringHelper;
import org.squashtest.tm.service.milestone.MilestoneModelService;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.service.workspace.WorkspaceDisplayService;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;
import static org.squashtest.tm.domain.infolist.SystemListItem.SYSTEM_INFO_LIST_IDENTIFIER;
import static org.squashtest.tm.domain.project.Project.PROJECT_TYPE;
import static org.squashtest.tm.service.internal.dto.CustomFieldModelFactory.CustomFieldOptionModel;
import static org.squashtest.tm.service.internal.dto.CustomFieldModelFactory.MultiSelectFieldModel;
import static org.squashtest.tm.service.internal.dto.PermissionWithMask.findByMask;
import static org.squashtest.tm.service.internal.dto.json.JsTreeNode.State;
import static org.squashtest.tm.jooq.domain.Tables.*;

public abstract class AbstractWorkspaceDisplayService implements WorkspaceDisplayService {

	@Inject
	private MessageSource messageSource;

	@Inject
	DSLContext DSL;

	@Inject
	protected ProjectFinder projectFinder;

	@Inject
	private MilestoneModelService milestoneModelService;

	@Inject
	private CustomFieldModelService customFieldModelService;

	@Inject
	private InfoListModelService infoListModelService;

	@Override
	public Collection<JsTreeNode> findAllLibraries(List<Long> readableProjectIds, UserDto currentUser) {
		Map<Long, JsTreeNode> jsTreeNodes = doFindLibraries(readableProjectIds, currentUser);
		findWizards(readableProjectIds, jsTreeNodes);

		if (currentUser.isNotAdmin()) {
			findPermissionMap(currentUser, jsTreeNodes);
		}

		return jsTreeNodes.values();
	}

	@Override
	public Collection<JsonProject> findAllProjects(List<Long> readableProjectIds, UserDto currentUser) {
		//1 As projects are objects with complex relationship we pre fetch some of the relation to avoid unnecessary joins or requests, and unnecessary conversion in DTO after fetch
		// We do that only on collaborators witch should not be too numerous versus the number of projects
		// good candidate for this pre fetch are infolists, custom fields (not bindings), milestones...
		Map<Long, JsonInfoList> infoListMap = infoListModelService.findUsedInfoList(readableProjectIds);

		//extracting cuf, options... and so on, to avoid multiple identical extractions when fetching projects
		Map<Long, CustomFieldModel> cufMap = customFieldModelService.findUsedCustomFields(readableProjectIds);

		Map<Long, JsonMilestone> milestoneMap = milestoneModelService.findUsedMilestones(readableProjectIds);

		//now we extract projects
		Map<Long, JsonProject> jsonProjects = doFindAllProjects(readableProjectIds, infoListMap, cufMap, milestoneMap);

		return jsonProjects.values();
	}


	//here come the fun part. Fetch project with custom field bindings and infolist ids in a first request -> hydrate with the already fetched models
	//milestone are fetched in a second request witch will be avoided if milestone are not activated on instance.
	protected Map<Long, JsonProject> doFindAllProjects(List<Long> readableProjectIds, Map<Long, JsonInfoList> infoListMap, Map<Long, CustomFieldModel> cufMap, Map<Long, JsonMilestone> milestoneMap) {

		Map<Long, JsonProject> jsonProjectMap = findJsonProjects(readableProjectIds, infoListMap);

		//Now we retrieve the bindings for projects, injecting cuf inside
		//it's a map like :
		//projectId :
			// TEST-CASE : List <CustomFieldBindingModel> linked to test case for this project
			// REQUIREMENT : List <CustomFieldBindingModel> linked to requirements for this project
		//...
		Map<Long, Map<String, List<CustomFieldBindingModel>>> bindingMap = findCustomFieldsBindingsByProject(readableProjectIds, cufMap);

		//We find the milestone bindings and provide projects with them
		Map<Long, List<JsonMilestone>> milestoneByProjectId = findMilestoneByProject(readableProjectIds, milestoneMap);

		//We provide the projects with their bindings and milestones
		jsonProjectMap.forEach((projectId, jsonProject) -> {
			if (bindingMap.containsKey(projectId)) {
				Map<String, List<CustomFieldBindingModel>> bindingsByEntityType = bindingMap.get(projectId);
				jsonProject.setCustomFieldBindings(bindingsByEntityType);
			}

			if (milestoneByProjectId.containsKey(projectId)) {
				List<JsonMilestone> jsonMilestone = milestoneByProjectId.get(projectId);
				jsonProject.setMilestones(new HashSet<>(jsonMilestone));
			}
		});

		return jsonProjectMap;
	}

	private Map<Long, List<JsonMilestone>> findMilestoneByProject(List<Long> readableProjectIds, Map<Long, JsonMilestone> milestoneMap) {
		return DSL.select(MILESTONE_BINDING.PROJECT_ID, MILESTONE_BINDING.MILESTONE_ID)
			.from(MILESTONE_BINDING)
			.where(MILESTONE_BINDING.PROJECT_ID.in(readableProjectIds))
			.fetch()
			.stream()
			.collect(groupingBy((r) -> r.get(MILESTONE_BINDING.PROJECT_ID),
				mapping((r) -> {
					Long milestoneId = r.get(MILESTONE_BINDING.MILESTONE_ID);
					return milestoneMap.get(milestoneId);
				}, toList())
			));
	}

	private Map<Long, Map<String, List<CustomFieldBindingModel>>> findCustomFieldsBindingsByProject(List<Long> readableProjectIds, Map<Long, CustomFieldModel> cufMap) {
		Result result = DSL
			.select(CUSTOM_FIELD_BINDING.CFB_ID, CUSTOM_FIELD_BINDING.BOUND_PROJECT_ID, CUSTOM_FIELD_BINDING.POSITION, CUSTOM_FIELD_BINDING.BOUND_ENTITY, CUSTOM_FIELD_BINDING.CF_ID
				, CUSTOM_FIELD_RENDERING_LOCATION.RENDERING_LOCATION)
			.from(CUSTOM_FIELD_BINDING)
			.leftJoin(CUSTOM_FIELD_RENDERING_LOCATION).on(CUSTOM_FIELD_BINDING.CFB_ID.eq(CUSTOM_FIELD_RENDERING_LOCATION.CFB_ID))
			.where(CUSTOM_FIELD_BINDING.BOUND_PROJECT_ID.in(readableProjectIds))
			.fetch();

		Function<Record,CustomFieldBindingModel> customFieldBindingModelTransformer = getCustomFieldModelTransformer(cufMap);

		Function<Record, RenderingLocationModel> renderingLocationModelTransformer = getRenderingLocationModelTransformer();

		//we inject the rendering location directly inside the binding model
		Function<Map.Entry<CustomFieldBindingModel,List<RenderingLocationModel>>, CustomFieldBindingModel> injector = entry -> {
			CustomFieldBindingModel bindingModel = entry.getKey();
			List<RenderingLocationModel> renderingLocationModels = entry.getValue();
			bindingModel.setRenderingLocations(renderingLocationModels.toArray(new RenderingLocationModel[]{}));
			return bindingModel;
		};

		List<CustomFieldBindingModel> list = StreamUtils.performJoinAggregate(customFieldBindingModelTransformer, renderingLocationModelTransformer, injector, result);

		return groupByProjectAndType(list);
	}

	private Map<Long, Map<String, List<CustomFieldBindingModel>>> groupByProjectAndType(List<CustomFieldBindingModel> list) {
		return list.stream().collect(
			groupingBy(CustomFieldBindingModel::getProjectId, //we groupBy project id
				//and we groupBy bindable entity, with an initial map already initialized with empty lists as required per model.
				groupingBy((CustomFieldBindingModel customFieldBindingModel) -> customFieldBindingModel.getBoundEntity().getEnumName(),
					() -> {
						//here we create the empty list, initial step of the reducing operation
						HashMap<String, List<CustomFieldBindingModel>> map = new HashMap<>();
						EnumSet<BindableEntity> bindableEntities = EnumSet.allOf(BindableEntity.class);
						bindableEntities.forEach(bindableEntity -> {
							map.put(bindableEntity.name(), new ArrayList<>());
						});
						return map;
					},
					mapping(
						Function.identity(),
						toList()
					))
			));
	}

	private Function<Record, RenderingLocationModel> getRenderingLocationModelTransformer() {
		return r -> {
			String renderingLocationKey = r.get(CUSTOM_FIELD_RENDERING_LOCATION.RENDERING_LOCATION);
			if (renderingLocationKey == null) {
				return null;//it's ok, we collect with a null filtering collector
			}
			RenderingLocationModel renderingLocationModel = new RenderingLocationModel();
			RenderingLocation renderingLocation = EnumUtils.getEnum(RenderingLocation.class, renderingLocationKey);
			renderingLocationModel.setEnumName(renderingLocationKey);
			renderingLocationModel.setFriendlyName(getMessage(renderingLocation.getI18nKey()));
			return renderingLocationModel;
		};
	}

	/**
	 * Return a function that will be responsible to transform a tuple to CustomFieldBidingModel
	 * @param cufMap a pre fetched map
	 * @return the function that will be called to transform a Tuple to a CustomFieldBindingModel
	 */
	private Function<Record, CustomFieldBindingModel> getCustomFieldModelTransformer(Map<Long, CustomFieldModel> cufMap) {
		return r -> {//creating a map <CustomFieldBindingModel, List<RenderingLocationModel>>
			//here we create custom field binding model
			//double created by joins are filtered by the grouping by as we have implemented equals on id attribute
			CustomFieldBindingModel customFieldBindingModel = new CustomFieldBindingModel();
			customFieldBindingModel.setId(r.get(CUSTOM_FIELD_BINDING.CFB_ID));
			customFieldBindingModel.setProjectId(r.get(CUSTOM_FIELD_BINDING.BOUND_PROJECT_ID));
			customFieldBindingModel.setPosition(r.get(CUSTOM_FIELD_BINDING.POSITION));
			String boundEntityKey = r.get(CUSTOM_FIELD_BINDING.BOUND_ENTITY);
			BindableEntity bindableEntity = EnumUtils.getEnum(BindableEntity.class, boundEntityKey);
			BindableEntityModel bindableEntityModel = new BindableEntityModel();
			bindableEntityModel.setEnumName(boundEntityKey);
			bindableEntityModel.setFriendlyName(getMessage(bindableEntity.getI18nKey()));
			customFieldBindingModel.setBoundEntity(bindableEntityModel);
			customFieldBindingModel.setCustomField(cufMap.get(r.get(CUSTOM_FIELD_BINDING.CF_ID)));
			return customFieldBindingModel;
		};
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

                }).collect(Collectors.toMap(JsonProject::getId, Function.identity()));
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

	public Map<Long, JsTreeNode> doFindLibraries(List<Long> readableProjectIds, UserDto currentUser) {
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
				node.setState(State.closed);
				return node;
			})
			.collect(Collectors.toMap(node -> (Long) node.getAttr().get("resId"), Function.identity()));

		//TODO opened nodes and content
		return jsTreeNodes;
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


}
