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
package org.squashtest.tm.service.internal.campaign;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.TableField;
import org.jooq.TableLike;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.campaign.CampaignLibraryPluginBinding;
import org.squashtest.tm.jooq.domain.tables.*;
import org.squashtest.tm.service.internal.dto.UserDto;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode.State;
import org.squashtest.tm.service.internal.repository.hibernate.HibernateCampaignDao;
import org.squashtest.tm.service.internal.repository.hibernate.HibernateCampaignFolderDao;
import org.squashtest.tm.service.internal.repository.hibernate.HibernateEntityDao;
import org.squashtest.tm.service.internal.repository.hibernate.HibernateIterationDao;
import org.squashtest.tm.service.internal.workspace.AbstractWorkspaceDisplayService;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.squashtest.tm.jooq.domain.Tables.*;

@Service("campaignWorkspaceDisplayService")
@Transactional(readOnly = true)
public class CampaignWorkspaceDisplayService extends AbstractWorkspaceDisplayService {

	@Inject
	DSLContext DSL;

	@Inject
	HibernateCampaignFolderDao hibernateCampaignFolderDao;

	@Inject
	HibernateCampaignDao hibernateCampaignDao;

	@Inject
	HibernateIterationDao hibernateIterationDao;

	private Campaign C = CAMPAIGN.as("C");
	private CampaignLibraryNode CLN = CAMPAIGN_LIBRARY_NODE.as("CLN");
	private CampaignFolder CF = CAMPAIGN_FOLDER.as("CF");
	private ClnRelationship CLNR = CLN_RELATIONSHIP.as("CLNR");
	private CampaignIteration CI = CAMPAIGN_ITERATION.as("CI");
	private MilestoneCampaign MC = MILESTONE_CAMPAIGN.as("MC");
	private Milestone M = MILESTONE.as("M");
	private IterationTestSuite ITS = ITERATION_TEST_SUITE.as("ITS");
	private Iteration IT = ITERATION.as("IT");
	private TestSuite TS = TEST_SUITE.as("TS");
	private TestSuiteTestPlanItem TSTPI = TEST_SUITE_TEST_PLAN_ITEM.as("TSTPI");
	private IterationTestPlanItem ITPI = ITERATION_TEST_PLAN_ITEM.as("ITPI");
	private TestCaseLibraryNode TCLN = TEST_CASE_LIBRARY_NODE.as("TCLN");

	private MultiMap campaignFatherChildrenMultimap = new MultiValueMap();
	private MultiMap iterationFatherChildrenMultiMap = new MultiValueMap();
	private Map<Long, JsTreeNode> iterationMap = new HashMap<>();
	private Map<Long, JsTreeNode> testSuiteMap = new HashMap<>();

	private static final String MILESTONE_STATUS_IN_PROGRESS = "IN_PROGRESS";
	private static final String MILESTONE_STATUS_FINISHED = "FINISHED";

