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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MultiMap;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.TableLike;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.requirement.RequirementLibraryPluginBinding;
import org.squashtest.tm.jooq.domain.tables.*;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;
import org.squashtest.tm.service.internal.workspace.AbstractWorkspaceDisplayService;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.squashtest.tm.jooq.domain.Tables.*;

@Service("requirementWorkspaceDisplayService")
@Transactional(readOnly = true)
public class RequirementWorkspaceDisplayService extends AbstractWorkspaceDisplayService {

	@Inject
	DSLContext DSL;

	private RequirementLibraryContent RLC = REQUIREMENT_LIBRARY_CONTENT.as("RLC");
	private RequirementLibraryNode RLN = REQUIREMENT_LIBRARY_NODE.as("RLN");
	private RequirementLibraryNode RLN_CHILD = REQUIREMENT_LIBRARY_NODE.as("RLN_CHILD");
	private RequirementFolder RF = REQUIREMENT_FOLDER.as("RF");
	private Requirement REQ = REQUIREMENT.as("REQ");
	private RequirementVersion RV = REQUIREMENT_VERSION.as("RV");
	private RequirementVersionCoverage RVC = REQUIREMENT_VERSION_COVERAGE.as("RVC");
	private RlnRelationship RLNR = RLN_RELATIONSHIP.as("RLNR");
	private Resource RES = RESOURCE.as("RES");
	MilestoneReqVersion MRV = MILESTONE_REQ_VERSION.as("MRV");


	@Override
	protected Map<Long, JsTreeNode> getChildren(MultiMap fatherChildrenLibrary, MultiMap fatherChildrenEntity) {
		Set<Long> childrenIds = new HashSet<>();
		childrenIds.addAll(fatherChildrenLibrary.values());
		childrenIds.addAll(fatherChildrenEntity.keySet());
		childrenIds.addAll(fatherChildrenEntity.values());
		return DSL
			.select(
				RLN.RLN_ID,
				org.jooq.impl.DSL.decode()
					.when(RF.RLN_ID.isNotNull(), "requirement-folders")
					.otherwise("requirements").as("RESTYPE"),
				RES.NAME,
				REQ.MODE,
				RV.REFERENCE,
				MRV.MILESTONE_ID,
//				org.jooq.impl.DSL.decode()
//					.when(RVC.VERIFYING_TEST_CASE_ID.isNull(), "false")
//					.otherwise("true")
//					.as("IS_REQ_COVERED"),
				org.jooq.impl.DSL.decode()
					.when(RLNR.ANCESTOR_ID.isNull(), "false")
					.otherwise("true")
					.as("HAS_CONTENT")
			)
			.from(RLN)
			.leftJoin(RF).on(RLN.RLN_ID.eq(RF.RLN_ID))
			.leftJoin(REQ).on(RLN.RLN_ID.eq(REQ.RLN_ID))
			.leftJoin(RES).on(RF.RES_ID.eq(RES.RES_ID).or(REQ.CURRENT_VERSION_ID.eq(RES.RES_ID)))
			.leftJoin(RV).on(RLN.RLN_ID.eq(RV.REQUIREMENT_ID))
			.leftJoin(MRV).on(REQ.CURRENT_VERSION_ID.eq(MRV.REQ_VERSION_ID))
			.leftJoin(RLNR).on(RLN.RLN_ID.eq(RLNR.ANCESTOR_ID))
			.where(RLN.RLN_ID.in(childrenIds))
			.groupBy(RLN.RLN_ID)
			.fetch()
			.stream()
			.map(r -> {
				if (r.get("RESTYPE").equals("requirement-folders")) {
					return buildFolder(r.get(RLN.RLN_ID), r.get(RES.NAME), (String) r.get("RESTYPE"), (String) r.get("HAS_CONTENT"));
				} else {
					return buildRequirement(r.get(RLN.RLN_ID), r.get(RES.NAME), (String) r.get("RESTYPE"), r.get(RV.REFERENCE),
						r.get(REQ.MODE), r.get(MRV.MILESTONE_ID));
				}
			})
			.collect(Collectors.toMap(node -> (Long) node.getAttr().get("resId"), Function.identity()));
	}

