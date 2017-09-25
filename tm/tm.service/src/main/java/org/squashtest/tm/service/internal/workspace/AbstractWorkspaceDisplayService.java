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
import org.squashtest.tm.dto.*;
import org.squashtest.tm.dto.CustomFieldModelFactory.DatePickerFieldModel;
import org.squashtest.tm.dto.CustomFieldModelFactory.SingleSelectFieldModel;
import org.squashtest.tm.dto.CustomFieldModelFactory.SingleValuedCustomFieldModel;
import org.squashtest.tm.dto.json.*;
import org.squashtest.tm.service.internal.helper.HyphenedStringHelper;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.service.workspace.WorkspaceDisplayService;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;
import static org.squashtest.tm.domain.infolist.SystemListItem.SYSTEM_INFO_LIST_IDENTIFIER;
import static org.squashtest.tm.domain.project.Project.PROJECT_TYPE;
import static org.squashtest.tm.dto.CustomFieldModelFactory.CustomFieldOptionModel;
import static org.squashtest.tm.dto.CustomFieldModelFactory.MultiSelectFieldModel;
import static org.squashtest.tm.dto.PermissionWithMask.findByMask;
import static org.squashtest.tm.dto.json.JsTreeNode.State;
import static org.squashtest.tm.jooq.domain.Tables.*;

public abstract class AbstractWorkspaceDisplayService implements WorkspaceDisplayService {

	@Inject
	private MessageSource messageSource;

	@Inject
	DSLContext DSL;

	@Inject
	protected ProjectFinder projectFinder;

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
		Map<Long, JsonInfoList> infoListMap = findUsedInfolist(readableProjectIds);

		//extracting cuf, options... and so on, to avoid multiple identical extractions when fetching projects
		Map<Long, CustomFieldModel> cufMap = findUsedCustomFields(readableProjectIds);

		Map<Long, JsonMilestone> milestoneMap = findUsedMilestones(readableProjectIds);

		//now we extract projects
		Map<Long, JsonProject> jsonProjects = doFindAllProjects(readableProjectIds, infoListMap, cufMap, milestoneMap);


