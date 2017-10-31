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
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.TableLike;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.requirement.RequirementLibraryPluginBinding;
import org.squashtest.tm.jooq.domain.tables.*;
import org.squashtest.tm.service.internal.dto.UserDto;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode.State;
import org.squashtest.tm.service.internal.repository.hibernate.HibernateEntityDao;
import org.squashtest.tm.service.internal.repository.hibernate.HibernateRequirementFolderDao;
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

	@Inject
	HibernateRequirementFolderDao hibernateRequirementFolderDao ;

	private RequirementLibraryNode RLN = REQUIREMENT_LIBRARY_NODE.as("RLN");
	private RequirementFolder RF = REQUIREMENT_FOLDER.as("RF");
	private Requirement REQ = REQUIREMENT.as("REQ");
	private RequirementVersion RV = REQUIREMENT_VERSION.as("RV");
	private RlnRelationship RLNR = RLN_RELATIONSHIP.as("RLNR");
	private Resource RES = RESOURCE.as("RES");
	private MilestoneReqVersion MRV = MILESTONE_REQ_VERSION.as("MRV");
	private InfoListItem ILI = INFO_LIST_ITEM.as("ILI");

	@Override
	protected Map<Long, JsTreeNode> getLibraryChildrenMap(Set<Long> childrenIds, MultiMap expansionCandidates, UserDto currentUser, Map<Long, List<Long>> allMilestonesForReqs) {

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
				org.jooq.impl.DSL.decode()
					.when(RLNR.ANCESTOR_ID.isNull(), "false")
					.otherwise("true")
					.as("HAS_CONTENT"),
				org.jooq.impl.DSL.decode()
					.when(ILI.ICON_NAME.eq(""), "def_cat_noicon")
					.otherwise(ILI.ICON_NAME).as("ICON_NAME")
			)
			.from(RLN)
			.leftJoin(RF).on(RLN.RLN_ID.eq(RF.RLN_ID))
			.leftJoin(REQ).on(RLN.RLN_ID.eq(REQ.RLN_ID))
			.leftJoin(RES).on(RF.RES_ID.eq(RES.RES_ID).or(REQ.CURRENT_VERSION_ID.eq(RES.RES_ID)))
			.leftJoin(RV).on(RLN.RLN_ID.eq(RV.REQUIREMENT_ID))
			.leftJoin(MRV).on(REQ.CURRENT_VERSION_ID.eq(MRV.REQ_VERSION_ID))
			.leftJoin(RLNR).on(RLN.RLN_ID.eq(RLNR.ANCESTOR_ID))
			.leftJoin(ILI).on(RV.CATEGORY.eq(ILI.ITEM_ID.cast(Long.class)))
			.where(RLN.RLN_ID.in(childrenIds))
			.groupBy(RLN.RLN_ID)
			.fetch()
			.stream()
			.map(r -> {
				if (r.get("RESTYPE").equals("requirement-folders")) {
					return buildFolder(r.get(RLN.RLN_ID), r.get(RES.NAME), (String) r.get("RESTYPE"), (String) r.get("HAS_CONTENT"), currentUser);
				} else {
					Integer milestonesNumber = getMilestonesNumberForReq(allMilestonesForReqs, r.get(RLN.RLN_ID));
					return buildRequirement(r.get(RLN.RLN_ID), r.get(RES.NAME), (String) r.get("RESTYPE"), r.get(RV.REFERENCE),
						r.get(REQ.MODE), r.get(MRV.MILESTONE_ID), (String) r.get("ICON_NAME"), (String) r.get("HAS_CONTENT"), currentUser, milestonesNumber);
				}
			})
			.collect(Collectors.toMap(node -> (Long) node.getAttr().get("resId"), Function.identity()));
	}

	private JsTreeNode buildRequirement(Long id, String name, String restype, String reference, String mode, Long milestone, String categoryIcon, String hasContent, UserDto currentUser, Integer milestonesNumber) {
		Map<String, Object> attr = new HashMap<>();
		State state;
		attr.put("resId", id);
		attr.put("resType", restype);
		attr.put("name", name);
		attr.put("id", "Requirement-" + id);
		attr.put("rel", "requirement");
		if (mode.equals("SYNCHRONIZED"))
			attr.put("synchronized", true);
		/*attr.put("milestone", milestone);*/
		attr.put("category-icon", categoryIcon);

		if (Boolean.parseBoolean(hasContent)) {
			state = JsTreeNode.State.closed;
		} else {
			state = State.leaf;
		}

		String title = name;
		if (!StringUtils.isEmpty(reference)) {
			title = reference + " - " + title;
			attr.put("reference", reference);
		}

		return buildNode(title, state, attr, currentUser, milestonesNumber);
	}

	private Integer getMilestonesNumberForReq(Map<Long, List<Long>> allMilestonesForReqs, Long id) {
		return (allMilestonesForReqs.get(id) != null) ? allMilestonesForReqs.get(id).size() : NODE_WITHOUT_MILESTONE;
	}

	public List<Long> findReqsWithChildrenLinkedToMilestone(List<Long> reqVersionIdsWithMilestone) {
		List<Long> reqIdsWithMilestone = DSL.select().from(REQUIREMENT_VERSION)
			.where(REQUIREMENT_VERSION.RES_ID.in(reqVersionIdsWithMilestone))
			.fetch(REQUIREMENT_VERSION.REQUIREMENT_ID, Long.class);

		return DSL.selectDistinct()
			.from(RLN_RELATIONSHIP_CLOSURE)
			.where(RLN_RELATIONSHIP_CLOSURE.DESCENDANT_ID.in(reqIdsWithMilestone)
				.and(RLN_RELATIONSHIP_CLOSURE.ANCESTOR_ID.notIn(DSL.select(REQUIREMENT_FOLDER.RLN_ID).from(REQUIREMENT_FOLDER)))
				.and(RLN_RELATIONSHIP_CLOSURE.ANCESTOR_ID.notIn(reqIdsWithMilestone)))
			.fetch(RLN_RELATIONSHIP_CLOSURE.ANCESTOR_ID, Long.class);
	}

	// *************************************** send stuff to abstract workspace ***************************************

	@Override
	protected Field<Long> getProjectLibraryColumn() {
		return PROJECT.RL_ID;
	}

	@Override
	protected String getFolderName() {
		return "RequirementFolder";
	}

	@Override
	protected String getNodeName() {
		return "Requirement";
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
	protected Map<Long, List<Long>> findAllMilestonesForLN() {
		return DSL.select()
			.from(MILESTONE_REQ_VERSION)
			.join(REQUIREMENT_VERSION).on(MILESTONE_REQ_VERSION.REQ_VERSION_ID.eq(REQUIREMENT_VERSION.RES_ID))
			.join(REQUIREMENT).on(REQUIREMENT_VERSION.REQUIREMENT_ID.eq(REQUIREMENT.RLN_ID))
			.fetchGroups(REQUIREMENT_VERSION.REQUIREMENT_ID, MILESTONE_REQ_VERSION.MILESTONE_ID);
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
	protected HibernateEntityDao hibernateFolderDao() {
		return hibernateRequirementFolderDao;
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

	@Override
	public Collection<JsTreeNode> getCampaignNodeContent(Long folderId, UserDto currentUser, String libraryNode) {
		return null;
	}
}
