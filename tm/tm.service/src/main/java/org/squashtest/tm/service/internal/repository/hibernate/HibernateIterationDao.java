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
package org.squashtest.tm.service.internal.repository.hibernate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.LongType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.MultiSorting;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.SingleToMultiSortingAdapter;
import org.squashtest.tm.core.foundation.collection.Sorting;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestPlanStatistics;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.testcase.TestCaseExecutionMode;
import org.squashtest.tm.domain.testcase.TestCaseExecutionStatus;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.service.campaign.IndexedIterationTestPlanItem;
import org.squashtest.tm.service.internal.foundation.collection.PagingUtils;
import org.squashtest.tm.service.internal.foundation.collection.SortingUtils;
import org.squashtest.tm.service.internal.repository.IterationDao;
import org.squashtest.tm.service.internal.repository.ParameterNames;

@Repository
public class HibernateIterationDao extends HibernateEntityDao<Iteration> implements IterationDao {
	private static final Logger LOGGER = LoggerFactory.getLogger(HibernateIterationDao.class);

	/*
	 * Because it is impossible to sort over the indices of ordered collection in a criteria query we must then build an
	 * hql string which will let us do that.
	 */
	private static final String HQL_INDEXED_TEST_PLAN_TEMPLATE_START = "select index(IterationTestPlanItem), " +
		"IterationTestPlanItem, " +
		"coalesce(group_concat(TestSuite.name, 'order by', TestSuite.name), '') as suitenames, " +
		"(select min(m.endDate) from IterationTestPlanItem itpi " +
		"left join itpi.referencedTestCase ctc left join ctc.milestones m where itpi.id = IterationTestPlanItem.id) as endDate "
		+ "from Iteration as Iteration inner join Iteration.testPlans as IterationTestPlanItem "
		+ "left outer join IterationTestPlanItem.referencedTestCase as TestCase "
		+ "left outer join TestCase.project as Project "
		+ "left outer join IterationTestPlanItem.referencedDataset as Dataset "
		+ "left outer join IterationTestPlanItem.user as User "
		+ "left outer join IterationTestPlanItem.testSuites as TestSuite "
		+ "where Iteration.id = :iterationId {whereClause} ";

	/*
	 * note : group by Iteration, ITPI is broken : produces `GROUP BY ITERATION.ID, null` SQL (HHH-1615)
	 * note : we have to group by Iteration.id *and* ITPI.iteration.id, otherwise a group by clause on the join table is
	 * missing. This may be a side effect of Iteration<->ITPI mapping : because of issue HHH-TBD, this is mapped as 2
	 * unidirectional associations instead of 1 bidi association.
	 */
	private static final String HQL_INDEXED_TEST_PLAN_TEMPLATE_END = "group by IterationTestPlanItem.iteration.id, IterationTestPlanItem.id, Iteration.id, index(IterationTestPlanItem) ";
	private static final String HQL_INDEXED_TEST_PLAN_TESTSUITE_FILTER = " having group_concat(TestSuite.name, 'order by', TestSuite.name) like :testsuiteFilter ";

	/*
	 * the following collection will forbid group by on certain columns
	 * because Hibernate would not call those columns as we asked it to.
	 */
	private static final Collection<String> HQL_NO_GROUP_BY_COLUMNS = Arrays.asList("suitenames", "endDate");
	/**
	 * HQL query which looks up the whole iteration test plan
	 */
	private final String hqlFullIndexedTestPlan = HQL_INDEXED_TEST_PLAN_TEMPLATE_START.replace("{whereClause}", "");
	/**
	 * HQL query which looks up the test plan assigned to a given user
	 */
	private final String hqlUserFilteredIndexedTestPlan = HQL_INDEXED_TEST_PLAN_TEMPLATE_START.replace("{whereClause}",
		"and User.login = :userLogin ");


	private static final Map<String, Map<String, String>> VALUE_DEPENDENT_FILTER_CLAUSES = new HashMap<>();
	private static final String VDFC_DEFAULT_KEY = "VDFC_DEFAULT_KEY";

	static {
		Map<String, String> modeDataMap = new HashMap<>(2);
		modeDataMap.put(TestCaseExecutionMode.MANUAL.name(),
			TestPlanFilteringHelper.HQL_INDEXED_TEST_PLAN_MODEMANUAL_FILTER);
		modeDataMap.put(VDFC_DEFAULT_KEY, TestPlanFilteringHelper.HQL_INDEXED_TEST_PLAN_MODEAUTO_FILTER);
		VALUE_DEPENDENT_FILTER_CLAUSES.put(TestPlanFilteringHelper.MODE_DATA, modeDataMap);

		Map<String, String> userData = new HashMap<>(2);
		userData.put("0", TestPlanFilteringHelper.HQL_INDEXED_TEST_PLAN_NULL_USER_FILTER);
		userData.put(VDFC_DEFAULT_KEY, TestPlanFilteringHelper.HQL_INDEXED_TEST_PLAN_USER_FILTER);
		VALUE_DEPENDENT_FILTER_CLAUSES.put(TestPlanFilteringHelper.USER_DATA, userData);

	}

