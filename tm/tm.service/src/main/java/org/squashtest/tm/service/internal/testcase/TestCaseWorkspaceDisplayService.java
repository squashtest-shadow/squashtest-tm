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
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.TableField;
import org.jooq.TableLike;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryPluginBinding;
import org.squashtest.tm.jooq.domain.tables.*;
import org.squashtest.tm.jooq.domain.tables.records.ProjectRecord;
import org.squashtest.tm.service.internal.dto.UserDto;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode.State;
import org.squashtest.tm.service.internal.repository.hibernate.HibernateEntityDao;
import org.squashtest.tm.service.internal.repository.hibernate.HibernateTestCaseFolderDao;
import org.squashtest.tm.service.internal.workspace.AbstractWorkspaceDisplayService;
import org.squashtest.tm.service.requirement.VerifiedRequirementsManagerService;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.squashtest.tm.jooq.domain.Tables.*;

@Service("testCaseWorkspaceDisplayService")
@Transactional(readOnly = true)
public class TestCaseWorkspaceDisplayService extends AbstractWorkspaceDisplayService {

	@Inject
	DSLContext DSL;

	@Inject
	HibernateTestCaseFolderDao hibernateTestCaseFolderDao;

	@Inject
	private VerifiedRequirementsManagerService verifiedRequirementsManagerService;

	private TestCaseLibraryNode TCLN = TEST_CASE_LIBRARY_NODE.as("TCLN");
	private TestCaseFolder TCF = TEST_CASE_FOLDER.as("TCF");
	private TestCase TC = TEST_CASE.as("TC");
	private RequirementVersionCoverage RVC = REQUIREMENT_VERSION_COVERAGE.as("RVC");
	private TestCaseSteps TCS = TEST_CASE_STEPS.as("TCS");
	private TclnRelationship TCLNR = TCLN_RELATIONSHIP.as("TCLNR");
	private Milestone M = MILESTONE.as("M");
	private MilestoneTestCase MTC = MILESTONE_TEST_CASE.as("MTC");

	@Override
	//TODO add milestones
	protected Map<Long, JsTreeNode> getLibraryChildrenMap(Set<Long> childrenIds, MultiMap expansionCandidates, UserDto currentUser, Map<Long, List<Long>> allMilestonesForTCs, List<Long> milestonesModifiable, Long activeMilestoneId) {

		return DSL
			.select(
				TCLN.TCLN_ID,
				org.jooq.impl.DSL.decode()
					.when(TCF.TCLN_ID.isNotNull(), "test-case-folders")
					.otherwise("test-cases").as("RESTYPE"),
				TCLN.NAME,
				TC.IMPORTANCE,
				TC.REFERENCE,
				TC.TC_STATUS,
				org.jooq.impl.DSL.decode()
					.when(TCS.TEST_CASE_ID.isNull(), "false")
					.otherwise("true")
					.as("HAS_STEP"),
				org.jooq.impl.DSL.decode()
					.when(RVC.VERIFYING_TEST_CASE_ID.isNull(), "false")
					.otherwise("true")
					.as("IS_REQ_COVERED"),
				org.jooq.impl.DSL.decode()
					.when(TCLNR.ANCESTOR_ID.isNull(), "false")
					.otherwise("true")
					.as("HAS_CONTENT")
			)
			.from(TCLN)
			.leftJoin(TCF).on(TCLN.TCLN_ID.eq(TCF.TCLN_ID))
			.leftJoin(TC).on(TCLN.TCLN_ID.eq(TC.TCLN_ID))
			.leftJoin(TCS).on(TC.TCLN_ID.eq(TCS.TEST_CASE_ID))
			.leftJoin(RVC).on(TC.TCLN_ID.eq(RVC.VERIFYING_TEST_CASE_ID))
			.leftJoin(TCLNR).on(TCLN.TCLN_ID.eq(TCLNR.ANCESTOR_ID))
			.where(TCLN.TCLN_ID.in(childrenIds))
			.groupBy(TCLN.TCLN_ID, TCF.TCLN_ID, TC.IMPORTANCE, TC.REFERENCE, TC.TC_STATUS, TCS.TEST_CASE_ID, RVC.VERIFYING_TEST_CASE_ID, TCLNR.ANCESTOR_ID)
			.fetch()
			.stream()
			.map(r -> {
				if (r.get("RESTYPE").equals("test-case-folders")) {
					return buildFolder(r.get(TCLN.TCLN_ID), r.get(TCLN.NAME), (String) r.get("RESTYPE"), (String) r.get("HAS_CONTENT"), currentUser);
				} else {
					Integer milestonesNumber = getMilestonesNumberForTC(allMilestonesForTCs, r.get(TCLN.TCLN_ID));
					String isMilestoneModifiable = isMilestoneModifiable(allMilestonesForTCs, milestonesModifiable,r.get(TCLN.TCLN_ID));
					return buildTestCase(r.get(TCLN.TCLN_ID), r.get(TCLN.NAME), (String) r.get("RESTYPE"), r.get(TC.REFERENCE),
						r.get(TC.IMPORTANCE), r.get(TC.TC_STATUS), (String) r.get("HAS_STEP"), (String) r.get("IS_REQ_COVERED"), currentUser, milestonesNumber, isMilestoneModifiable);
				}
			})
			.collect(Collectors.toMap(node -> (Long) node.getAttr().get("resId"), Function.identity()));
	}

