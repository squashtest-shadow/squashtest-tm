/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.csp.tm.internal.repository.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.csp.core.domain.IdentifiedUtil;
import org.squashtest.csp.core.infrastructure.hibernate.PagingUtils;
import org.squashtest.csp.core.infrastructure.hibernate.SortingUtils;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.requirement.RequirementSearchCriteria;
import org.squashtest.csp.tm.domain.testcase.ActionTestStep;
import org.squashtest.csp.tm.domain.testcase.ExportTestCaseData;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseFolder;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.csp.tm.domain.testcase.TestCaseSearchCriteria;
import org.squashtest.csp.tm.domain.testcase.TestStep;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.internal.repository.TestCaseDao;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;

/**
 * DAO for org.squashtest.csp.tm.domain.testcase.TestCase
 * 
 * @author bsiri
 * 
 */

@Repository
public class HibernateTestCaseDao extends HibernateEntityDao<TestCase> implements TestCaseDao {
	/**
	 * "Standard" name for a query parameter representing a test case id.
	 */
	private static final String TEST_CASE_ID_PARAM_NAME = "testCaseId";
	private static final String PROJECT = "project";
	private static final String FIND_DESCENDANT_QUERY = "select DESCENDANT_ID from TCLN_RELATIONSHIP where ANCESTOR_ID in (:list)";

	private static final class SetIdParameter implements SetQueryParametersCallback {
		private final long testCaseId;

		public SetIdParameter(long testCaseId) {
			super();
			this.testCaseId = testCaseId;
		}

		@Override
		public void setQueryParameters(Query query) {
			query.setLong(TEST_CASE_ID_PARAM_NAME, testCaseId);
		}
	};

	@Override
	// FIXME Uh, should be init'd by a query !
	public TestCase findAndInit(Long testCaseId) {
		Session session = currentSession();
		TestCase tc = (TestCase) session.get(TestCase.class, testCaseId);
		if (tc == null) {
			return null;
		}
		Hibernate.initialize(tc.getSteps());

		return tc;
	}

	@Override
	public TestCase findByIdWithInitializedSteps(long testCaseId) {
		return executeEntityNamedQuery("testCase.findByIdWithInitializedSteps", idParameter(testCaseId));

	}

	@Override
	public List<ActionTestStep> getTestCaseSteps(final Long testCaseId) {
		return executeListNamedQuery("testCase.findAllTestSteps", idParameter(testCaseId));

	}

	private SetQueryParametersCallback idParameter(final long testCaseId) {
		return new SetIdParameter(testCaseId);
	}

	/**
	 * @deprecated not used
	 */
	@Deprecated
	@Override
	public List<TestCase> findAllByIdListOrderedByName(final List<Long> testCasesIds) {
		SetQueryParametersCallback setParams = new SetIdsParameter(testCasesIds);
		return executeListNamedQuery("testCase.findAllByIdListOrderedByName", setParams);
	}

	private static final class SetIdsParameter implements SetQueryParametersCallback {
		private List<Long> testCasesIds;

		private SetIdsParameter(List<Long> testCasesIds) {
			this.testCasesIds = testCasesIds;
		}

		@Override
		public void setQueryParameters(Query query) {
			query.setParameterList("testCasesIds", testCasesIds);
		}
	}

	@Override
	public List<String> findNamesInFolderStartingWith(final long folderId, final String nameStart) {
		SetQueryParametersCallback newCallBack1 = new ContainerIdNameStartParameterCallback(folderId, nameStart);
		return executeListNamedQuery("testCase.findNamesInFolderStartingWith", newCallBack1);
	}

	@Override
	public List<String> findNamesInLibraryStartingWith(final long libraryId, final String nameStart) {
		SetQueryParametersCallback newCallBack1 = new ContainerIdNameStartParameterCallback(libraryId, nameStart);
		return executeListNamedQuery("testCase.findNamesInLibraryStartingWith", newCallBack1);
	}

	/*
	 * we can't use the Criteria API because we need to get the ordered list and we can't access the join table to sort
	 * them (again).
	 */

