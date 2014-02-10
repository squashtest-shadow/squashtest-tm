/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.IdentifiedUtil;
import org.squashtest.tm.domain.requirement.ExportRequirementData;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.domain.requirement.RequirementSearchCriteria;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.requirement.VerificationCriterion;
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
	private static final String FIND_DESCENDANT_QUERY = "select DESCENDANT_ID from RLN_RELATIONSHIP where ANCESTOR_ID in (:list)";
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
		private List<Long> requirementIds;

		private SetRequirementsIdsParameterCallback(List<Long> requirementIds) {
			this.requirementIds = requirementIds;
		}

		@Override
		public void setQueryParameters(Query query) {
			query.setParameterList("requirementIds", requirementIds);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<RequirementLibraryNode> findAllBySearchCriteria(RequirementSearchCriteria searchCriteria) {
		List<RequirementLibraryNode> resultList = new ArrayList<RequirementLibraryNode>();
		// requirements
		DetachedCriteria requirementCriteria = createRequirementCriteria(searchCriteria);
		requirementCriteria.addOrder(Order.asc(RES_NAME));
		List<RequirementLibraryNode> requirementList = requirementCriteria.getExecutableCriteria(currentSession())
				.list();
		resultList.addAll(requirementList);
		// requirement Folders
		if (searchCriteria.libeleIsOnlyCriteria()) {
			DetachedCriteria requirementFolderCriteria = createRequirementFolderCriteria(searchCriteria);
			requirementFolderCriteria.addOrder(Order.asc(RES_NAME));
			List<RequirementLibraryNode> requirementFolderList = requirementFolderCriteria.getExecutableCriteria(
					currentSession()).list();
			resultList.addAll(requirementFolderList);
		}

		return resultList;
	}

	private DetachedCriteria createRequirementFolderCriteria(RequirementSearchCriteria searchCriteria) {
		DetachedCriteria criteria = DetachedCriteria.forClass(RequirementFolder.class);
		criteria.createCriteria("resource", "res");

		if (StringUtils.isNotBlank(searchCriteria.getName())) {
			criteria.add(Restrictions.ilike(RES_NAME, searchCriteria.getName(), MatchMode.ANYWHERE));
		}

		return criteria;

	}

	private DetachedCriteria createRequirementCriteria(RequirementSearchCriteria searchCriteria) {
		DetachedCriteria versionCriteria = DetachedCriteria.forClass(Requirement.class);
		versionCriteria.createCriteria("resource", "res");

		if (StringUtils.isNotBlank(searchCriteria.getName())) {
			versionCriteria.add(Restrictions.ilike(RES_NAME, searchCriteria.getName(), MatchMode.ANYWHERE));
		}
		if (StringUtils.isNotBlank(searchCriteria.getReference())) {
			versionCriteria.add(Restrictions.ilike("res.reference", searchCriteria.getReference(), MatchMode.ANYWHERE));
		}
		if (!searchCriteria.getCriticalities().isEmpty()) {
			versionCriteria.add(Restrictions.in("res.criticality", searchCriteria.getCriticalities()));
		}
		if (!searchCriteria.getCategories().isEmpty()) {
			versionCriteria.add(Restrictions.in("res.category", searchCriteria.getCategories()));
		}

		addVerificationRestriction(searchCriteria, versionCriteria);

		return versionCriteria;
	}

	private void addVerificationRestriction(RequirementSearchCriteria searchCriteria, DetachedCriteria criteria) {
		Criterion restriction = (Criterion) HIBERNATE_RESTRICTION_BY_VERIFICATION_CRITERION.get(searchCriteria
				.getVerificationCriterion());

		if (restriction != null) {
			criteria.add(restriction);
		}
	}
	
	@Override
	public List<Requirement> findChildrenRequirements(long requirementId) {
		SetQueryParametersCallback setId = new SetIdParameter("requirementId", requirementId);
		return executeListNamedQuery("requirement.findChildrenRequirements", setId);
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public List<RequirementLibraryNode> findAllBySearchCriteriaOrderByProject(RequirementSearchCriteria searchCriteria) {
		List<RequirementLibraryNode> resultList = findAllBySearchCriteria(searchCriteria);
		Collections.sort(resultList, new Comparator<RequirementLibraryNode>() {

			@Override
			public int compare(RequirementLibraryNode req1, RequirementLibraryNode req2) {
				return req1.getProject().getName().compareTo(req2.getProject().getName());
			}
		});
		return resultList;
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
		// find all leafs contained in ids and contained by folders in ids
		List<Long> listReqNodeId = findDescendantIds(params, FIND_DESCENDANT_QUERY);

		// Case 1. Only root leafs are found
		if (listReqNodeId == null || listReqNodeId.isEmpty()) {
			return formatExportResult(addRootContentToExportData(rootReqs, new ArrayList<Object[]>()));
		}

		// Case 2. More than root leafs are found
		List<Long> reqIds = findRequirementIdsInIdsList(listReqNodeId);
		List<Object[]> listObject = findRequirementAndParentFolder(reqIds);

		if (!rootReqs.isEmpty()) {
			listObject = addRootContentToExportData(rootReqs, listObject);
		}

		return formatExportResult(listObject);
	}

	private List<Object[]> findRequirementAndParentFolder(final List<Long> requirementsIds) {
		if (!requirementsIds.isEmpty()) {
			SetQueryParametersCallback newCallBack1 = new SetRequirementsIdsParameterCallback(requirementsIds);
			return executeListNamedQuery("requirement.findRequirementWithParentFolder", newCallBack1);

		} else {
			return Collections.emptyList();
		}
	}

	private List<Long> findRequirementIdsInIdsList(final List<Long> requirementIds) {
		if (!requirementIds.isEmpty()) {

			List<Requirement> resultList = findAllByIds(requirementIds);
			return IdentifiedUtil.extractIds(resultList);

		} else {
			return Collections.emptyList();
		}
	}

	private List<ExportRequirementData> formatExportResult(List<Object[]> list) {
		if (!list.isEmpty()) {
			List<ExportRequirementData> exportList = new ArrayList<ExportRequirementData>();

			for (Object[] tuple : list) {
				Requirement req = (Requirement) tuple[0];
				RequirementFolder folder = (RequirementFolder) tuple[1];
				ExportRequirementData erd = new ExportRequirementData(req, folder);
				exportList.add(erd);
			}

			return exportList;
		} else {
			return Collections.emptyList();
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
			Object[] tab = { requirement, null };
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
		if (! requirementIds.isEmpty()){
			List<Object[]> allpairs = new ArrayList<Object[]>(requirementIds.size());
			
			List<Object[]> libraryReqs = executeListNamedQuery("requirement.findAllLibraryParents", new SetRequirementsIdsParameterCallback(requirementIds));
			List<Object[]> folderReqs = executeListNamedQuery("requirement.findAllFolderParents", new SetRequirementsIdsParameterCallback(requirementIds));
			List<Object[]> reqReqs = executeListNamedQuery("requirement.findAllRequirementParents", new SetRequirementsIdsParameterCallback(requirementIds));
			
			allpairs.addAll(libraryReqs);
			allpairs.addAll(folderReqs);
			allpairs.addAll(reqReqs);
			
			return allpairs;
		}
		else{
			return Collections.emptyList();
		}
	}

}