	@Override
	protected Map<Long, JsTreeNode> getLibraryChildrenMap(Set<Long> childrenIds, MultiMap expansionCandidates, UserDto currentUser, Map<Long, List<Long>> allMilestonesForLN) {

		getCampaignHierarchy(currentUser, expansionCandidates);

		return DSL
			.select(
				CLN.CLN_ID,
				org.jooq.impl.DSL.decode()
					.when(CF.CLN_ID.isNotNull(), "campaign-folders")
					.otherwise("campaigns").as("RESTYPE"),
				CLN.NAME,
				C.REFERENCE,
				org.jooq.impl.DSL.decode()
					.when(CLNR.ANCESTOR_ID.isNotNull().or(CI.CAMPAIGN_ID.isNotNull()), "true")
					.otherwise("false")
					.as("HAS_CONTENT"),
				MC.MILESTONE_ID,
				org.jooq.impl.DSL.decode()
					.when(M.STATUS.isNull().or(M.STATUS.eq(MILESTONE_STATUS_IN_PROGRESS)).or(M.STATUS.eq(MILESTONE_STATUS_FINISHED)), "true")
					.otherwise("false")
					.as("IS_MILESTONE_MODIFIABLE")
			)
			.from(CLN)
			.leftJoin(CF).on(CLN.CLN_ID.eq(CF.CLN_ID))
			.leftJoin(CLNR).on(CLN.CLN_ID.eq(CLNR.ANCESTOR_ID))
			.leftJoin(CI).on(CLN.CLN_ID.eq(CI.CAMPAIGN_ID))
			.leftJoin(C).on(CLN.CLN_ID.eq(C.CLN_ID))
			.leftJoin(MC).on(CLN.CLN_ID.eq(MC.CAMPAIGN_ID))
			.leftJoin(M).on(MC.MILESTONE_ID.eq(M.MILESTONE_ID))
			.where(CLN.CLN_ID.in(childrenIds))
			.groupBy(CLN.CLN_ID, CF.CLN_ID, CLN.NAME, C.REFERENCE, MC.MILESTONE_ID, M.STATUS)
			.fetch()
			.stream()
			.map(r -> {
				if (r.get("RESTYPE").equals("campaign-folders")) {
					return buildFolder(r.get(CLN.CLN_ID), r.get(CLN.NAME), (String) r.get("RESTYPE"), (String) r.get("HAS_CONTENT"), currentUser);
				} else {
					return buildCampaign(r.get(CLN.CLN_ID), r.get(CLN.NAME), (String) r.get("RESTYPE"), r.get(C.REFERENCE), (String) r.get("HAS_CONTENT"), currentUser, r.get(MC.MILESTONE_ID), (String) r.get("IS_MILESTONE_MODIFIABLE"));
				}
			})
			.collect(Collectors.toMap(node -> (Long) node.getAttr().get("resId"), Function.identity()));
	}

	public Collection<JsTreeNode> getCampaignNodeContent(Long entityId, UserDto currentUser, String entityClass) {
		Set<Long> childrenIds = new HashSet<>();
		MultiMap expansionCandidates = new MultiValueMap();
		expansionCandidates.put(entityClass, entityId);
		Long libraryId;

		MultiMap entityFatherChildrenMultimap = getFatherChildrenLibraryNode(entityClass, expansionCandidates);
		childrenIds.remove(entityId);

		Map<Long, JsTreeNode> libraryChildrenMap =
			(entityClass.equals("Campaign") ? getCampaignChildren(entityFatherChildrenMultimap, currentUser) : getIterationChildren(entityFatherChildrenMultimap, currentUser));

		libraryId = entityClass.equals("Campaign") ? hibernateCampaignDao.findById(entityId).getLibrary().getId() : hibernateIterationDao.findById(entityId).getCampaignLibrary().getId();

		if (currentUser.isNotAdmin()) {
			findNodeChildrenPermissionMap(currentUser, libraryChildrenMap, libraryId);
		}

		return libraryChildrenMap.values();
	}

	// ********************************************** Utils ************************************************************

	private JsTreeNode buildCampaign(Long campaignId, String name, String restype, String reference, String hasContent, UserDto currentUser, Long milestone, String isMilestoneModifiable) {
		Map<String, Object> attr = new HashMap<>();

		attr.put("resId", campaignId);
		attr.put("resType", restype);
		attr.put("name", name);
		attr.put("id", "Campaign-" + campaignId);
		attr.put("rel", "campaign");

		String title = name;
		if (!StringUtils.isEmpty(reference)) {
			attr.put("reference", reference);
			title = reference + " - " + title;
		}
		Integer milestonesNumber = getMilestoneNumber(milestone);

		JsTreeNode campaign = buildNode(title, null, attr, currentUser, milestonesNumber, isMilestoneModifiable);

		// Messy but still simpler than GOT's genealogy
		if (!Boolean.parseBoolean(hasContent)) {
			campaign.setState(State.leaf);
		} else if (campaignFatherChildrenMultimap.containsKey(campaignId)) {
			campaign.setState(State.open);
			for (Long iterationId : (ArrayList<Long>) campaignFatherChildrenMultimap.get(campaignId)) {
				if (iterationFatherChildrenMultiMap.containsKey(iterationId)) {
					iterationMap.get(iterationId).setState(State.open);
					for (Long testSuiteId : (ArrayList<Long>) iterationFatherChildrenMultiMap.get(iterationId)) {
						iterationMap.get(iterationId).addChild(testSuiteMap.get(testSuiteId));
					}
				}
				campaign.addChild(iterationMap.get(iterationId));
			}
		} else {
			campaign.setState(State.closed);
		}
		return campaign;
	}

