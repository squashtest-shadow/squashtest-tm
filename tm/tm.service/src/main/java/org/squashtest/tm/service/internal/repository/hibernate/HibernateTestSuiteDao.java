/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.type.DateType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.SingleToMultiSortingAdapter;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestPlanStatistics;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.service.campaign.IndexedIterationTestPlanItem;
import org.squashtest.tm.service.internal.foundation.collection.PagingUtils;
import org.squashtest.tm.service.internal.foundation.collection.SortingUtils;
import org.squashtest.tm.service.internal.repository.CustomTestSuiteDao;

/*
 * todo : make it a dynamic call
 *
 */
@Repository("CustomTestSuiteDao")
public class HibernateTestSuiteDao extends HibernateEntityDao<TestSuite> implements CustomTestSuiteDao {

	private static final String PROJECT_FILTER = "projectFilter";
	private static final String REFERENCE_FILTER = "referenceFilter";
	private static final String TESTCASE_FILTER = "testcaseFilter";	
	private static final String WEIGHT_FILTER = "weightFilter";
	private static final String DATASET_FILTER = "datasetFilter";
	private static final String STATUS_FILTER = "statusFilter";
	private static final String USER_FILTER = "userFilter";
	private static final String START_DATE = "startDate";
	private static final String END_DATE = "endDate";
	private static final String DATE_FORMAT = "dd/MM/yyyy";
	private static final String MODE_FILTER = "modeFilter";
	
	private static final String PROJECT_DATA = "project-name";
	private static final String REFERENCE_DATA = "reference";
	private static final String TESTCASE_DATA = "tc-name";	
	private static final String WEIGHT_DATA= "importance";
	private static final String DATASET_DATA = "dataset";
	private static final String STATUS_DATA = "status";
	private static final String USER_DATA = "assignee-login";
	private static final String MODE_DATA = "exec-mode";
	private static final String LASTEXEC_DATA = "last-exec-on";
	/*
	 * Because it is impossible to sort over the indices of ordered collection in a criteria query we must then build an
	 * hql string which will let us do that.
	 */
	private static final String HQL_INDEXED_TEST_PLAN = "select index(IterationTestPlanItem), IterationTestPlanItem "
			+ "from TestSuite as TestSuite inner join TestSuite.testPlan as IterationTestPlanItem "
			+ "left outer join IterationTestPlanItem.referencedTestCase as TestCase "
			+ "left outer join TestCase.project as Project "
			+ "left outer join IterationTestPlanItem.referencedDataset as Dataset "
			+ "left outer join IterationTestPlanItem.user as User " + "where TestSuite.id = :suiteId ";

	private static final String HQL_INDEXED_TEST_PLAN_PROJECT_FILTER =
			"and Project.name like :projectFilter ";

	private static final String HQL_INDEXED_TEST_PLAN_REFERENCE_FILTER =
			"and TestCase.reference like :referenceFilter ";
	
	private static final String HQL_INDEXED_TEST_PLAN_TESTCASE_FILTER =
			"and TestCase.name like :testcaseFilter ";
	
	private static final String HQL_INDEXED_TEST_PLAN_WEIGHT_FILTER =
			"and TestCase.importance = :weightFilter ";
	
	private static final String HQL_INDEXED_TEST_PLAN_DATASET_FILTER =
			"and Dataset like :datasetFilter ";

	private static final String HQL_INDEXED_TEST_PLAN_STATUS_FILTER =
			"and IterationTestPlanItem.executionStatus = :statusFilter ";

	private static final String HQL_INDEXED_TEST_PLAN_MODE_FILTER =
			"and TestCase.executionMode = :modeFilter ";
	
	private static final String HQL_INDEXED_TEST_PLAN_USER_FILTER =
			"and IterationTestPlanItem.user.id = :userFilter ";

	private static final String HQL_INDEXED_TEST_PLAN_NULL_USER_FILTER =
			"and IterationTestPlanItem.user is null ";
	