	private JsTreeNode buildRequirement(Long id, String name, String restype, String reference, String mode, Long milestone) {
		Map<String, Object> attr = new HashMap<>();

		attr.put("resId", id);
		attr.put("resType", restype);
		attr.put("name", name);
		attr.put("id", "TestCase-" + id);
		attr.put("rel", "test-case");

		attr.put("reference", reference);
		attr.put("mode", mode);
		attr.put("milestone", milestone);

		return buildNode(name, JsTreeNode.State.leaf, attr);
	}

	// *************************************** send stuff to abstract workspace ***************************************

	@Override
	protected Field<Long> getProjectLibraryColumn() {
		return PROJECT.RL_ID;
	}

	@Override
	protected String getFolderName() {
		return "RequirementFolder-";
	}

//	@Override
//	protected Map<Long, JsTreeNode> doFindLibraries(List<Long> readableProjectIds, UserDto currentUser, List<Long> openedLibraryIds, Map<Long, JsTreeNode> expandedJsTreeNodes, JsonMilestone activeMilestone) {
//		List<Long> filteredProjectIds;
//		if (hasActiveFilter(currentUser.getUsername())) {
//			filteredProjectIds = findFilteredProjectIds(readableProjectIds, currentUser.getUsername());
//		} else {
//			filteredProjectIds = readableProjectIds;
//		}
//
//		Table<?> childrenInfo = DSL
//			.select(selectLibraryId(),
//				org.jooq.impl.DSL.groupConcat(org.jooq.impl.DSL.concat(RLC.CONTENT_ID.cast(String.class), "=Sep="))
//					.orderBy(RLC.CONTENT_ORDER).as("CHILDREN_ID"),
//				org.jooq.impl.DSL.groupConcat(org.jooq.impl.DSL.concat(org.jooq.impl.DSL.decode()
//					.when(RCF.RLN_ID.isNotNull(), "requirement-folders")
//					.otherwise("requirements"), "=Sep="))
//					.orderBy(RLC.CONTENT_ORDER).as("CHILDREN_CLASS")
////				org.jooq.impl.DSL.groupConcat(org.jooq.impl.DSL.concat(RES.NAME, "=Sep="))
////					.orderBy(RLC.CONTENT_ORDER).as("CHILDREN_NAME"),
////				org.jooq.impl.DSL.groupConcat(org.jooq.impl.DSL.concat(org.jooq.impl.DSL.decode()
////					.when(groupedTCLNR.field("ANCESTOR_ID").isNull(), "false")
////					.otherwise("true"), "=Sep="))
////					.orderBy(TCLC.CONTENT_ORDER).as("CHILDREN_HAS_CONTENT")
//			)
//			.from(getLibraryTable())
//			.join(PROJECT).using(selectLibraryId())
//			.leftJoin(RLC).on(selectLibraryId().eq(RLC.LIBRARY_ID))
// 			.leftJoin(RLN).on(RLN.RLN_ID.eq(RLC.CONTENT_ID))
//			.leftJoin(RCF).on(RCF.RLN_ID.eq(RLC.CONTENT_ID))
////			.leftJoin(TC).on(TCLC.CONTENT_ID.eq(TC.TCLN_ID))
////			.leftJoin(groupedTestCaseStep).on(TC.TCLN_ID.eq(groupedTestCaseStep.field("TEST_CASE_ID", Long.class)))
////			.leftJoin(RVC).on(TCLC.CONTENT_ID.eq(RVC.VERIFYING_TEST_CASE_ID))
////			.leftJoin(groupedTCLNR).on(TCLC.CONTENT_ID.eq(groupedTCLNR.field("ANCESTOR_ID", Long.class)))
//			.where(PROJECT.PROJECT_ID.in(openedLibraryIds))
//			.and(PROJECT.PROJECT_TYPE.eq(PROJECT_TYPE))
//			.groupBy(selectLibraryId())
//			.asTable("CHILDREN_INFO");
//
//		Map<Long, JsTreeNode> jsTreeNodes = DSL
//			.select(selectLibraryId(),
//				PROJECT.PROJECT_ID,
//				PROJECT.NAME,
//				PROJECT.LABEL,
//				childrenInfo.field("CHILDREN_ID"),
//				childrenInfo.field("CHILDREN_CLASS")
//			)
//			.from(getLibraryTable())
//			.join(PROJECT).using(selectLibraryId())
//			.leftJoin(childrenInfo).using(selectLibraryId())
//			.where(PROJECT.PROJECT_ID.in(filteredProjectIds))
//			.and(PROJECT.PROJECT_TYPE.eq(PROJECT_TYPE))
//			.fetch()
//			.stream()
//			.map(r -> {
//				JsTreeNode node = new JsTreeNode();
//				Long libraryId = r.get(selectLibraryId(), Long.class);
//				node.addAttr("resId", libraryId);
//				node.setTitle(r.get(PROJECT.NAME));
//				node.addAttr("resType", getResType());
//				node.addAttr("rel", getRel());
//				node.addAttr("name", getClassName());
//				node.addAttr("id", getClassName() + '-' + libraryId);
//				node.addAttr("title", r.get(PROJECT.LABEL));
//				node.addAttr("project", r.get(PROJECT.PROJECT_ID));
//
//				//permissions set to false by default except for admin witch have rights by definition
//				EnumSet<PermissionWithMask> permissions = EnumSet.allOf(PermissionWithMask.class);
//				for (PermissionWithMask permission : permissions) {
//					node.addAttr(permission.getQuality(), String.valueOf(currentUser.isAdmin()));
//				}
//
//				// milestone attributes : libraries are yes-men
//				node.addAttr("milestone-creatable-deletable", "true");
//				node.addAttr("milestone-editable", "true");
//				node.addAttr("wizards", new HashSet<String>());
//				node.setState(JsTreeNode.State.closed);
//				return node;
//			})
//			.collect(Collectors.toMap(node -> (Long) node.getAttr().get("resId"), Function.identity()));
//
//		//TODO opened nodes and content
//		return jsTreeNodes;
//	}


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
	protected TableLike<?> getLibraryTableContent() {
		return REQUIREMENT_LIBRARY_CONTENT;
	}

