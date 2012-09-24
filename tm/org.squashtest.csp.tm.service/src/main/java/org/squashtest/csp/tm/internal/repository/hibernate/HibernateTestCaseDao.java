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
import org.squashtest.csp.core.infrastructure.hibernate.PagingUtils;
import org.squashtest.csp.core.infrastructure.hibernate.SortingUtils;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.requirement.RequirementSearchCriteria;
import org.squashtest.csp.tm.domain.testcase.ActionTestStep;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseImportance;
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

	private static class SetIdParameter implements SetQueryParametersCallback {
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
			SetQueryParametersCallback setParams = new SetQueryParametersCallback() {
	
				@Override
				public void setQueryParameters(Query query) {
					query.setParameterList("testCasesIds", testCasesIds);
				}
			};
			return executeListNamedQuery("testCase.findAllByIdListOrderedByName", setParams);
		}
	
	@Override
	public List<String> findNamesInFolderStartingWith(final long folderId, final String nameStart) {
		SetQueryParametersCallback newCallBack1 = new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setParameter("containerId", folderId);
				query.setParameter("nameStart", nameStart + "%");
			}
		};
		return executeListNamedQuery("testCase.findNamesInFolderStartingWith", newCallBack1);
	}

	@Override
	public List<String> findNamesInLibraryStartingWith(final long libraryId, final String nameStart) {
		SetQueryParametersCallback newCallBack1 = new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setParameter("containerId", libraryId);
				query.setParameter("nameStart", nameStart + "%");
			}
		};
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

		SetQueryParametersCallback callback = new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {

				query.setParameter("testCaseId", testCaseId);
				query.setParameter("firstIndex", firstIndex);
				query.setParameter("lastIndex", lastIndex);

			}

		};

		return executeListNamedQuery("testCase.findAllStepsByIdFiltered", callback);
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

		SetQueryParametersCallback secondQueryCallback = new SetQueryParametersCallback() {
			@Override
			public void setQueryParameters(Query query) {
				query.setParameterList("nonCalledIds", nonCalledIds, new LongType());
				query.setReadOnly(true);
			}
		};

		List<Object[]> nonCalledDetails = executeListNamedQuery("testCase.findTestCasesHavingNoCallerDetails",
				secondQueryCallback);

		// now we can return
		calledDetails.addAll(nonCalledDetails);
		return calledDetails;

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
		return (Long) executeEntityNamedQuery("testCase.countByVerifiedRequirementVersion",
				new SetQueryParametersCallback() {
					@Override
					public void setQueryParameters(Query query) {
						query.setLong("verifiedId", verifiedId);

					}
				});
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
	public List<TestCaseLibraryNode> findBySearchCriteria(TestCaseSearchCriteria criteria){
		Criteria hCriteria;
		
		if (usesImportance(criteria)){
			hCriteria = testCaseRootedCriteria(criteria);
		}else{
			hCriteria = tcNodeRootedCriteria(criteria); 
		}
		
		 
		if (criteria.isGroupByProject()){
			hCriteria.addOrder(Order.asc(PROJECT));
		}
		
		
		if (StringUtils.isNotBlank(criteria.getName())){
			hCriteria.add(Restrictions.ilike("name", criteria.getName(), MatchMode.ANYWHERE));
		}

		hCriteria.addOrder(Order.asc("name"));
		
		return hCriteria.list();
		
											
	}
	
	private boolean usesImportance(TestCaseSearchCriteria criteria){
		return ((!criteria.getImportanceFilterSet().isEmpty())&&(criteria.getImportanceFilterSet().size() != TestCaseImportance.values().length));
	}
	
	private Criteria testCaseRootedCriteria(TestCaseSearchCriteria criteria){
		Criteria crit = currentSession().createCriteria(TestCase.class);
		if(!criteria.getImportanceFilterSet().isEmpty()){
			crit.add(Restrictions.in("importance", criteria.getImportanceFilterSet()));
		}
		return crit;
	}
	
	private Criteria tcNodeRootedCriteria(TestCaseSearchCriteria criteria){
		return currentSession().createCriteria(TestCaseLibraryNode.class);
	}

	@Override
	public List<Execution> findAllExecutionByTestCase(Long tcId) {
		SetQueryParametersCallback callback = idParameter(tcId);
		return executeListNamedQuery("testCase.findAllExecutions", callback);
	}
}