	private JsTreeNode buildIteration(Long id, String name, String reference, Integer iterationOrder, String hasContent, UserDto currentUser, Long milestone, String isMilestoneModifiable) {
		Map<String, Object> attr = new HashMap<>();
		JsTreeNode.State state;

		attr.put("resId", id);
		attr.put("resType", "iterations");
		attr.put("name", name);
		attr.put("id", "Iteration-" + id);
		attr.put("rel", "iteration");
		attr.put("iterationIndex", String.valueOf(iterationOrder + 1));
		if (Boolean.parseBoolean(hasContent)) {
			state = State.closed;
		} else {
			state = State.leaf;
		}

		String title = name;
		if (!StringUtils.isEmpty(reference)) {
			title = reference + " - " + title;
			attr.put("reference", reference);
		}
		Integer milestonesNumber = getMilestoneNumber(milestone);

		return buildNode(title, state, attr, currentUser, milestonesNumber, isMilestoneModifiable);
	}

	private JsTreeNode buildTestSuite(Long id, String name, String executionStatus, String description, UserDto currentUser, Long milestone, String isMilestoneModifiable) {
		Map<String, Object> attr = new HashMap<>();

		attr.put("resId", id);
		attr.put("name", name);
		attr.put("id", "TestSuite-" + id);
		attr.put("executionstatus", executionStatus);
		attr.put("resType", "test-suites");
		attr.put("rel", "test-suite");
		//build tooltip
		String[] args = {getMessage("execution.execution-status." + executionStatus)};
		String tooltip = getMessage("label.tree.testSuite.tooltip", args);
		attr.put("title", tooltip + "\n" + removeHtml(description));
		Integer milestonesNumber = getMilestoneNumber(milestone);
		return buildNode(name, State.leaf, attr, currentUser, milestonesNumber, isMilestoneModifiable);

	}

	//Campaigns got iterations and test suites which aren't located in the campaign_library_node table.
	// We must fetch them separately, because they might have identical ids
	private void getCampaignHierarchy(UserDto currentUser, MultiMap expansionCandidates) {
		//first: iterations, get father-children relation, fetch them and add them to the campaigns
		campaignFatherChildrenMultimap = getFatherChildrenLibraryNode("Campaign", expansionCandidates);
		iterationMap = getCampaignChildren(campaignFatherChildrenMultimap, currentUser);
		//second test suites, get father-children relation, fetch them and add them  to  the iterations
		iterationFatherChildrenMultiMap = getFatherChildrenLibraryNode("Iteration", expansionCandidates);
		testSuiteMap = getIterationChildren(iterationFatherChildrenMultiMap, currentUser);

	}

	private MultiMap getFatherChildrenLibraryNode(String resType, MultiMap expansionCandidates) {
		MultiMap result = new MultiValueMap();

		TableField<?, ?> fatherColumn;
		TableField<?, ?> childColumn;
		TableField<?, ?> orderColumn;
		TableLike<?> table;

		if (resType.equals("Campaign")) {
			table = CI;
			fatherColumn = CI.CAMPAIGN_ID;
			childColumn = CI.ITERATION_ID;
			orderColumn = CI.ITERATION_ORDER;
		} else {
			table = ITS;
			fatherColumn = ITS.ITERATION_ID;
			childColumn = ITS.TEST_SUITE_ID;
			orderColumn = ITS.TEST_SUITE_ID;

		}
		List<Long> openedEntityIds = (List<Long>) expansionCandidates.get(resType);
		if (!CollectionUtils.isEmpty(openedEntityIds)) {
			DSL
				.select(
					fatherColumn,
					childColumn
				)
				.from(table)
				.where(fatherColumn.in(openedEntityIds))
				.orderBy(orderColumn)
				.fetch()
				.stream()
				.forEach(r ->
						result.put(r.get(fatherColumn), r.get(childColumn))
				);
		}
		return result;
	}