	@Override
	protected Field<Long> selectLNRelationshipAncestorId() {
		return RLN_RELATIONSHIP.ANCESTOR_ID;
	}

	@Override
	protected Field<Long> selectLNRelationshipDescendantId() {
		return RLN_RELATIONSHIP.DESCENDANT_ID;
	}

	@Override
	protected Field<Integer> selectLNRelationshipContentOrder() {
		return RLN_RELATIONSHIP.CONTENT_ORDER;
	}

	@Override
	protected TableLike<?> getLNRelationshipTable() {
		return RLN_RELATIONSHIP;
	}

	@Override
	protected List<Long> getOpenedEntityIds(MultiMap expansionCandidates) {
		List<Long> openedEntityIds = new ArrayList<>();
		List<Long> folderId = (List<Long>) expansionCandidates.get("RequirementFolder");
		List<Long> requirementId = (List<Long>) expansionCandidates.get("Requirement");

		if (!CollectionUtils.isEmpty(folderId)) {
			openedEntityIds.addAll(folderId);
		}
		if (!CollectionUtils.isEmpty(requirementId)) {
			openedEntityIds.addAll(requirementId);
		}

		return openedEntityIds;
	}

	@Override
	protected Field<Long> getMilestoneLibraryNodeId() {
		return MILESTONE_REQ_VERSION.REQ_VERSION_ID;
	}

	@Override
	protected TableLike<?> getMilestoneLibraryNodeTable() {
		return MILESTONE_REQ_VERSION;
	}

	@Override
	protected Field<Long> getMilestoneId() {
		return MILESTONE_REQ_VERSION.MILESTONE_ID;
	}

	@Override
	protected Field<Long> selectLibraryContentContentId() {
		return REQUIREMENT_LIBRARY_CONTENT.CONTENT_ID;
	}

	@Override
	protected Field<Integer> selectLibraryContentOrder() {
		return REQUIREMENT_LIBRARY_CONTENT.CONTENT_ORDER;
	}

	@Override
	protected Field<Long> selectLibraryContentLibraryId() {
		return REQUIREMENT_LIBRARY_CONTENT.LIBRARY_ID;
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