	private JsTreeNode buildTestCase(Long id, String name, String restype, String reference, String importance, String status,
									 String hasStep, String isDirectlyReqCovered, UserDto currentUser, Integer milestonesNumber, String isMilestoneModifiable) {
		Map<String, Object> attr = new HashMap<>();
		Boolean isreqcovered = Boolean.parseBoolean(isDirectlyReqCovered) ||
			verifiedRequirementsManagerService.testCaseHasUndirectRequirementCoverage(id);

		attr.put("resId", id);
		attr.put("resType", restype);
		attr.put("name", name);
		attr.put("id", "TestCase-" + id);
		attr.put("rel", "test-case");


		attr.put("importance", importance.toLowerCase());
		attr.put("status", status.toLowerCase());
		attr.put("hassteps", hasStep);
		attr.put("isreqcovered", isreqcovered);

		//build tooltip
		String[] args = {getMessage("test-case.status." + status), getMessage("test-case.importance." + importance),
			getMessage("squashtm.yesno." + isreqcovered), getMessage("tooltip.tree.testCase.hasSteps." + hasStep)};
		attr.put("title", getMessage("label.tree.testCase.tooltip", args));

		String title = name;
		if (!StringUtils.isEmpty(reference)) {
			attr.put("reference", reference);
			title = reference + " - " + title;
		}
		return buildNode(title, State.leaf, attr, currentUser, milestonesNumber, isMilestoneModifiable);
	}

	private Integer getMilestonesNumberForTC(Map<Long, List<Long>> allMilestonesForTCs, Long id) {
		return (allMilestonesForTCs.get(id) != null) ? allMilestonesForTCs.get(id).size() : NODE_WITHOUT_MILESTONE;
	}

	private String isMilestoneModifiable(Map<Long, List<Long>> allMilestonesForTCs, List<Long> milestonesModifiable, Long id) {
		if (allMilestonesForTCs.get(id) != null && milestonesModifiable != null) {
			List<Long> allMilestonesForTC = allMilestonesForTCs.get(id);
			for (Long milestone : allMilestonesForTC) {
				if (!milestonesModifiable.contains(milestone)) {
					return "false";
				}
			}
		}
		return "true";
	}

	// *************************************** send stuff to abstract workspace ***************************************

	@Override
	protected String getRel() {
		return "drive";
	}

	@Override
	protected Field<Long> selectLibraryId() {
		return TEST_CASE_LIBRARY.TCL_ID;
	}

	@Override
	protected Map<Long, List<Long>> findAllMilestonesForLN() {
		return DSL.select(MILESTONE_TEST_CASE.TEST_CASE_ID, MILESTONE_TEST_CASE.MILESTONE_ID)
			.from(MILESTONE_TEST_CASE)
			.union(
				DSL.select(REQUIREMENT_VERSION_COVERAGE.VERIFYING_TEST_CASE_ID, MILESTONE_REQ_VERSION.MILESTONE_ID)
					.from(REQUIREMENT_VERSION_COVERAGE)
					.join(REQUIREMENT_VERSION).on(REQUIREMENT_VERSION_COVERAGE.VERIFIED_REQ_VERSION_ID.eq(REQUIREMENT_VERSION.RES_ID))
					.join(MILESTONE_REQ_VERSION).on(REQUIREMENT_VERSION.RES_ID.eq(MILESTONE_REQ_VERSION.REQ_VERSION_ID))
			)
			.fetchGroups(MILESTONE_TEST_CASE.TEST_CASE_ID, MILESTONE_TEST_CASE.MILESTONE_ID);
	}

