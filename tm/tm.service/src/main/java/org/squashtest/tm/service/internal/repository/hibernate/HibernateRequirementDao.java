/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.service.internal.repository.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.IdentifiedUtil;
import org.squashtest.tm.domain.requirement.ExportRequirementData;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.requirement.VerificationCriterion;
import org.squashtest.tm.service.internal.library.HibernatePathService;
import org.squashtest.tm.service.internal.repository.RequirementDao;

@Repository
public class HibernateRequirementDao extends HibernateEntityDao<Requirement> implements RequirementDao {
	private static final Map<VerificationCriterion, Criterion> HIBERNATE_RESTRICTION_BY_VERIFICATION_CRITERION = new HashMap<VerificationCriterion, Criterion>(
			VerificationCriterion.values().length);

	static {
		HIBERNATE_RESTRICTION_BY_VERIFICATION_CRITERION.put(VerificationCriterion.ANY, null); // yeah, it's a null.

		HIBERNATE_RESTRICTION_BY_VERIFICATION_CRITERION.put(VerificationCriterion.SHOULD_BE_VERIFIED,
				Restrictions.isNotEmpty("res.requirementVersionCoverages"));
		HIBERNATE_RESTRICTION_BY_VERIFICATION_CRITERION.put(VerificationCriterion.SHOULD_NOT_BE_VERIFIED,
				Restrictions.isEmpty("res.requirementVersionCoverages"));

	}
	private static final String RES_NAME = "res.name";
	private static final String FIND_ALL_FOR_LIBRARY_QUERY = "select distinct requirment.RLN_ID"
			+ " from REQUIREMENT requirment" + " where requirment.RLN_ID in (" + " select dRequirement.RLN_ID"
			+ " from REQUIREMENT dRequirement"
			+ " JOIN RLN_RELATIONSHIP_CLOSURE closure ON dRequirement.RLN_ID = closure.DESCENDANT_ID"
			+ " JOIN REQUIREMENT_LIBRARY_CONTENT dRoot ON dRoot.CONTENT_ID = closure.ANCESTOR_ID"
			+ " where dRoot.LIBRARY_ID = :libraryId" + " union" + " select rRequirement.RLN_ID"
			+ " from REQUIREMENT rRequirement"
			+ " JOIN REQUIREMENT_LIBRARY_CONTENT rRoot ON rRoot.CONTENT_ID = rRequirement.RLN_ID"
			+ " where rRoot.LIBRARY_ID = :libraryId" + " )";

	private static final class SetRequirementsIdsParameterCallback implements SetQueryParametersCallback {
		private Collection<Long> requirementIds;

		private SetRequirementsIdsParameterCallback(Collection<Long> requirementIds) {
			this.requirementIds = requirementIds;
		}

		@Override
		public void setQueryParameters(Query query) {
			query.setParameterList("requirementIds", requirementIds);
		}
	}



	@Override
	public List<Requirement> findChildrenRequirements(long requirementId) {
		SetQueryParametersCallback setId = new SetIdParameter("requirementId", requirementId);
		return executeListNamedQuery("requirement.findChildrenRequirements", setId);
	}


	/* ----------------------------------------------------EXPORT METHODS----------------------------------------- */

	@Override
	public List<ExportRequirementData> findRequirementToExportFromNodes(List<Long> params) {
		if (!params.isEmpty()) {
			return doFindRequirementToExportFromNodes(params);

		} else {
			return Collections.emptyList();

		}
	}

	private List<ExportRequirementData> doFindRequirementToExportFromNodes(List<Long> params) {
		// find root leafs
		List<Requirement> rootReqs = findRootContentRequirement(params);
		List<Long> rootReqsIds = IdentifiedUtil.extractIds(rootReqs);
		// find all leafs contained in param ids and contained by folders or requirements in param ids
		Set<Long> nonRootReqNodesIds = new HashSet<Long>();
		List<Long> listReqNodeId = findDescendantRequirementIds(params);
		nonRootReqNodesIds.addAll(listReqNodeId);
		List<Long> listParentRequirementIds = findRequirementParents(params);
		nonRootReqNodesIds.addAll(listParentRequirementIds);
		nonRootReqNodesIds.addAll(params);
		nonRootReqNodesIds.removeAll(rootReqsIds);

		// Case 1. Only root leafs are found
		if (nonRootReqNodesIds.isEmpty()) {
			return formatExportResult(rootReqs, new ArrayList<Object[]>(0));
		}

		// Case 2. More than root leafs are found
		List<Requirement> nonRootReqs = findAllByIds(nonRootReqNodesIds);
		Collection<Object[]> listObject = addPathInfos(nonRootReqs);

		return formatExportResult(rootReqs, listObject);
	}