	private static final String HQL_INDEXED_TEST_PLAN_EXECUTIONDATE_FILTER =
			"and IterationTestPlanItem.lastExecutedOn between :startDate and :endDate ";
	
	
	@Override
	public List<TestSuite> findAllByIterationId(final long iterationId) {

		return executeListNamedQuery("testSuite.findAllByIterationId", new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setParameter("1", iterationId);
			}

		});
	}

	@Override
	public List<IterationTestPlanItem> findLaunchableTestPlan(final long testSuiteId) {
		return executeListNamedQuery("testSuite.findLaunchableTestPlan", new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setParameter("1", testSuiteId);
				query.setParameter("2", testSuiteId);
			}

		});
	}

	public List<IterationTestPlanItem> findTestPlanPaged(final long testSuiteId, final Paging paging) {
		SetQueryParametersCallback callback = new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {

				query.setParameter("id", testSuiteId);
				query.setParameter("id2", testSuiteId);
				query.setFirstResult(paging.getFirstItemIndex());
				query.setMaxResults(paging.getPageSize());
			}

		};

		return executeListNamedQuery("TestSuite.findAllTestPlanItemsPaged", callback);
	}

	private Long countTestPlanItems(long testSuiteId) {
		return (Long) executeEntityNamedQuery("TestSuite.countTestPlanItems", idParameter(testSuiteId));
	}

	private Long countTestPlanItems(long testSuiteId, String userLogin) {
		return (Long) executeEntityNamedQuery("TestSuite.countTestPlanItemsForUsers",
				idLoginParameter(testSuiteId, userLogin));
	}

	@Override
	public TestPlanStatistics getTestSuiteStatistics(final long testSuiteId) {
		// Add Total number of TestCases
		Integer nbTestPlans = countTestPlanItems(testSuiteId).intValue();

		// Add number of testCase for each ExecutionStatus
		SetQueryParametersCallback newCallBack = new IdId2ParameterCallback(testSuiteId);
		List<Object[]> result = executeListNamedQuery("testSuite.countStatuses", newCallBack);

		return fillTestPlanStatistics(nbTestPlans, result);
	}


	@Override
	public TestPlanStatistics getTestSuiteStatistics(long testSuiteId, String userLogin) {
		// Add Total number of TestCases
		Integer nbTestPlans = countTestPlanItems(testSuiteId, userLogin).intValue();

		// Add number of testCase for each ExecutionStatus
		SetQueryParametersCallback newCallBack = new IdId2LoginParameterCallback(testSuiteId, userLogin);
		List<Object[]> result = executeListNamedQuery("testSuite.countStatusesForUser", newCallBack);
		
		return fillTestPlanStatistics(nbTestPlans, result);
	}

	private TestPlanStatistics fillTestPlanStatistics(Integer nbTestPlans, List<Object[]> queryResult) {
		Map<String, Integer> statusMap = new HashMap<String, Integer>();
		statusMap.put(TestPlanStatistics.TOTAL_NUMBER_OF_TEST_CASE_KEY, nbTestPlans);
		for (Object[] objTab : queryResult) {
			statusMap.put(((ExecutionStatus) objTab[0]).name(), ((Long) objTab[1]).intValue());
		}
		return new TestPlanStatistics(statusMap);
	}
	

	private static class IdId2ParameterCallback implements SetQueryParametersCallback {
		private long id;

		public IdId2ParameterCallback(long id) {
			this.id = id;
		}

		@Override
		public void setQueryParameters(Query query) {
			query.setLong("id", id);
			query.setLong("id2", id);
		}
	}

	private static class IdId2LoginParameterCallback implements SetQueryParametersCallback {
		private long id;
		private String login;

		public IdId2LoginParameterCallback(long id, String login) {
			this.id = id;
			this.login = login;
		}

		@Override
		public void setQueryParameters(Query query) {
			query.setLong("id", id);
			query.setLong("id2", id);
			query.setParameter("login", login);
		}
	}

	@Override
	public List<IterationTestPlanItem> findTestPlanPartition(final long testSuiteId, final List<Long> testPlanItemIds) {

		SetQueryParametersCallback callback = new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setParameter("suiteId", testSuiteId);
				query.setParameterList("itemIds", testPlanItemIds, LongType.INSTANCE);
			}
		};

		return executeListNamedQuery("testSuite.findTestPlanPartition", callback);
	}

	private SetQueryParametersCallback idParameter(final long id) {
		SetQueryParametersCallback newCallBack = new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setLong("1", id);
			}
		};
		return newCallBack;
	}

	private SetQueryParametersCallback idLoginParameter(final long id, final String login) {
		SetQueryParametersCallback newCallBack = new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setLong("id", id);
				query.setParameter("login", login);
			}
		};
		return newCallBack;
	}

	private SetQueryParametersCallback IdAndLoginParameter(final long id, final String login) {

		return new SetQueryParametersCallback() {
			@Override
			public void setQueryParameters(Query query) {
				query.setParameter("suiteId", id);
				query.setParameter("userLogin", login);
			}
		};
	}

	@Override
	public List<Execution> findAllExecutionByTestSuite(long testSuiteId) {
		SetQueryParametersCallback callback = idParameter(testSuiteId);
		return executeListNamedQuery("testSuite.findAllExecutions", callback);
	}

	@Override
	public List<IterationTestPlanItem> findAllTestPlanItemsPaged(final long testSuiteId, Paging paging) {

		final int firstIndex = paging.getFirstItemIndex();
		final int lastIndex = paging.getFirstItemIndex() + paging.getPageSize() - 1;

		SetQueryParametersCallback callback = new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {

				query.setParameter("testSuiteId", testSuiteId);
				query.setParameter("firstIndex", firstIndex);
				query.setParameter("lastIndex", lastIndex);

			}

		};

		return executeListNamedQuery("testSuite.findTestPlanFiltered", callback);

	}

	@Override
	public List<IterationTestPlanItem> findTestPlan(long suiteId, PagingAndMultiSorting sorting, Filtering filtering, ColumnFiltering columnFiltering) {
		List<Object[]> tuples = _findIndexedTestPlan(suiteId, sorting, filtering, columnFiltering);
		return buildItems(tuples);
	}

	@Override
	public List<IndexedIterationTestPlanItem> findIndexedTestPlan(long suiteId, PagingAndSorting sorting,
			Filtering filtering, ColumnFiltering columnFiltering) {

		return findIndexedTestPlan(suiteId, new SingleToMultiSortingAdapter(sorting), filtering, columnFiltering);
	}

	@Override
	public List<IndexedIterationTestPlanItem> findIndexedTestPlan(final long suiteId, PagingAndMultiSorting sorting,
			Filtering filtering, ColumnFiltering columnFiltering) {

		List<Object[]> tuples = _findIndexedTestPlan(suiteId, sorting, filtering, columnFiltering);
		return buildIndexedItems(tuples);

	}

	private StringBuilder buildTestPlanQueryBody(Filtering filtering, ColumnFiltering columnFiltering){
		
		StringBuilder hqlbuilder = new StringBuilder(HQL_INDEXED_TEST_PLAN);

		// check if we want to filter on the user login
		if (filtering.isDefined()) {
			hqlbuilder.append("and User.login = :userLogin ");
		}

		if(columnFiltering.hasFilter(PROJECT_DATA)){
			hqlbuilder.append(HQL_INDEXED_TEST_PLAN_PROJECT_FILTER);
		}
		if(columnFiltering.hasFilter(MODE_DATA)){
			hqlbuilder.append(HQL_INDEXED_TEST_PLAN_MODE_FILTER);
		}
		if(columnFiltering.hasFilter(REFERENCE_DATA)){
			hqlbuilder.append(HQL_INDEXED_TEST_PLAN_REFERENCE_FILTER);
		}
		if(columnFiltering.hasFilter(TESTCASE_DATA)){
			hqlbuilder.append(HQL_INDEXED_TEST_PLAN_TESTCASE_FILTER);
		}
		if(columnFiltering.hasFilter(WEIGHT_DATA)){
			hqlbuilder.append(HQL_INDEXED_TEST_PLAN_WEIGHT_FILTER);
		}
		if(columnFiltering.hasFilter(DATASET_DATA)){
			hqlbuilder.append(HQL_INDEXED_TEST_PLAN_DATASET_FILTER);
		}
		if(columnFiltering.hasFilter(STATUS_DATA)){
			hqlbuilder.append(HQL_INDEXED_TEST_PLAN_STATUS_FILTER);
		}
		if(columnFiltering.hasFilter(USER_DATA)){
			if("0".equals(columnFiltering.getFilter(USER_DATA))){
				hqlbuilder.append(HQL_INDEXED_TEST_PLAN_NULL_USER_FILTER);
			} else {
				hqlbuilder.append(HQL_INDEXED_TEST_PLAN_USER_FILTER);
			}
		}
		if(columnFiltering.hasFilter(LASTEXEC_DATA)){
			hqlbuilder.append(HQL_INDEXED_TEST_PLAN_EXECUTIONDATE_FILTER);				
		}
		
		return hqlbuilder;
	}
	
	private Query assignParameterValuesToTestPlanQuery(String queryString, Long suiteId, Filtering filtering, ColumnFiltering columnFiltering){
		
		Query query = currentSession().createQuery(queryString);

		query.setParameter("suiteId", suiteId, LongType.INSTANCE);

		if (filtering.isDefined()) {
			query.setParameter("userLogin", filtering.getFilter(), StringType.INSTANCE);
		}

		if(columnFiltering.hasFilter(PROJECT_DATA)){
			query.setParameter(PROJECT_FILTER, "%"+columnFiltering.getFilter(PROJECT_DATA)+"%", StringType.INSTANCE);
		}
		if(columnFiltering.hasFilter(MODE_DATA)){
			query.setParameter(MODE_FILTER, "%"+columnFiltering.getFilter(MODE_DATA)+"%", StringType.INSTANCE);
		}
		if(columnFiltering.hasFilter(REFERENCE_DATA)){
			query.setParameter(REFERENCE_FILTER, "%"+columnFiltering.getFilter(REFERENCE_DATA)+"%", StringType.INSTANCE);
		} 
		if(columnFiltering.hasFilter(TESTCASE_DATA)){
			query.setParameter(TESTCASE_FILTER, "%"+columnFiltering.getFilter(TESTCASE_DATA)+"%", StringType.INSTANCE);
		}
		if(columnFiltering.hasFilter(WEIGHT_DATA)){
			query.setParameter(WEIGHT_FILTER, columnFiltering.getFilter(WEIGHT_DATA), StringType.INSTANCE);
		}
		if(columnFiltering.hasFilter(DATASET_DATA)){
			query.setParameter(DATASET_FILTER, "%"+columnFiltering.getFilter(DATASET_DATA)+"%", StringType.INSTANCE);
		}
		if(columnFiltering.hasFilter(STATUS_DATA)){
			query.setParameter(STATUS_FILTER, columnFiltering.getFilter(STATUS_DATA), StringType.INSTANCE);
		}
		if(columnFiltering.hasFilter(USER_DATA) && !"0".equals(columnFiltering.getFilter(USER_DATA))){
			query.setParameter(USER_FILTER, Long.parseLong(columnFiltering.getFilter(USER_DATA)), LongType.INSTANCE);
		}
		if(columnFiltering.hasFilter(LASTEXEC_DATA)){
			String dates = columnFiltering.getFilter(LASTEXEC_DATA);
			if(dates.contains("-")){
				String[] dateArray = dates.split("-");
				Date startDate;
				try {
					startDate = new SimpleDateFormat(DATE_FORMAT).parse(dateArray[0].trim());
					Date endDate = new SimpleDateFormat(DATE_FORMAT).parse(dateArray[1].trim());
					query.setParameter(START_DATE, startDate, DateType.INSTANCE);
					query.setParameter(END_DATE, nextDay(endDate), DateType.INSTANCE);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else {
				Date date;
				try {
					date = new SimpleDateFormat(DATE_FORMAT).parse(dates.trim());
					query.setParameter(START_DATE, date, DateType.INSTANCE);
					query.setParameter(END_DATE, nextDay(date), DateType.INSTANCE);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}
		
		return query;
	}
	
	private List<Object[]> _findIndexedTestPlan(final long suiteId, PagingAndMultiSorting sorting, Filtering filtering, ColumnFiltering columnFiltering) {
		
		StringBuilder hqlbuilder = buildTestPlanQueryBody(filtering, columnFiltering);
				
		// tune the sorting to make hql happy
		LevelImplementorSorter wrapper = new LevelImplementorSorter(sorting);
		wrapper.map("TestCase.importance", TestCaseImportance.class);
		wrapper.map("IterationTestPlanItem.executionStatus", ExecutionStatus.class);

		SortingUtils.addOrder(hqlbuilder, wrapper);

		Query query = assignParameterValuesToTestPlanQuery(hqlbuilder.toString(), suiteId, filtering, columnFiltering);
		
		PagingUtils.addPaging(query, sorting);

		return query.list();
	}

	private Date nextDay(Date day){
		Calendar cal = Calendar.getInstance(); 
	    cal.setTime(day);
	    cal.add(Calendar.DAY_OF_YEAR, 1);
	    return cal.getTime();
	}
	
	@Override
	public long countTestPlans(Long suiteId, Filtering filtering) {
		if (!filtering.isDefined()) {
			return countTestPlanItems(suiteId);
		} else {
			return (Long) executeEntityNamedQuery("testSuite.countTestPlansFiltered",
					IdAndLoginParameter(suiteId, filtering.getFilter()));
		}
	}

	@Override
	public long findProjectIdBySuiteId(long suiteId) {
		return (Long) executeEntityNamedQuery("testSuite.findProjectIdBySuiteId", idParameter(suiteId));
	}

	// ************************ utils ********************

	private List<IterationTestPlanItem> buildItems(List<Object[]> tuples) {

		List<IterationTestPlanItem> items = new ArrayList<IterationTestPlanItem>(tuples.size());

		for (Object[] tuple : tuples) {
			IterationTestPlanItem itpi = (IterationTestPlanItem) tuple[1];
			items.add(itpi);
		}

		return items;
	}

	private List<IndexedIterationTestPlanItem> buildIndexedItems(List<Object[]> tuples) {
		List<IndexedIterationTestPlanItem> indexedItems = new ArrayList<IndexedIterationTestPlanItem>(tuples.size());

		for (Object[] tuple : tuples) {
			Integer index = (Integer) tuple[0];
			IterationTestPlanItem itpi = (IterationTestPlanItem) tuple[1];
			indexedItems.add(new IndexedIterationTestPlanItem(index, itpi));
		}

		return indexedItems;
	}

	@Override
	public long countTestPlans(Long suiteId, Filtering filtering, ColumnFiltering columnFiltering) {
		

		StringBuilder hqlbuilder = buildTestPlanQueryBody(filtering, columnFiltering);
		
		Query query = assignParameterValuesToTestPlanQuery(hqlbuilder.toString(), suiteId, filtering, columnFiltering);

		return query.list().size();
	}
}