	@Override
	protected TableLike<?> getLibraryTable() {
		return org.squashtest.tm.jooq.domain.tables.TestCaseLibrary.TEST_CASE_LIBRARY;
	}

	@Override
	protected TableLike<?> getLibraryTableContent() {
		return TEST_CASE_LIBRARY_CONTENT;
	}

	@Override
	protected Field<Long> selectLibraryContentContentId() {
		return TEST_CASE_LIBRARY_CONTENT.CONTENT_ID;
	}

	@Override
	protected Field<Integer> selectLibraryContentOrder() {
		return TEST_CASE_LIBRARY_CONTENT.CONTENT_ORDER;
	}

	@Override
	protected Field<Long> selectLibraryContentLibraryId() {
		return TEST_CASE_LIBRARY_CONTENT.LIBRARY_ID;
	}

	@Override
	protected Field<Long> selectLNRelationshipAncestorId() {
		return TCLN_RELATIONSHIP.ANCESTOR_ID;
	}

	@Override
	protected Field<Long> selectLNRelationshipDescendantId() {
		return TCLN_RELATIONSHIP.DESCENDANT_ID;
	}

	@Override
	protected Field<Integer> selectLNRelationshipContentOrder() {
		return TCLN_RELATIONSHIP.CONTENT_ORDER;
	}

	@Override
	protected TableLike<?> getLNRelationshipTable() {
		return TCLN_RELATIONSHIP;
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
	protected TableField<ProjectRecord, Long> getProjectLibraryColumn() {
		return PROJECT.TCL_ID;
	}

	@Override
	protected String getFolderName() {
		return "TestCaseFolder";
	}

	@Override
	protected String getNodeName() {
		return "TestCase";
	}

	@Override
	protected Field<Long> getMilestoneLibraryNodeId() {
		return MILESTONE_TEST_CASE.TEST_CASE_ID;
	}

	@Override
	protected TableLike<?> getMilestoneLibraryNodeTable() {
		return MILESTONE_TEST_CASE;
	}

	@Override
	protected Field<Long> getMilestoneId() {
		return MILESTONE_TEST_CASE.MILESTONE_ID;
	}

	@Override
	protected HibernateEntityDao hibernateFolderDao() {
		return hibernateTestCaseFolderDao;
	}

	@Override
	protected Set<Long> findLNByMilestoneId(Long activeMilestoneId) {
		return new HashSet<>(DSL.select(MILESTONE_TEST_CASE.TEST_CASE_ID)
			.from(MILESTONE_TEST_CASE)
			.where(MILESTONE_TEST_CASE.MILESTONE_ID.eq(activeMilestoneId))
			.union(DSL.select(TEST_CASE_FOLDER.TCLN_ID).from(TEST_CASE_FOLDER))
			.union(
				DSL.select(REQUIREMENT_VERSION_COVERAGE.VERIFYING_TEST_CASE_ID)
					.from(REQUIREMENT_VERSION_COVERAGE)
					.join(REQUIREMENT_VERSION).on(REQUIREMENT_VERSION_COVERAGE.VERIFIED_REQ_VERSION_ID.eq(REQUIREMENT_VERSION.RES_ID))
					.join(MILESTONE_REQ_VERSION).on(REQUIREMENT_VERSION.RES_ID.eq(MILESTONE_REQ_VERSION.REQ_VERSION_ID))
				.where(MILESTONE_REQ_VERSION.MILESTONE_ID.eq(activeMilestoneId))
			)
			.fetch(MILESTONE_TEST_CASE.TEST_CASE_ID, Long.class));
	}

	@Override
	protected boolean passesMilestoneFilter(JsTreeNode node, Long activeMilestoneId) {
		return (node != null && (NO_ACTIVE_MILESTONE_ID.equals(activeMilestoneId) || node.getAttr().get("rel").equals("folder") || nodeHasActiveMilestone(nodeLinkedToMilestone, (Long) node.getAttr().get("resId"))));
	}

	@Override
	public Collection<JsTreeNode> getCampaignNodeContent(Long folderId, UserDto currentUser, String libraryNode) {
		return null;
	}
}