		return jsonProjects.values();
	}

	@Override
	public Map<String, String> findSystemInfoListItemLabels() {
		return DSL.select(INFO_LIST_ITEM.LABEL)
			.from(INFO_LIST_ITEM)
			.where(INFO_LIST_ITEM.ITEM_TYPE.eq(SYSTEM_INFO_LIST_IDENTIFIER))
			.fetch(INFO_LIST_ITEM.LABEL, String.class)
			.stream()
			.collect(Collectors.toMap(Function.identity(), this::getMessage));
	}

	@Override
	public JsonMilestone findMilestoneModel(Long activeMilestoneId) {
		ArrayList<Long> milestoneIds = new ArrayList<>();
		milestoneIds.add(activeMilestoneId);
		Map<Long, JsonMilestone> jsonMilestones = findJsonMilestones(milestoneIds);
		return jsonMilestones.get(activeMilestoneId);
	}

	private Map<Long, CustomFieldModel> findUsedCustomFields(List<Long> readableProjectIds) {
		List<Long> usedCufIds = findUsedCustomFieldIds(readableProjectIds);
		return findCufMap(usedCufIds);
	}

	private Map<Long, JsonInfoList> findUsedInfolist(List<Long> readableProjectIds) {
		Set<Long> usedInfoListIds = findUsedInfoListIds(readableProjectIds);
		return findInfoListMap(usedInfoListIds);
	}

	private Map<Long, JsonMilestone> findUsedMilestones(List<Long> readableProjectIds) {
		List<Long> usedMilestonesIds = findUsedMilestoneIds(readableProjectIds);
		return findJsonMilestones(usedMilestonesIds);
	}


	protected Map<Long, JsonMilestone> findJsonMilestones(List<Long> usedMilestonesIds) {
		return DSL.select(MILESTONE.MILESTONE_ID, MILESTONE.LABEL, MILESTONE.M_RANGE, MILESTONE.STATUS, MILESTONE.END_DATE
			, CORE_USER.LOGIN)
			.from(MILESTONE)
			.join(CORE_USER).on(MILESTONE.USER_ID.eq(CORE_USER.PARTY_ID))
			.where(MILESTONE.MILESTONE_ID.in(usedMilestonesIds))
			.fetch()
			.stream()
			.map(r -> {
				String mRangeKey = r.get(MILESTONE.M_RANGE);
				MilestoneRange milestoneRange = EnumUtils.getEnum(MilestoneRange.class, mRangeKey);

				String mStatusKey = r.get(MILESTONE.STATUS);
				MilestoneStatus milestoneStatus = EnumUtils.getEnum(MilestoneStatus.class, mStatusKey);

				return new JsonMilestone(r.get(MILESTONE.MILESTONE_ID), r.get(MILESTONE.LABEL), milestoneStatus, milestoneRange, r.get(MILESTONE.END_DATE), r.get(CORE_USER.LOGIN));
			})
			.collect(Collectors.toMap(JsonMilestone::getId, Function.identity()));
	}

	protected List<Long> findUsedMilestoneIds(List<Long> readableProjectIds) {
		return DSL.selectDistinct(MILESTONE_BINDING.MILESTONE_ID)
			.from(PROJECT)
			.naturalJoin(MILESTONE_BINDING)//YEAH !!! at last one clean pair of tables on witch we can do a natural join...
			.where(PROJECT.PROJECT_ID.in(readableProjectIds))
			.fetch(MILESTONE_BINDING.MILESTONE_ID, Long.class);
	}

	protected Map<Long, CustomFieldModel> findCufMap(List<Long> usedCufIds) {
		Map<Long, CustomFieldModel> cufMap = new HashMap<>();

		DSL.select(CUSTOM_FIELD.CF_ID, CUSTOM_FIELD.INPUT_TYPE, CUSTOM_FIELD.NAME, CUSTOM_FIELD.LABEL, CUSTOM_FIELD.CODE, CUSTOM_FIELD.OPTIONAL, CUSTOM_FIELD.DEFAULT_VALUE, CUSTOM_FIELD.LARGE_DEFAULT_VALUE
			, CUSTOM_FIELD_OPTION.CODE, CUSTOM_FIELD_OPTION.LABEL, CUSTOM_FIELD_OPTION.POSITION)
			.from(CUSTOM_FIELD)
			.leftJoin(CUSTOM_FIELD_OPTION).using(CUSTOM_FIELD.CF_ID)
			.where(CUSTOM_FIELD.CF_ID.in(usedCufIds))
			.fetch()
			.forEach(r -> {
				Long cufId = r.get(CUSTOM_FIELD.CF_ID);
				String type = r.get(CUSTOM_FIELD.INPUT_TYPE);
				InputType inputType = EnumUtils.getEnum(InputType.class, type);
				switch (inputType) {
					case RICH_TEXT:
						CustomFieldModel richTextCustomFieldModel = getRichTextCustomFieldModel(r);
						cufMap.put(richTextCustomFieldModel.getId(), richTextCustomFieldModel);
						break;
					//here is the not fun case
					//as we have made a left join, we can have the first tuple witch need to be treated as a cuf AND an option
					//or subsequent tuple witch must be treated only as option...
					case DROPDOWN_LIST:
						if (cufMap.containsKey(cufId)) {
							SingleSelectFieldModel singleSelectFieldModel = (SingleSelectFieldModel) cufMap.get(cufId);
							singleSelectFieldModel.addOption(getCufValueOptionModel(r));
						} else {
							SingleSelectFieldModel singleSelectFieldModel = getSingleSelectFieldModel(r);
							singleSelectFieldModel.addOption(getCufValueOptionModel(r));
							cufMap.put(singleSelectFieldModel.getId(), singleSelectFieldModel);
						}
						break;

					case DATE_PICKER:
						CustomFieldModel datePickerCustomFieldModel = getDatePickerCustomFieldModel(r);
						cufMap.put(datePickerCustomFieldModel.getId(), datePickerCustomFieldModel);
						break;

					case TAG:
						if (cufMap.containsKey(cufId)) {
							MultiSelectFieldModel multiSelectFieldModel = (MultiSelectFieldModel) cufMap.get(cufId);
							multiSelectFieldModel.addOption(getCufValueOptionModel(r));
						} else {
							MultiSelectFieldModel multiSelectFieldModel = getMultiSelectFieldModel(r);
							multiSelectFieldModel.addOption(getCufValueOptionModel(r));
							cufMap.put(multiSelectFieldModel.getId(), multiSelectFieldModel);
						}
						break;

					default:
						CustomFieldModel cufModel = getSingleValueCustomFieldModel(r);
						cufMap.put(cufId, cufModel);
				}
			});

		return cufMap;
	}

	private MultiSelectFieldModel getMultiSelectFieldModel(Record r) {
		MultiSelectFieldModel multiSelectFieldModel = new MultiSelectFieldModel();
		initCufModel(r, multiSelectFieldModel);
		for (String value : r.get(CUSTOM_FIELD.DEFAULT_VALUE).split(MultiSelectField.SEPARATOR_EXPR)) {
			multiSelectFieldModel.addDefaultValue(value);
		}
		return multiSelectFieldModel;
	}

	private SingleSelectFieldModel getSingleSelectFieldModel(Record r) {
		SingleSelectFieldModel singleSelectFieldModel = new SingleSelectFieldModel();
		initCufModel(r, singleSelectFieldModel);
		singleSelectFieldModel.setDefaultValue(r.get(CUSTOM_FIELD.DEFAULT_VALUE));
		return singleSelectFieldModel;
	}

	private CustomFieldOptionModel getCufValueOptionModel(Record r) {
		CustomFieldOptionModel optionModel = new CustomFieldOptionModel();
		optionModel.setCode(r.get(CUSTOM_FIELD_OPTION.CODE));
		optionModel.setLabel(r.get(CUSTOM_FIELD_OPTION.LABEL));
		return optionModel;
	}

	private CustomFieldModel getDatePickerCustomFieldModel(Record r) {
		DatePickerFieldModel cufModel = new DatePickerFieldModel();
		initCufModel(r, cufModel);
		Locale locale = LocaleContextHolder.getLocale();
		cufModel.setFormat(getMessage("squashtm.dateformatShort.datepicker"));
		cufModel.setLocale(locale.toString());
		cufModel.setDefaultValue(r.get(CUSTOM_FIELD.DEFAULT_VALUE));
		return cufModel;
	}

	private CustomFieldModel getRichTextCustomFieldModel(Record r) {
		SingleValuedCustomFieldModel cufModel = new SingleValuedCustomFieldModel();
		initCufModel(r, cufModel);
		cufModel.setDefaultValue(r.get(CUSTOM_FIELD.LARGE_DEFAULT_VALUE));
		return cufModel;
	}

	private SingleValuedCustomFieldModel getSingleValueCustomFieldModel(Record r) {//take care if you change the JOOQ request, the result can become incompatible.
		SingleValuedCustomFieldModel cufModel = new SingleValuedCustomFieldModel();
		initCufModel(r, cufModel);
		cufModel.setDefaultValue(r.get(CUSTOM_FIELD.DEFAULT_VALUE));
		return cufModel;
	}

	private void initCufModel(Record r, CustomFieldModel cufModel) {
		cufModel.setId(r.get(CUSTOM_FIELD.CF_ID));
		cufModel.setCode(r.get(CUSTOM_FIELD.CODE));
		cufModel.setName(r.get(CUSTOM_FIELD.NAME));
		cufModel.setLabel(r.get(CUSTOM_FIELD.LABEL));
		cufModel.setOptional(r.get(CUSTOM_FIELD.OPTIONAL));

		cufModel.setDenormalized(false);

		InputTypeModel inputTypeModel = new InputTypeModel();
		String inputTypeKey = r.get(CUSTOM_FIELD.INPUT_TYPE);
		InputType inputType = EnumUtils.getEnum(InputType.class, inputTypeKey);
		inputTypeModel.setEnumName(inputTypeKey);
		inputTypeModel.setFriendlyName(getMessage(inputType.getI18nKey()));

		cufModel.setInputType(inputTypeModel);
	}

	protected List<Long> findUsedCustomFieldIds(List<Long> readableProjectIds) {
		return DSL
			.selectDistinct(CUSTOM_FIELD_BINDING.CF_ID)
			.from(CUSTOM_FIELD_BINDING)
			.where(CUSTOM_FIELD_BINDING.BOUND_PROJECT_ID.in(readableProjectIds))
			.fetch(CUSTOM_FIELD_BINDING.CF_ID, Long.class);
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

	protected Set<Long> findUsedInfoListIds(List<Long> readableProjectIds) {
		Set<Long> ids = new HashSet<>();
		DSL.select(PROJECT.REQ_CATEGORIES_LIST, PROJECT.TC_NATURES_LIST, PROJECT.TC_TYPES_LIST)
			.from(PROJECT)
			.where(PROJECT.PROJECT_ID.in(readableProjectIds))
			.fetch()
			.forEach(r -> {
				ids.add(r.get(PROJECT.REQ_CATEGORIES_LIST));
				ids.add(r.get(PROJECT.TC_NATURES_LIST));
				ids.add(r.get(PROJECT.TC_TYPES_LIST));
			});

		return ids;
	}

	protected Map<Long, JsonInfoList> findInfoListMap(Set<Long> usedInfoListIds) {
		return DSL.select(INFO_LIST.INFO_LIST_ID, INFO_LIST.CODE, INFO_LIST.LABEL, INFO_LIST.DESCRIPTION
			, INFO_LIST_ITEM.ITEM_ID, INFO_LIST_ITEM.CODE, INFO_LIST_ITEM.LABEL, INFO_LIST_ITEM.ICON_NAME, INFO_LIST_ITEM.IS_DEFAULT, INFO_LIST_ITEM.ITEM_TYPE)
			.from(INFO_LIST)
			.innerJoin(INFO_LIST_ITEM).on(INFO_LIST.INFO_LIST_ID.eq(INFO_LIST_ITEM.LIST_ID))
			.where(INFO_LIST.INFO_LIST_ID.in(usedInfoListIds))
			.fetch()
			.stream()
			.collect(groupingBy(//groupingBy as map <JsonInfoList,List<InfoListItems>>
				r -> {
					Long id = r.get(INFO_LIST.INFO_LIST_ID);
					String code = r.get(INFO_LIST.CODE);
					String label = r.get(INFO_LIST.LABEL);
					String description = r.get(INFO_LIST.DESCRIPTION);
					return new JsonInfoList(id, "todo", code, label, description);
				},
				mapping(
					r -> {
						JsonInfoListItem jsonInfoListItem = new JsonInfoListItem();
						jsonInfoListItem.setId(r.get(INFO_LIST_ITEM.ITEM_ID));
						jsonInfoListItem.setCode(r.get(INFO_LIST_ITEM.CODE));
						jsonInfoListItem.setLabel(r.get(INFO_LIST_ITEM.LABEL));
						jsonInfoListItem.setIconName(r.get(INFO_LIST_ITEM.ICON_NAME));
						jsonInfoListItem.setDefault(r.get(INFO_LIST_ITEM.IS_DEFAULT));
						jsonInfoListItem.setSystem(r.get(INFO_LIST_ITEM.ITEM_TYPE).equals(SYSTEM_INFO_LIST_IDENTIFIER));
						jsonInfoListItem.setDenormalized(false);
						jsonInfoListItem.setFriendlyLabel(messageSource.getMessage(r.get(INFO_LIST_ITEM.LABEL), null, r.get(INFO_LIST_ITEM.LABEL), LocaleContextHolder.getLocale()));
						jsonInfoListItem.setUri("todo");
						return jsonInfoListItem;
					},
					toList()
				)
			))
			.entrySet().stream()
			.map(jsonInfoListListEntry -> {//now we assign each List of items directly inside their infolist as required per object structure
				JsonInfoList infoList = jsonInfoListListEntry.getKey();
				infoList.setItems(jsonInfoListListEntry.getValue());
				return infoList;
			})
			.collect(Collectors.toMap(JsonInfoList::getId, Function.identity()));//now put the fully hydrated infolist in a map <id,infolist>
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