	@SuppressWarnings("unchecked")
	private List<Long> findRequirementParents(List<Long> params) {
		Query query = currentSession().getNamedQuery("requirement.findRequirementParentIds");
		query.setParameterList("nodeIds", params);
		return query.list();

	}

	@SuppressWarnings("unchecked")
	private List<Long> findDescendantRequirementIds(Collection<Long> params) {
		Query query = currentSession().getNamedQuery("requirement.findRequirementDescendantIds");
		query.setParameterList("nodeIds", params);
		return query.list();

	}

	/**
	 * Returns a list of objects that holds infos for requirements and their position in requirement hierarchy
	 * 
	 * @param requirements
	 * @return list of object with, for all objects obj[0] = requirement , obj[1] folder path , obj[2] requirement path
	 */
	private Collection<Object[]> addPathInfos(final List<Requirement> requirements) {
		if (!requirements.isEmpty()) {

			Map<Long, Object[]> exportInfosById = new HashMap<Long, Object[]>(requirements.size());
			for (Requirement requirement : requirements) {
				Object[] exportInfo = new Object[3];
				exportInfo[0] = requirement;
				exportInfo[1] = "";
				exportInfo[2] = "";
				exportInfosById.put(requirement.getId(), exportInfo);
			}
			SetQueryParametersCallback newCallBack1 = new SetRequirementsIdsParameterCallback(exportInfosById.keySet());
			Session session = currentSession();

			Query q = session.getNamedQuery("requirement.findReqPaths");
			newCallBack1.setQueryParameters(q);
			List<Object[]> idAndReqPaths = q.list();
			addPathInfosToExportInfos(exportInfosById, idAndReqPaths, 2);
			q = session.getNamedQuery("requirement.findFolderPaths");
			newCallBack1.setQueryParameters(q);
			List<Object[]> folderPaths = q.list();
			addPathInfosToExportInfos(exportInfosById, folderPaths, 1);

			return exportInfosById.values();

		} else {
			return Collections.emptyList();
		}
	}

	public void addPathInfosToExportInfos(Map<Long, Object[]> reqAndFolderPathAndReqPathById,
			List<Object[]> idAndPaths, int pathInfoIndex) {
		for (Object[] idAndPath : idAndPaths) {
			Long reqId = (Long) idAndPath[0];
			String reqPath = (String) idAndPath[1];
			String escapedPath = HibernatePathService.escapePath(reqPath);
			Object[] reqAndFolderPathAndReqPath = reqAndFolderPathAndReqPathById.get(reqId);
			reqAndFolderPathAndReqPath[pathInfoIndex] = escapedPath;
		}
	}

	private List<ExportRequirementData> formatExportResult(Collection<Requirement> rootReq,
			Collection<Object[]> nonRootReq) {
		List<ExportRequirementData> exportList = new ArrayList<ExportRequirementData>();

		for (Requirement requirement : rootReq) {
			ExportRequirementData erd = new ExportRequirementData(requirement, "", "");
			exportList.add(erd);
		}

		for (Object[] exportReqInfos : nonRootReq) {
			Requirement requirement = (Requirement) exportReqInfos[0];
			String folderPath = (String) exportReqInfos[1];
			String requirementPath = (String) exportReqInfos[2];
			ExportRequirementData erd = new ExportRequirementData(requirement, folderPath, requirementPath);
			exportList.add(erd);
		}
		Collections.sort(exportList, new ExportRequirementDataComparator());
		return exportList;
	}

	private static final class ExportRequirementDataComparator implements Comparator<ExportRequirementData> {

		@Override
		public int compare(ExportRequirementData o1, ExportRequirementData o2) {
			int folderCompare = o1.getFolderName().compareTo(o2.getFolderName());
			if (folderCompare != 0) {
				return folderCompare;
			} else {
				return o1.getRequirementParentPath().compareTo(o2.getRequirementParentPath());
			}

		}

	}

	private List<Requirement> findRootContentRequirement(final List<Long> params) {
		if (!params.isEmpty()) {
			SetQueryParametersCallback newCallBack1 = new SetParamIdsParametersCallback(params);
			return executeListNamedQuery("requirement.findRootContentRequirement", newCallBack1);
		} else {
			return Collections.emptyList();

		}
	}

