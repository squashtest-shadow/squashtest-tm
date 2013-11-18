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
import java.util.ListIterator;
import java.util.Map;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.DateType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.core.foundation.collection.DefaultFiltering;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.SingleToMultiSortingAdapter;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestPlanStatistics;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.service.campaign.IndexedIterationTestPlanItem;
import org.squashtest.tm.service.internal.foundation.collection.PagingUtils;
import org.squashtest.tm.service.internal.foundation.collection.SortingUtils;
import org.squashtest.tm.service.internal.repository.IterationDao;

@Repository
public class HibernateIterationDao extends HibernateEntityDao<Iteration> implements IterationDao {

	private static final String PROJECT_FILTER = "projectFilter";
	private static final String REFERENCE_FILTER = "referenceFilter";
	private static final String TESTCASE_FILTER = "testcaseFilter";	
	private static final String WEIGHT_FILTER = "weightFilter";
	private static final String DATASET_FILTER = "datasetFilter";
	private static final String TESTSUITE_FILTER = "testsuiteFilter";
	private static final String STATUS_FILTER = "statusFilter";
	private static final String USER_FILTER = "userFilter";
	private static final String START_DATE = "startDate";
	private static final String END_DATE = "endDate";
	private static final String DATE_FORMAT = "dd/MM/yyyy";
	
	/*
	 * Because it is impossible to sort over the indices of ordered collection in a criteria query 
	 * we must then build an hql string which will let us do that. 
	 */	
	private static final String HQL_INDEXED_TEST_PLAN_TEMPLATE_START = 
			"select index(IterationTestPlanItem), IterationTestPlanItem, group_concat(TestSuite.name, 'order by', TestSuite.name) as suitenames "+
			"from Iteration as Iteration inner join Iteration.testPlans as IterationTestPlanItem "+
			"left outer join IterationTestPlanItem.referencedTestCase as TestCase " +
			"left outer join TestCase.project as Project " + 
			"left outer join IterationTestPlanItem.referencedDataset as Dataset " +
			"left outer join IterationTestPlanItem.user as User "+
			"left outer join IterationTestPlanItem.testSuites as TestSuite "+
			"where Iteration.id = :iterationId {whereClause} ";
	
private static final String HQL_INDEXED_TEST_PLAN_TEMPLATE_END =	
			"group by index(IterationTestPlanItem), IterationTestPlanItem.id ";
	
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

	private static final String HQL_INDEXED_TEST_PLAN_TESTSUITE_FILTER =
			"having group_concat(TestSuite.name, 'order by', TestSuite.name) like :testsuiteFilter ";
	
	private static final String HQL_INDEXED_TEST_PLAN_STATUS_FILTER =
			"and IterationTestPlanItem.executionStatus = :statusFilter ";
	
	private static final String HQL_INDEXED_TEST_PLAN_USER_FILTER =
			"and IterationTestPlanItem.user = :userFilter ";

	private static final String HQL_INDEXED_TEST_PLAN_EXECUTIONDATE_FILTER =
			"and IterationTestPlanItem.lastExecutedOn between :startDate and :endDate ";

	/**
	 * HQL query which looks up the whole iteration test plan
	 */
	private final String hqlFullIndexedTestPlan = HQL_INDEXED_TEST_PLAN_TEMPLATE_START.replace("{whereClause}", "");
	/**
	 * HQL query which looks up the test plan assigned to a given user
	 */
	private final String hqlUserFilteredIndexedTestPlan = HQL_INDEXED_TEST_PLAN_TEMPLATE_START.replace("{whereClause}", "and User.login = :userLogin ");

	@Override
	public List<Iteration> findAllByCampaignId(long campaignId) {

		return executeListNamedQuery("iterationDao.findAllByCampaignId", 
									  new SetIdParameter("campaignId", campaignId));
	}

	@Override
	public List<Iteration> findAllIterationContainingTestCase(long testCaseId) {
		return executeListNamedQuery("iterationDao.findAllIterationContainingTestCase", 
									  new SetIdParameter("testCaseId", testCaseId));
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

		if (tcList.size() > 0) {
			Campaign ca = tcList.get(0);
			Hibernate.initialize(ca.getIterations());
			return ca;
		} else {
			return null;
		}
	}

	@Override
	public List<Execution> findOrderedExecutionsByIterationId(long iterationId) {
		return executeListNamedQuery("iteration.findAllExecutions", new SetIdParameter("iterationId", iterationId));
	}

