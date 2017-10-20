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

import org.apache.commons.collections.MultiMap;
import org.jooq.Field;
import org.jooq.TableLike;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.requirement.RequirementLibraryPluginBinding;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;
import org.squashtest.tm.service.internal.workspace.AbstractWorkspaceDisplayService;

import java.util.Map;

import static org.squashtest.tm.jooq.domain.Tables.*;

@Service("requirementWorkspaceDisplayService")
@Transactional(readOnly = true)
public class RequirementWorkspaceDisplayService extends AbstractWorkspaceDisplayService {

//	@Inject
//	DSLContext DSL;
//
//	private RequirementLibraryContent RLC = REQUIREMENT_LIBRARY_CONTENT.as("RLC");
//	private RequirementLibraryNode RLN = REQUIREMENT_LIBRARY_NODE.as("RLN");
//	private RequirementLibraryNode RLN_CHILD = REQUIREMENT_LIBRARY_NODE.as("RLN_CHILD");
//	private RequirementFolder RCF = REQUIREMENT_FOLDER.as("RCF");
//	private Requirement REQ = REQUIREMENT.as("REQ");
//	private RequirementVersionCoverage RVC = REQUIREMENT_VERSION_COVERAGE.as("RVC");
//	private RlnRelationship RLNR = RLN_RELATIONSHIP.as("RLNR");
//	private Resource RES = RESOURCE.as("RES");

	@Override
	protected Field<Long> getProjectLibraryColumn() {
		return PROJECT.RL_ID;
	}

	@Override
	protected String getFolderName() {
		return null;
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
		return null;
	}

	@Override
	protected Field<Long> selectLNRelationshipDescendantId() {
		return null;
	}

	@Override
	protected Field<Integer> selectLNRelationshipContentOrder() {
		return null;
	}

	@Override
	protected TableLike<?> getLNRelationshipTable() {
		return null;
	}

	@Override
	protected Map<Long, JsTreeNode> getChildren(MultiMap fatherChildrenLibrary, MultiMap fatherChildrenEntity) {
		return null;
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