	private Map<Long, JsTreeNode> getCampaignChildren(MultiMap fatherChildrenEntity, UserDto currentUser) {
		return DSL
			.select(
				IT.ITERATION_ID,
				IT.NAME,
				IT.REFERENCE,
				CI.ITERATION_ORDER,
				org.jooq.impl.DSL.decode()
					.when(ITS.ITERATION_ID.isNull(), "false")
					.otherwise("true")
					.as("HAS_CONTENT"),
				MC.MILESTONE_ID,
				org.jooq.impl.DSL.decode()
					.when(M.STATUS.isNull().or(M.STATUS.eq(MILESTONE_STATUS_IN_PROGRESS)).or(M.STATUS.eq(MILESTONE_STATUS_FINISHED)), "true")
					.otherwise("false")
					.as("IS_MILESTONE_MODIFIABLE")
			)
			.from(IT)
			.leftJoin(CI).on(IT.ITERATION_ID.eq(CI.ITERATION_ID))
			.leftJoin(MC).on(CI.CAMPAIGN_ID.eq(MC.CAMPAIGN_ID))
			.leftJoin(M).on(MC.MILESTONE_ID.eq(M.MILESTONE_ID))
			.leftJoin(ITS).on(IT.ITERATION_ID.eq(ITS.ITERATION_ID))
			.where(IT.ITERATION_ID.in(fatherChildrenEntity.values()))
			.groupBy(IT.ITERATION_ID, CI.ITERATION_ORDER, ITS.ITERATION_ID, MC.MILESTONE_ID, M.STATUS)
			.fetch()
			.stream()
			.map(r -> buildIteration(r.get(IT.ITERATION_ID), r.get(IT.NAME), r.get(IT.REFERENCE),
				r.get(CI.ITERATION_ORDER), (String) r.get("HAS_CONTENT"), currentUser, r.get(MC.MILESTONE_ID), (String) r.get("IS_MILESTONE_MODIFIABLE")))
			.collect(Collectors.toMap(node -> (Long) node.getAttr().get("resId"), Function.identity()));
	}

	private Map<Long, JsTreeNode> getIterationChildren(MultiMap fatherChildrenEntity, UserDto currentUser) {
		return DSL
			.select(
				TS.ID,
				TS.NAME,
				TS.EXECUTION_STATUS,
				org.jooq.impl.DSL.coalesce(org.jooq.impl.DSL.left(TCLN.DESCRIPTION, 30), "").as("DESCRIPTION"),
				MC.MILESTONE_ID,
				org.jooq.impl.DSL.decode()
					.when(M.STATUS.isNull().or(M.STATUS.eq(MILESTONE_STATUS_IN_PROGRESS)).or(M.STATUS.eq(MILESTONE_STATUS_FINISHED)), "true")
					.otherwise("false")
					.as("IS_MILESTONE_MODIFIABLE")
			)
			.from(TS)
			.leftJoin(TSTPI).on(TS.ID.eq(TSTPI.SUITE_ID))
			.leftJoin(ITPI).on(TSTPI.TPI_ID.eq(ITPI.ITEM_TEST_PLAN_ID))
			.leftJoin(TCLN).on(ITPI.TCLN_ID.eq(TCLN.TCLN_ID))
			.leftJoin(ITS).on(TS.ID.eq(ITS.TEST_SUITE_ID))
			.leftJoin(CI).on(ITS.ITERATION_ID.eq(CI.ITERATION_ID))
			.leftJoin(MC).on(CI.CAMPAIGN_ID.eq(MC.CAMPAIGN_ID))
			.leftJoin(M).on(MC.MILESTONE_ID.eq(M.MILESTONE_ID))
			.where(TS.ID.in(fatherChildrenEntity.values()))
			.groupBy(TS.ID, TCLN.DESCRIPTION, MC.MILESTONE_ID, M.STATUS)
			.fetch()
			.stream()
			.map(r -> buildTestSuite(r.get(TS.ID), r.get(TS.NAME), r.get(TS.EXECUTION_STATUS), (String) r.get("DESCRIPTION"), currentUser, r.get(MC.MILESTONE_ID), (String) r.get("IS_MILESTONE_MODIFIABLE")))
			.collect(Collectors.toMap(node -> (Long) node.getAttr().get("resId"), Function.identity()));
	}