	@Override
	public List<Execution> findOrderedExecutionsByIterationAndTestCase(final long iterationId, final long testCaseId) {
		return executeListNamedQuery("iteration.findAllExecutionsByTestCase", new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setParameter("iterationId", iterationId, LongType.INSTANCE);
				query.setParameter("testCaseId", testCaseId, LongType.INSTANCE);

			}
		});

	}

	@Override
	public List<Execution> findOrderedExecutionsByIterationAndTestPlan(final long iterationId, final long testPlanId) {
		return executeListNamedQuery("iteration.findAllExecutionsByTestPlan", new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setParameter("iterationId", iterationId, LongType.INSTANCE);
				query.setParameter("testPlanId", testPlanId, LongType.INSTANCE);

			}
		});
	}
	
	private SetQueryParametersCallback idParameter(final long id) {
		return new SetIdParameter("iterationId", id);
	}
	
	
	private SetQueryParametersCallback IdAndLoginParameter(final long id, final String login){
		
		return new SetQueryParametersCallback() {			
			@Override
			public void setQueryParameters(Query query) {
				query.setParameter("iterationId", id);
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
	public TestPlanStatistics getIterationStatistics(long iterationId) {

		Map<String, Integer> statusMap = new HashMap<String, Integer>();

		fillStatusMapWithQueryResult(iterationId, statusMap);

		return new TestPlanStatistics(statusMap);
	}

	private void fillStatusMapWithQueryResult(final long iterationId, Map<String, Integer> statusMap) {
		// Add Total number of TestCases
		Integer nbTestPlans = ((Long) countTestPlans(iterationId, DefaultFiltering.NO_FILTERING)).intValue();
		statusMap.put(TestPlanStatistics.TOTAL_NUMBER_OF_TEST_CASE_KEY, nbTestPlans);

		// Add number of testCase for each ExecutionStatus
		SetQueryParametersCallback newCallBack = idParameter(iterationId);
		List<Object[]> result = executeListNamedQuery("iteration.countStatuses", newCallBack);
		for (Object[] objTab : result) {
			statusMap.put(((ExecutionStatus) objTab[0]).name(), ((Long) objTab[1]).intValue());
		}
	}

	@Override
	public long countRunningOrDoneExecutions(long iterationId) {
		return (Long) executeEntityNamedQuery("iteration.countRunningOrDoneExecutions", idParameter(iterationId));
	}
	
	
	// **************************** TEST PLAN ******************************
	

	@Override
	public List<IterationTestPlanItem> findTestPlan(long iterationId, PagingAndMultiSorting sorting, Filtering filtering, ColumnFiltering columnFiltering) {
		
		/* get the data */
		List<Object[]> tuples = findIndexedTestPlanData(iterationId, sorting, filtering, columnFiltering);
		
		/* filter them */
		List<IterationTestPlanItem> items = new ArrayList<IterationTestPlanItem>(tuples.size());
		
		for (Object[] tuple : tuples){
			IterationTestPlanItem itpi = (IterationTestPlanItem) tuple[1];
			items.add(itpi);
		}
		
		return items;
	}
	
	@Override
	public List<IndexedIterationTestPlanItem> findIndexedTestPlan(long iterationId, PagingAndSorting sorting, Filtering filtering, ColumnFiltering columnFiltering) {		
		return findIndexedTestPlan(iterationId, new SingleToMultiSortingAdapter(sorting), filtering, columnFiltering);
	}

	@Override
	public List<IndexedIterationTestPlanItem> findIndexedTestPlan(final long iterationId, PagingAndMultiSorting sorting, 
			Filtering filtering, ColumnFiltering columnFiltering) {
	
		/* get the data */
		List<Object[]> tuples = findIndexedTestPlanData(iterationId, sorting, filtering, columnFiltering);
		
		/* format them*/
		List<IndexedIterationTestPlanItem> indexedItems = new ArrayList<IndexedIterationTestPlanItem>(tuples.size());
		
		for (Object[] tuple : tuples){
			Integer index = (Integer)tuple[0];
			IterationTestPlanItem itpi = (IterationTestPlanItem) tuple[1];
			indexedItems.add(new IndexedIterationTestPlanItem(index, itpi));
		}
		
		return indexedItems;
		
	}
	
	private String buildIndexedTestPlanQueryString(PagingAndMultiSorting sorting, Filtering filtering, ColumnFiltering columnFiltering){
		
		StringBuilder hqlbuilder = new StringBuilder();
		
		String hql = filtering.isDefined() ? hqlUserFilteredIndexedTestPlan : hqlFullIndexedTestPlan;
		hqlbuilder.append(hql);
		
		if(columnFiltering.hasFilter(0)){
			hqlbuilder.append(HQL_INDEXED_TEST_PLAN_PROJECT_FILTER);
		}
		if(columnFiltering.hasFilter(1)){
			hqlbuilder.append(HQL_INDEXED_TEST_PLAN_REFERENCE_FILTER);
		}
		if(columnFiltering.hasFilter(2)){
			hqlbuilder.append(HQL_INDEXED_TEST_PLAN_TESTCASE_FILTER);
		}
		if(columnFiltering.hasFilter(3)){
			hqlbuilder.append(HQL_INDEXED_TEST_PLAN_WEIGHT_FILTER);
		}
		if(columnFiltering.hasFilter(4)){
			hqlbuilder.append(HQL_INDEXED_TEST_PLAN_DATASET_FILTER);
		}
		if(columnFiltering.hasFilter(6)){
			hqlbuilder.append(HQL_INDEXED_TEST_PLAN_STATUS_FILTER);
		}
		if(columnFiltering.hasFilter(7)){
			hqlbuilder.append(HQL_INDEXED_TEST_PLAN_USER_FILTER);
		}
		if(columnFiltering.hasFilter(8)){
			hqlbuilder.append(HQL_INDEXED_TEST_PLAN_EXECUTIONDATE_FILTER);				
		}
		
		hqlbuilder.append(HQL_INDEXED_TEST_PLAN_TEMPLATE_END);
		
		if(columnFiltering.hasFilter(5)){
			hqlbuilder.append(HQL_INDEXED_TEST_PLAN_TESTSUITE_FILTER);
		}
		
		// tune the sorting to make hql happy
		LevelImplementorSorter wrapper= new LevelImplementorSorter(sorting);
		wrapper.map("TestCase.importance", TestCaseImportance.class);
		wrapper.map("IterationTestPlanItem.executionStatus", ExecutionStatus.class);
		
		return SortingUtils.addOrder(hqlbuilder.toString(), wrapper);
	}
	
	// this method will use one or another strategy to fetch its data depending on what the user is requesting. 
	@SuppressWarnings("unchecked")
	private List<Object[]> findIndexedTestPlanData(final long iterationId, PagingAndMultiSorting sorting, Filtering filtering, ColumnFiltering columnFiltering){
		
		String queryString = buildIndexedTestPlanQueryString(sorting, filtering, columnFiltering);
				
		Query query = currentSession().createQuery(queryString);
		
		query.setParameter("iterationId", iterationId, LongType.INSTANCE);
		
		if (filtering.isDefined()){
			query.setParameter("userLogin", filtering.getFilter(), StringType.INSTANCE);
		}
		
		if(columnFiltering.hasFilter(0)){
			query.setParameter(PROJECT_FILTER, "%"+columnFiltering.getFilter(0)+"%", StringType.INSTANCE);
		}
		if(columnFiltering.hasFilter(1)){
			query.setParameter(REFERENCE_FILTER, "%"+columnFiltering.getFilter(1)+"%", StringType.INSTANCE);
		} 
		if(columnFiltering.hasFilter(2)){
			query.setParameter(TESTCASE_FILTER, "%"+columnFiltering.getFilter(2)+"%", StringType.INSTANCE);
		}
		if(columnFiltering.hasFilter(3)){
			query.setParameter(WEIGHT_FILTER, columnFiltering.getFilter(3), StringType.INSTANCE);
		}
		if(columnFiltering.hasFilter(4)){
			query.setParameter(DATASET_FILTER, "%"+columnFiltering.getFilter(4)+"%", StringType.INSTANCE);
		}
		if(columnFiltering.hasFilter(5)){
			query.setParameter(TESTSUITE_FILTER, "%"+columnFiltering.getFilter(5)+"%", StringType.INSTANCE);
		}
		if(columnFiltering.hasFilter(6)){
			query.setParameter(STATUS_FILTER, columnFiltering.getFilter(6), StringType.INSTANCE);
		}
		if(columnFiltering.hasFilter(7)){
			query.setParameter(USER_FILTER, Long.parseLong(columnFiltering.getFilter(7)), LongType.INSTANCE);
		}
		if(columnFiltering.hasFilter(8)){
			String dates = columnFiltering.getFilter(8);
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
	public long countTestPlans(Long iterationId, Filtering filtering) {
		if (! filtering.isDefined()){
			return (Long) executeEntityNamedQuery("iteration.countTestPlans", idParameter(iterationId));
		}
		else{
			return (Long) executeEntityNamedQuery("iteration.countTestPlansFiltered", IdAndLoginParameter(iterationId, filtering.getFilter()));
		}
	}

	@Override
	public long countTestPlans(Long iterationId, Filtering filtering, ColumnFiltering columnFiltering) {

		StringBuilder hqlbuilder = new StringBuilder();
		
		String hql = filtering.isDefined() ? hqlUserFilteredIndexedTestPlan : hqlFullIndexedTestPlan;
		hqlbuilder.append(hql);
		
		if(columnFiltering.hasFilter(0)){
			hqlbuilder.append(HQL_INDEXED_TEST_PLAN_PROJECT_FILTER);
		}
		if(columnFiltering.hasFilter(1)){
			hqlbuilder.append(HQL_INDEXED_TEST_PLAN_REFERENCE_FILTER);
		}
		if(columnFiltering.hasFilter(2)){
			hqlbuilder.append(HQL_INDEXED_TEST_PLAN_TESTCASE_FILTER);
		}
		if(columnFiltering.hasFilter(3)){
			hqlbuilder.append(HQL_INDEXED_TEST_PLAN_WEIGHT_FILTER);
		}
		if(columnFiltering.hasFilter(4)){
			hqlbuilder.append(HQL_INDEXED_TEST_PLAN_DATASET_FILTER);
		}
		if(columnFiltering.hasFilter(6)){
			hqlbuilder.append(HQL_INDEXED_TEST_PLAN_STATUS_FILTER);
		}
		if(columnFiltering.hasFilter(7)){
			hqlbuilder.append(HQL_INDEXED_TEST_PLAN_USER_FILTER);
		}
		if(columnFiltering.hasFilter(8)){
			hqlbuilder.append(HQL_INDEXED_TEST_PLAN_EXECUTIONDATE_FILTER);				
		}
		
		hqlbuilder.append(HQL_INDEXED_TEST_PLAN_TEMPLATE_END);
		
		if(columnFiltering.hasFilter(5)){
			hqlbuilder.append(HQL_INDEXED_TEST_PLAN_TESTSUITE_FILTER);
		}
		
		Query query = currentSession().createQuery(hqlbuilder.toString());
		
		query.setParameter("iterationId", iterationId, LongType.INSTANCE);
		
		if (filtering.isDefined()){
			query.setParameter("userLogin", filtering.getFilter(), StringType.INSTANCE);
		}
		
		if(columnFiltering.hasFilter(0)){
			query.setParameter(PROJECT_FILTER, "%"+columnFiltering.getFilter(0)+"%", StringType.INSTANCE);
		}
		if(columnFiltering.hasFilter(1)){
			query.setParameter(REFERENCE_FILTER, "%"+columnFiltering.getFilter(1)+"%", StringType.INSTANCE);
		} 
		if(columnFiltering.hasFilter(2)){
			query.setParameter(TESTCASE_FILTER, "%"+columnFiltering.getFilter(2)+"%", StringType.INSTANCE);
		}
		if(columnFiltering.hasFilter(3)){
			query.setParameter(WEIGHT_FILTER, columnFiltering.getFilter(3), StringType.INSTANCE);
		}
		if(columnFiltering.hasFilter(4)){
			query.setParameter(DATASET_FILTER, "%"+columnFiltering.getFilter(4)+"%", StringType.INSTANCE);
		}
		if(columnFiltering.hasFilter(5)){
			query.setParameter(TESTSUITE_FILTER, "%"+columnFiltering.getFilter(5)+"%", StringType.INSTANCE);
		}
		if(columnFiltering.hasFilter(6)){
			query.setParameter(STATUS_FILTER, columnFiltering.getFilter(6), StringType.INSTANCE);
		}
		if(columnFiltering.hasFilter(7)){
			query.setParameter(USER_FILTER, Long.parseLong(columnFiltering.getFilter(7)), LongType.INSTANCE);
		}
		if(columnFiltering.hasFilter(8)){
			String dates = columnFiltering.getFilter(8);
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
		
		return query.list().size();
	}
	

	


}