	@Override
	public List<Iteration> findAllByCampaignId(long campaignId) {

		return executeListNamedQuery("iterationDao.findAllByCampaignId", new SetIdParameter(ParameterNames.CAMPAIGN_ID, campaignId));
	}

	@Override
	public List<Iteration> findAllIterationContainingTestCase(long testCaseId) {
		return executeListNamedQuery("iterationDao.findAllIterationContainingTestCase", new SetIdParameter(
			"testCaseId", testCaseId));
	}

	/*
	 * as long as the ordering of a collection is managed by @OrderColumn, but you can't explicitly reference the
	 * ordering column in the join table, initialize the collection itself is the only solution
	 *
	 * (non-Javadoc)
	 *
	 * @see org.squashtest.csp.tm.internal.repository.IterationDao#findOrderedExecutionsByIterationId(long)
	 */
	@Override
	public Iteration findAndInit(long iterationId) {
		Iteration iteration = findById(iterationId);
		Hibernate.initialize(iteration.getExecutions());
		return iteration;
	}

	@Override
	public void removeFromCampaign(Iteration iteration) {

		Campaign campaign = findCampaignByIterationId(iteration.getId());

		if (campaign == null) {
			return;
		}

		ListIterator<Iteration> iterator = campaign.getIterations().listIterator();
		while (iterator.hasNext()) {
			Iteration ts = iterator.next();
			if (ts.getId().equals(iteration.getId())) {
				iterator.remove();
				break;
			}
		}

	}

	/*
	 * Returns a Campaign if it contains an Iteration with the provided Id Returns null otherwise
	 *
	 * Note : as long as the relation between Campaign and Iteration is OneToMany, there will be either 0 either 1
	 * results, no more.
	 */
	@SuppressWarnings("unchecked")
	private Campaign findCampaignByIterationId(Long iterationId) {
		Session session = currentSession();

		List<Campaign> tcList = session.createCriteria(Campaign.class).createCriteria("iterations")
			.add(Restrictions.eq("id", iterationId)).list();

		if (!tcList.isEmpty()) {
			Campaign ca = tcList.get(0);
			Hibernate.initialize(ca.getIterations());
			return ca;
		} else {
			return null;
		}
	}

	@Override
	public List<Execution> findOrderedExecutionsByIterationId(long iterationId) {
		return executeListNamedQuery("iteration.findAllExecutions", new SetIdParameter(ParameterNames.ITERATION_ID, iterationId));
	}