	private String removeHtml(String html) {
		if (StringUtils.isBlank(html)) {
			return "";
		}
		return html.replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", "");
	}

	private Integer getMilestoneNumber(Long milestone) {
		return (milestone == null) ? NODE_WITHOUT_MILESTONE : 1;
	}

	// *************************************** send stuff to abstract workspace ***************************************

	@Override
	protected Field<Long> getProjectLibraryColumn() {
		return PROJECT.CL_ID;
	}

	@Override
	protected String getFolderName() {
		return "CampaignFolder";
	}

	@Override
	protected String getNodeName() {
		return "Campaign";
	}

	@Override
	protected String getRel() {
		return "drive";
	}

	@Override
	protected Field<Long> selectLibraryId() {
		return CAMPAIGN_LIBRARY.CL_ID;
	}

	@Override
	protected Map<Long, List<Long>> findAllMilestonesForLN() {
		return null;
	}

	@Override
	protected TableLike<?> getLibraryTable() {
		return CAMPAIGN_LIBRARY;
	}

	@Override
	protected TableLike<?> getLibraryTableContent() {
		return CAMPAIGN_LIBRARY_CONTENT;
	}

	@Override
	protected Field<Long> selectLNRelationshipAncestorId() {
		return CLN_RELATIONSHIP.ANCESTOR_ID;
	}

	@Override
	protected Field<Long> selectLNRelationshipDescendantId() {
		return CLN_RELATIONSHIP.DESCENDANT_ID;
	}

	@Override
	protected Field<Integer> selectLNRelationshipContentOrder() {
		return CLN_RELATIONSHIP.CONTENT_ORDER;
	}

	@Override
	protected TableLike<?> getLNRelationshipTable() {
		return CLN_RELATIONSHIP;
	}

	@Override
	protected Field<Long> getMilestoneLibraryNodeId() {
		return MILESTONE_CAMPAIGN.CAMPAIGN_ID;
	}

	@Override
	protected TableLike<?> getMilestoneLibraryNodeTable() {
		return MILESTONE_CAMPAIGN;
	}

	@Override
	protected Field<Long> getMilestoneId() {
		return MILESTONE_CAMPAIGN.MILESTONE_ID;
	}

	@Override
	protected HibernateEntityDao hibernateFolderDao() {
		return hibernateCampaignFolderDao;
	}

	@Override
	protected Set<Long> findLNByMilestoneId(Long activeMilestoneId) {
		return new HashSet<>(DSL.select(MILESTONE_CAMPAIGN.CAMPAIGN_ID)
			.from(MILESTONE_CAMPAIGN)
			.where(MILESTONE_CAMPAIGN.MILESTONE_ID.eq(activeMilestoneId))
			.union(DSL.select(CAMPAIGN_FOLDER.CLN_ID).from(CAMPAIGN_FOLDER))
			.fetch(MILESTONE_CAMPAIGN.CAMPAIGN_ID, Long.class));
	}

	@Override
	protected Field<Long> selectLibraryContentContentId() {
		return CAMPAIGN_LIBRARY_CONTENT.CONTENT_ID;
	}

	@Override
	protected Field<Integer> selectLibraryContentOrder() {
		return CAMPAIGN_LIBRARY_CONTENT.CONTENT_ORDER;
	}

	@Override
	protected Field<Long> selectLibraryContentLibraryId() {
		return CAMPAIGN_LIBRARY_CONTENT.LIBRARY_ID;
	}

	@Override
	protected String getClassName() {
		return CampaignLibrary.class.getSimpleName();
	}

	@Override
	protected String getLibraryClassName() {
		return CampaignLibrary.class.getName();
	}

	@Override
	protected String getLibraryPluginType() {
		return CampaignLibraryPluginBinding.CL_TYPE;
	}
}