	@Override
	public List<ExportRequirementData> findRequirementToExportFromLibrary(final List<Long> libraryIds) {

		if (!libraryIds.isEmpty()) {
			SetLibraryIdsCallback newCallBack1 = new SetLibraryIdsCallback(libraryIds);
			List<Long> result = executeListNamedQuery("requirement.findAllRootContent", newCallBack1);

			return findRequirementToExportFromNodes(result);
		} else {
			return Collections.emptyList();
		}
	}

	private List<Object[]> addRootContentToExportData(List<Requirement> rootRequirement, List<Object[]> folderContent) {
		for (Requirement requirement : rootRequirement) {
			Object[] tab = { requirement, "", ExportRequirementData.NO_REQUIREMENT_PARENT_PATH };
			folderContent.add(tab);
		}
		return folderContent;
	}

	/* ----------------------------------------------------/EXPORT METHODS----------------------------------------- */

	@SuppressWarnings("unchecked")
	@Override
	public List<RequirementCriticality> findDistinctRequirementsCriticalitiesVerifiedByTestCases(Set<Long> testCasesIds) {
		if (!testCasesIds.isEmpty()) {
			Query query = currentSession().getNamedQuery(
					"requirementVersion.findDistinctRequirementsCriticalitiesVerifiedByTestCases");
			query.setParameterList("testCasesIds", testCasesIds);
			return query.list();
		} else {
			return Collections.emptyList();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<RequirementCriticality> findDistinctRequirementsCriticalities(List<Long> requirementVersionsIds) {
		if (!requirementVersionsIds.isEmpty()) {
			Query query = currentSession().getNamedQuery("requirementVersion.findDistinctRequirementsCriticalities");
			query.setParameterList("requirementsIds", requirementVersionsIds);
			return query.list();
		} else {
			return Collections.emptyList();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<RequirementVersion> findVersions(Long requirementId) {
		Query query = currentSession().getNamedQuery("requirement.findVersions");
		query.setParameter("requirementId", requirementId);
		return query.list();

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<RequirementVersion> findVersionsForAll(List<Long> requirementIds) {
		if (!requirementIds.isEmpty()) {
			Query query = currentSession().getNamedQuery("requirement.findVersionsForAll");
			query.setParameterList("requirementIds", requirementIds, LongType.INSTANCE);
			return query.list();
		} else {
			return Collections.emptyList();
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> findAllRequirementsIdsByLibrary(long libraryId) {
		Session session = currentSession();
		SQLQuery query = session.createSQLQuery(FIND_ALL_FOR_LIBRARY_QUERY);
		query.setParameter("libraryId", Long.valueOf(libraryId));
		query.setResultTransformer(new SqLIdResultTransformer());
		return query.list();
	}

	@Override
	public Requirement findByContent(final Requirement child) {
		SetQueryParametersCallback callback = new SetNodeContentParameter(child);

		return executeEntityNamedQuery("requirement.findByContent", callback);
	}

	@Override
	public List<Object[]> findAllParentsOf(List<Long> requirementIds) {
		if (!requirementIds.isEmpty()) {
			List<Object[]> allpairs = new ArrayList<Object[]>(requirementIds.size());

			List<Object[]> libraryReqs = executeListNamedQuery("requirement.findAllLibraryParents",
					new SetRequirementsIdsParameterCallback(requirementIds));
			List<Object[]> folderReqs = executeListNamedQuery("requirement.findAllFolderParents",
					new SetRequirementsIdsParameterCallback(requirementIds));
			List<Object[]> reqReqs = executeListNamedQuery("requirement.findAllRequirementParents",
					new SetRequirementsIdsParameterCallback(requirementIds));

			allpairs.addAll(libraryReqs);
			allpairs.addAll(folderReqs);
			allpairs.addAll(reqReqs);

			return allpairs;
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public List<Long> findRequirementIdsHavingMultipleMilestones(List<Long> nodeIds) {
		Query q = currentSession().getNamedQuery("requirement.findRequirementIdsHavingMultipleMilestones");
		q.setParameterList("nodeIds", nodeIds, LongType.INSTANCE);
		return q.list();
	}

	@Override
	public List<Long> findNonBoundRequirement(Collection<Long> nodeIds, Long milestoneId) {
		Query q = currentSession().getNamedQuery("requirement.findNonBoundRequirement");
		q.setParameterList("nodeIds", nodeIds, LongType.INSTANCE);
		q.setParameter("milestoneId", milestoneId);
		return q.list();
	}

}