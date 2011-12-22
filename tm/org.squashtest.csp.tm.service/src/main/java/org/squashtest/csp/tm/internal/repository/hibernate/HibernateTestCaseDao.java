/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.requirement.RequirementCriticality;
import org.squashtest.csp.tm.domain.requirement.RequirementSearchCriteria;
import org.squashtest.csp.tm.domain.testcase.ActionTestStep;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.csp.tm.domain.testcase.TestStep;
import org.squashtest.csp.tm.infrastructure.filter.CollectionFilter;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.internal.repository.TestCaseDao;

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

	/***
	 * Names for parameters for the search of test case by requirement
	 */
	private static final String BY_REQ_REFERENCE_PARAM_NAME = "rReference";
	private static final String BY_REQ_NAME_PARAM_NAME = "rName";
	private static final String BY_REQ_CRITICALITY_PARAM_NAME = "rCriticality";

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

	@SuppressWarnings("unchecked")
	@Override
	public List<Requirement> findAllDirectlyVerifiedRequirementsByIdFiltered(final long testCaseId,
			final CollectionSorting filter) {

		Session session = currentSession();

		String sortedAttribute = filter.getSortedAttribute();
		String order = filter.getSortingOrder();

		Criteria crit = session.createCriteria(TestCase.class).add(Restrictions.eq("id", testCaseId))
				.createAlias("verifiedRequirements", "Requirement").createAlias("Requirement.project", "Project")
				.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);

		/* add ordering */
		if (sortedAttribute != null) {
			if (order.equals("asc")) {
				crit.addOrder(Order.asc(sortedAttribute).ignoreCase());
			} else {
				crit.addOrder(Order.desc(sortedAttribute).ignoreCase());
			}
		}

		/* result range */
		crit.setFirstResult(filter.getFirstItemIndex());
		crit.setMaxResults(filter.getMaxNumberOfItems());

		List<Map<String, ?>> rawResult = crit.list();

		List<Requirement> reqs = new ArrayList<Requirement>();
		ListIterator<Map<String, ?>> iter = rawResult.listIterator();
		while (iter.hasNext()) {
			Map<String, ?> map = iter.next();
			reqs.add((Requirement) map.get("Requirement"));
		}

		return reqs;

	}

	@Override
	public long countVerifiedRequirementsById(long testCaseId) {
		return (Long) executeEntityNamedQuery("testCase.countVerifiedRequirementsById", idParameter(testCaseId));
	}

	@Override
	public List<TestCase> findAllByIdList(final List<Long> testCasesIds) {
		SetQueryParametersCallback setParams = new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setParameterList("testCasesIds", testCasesIds);
			}
		};
		return executeListNamedQuery("testCase.findAllByIdList", setParams);
	}

	@Override
	public List<TestCase> findAllByIdListNonOrdered(final List<Long> testCasesIds) {
		SetQueryParametersCallback setParams = new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setParameterList("testCasesIds", testCasesIds);
			}
		};
		return executeListNamedQuery("testCase.findAllByIdListNonOrdered", setParams);
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
	public List<TestStep> findAllStepsByIdFiltered(final long testCaseId, final CollectionFilter filter) {
		final int firstIndex = filter.getFirstItemIndex();
		final int lastIndex = filter.getFirstItemIndex() + filter.getMaxNumberOfItems() - 1;

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
				.createAlias("testCaseLibraryNode.project", "project")
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
			query.setMaxResults(sorting.getMaxNumberOfItems());
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

		List<Object[]> calledDetails =  executeListNamedQuery("testCase.findTestCasesHavingCallerDetails", firstQueryCallback);

		//now we must fetch the same informations for those who aren't, so we can emulate the right outer join we need
		//first, get the ids that aren't part of the result. If we already got them all, no need for additional queries.
		List<Long> calledIds = getCalledDetailsIds(calledDetails);

		if (calledIds.size()==testCaseIds.size()){
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

		List<Object[]> nonCalledDetails = executeListNamedQuery("testCase.findTestCasesHavingNoCallerDetails", secondQueryCallback);

		//now we can return
		calledDetails.addAll(nonCalledDetails);
		return calledDetails;


	}

	private List<Long> getCalledDetailsIds(List<Object[]> calledDetails) {
		List<Long> calledIds = new LinkedList<Long>();

		for (Object[] called : calledDetails) {
			Object item = called[2];

			if (! calledIds.contains(item)){
				calledIds.add((Long)item);
			}
		}
		return calledIds;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TestCase> findAllByRequirement(RequirementSearchCriteria criteria, boolean isProjectOrdered) {

		Query query = currentSession().createQuery(generateByRequirementQuery(criteria, isProjectOrdered));
		// set parameters
		if (criteria.getName() != null) {
			query.setParameter(BY_REQ_NAME_PARAM_NAME, "%" + criteria.getName() + "%");
		}
		if (criteria.getReference() != null) {
			query.setParameter(BY_REQ_REFERENCE_PARAM_NAME, "%" + criteria.getReference() + "%");
		}

		if (!criteria.getCriticalities().isEmpty()) {
			int number = 1;
			for (RequirementCriticality c : criteria.getCriticalities()) {
				query.setParameter(BY_REQ_CRITICALITY_PARAM_NAME + number, c);
				number++;
			}
		}

		return query.list();
	}

	/***
	 * Method which generate the hql query with the specified parameters
	 *
	 * @param criteria
	 *            the query criteria
	 * @param isProjectOrdered
	 *            if true, results are ordered by project
	 * @return the query (String)
	 */
	private String generateByRequirementQuery(RequirementSearchCriteria criteria, boolean isProjectOrdered) {
		// Compose the request
		StringBuilder requirementHql = new StringBuilder(
				"select distinct tc from Requirement r join r.verifyingTestCases tc fetch all properties where r.id in (select req.id from Requirement req where ");

		boolean isPreviousParam = false;
		if (criteria.getName() != null) {
			requirementHql.append(" req.name like :");
			requirementHql.append(BY_REQ_NAME_PARAM_NAME);
			isPreviousParam = true;
		}
		if (criteria.getReference() != null) {
			if (isPreviousParam) {
				requirementHql.append(" and");
			} else {
				isPreviousParam = true;
			}
			requirementHql.append(" req.reference like :");
			requirementHql.append(BY_REQ_REFERENCE_PARAM_NAME);
		}
		if (!criteria.getCriticalities().isEmpty()) {
			if (isPreviousParam) {
				requirementHql.append(" and");
			}
			requirementHql.append(" req.criticality in (");
			int total = criteria.getCriticalities().size();
			for (int number = 1; number <= total; number++) {
				requirementHql.append(":" + BY_REQ_CRITICALITY_PARAM_NAME + number);
				if (number < total) {
					requirementHql.append(", ");
				}
			}
			requirementHql.append(")");
		}
		// end of the request
		requirementHql.append(")");

		// order by
		if (isProjectOrdered) {
			requirementHql.append(" order by tc.project");
		}
		return requirementHql.toString();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> findCalledTestCaseOfCallSteps(List<Long> testStepsIds) {
		Query query = currentSession().getNamedQuery("testCase.findCalledTestCaseOfCallSteps");
		query.setParameterList("testStepsIds", testStepsIds);
		return query.list();
	}



}