	@Override
	public List<TestStep> findAllStepsByIdFiltered(final long testCaseId, final Paging filter) {
		final int firstIndex = filter.getFirstItemIndex();
		final int lastIndex = filter.getFirstItemIndex() + filter.getPageSize() - 1;

		SetQueryParametersCallback callback = new SetIdsIndexesParameters(testCaseId, firstIndex, lastIndex);

		return executeListNamedQuery("testCase.findAllStepsByIdFiltered", callback);
	}

	private static final class SetIdsIndexesParameters implements SetQueryParametersCallback {
		private int firstIndex;
		private long testCaseId;
		private int lastIndex;

		private SetIdsIndexesParameters(long testCaseId, int firstIndex, int lastIndex) {
			this.testCaseId = testCaseId;
			this.firstIndex = firstIndex;
			this.lastIndex = lastIndex;
		}

		@Override
		public void setQueryParameters(Query query) {

			query.setParameter("testCaseId", testCaseId);
			query.setParameter("firstIndex", firstIndex);
			query.setParameter("lastIndex", lastIndex);

		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TestCaseLibraryNode> findAllByNameContaining(final String tokenInName, boolean groupByProject) {
		List<TestCaseLibraryNode> result;

		Criteria criteria = currentSession().createCriteria(TestCaseLibraryNode.class, "testCaseLibraryNode")
				.createAlias("testCaseLibraryNode.project", PROJECT)
				.add(Restrictions.ilike("testCaseLibraryNode.name", tokenInName, MatchMode.ANYWHERE));

		if (groupByProject) {
			criteria = criteria.addOrder(Order.asc("project.id"));

		}

		criteria = criteria.addOrder(Order.asc("testCaseLibraryNode.name"));

		result = criteria.list();

		return result;
	}

	@Override
	public TestCase findTestCaseByTestStepId(final long testStepId) {
		Query query = currentSession().getNamedQuery("testStep.findParentNode");
		query.setParameter("childId", testStepId);
		return (TestCase) query.uniqueResult();
	}

	@Override
	public long countCallingTestSteps(long testCaseId) {
		return (Long) executeEntityNamedQuery("testCase.countCallingTestSteps", new SetIdParameter(testCaseId));
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> findTestCasesHavingCaller(Collection<Long> testCasesIds) {
		Query query = currentSession().getNamedQuery("testCase.findTestCasesHavingCaller");
		query.setParameterList("testCasesIds", testCasesIds);
		return query.list();
	}

	@Override
	public List<Long> findAllTestCasesIdsCalledByTestCase(long testCaseId) {
		return executeListNamedQuery("testCase.findAllTestCasesIdsCalledByTestCase", new SetIdParameter(testCaseId));
	}

	@Override
	public List<Long> findDistinctTestCasesIdsCalledByTestCase(Long testCaseId) {
		return executeListNamedQuery("testCase.findDistinctTestCasesIdsCalledByTestCase",
				new SetIdParameter(testCaseId));
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> findAllTestCasesIdsCalledByTestCases(List<Long> testCasesIds) {
		Query query = currentSession().getNamedQuery("testCase.findAllTestCasesIdsCalledByTestCases");
		query.setParameterList("testCasesIds", testCasesIds);
		return query.list();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<TestCase> findAllCallingTestCases(final long testCaseId, final CollectionSorting sorting) {

		String hql = "select TestCase from TestCase as TestCase left join TestCase.project as Project "
				+ " join TestCase.steps as Steps where Steps.calledTestCase.id = :testCaseId group by TestCase ";

		String orderBy = "";

		if (sorting != null) {
			orderBy = " order by " + sorting.getSortedAttribute() + ' ' + sorting.getSortingOrder();
		}

		Query query = currentSession().createQuery(hql + orderBy);
		query.setParameter("testCaseId", testCaseId);

		if (sorting != null) {
			query.setMaxResults(sorting.getPageSize());
			query.setFirstResult(sorting.getFirstItemIndex());
		}
		return query.list();

	}

	@Override
	@SuppressWarnings("unchecked")
	/*
	 * implementation note : the following query could not use a right outer join. So we'll do the job manually. Hence
	 * the weird things done below.
	 */
	public List<Object[]> findTestCasesHavingCallerDetails(final Collection<Long> testCaseIds) {

		if (testCaseIds.isEmpty()) {
			return Collections.emptyList();
		}

		// the easy part : fetch the informations for those who are called
		SetQueryParametersCallback firstQueryCallback = new SetQueryParametersCallback() {
			@Override
			public void setQueryParameters(Query query) {
				query.setParameterList("testCaseIds", testCaseIds, new LongType());
				query.setReadOnly(true);
			}
		};

		List<Object[]> calledDetails = executeListNamedQuery("testCase.findTestCasesHavingCallerDetails",
				firstQueryCallback);

		// now we must fetch the same informations for those who aren't, so we can emulate the right outer join we need
		// first, get the ids that aren't part of the result. If we already got them all, no need for additional
		// queries.
		List<Long> calledIds = getCalledDetailsIds(calledDetails);

		if (calledIds.size() == testCaseIds.size()) {
			return calledDetails;
		}

		// otherwise a second query is necessary.
		final List<Long> nonCalledIds = new LinkedList<Long>(CollectionUtils.subtract(testCaseIds, calledIds));

		SetQueryParametersCallback secondQueryCallback = new SetNonCalledIdsParameter(nonCalledIds);

		List<Object[]> nonCalledDetails = executeListNamedQuery("testCase.findTestCasesHavingNoCallerDetails",
				secondQueryCallback);

		// now we can return
		calledDetails.addAll(nonCalledDetails);
		return calledDetails;

	}

	private static final class SetNonCalledIdsParameter implements SetQueryParametersCallback {
		private List<Long> nonCalledIds;

		private SetNonCalledIdsParameter(List<Long> nonCalledIds) {
			this.nonCalledIds = nonCalledIds;
		}

		@Override
		public void setQueryParameters(Query query) {
			query.setParameterList("nonCalledIds", nonCalledIds, new LongType());
			query.setReadOnly(true);
		}
	}

	private List<Long> getCalledDetailsIds(List<Object[]> calledDetails) {
		List<Long> calledIds = new LinkedList<Long>();

		for (Object[] called : calledDetails) {
			Object item = called[2];

			if (!calledIds.contains(item)) {
				calledIds.add((Long) item);
			}
		}
		return calledIds;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TestCase> findAllByRequirement(RequirementSearchCriteria criteria, boolean isProjectOrdered) {

		DetachedCriteria crit = createFindAllByRequirementCriteria(criteria);

		if (isProjectOrdered) {
			crit.addOrder(Order.asc(PROJECT));
		}

		return crit.getExecutableCriteria(currentSession()).list();
	}

	private DetachedCriteria createFindAllByRequirementCriteria(RequirementSearchCriteria criteria) {
		DetachedCriteria crit = DetachedCriteria.forClass(TestCase.class);
		crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		DetachedCriteria reqCrit = crit.createCriteria("verifiedRequirementVersions");

		if (criteria.getName() != null) {
			reqCrit.add(Restrictions.ilike("name", criteria.getName(), MatchMode.ANYWHERE));
		}

		if (criteria.getReference() != null) {
			reqCrit.add(Restrictions.ilike("reference", criteria.getReference(), MatchMode.ANYWHERE));
		}
		if (!criteria.getCriticalities().isEmpty()) {
			reqCrit.add(Restrictions.in("criticality", criteria.getCriticalities()));
		}
		if (!criteria.getCategories().isEmpty()) {
			reqCrit.add(Restrictions.in("category", criteria.getCategories()));
		}
		return crit;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> findCalledTestCaseOfCallSteps(List<Long> testStepsIds) {
		Query query = currentSession().getNamedQuery("testCase.findCalledTestCaseOfCallSteps");
		query.setParameterList("testStepsIds", testStepsIds);
		return query.list();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.squashtest.csp.tm.internal.repository.TestCaseDao#findAllVerifyingRequirementVersion(long,
	 * org.squashtest.tm.core.foundation.collection.PagingAndSorting)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<TestCase> findAllByVerifiedRequirementVersion(long verifiedId, PagingAndSorting sorting) {
		Criteria crit = createFindAllVerifyingCriteria(sorting);

		crit.add(Restrictions.eq("RequirementVersion.id", Long.valueOf(verifiedId)));

		return crit.list();
	}

	/**
	 * @param sorting
	 * @return
	 */
	private Criteria createFindAllVerifyingCriteria(PagingAndSorting sorting) {
		Criteria crit = currentSession().createCriteria(TestCase.class, "TestCase");
		crit.createAlias("verifiedRequirementVersions", "RequirementVersion");
		crit.createAlias("verifiedRequirementVersions.requirement", "Requirement", Criteria.LEFT_JOIN);
		crit.createAlias(PROJECT, PROJECT, Criteria.LEFT_JOIN);

		PagingUtils.addPaging(crit, sorting);
		SortingUtils.addOrder(crit, sorting);
		return crit;
	}

	/**
	 * @see org.squashtest.csp.tm.internal.repository.TestCaseDao#countByVerifiedRequirementVersion(long)
	 */
	@Override
	public long countByVerifiedRequirementVersion(final long verifiedId) {
		return (Long) executeEntityNamedQuery("testCase.countByVerifiedRequirementVersion", new SetVerifiedIdParameter(
				verifiedId));
	}

	private static final class SetVerifiedIdParameter implements SetQueryParametersCallback {
		private long verifiedId;

		private SetVerifiedIdParameter(long verifiedId) {
			this.verifiedId = verifiedId;
		}

		@Override
		public void setQueryParameters(Query query) {
			query.setLong("verifiedId", verifiedId);

		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TestCase> findUnsortedAllByVerifiedRequirementVersion(long requirementVersionId) {
		Query query = currentSession().getNamedQuery("testCase.findUnsortedAllByVerifiedRequirementVersion");
		query.setParameter("requirementVersionId", requirementVersionId);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TestCaseLibraryNode> findBySearchCriteria(TestCaseSearchCriteria criteria) {
		Criteria hCriteria;

		if (usesImportance(criteria)) {
			hCriteria = testCaseRootedCriteria(criteria);
		} else {
			hCriteria = tcNodeRootedCriteria(criteria);
		}
		
		if (usesNature(criteria)) {
			hCriteria = testCaseRootedCriteria(criteria);
		} else {
			hCriteria = tcNodeRootedCriteria(criteria);
		}

		if (usesType(criteria)) {
			hCriteria = testCaseRootedCriteria(criteria);
		} else {
			hCriteria = tcNodeRootedCriteria(criteria);
		}

		if (criteria.isGroupByProject()) {
			hCriteria.addOrder(Order.asc(PROJECT));
		}

		if (StringUtils.isNotBlank(criteria.getName())) {
			hCriteria.add(Restrictions.ilike("name", criteria.getName(), MatchMode.ANYWHERE));
		}

		hCriteria.addOrder(Order.asc("name"));

		return hCriteria.list();

	}

	private boolean usesImportance(TestCaseSearchCriteria criteria) {
		return (!criteria.getImportanceFilterSet().isEmpty());
	}

	private boolean usesNature(TestCaseSearchCriteria criteria) {
		return (!criteria.getNatureFilterSet().isEmpty());
	}
	
	private boolean usesType(TestCaseSearchCriteria criteria) {
		return (!criteria.getTypeFilterSet().isEmpty());
	}
	
	private Criteria testCaseRootedCriteria(TestCaseSearchCriteria criteria) {
		Criteria crit = currentSession().createCriteria(TestCase.class);
		if (!criteria.getImportanceFilterSet().isEmpty()) {
			crit.add(Restrictions.in("importance", criteria.getImportanceFilterSet()));
		}
		if (!criteria.getNatureFilterSet().isEmpty()) {
			crit.add(Restrictions.in("nature", criteria.getNatureFilterSet()));
		}
		if (!criteria.getTypeFilterSet().isEmpty()) {
			crit.add(Restrictions.in("type", criteria.getTypeFilterSet()));
		}
		return crit;
	}

	private Criteria tcNodeRootedCriteria(TestCaseSearchCriteria criteria) {
		return currentSession().createCriteria(TestCaseLibraryNode.class);
	}

	@Override
	public List<Execution> findAllExecutionByTestCase(Long tcId) {
		SetQueryParametersCallback callback = idParameter(tcId);
		return executeListNamedQuery("testCase.findAllExecutions", callback);
	}

	/* ----------------------------------------------------EXPORT METHODS----------------------------------------- */
	//TODO try to avoid duplicate code with requirementExport
	@Override
	public List<ExportTestCaseData> findTestCaseToExportFromProject(List<Long> projectIds) {
		if (!projectIds.isEmpty()) {
			SetQueryParametersCallback newCallBack1 = new SetProjectIdsParameterCallback(projectIds);
			List<Long> result = executeListNamedQuery("testCase.findAllRootContent", newCallBack1);

			return findTestCaseToExportFromNodes(result);
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public List<ExportTestCaseData> findTestCaseToExportFromNodes(List<Long> params) {
		if (!params.isEmpty()) {
			return doFindTestCaseToExportFromNodes(params);

		} else {
			return Collections.emptyList();

		}
	}

	private List<ExportTestCaseData> doFindTestCaseToExportFromNodes(List<Long> params) {
		// find root leafs
		List<TestCase> rootTestCases = findRootContentTestCase(params);
		// find all leafs contained in ids and contained by folders in ids
		List<Long> descendantIds = findDescendantIds(params, FIND_DESCENDANT_QUERY);

		// Case 1. Only root leafs are found
		if (descendantIds == null || descendantIds.isEmpty()) {
			List<Object[]> testCasesWithParentFolder = new ArrayList<Object[]>();
			return formatExportResult(mergeRootWithTestCasesWithParentFolder(rootTestCases, testCasesWithParentFolder));
		}

		// Case 2. More than root leafs are found
		List<Long> tcIds = findTestCaseIdsInIdList(descendantIds);
		List<Object[]> testCasesWithParentFolder = findTestCaseAndParentFolder(tcIds);

		if (!rootTestCases.isEmpty()) {
			mergeRootWithTestCasesWithParentFolder(rootTestCases, testCasesWithParentFolder);
		}

		return formatExportResult(testCasesWithParentFolder);
	}

	private List<Object[]> findTestCaseAndParentFolder(List<Long> tcIds) {
		if (!tcIds.isEmpty()) {
			SetQueryParametersCallback newCallBack1 = new SetIdsParameter(tcIds);
			return executeListNamedQuery("testCase.findTestCasesWithParentFolder", newCallBack1);

		} else {
			return Collections.emptyList();
		}
	}

	private List<Long> findTestCaseIdsInIdList(List<Long> nodesIds) {
		if (!nodesIds.isEmpty()) {

			List<TestCase> resultList = findAllByIds(nodesIds);
			return IdentifiedUtil.extractIds(resultList);

		} else {
			return Collections.emptyList();
		}
	}

	private List<Object[]> mergeRootWithTestCasesWithParentFolder(List<TestCase> rootTestCases,
			List<Object[]> testCasesWithParentFolder) {
		for (TestCase testCase : rootTestCases) {
			Object[] testCaseWithNullParentFolder = { testCase, null };
			testCasesWithParentFolder.add(testCaseWithNullParentFolder);
		}
		return testCasesWithParentFolder;
	}

	private List<TestCase> findRootContentTestCase(final List<Long> params) {
		if (!params.isEmpty()) {
			SetQueryParametersCallback newCallBack1 = new SetParamIdsParametersCallback(params);
			return executeListNamedQuery("testCase.findRootContentTestCase", newCallBack1);

		} else {
			return Collections.emptyList();

		}
	}

	private List<ExportTestCaseData> formatExportResult(List<Object[]> list) {
		if (!list.isEmpty()) {
			List<ExportTestCaseData> exportList = new ArrayList<ExportTestCaseData>();

			for (Object[] tuple : list) {
				TestCase tc = (TestCase) tuple[0];
				TestCaseFolder folder = (TestCaseFolder) tuple[1];
				ExportTestCaseData etcd = new ExportTestCaseData(tc, folder);
				exportList.add(etcd);
			}

			return exportList;
		} else {
			return Collections.emptyList();
		}
	}
	/* ----------------------------------------------------/EXPORT METHODS----------------------------------------- */

}