	@Override
	public List<Execution> findOrderedExecutionsByIterationAndTestPlan(final long iterationId, final long testPlanId) {
		return executeListNamedQuery("iteration.findAllExecutionsByTestPlan", new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setParameter(ParameterNames.ITERATION_ID, iterationId);
				query.setParameter("testPlanId", testPlanId);

			}
		});
	}

	private SetQueryParametersCallback idParameter(final long id) {
		return new SetIdParameter(ParameterNames.ITERATION_ID, id);
	}

	private SetQueryParametersCallback idAndLoginParameter(final long id, final String login) {

		return new SetQueryParametersCallback() {
			@Override
			public void setQueryParameters(Query query) {
				query.setParameter(ParameterNames.ITERATION_ID, id);
				query.setParameter("userLogin", login);
			}
		};
	}

	@Override
	public List<TestSuite> findAllTestSuites(final long iterationId) {
		SetQueryParametersCallback callback = idParameter(iterationId);
		return executeListNamedQuery("iteration.findAllTestSuites", callback);
	}

	@Override
	public void persistIterationAndTestPlan(Iteration iteration) {
		persistTestPlan(iteration);
		persist(iteration);

	}

	private void persistTestPlan(Iteration iteration) {
		for (IterationTestPlanItem iterationTestPlanItem : iteration.getTestPlans()) {
			currentSession().persist(iterationTestPlanItem);
		}
	}

	@Override
	public List<Execution> findAllExecutionByIterationId(long iterationId) {
		SetQueryParametersCallback callback = idParameter(iterationId);
		return executeListNamedQuery("iteration.findAllExecutions", callback);
	}

	@Override
	@SuppressWarnings("unchecked")
	public TestPlanStatistics getIterationStatistics(long iterationId) {

		Query q = currentSession().getNamedQuery("iteration.countStatuses");
		q.setParameter("iterationId", iterationId);
		List<Object[]> result = q.list();

		return new TestPlanStatistics(result);
	}


	@Override
	public long countRunningOrDoneExecutions(long iterationId) {
		return (Long) executeEntityNamedQuery("iteration.countRunningOrDoneExecutions", idParameter(iterationId));
	}

	// **************************** TEST PLAN ******************************

	@Override
	public List<IterationTestPlanItem> findTestPlan(long iterationId, PagingAndMultiSorting sorting,
													Filtering filtering, ColumnFiltering columnFiltering) {

		// get the data
		List<Object[]> tuples = findIndexedTestPlanData(iterationId, sorting, filtering, columnFiltering);

		// filter them
		List<IterationTestPlanItem> items = new ArrayList<>(tuples.size());

		for (Object[] tuple : tuples) {
			IterationTestPlanItem itpi = (IterationTestPlanItem) tuple[1];
			items.add(itpi);
		}

		return items;
	}

	@Override
	public List<IndexedIterationTestPlanItem> findIndexedTestPlan(long iterationId, PagingAndSorting sorting,
																  Filtering filtering, ColumnFiltering columnFiltering) {
		return findIndexedTestPlan(iterationId, new SingleToMultiSortingAdapter(sorting), filtering, columnFiltering);
	}

	@Override
	public List<IndexedIterationTestPlanItem> findIndexedTestPlan(final long iterationId,
																  PagingAndMultiSorting sorting, Filtering filtering, ColumnFiltering columnFiltering) {

		/* get the data */
		List<Object[]> tuples = findIndexedTestPlanData(iterationId, sorting, filtering, columnFiltering);

		/* format them */
		List<IndexedIterationTestPlanItem> indexedItems = new ArrayList<>(tuples.size());

		for (Object[] tuple : tuples) {
			Integer index = (Integer) tuple[0];
			IterationTestPlanItem itpi = (IterationTestPlanItem) tuple[1];
			indexedItems.add(new IndexedIterationTestPlanItem(index, itpi));
		}

		return indexedItems;

	}


	private StringBuilder buildTestPlanQueryBody(Filtering filtering, ColumnFiltering columnFiltering,
												 MultiSorting multiSorting) {
		StringBuilder hqlBuilder = new StringBuilder();

		String hql = filtering.isDefined() ? hqlUserFilteredIndexedTestPlan : hqlFullIndexedTestPlan;
		hqlBuilder.append(hql);

		// additional where clauses
		TestPlanFilteringHelper.appendFilteringRestrictions(hqlBuilder, columnFiltering);

		for (Entry<String, Map<String, String>> valueDependantFilterClause : VALUE_DEPENDENT_FILTER_CLAUSES.entrySet()) {
			String filterName = valueDependantFilterClause.getKey();
			Map<String, String> clausesByValues = valueDependantFilterClause.getValue();
			if (columnFiltering.hasFilter(filterName)) {
				String filterValue = columnFiltering.getFilter(filterName);
				String clause = clausesByValues.get(filterValue);
				if (clause == null) {
					clause = clausesByValues.get(VDFC_DEFAULT_KEY);
				}
				hqlBuilder.append(clause);
			}
		}

		// group by
		hqlBuilder.append(HQL_INDEXED_TEST_PLAN_TEMPLATE_END);

		// Strict SQL (postgres) : sort colums have to appear in group by clause.
		for (Sorting sorting : multiSorting.getSortings()) {
			if (!HQL_NO_GROUP_BY_COLUMNS.contains(sorting.getSortedAttribute())) {
				hqlBuilder.append(", ").append(sorting.getSortedAttribute());
			}
		}

		if (columnFiltering.hasFilter(TestPlanFilteringHelper.TESTSUITE_DATA)) {
			hqlBuilder.append(HQL_INDEXED_TEST_PLAN_TESTSUITE_FILTER);
		}

		return hqlBuilder;
	}

	private String buildIndexedTestPlanQueryString(PagingAndMultiSorting sorting, Filtering filtering,
												   ColumnFiltering columnFiltering) {

		StringBuilder hqlbuilder = buildTestPlanQueryBody(filtering, columnFiltering, sorting);

		// tune the sorting to make hql happy
		LevelImplementorSorter wrapper = new LevelImplementorSorter(sorting);
		wrapper.map("TestCase.importance", TestCaseImportance.class);
		wrapper.map("IterationTestPlanItem.executionStatus", ExecutionStatus.class);

		return SortingUtils.addOrder(hqlbuilder.toString(), wrapper);
	}

	// this method will use one or another strategy to fetch its data depending on what the user is requesting.
	@SuppressWarnings("unchecked")
	private List<Object[]> findIndexedTestPlanData(final long iterationId, PagingAndMultiSorting sorting,
												   Filtering filtering, ColumnFiltering columnFiltering) {

		String queryString = buildIndexedTestPlanQueryString(sorting, filtering, columnFiltering);

		Query query = assignParameterValuesToTestPlanQuery(queryString, iterationId, filtering, columnFiltering);

		PagingUtils.addPaging(query, sorting);

		return query.list();
	}

	@Override
	public long countTestPlans(Long iterationId, Filtering filtering) {
		if (!filtering.isDefined()) {
			return (Long) executeEntityNamedQuery("iteration.countTestPlans", idParameter(iterationId));
		} else {
			return (Long) executeEntityNamedQuery("iteration.countTestPlansFiltered",
				idAndLoginParameter(iterationId, filtering.getFilter()));
		}
	}

	private Query assignParameterValuesToTestPlanQuery(String queryString, Long iterationId, Filtering filtering,
													   ColumnFiltering columnFiltering) {
		Query query = currentSession().createQuery(queryString);
		query.setParameter(ParameterNames.ITERATION_ID, iterationId);
		TestPlanFilteringHelper.setFilters(query, filtering, columnFiltering);

		return query;
	}

	@Override
	public long countTestPlans(Long iterationId, Filtering filtering, ColumnFiltering columnFiltering) {

		StringBuilder hqlbuilder = buildTestPlanQueryBody(filtering, columnFiltering, new MultiSorting() {

			@Override
			public List<Sorting> getSortings() {
				return Collections.emptyList();
			}
		});

		Query query = assignParameterValuesToTestPlanQuery(hqlbuilder.toString(), iterationId, filtering,
			columnFiltering);

		return query.list().size();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TestCaseExecutionStatus> findExecStatusForIterationsAndTestCases(
		List<Long> testCasesIds, List<Long> iterationsIds) {
		if (testCasesIds.isEmpty()) {
			return Collections.emptyList();
		}
		Query q = currentSession().getNamedQuery("iteration.findITPIByTestCaseGroupByStatus");
		q.setParameterList("testCasesIds", testCasesIds, LongType.INSTANCE);
		q.setParameterList("iterationsIds", iterationsIds, LongType.INSTANCE);
		List<TestCaseExecutionStatus> formatedResult = new ArrayList<>();
		List<Object[]> results = q.list();
		for (Object[] result : results) {
			formatedResult.add(new TestCaseExecutionStatus((ExecutionStatus) result[0], (Long) result[1]));
		}
		return formatedResult;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> findVerifiedTcIdsInIterations(List<Long> testCasesIds,
													List<Long> iterationsIds) {
		if (testCasesIds.isEmpty()) {
			return Collections.emptyList();
		}
		Query q = currentSession().getNamedQuery("iteration.findVerifiedTcIdsInIterations");
		q.setParameterList("testCasesIds", testCasesIds, LongType.INSTANCE);
		q.setParameterList("iterationsIds", iterationsIds, LongType.INSTANCE);
		return q.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> findVerifiedTcIdsInIterationsWithExecution(
		List<Long> tcIds, List<Long> iterationsIds) {
		if (tcIds.isEmpty()) {
			return Collections.emptyList();
		}
		Query q = currentSession().getNamedQuery("iteration.findVerifiedAndExecutedTcIdsInIterations");
		q.setParameterList("testCasesIds", tcIds, LongType.INSTANCE);
		q.setParameterList("iterationsIds", iterationsIds, LongType.INSTANCE);
		return q.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public MultiMap findVerifiedITPI(List<Long> tcIds, List<Long> iterationsIds) {
		if (tcIds.isEmpty()) {
			return new MultiValueMap();
		}
		Query q = currentSession().getNamedQuery("iteration.findITPIByTestCaseGroupByStatus");
		q.setParameterList("testCasesIds", tcIds, LongType.INSTANCE);
		q.setParameterList("iterationsIds", iterationsIds, LongType.INSTANCE);
		List<Object[]> itpis = q.list();
		MultiMap result = new MultiValueMap();
		for (Object[] itpi : itpis) {
			TestCaseExecutionStatus tcStatus = new TestCaseExecutionStatus((ExecutionStatus) itpi[0], (Long) itpi[1]);
			result.put(tcStatus.getTestCaseId(), tcStatus);
		}
		return result;
	}

}
