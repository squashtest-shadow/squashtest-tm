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
package org.squashtest.tm.service.internal.testcase;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import javax.inject.Inject;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.type.LongType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.service.testcase.TestCaseStatisticsBundle;
import org.squashtest.tm.service.testcase.TestCaseStatisticsBundle.TestCaseBoundRequirementsStatistics;
import org.squashtest.tm.service.testcase.TestCaseStatisticsBundle.TestCaseImportanceStatistics;
import org.squashtest.tm.service.testcase.TestCaseStatisticsBundle.TestCaseSizeStatistics;
import org.squashtest.tm.service.testcase.TestCaseStatisticsBundle.TestCaseStatusesStatistics;
import org.squashtest.tm.service.testcase.TestCaseStatisticsService;

@Service("TestCaseStatisticsService")
@Transactional(readOnly=true)
@NamedQueries({
	
	@NamedQuery(name = "TestCaseStatistics.boundRequirements", 
				query = "select count(distinct rvc) from RequirementVersionCoverage rvc where rvc.verifyingTestCase.id in (:testCaseIds)"),
	
	@NamedQuery(name = "TestCaseStatistics.importanceStatistics",
				query = "select tc.importance, count(tc) from TestCase tc where tc.id in (:testCaseIds) group by tc.importance"),
				
	@NamedQuery(name = "TestCaseStatistics.statusesStatistics",
				query = "select tc.status, count(tc) from TestCase tc where tc.id in (:testCaseIds) group by tc.status"),
				
	@NamedQuery(name = "TestCaseStatistics.sizeStatistics", 
				query = "select count(tc.steps) from TestCase tc where tc.id in (:testCaseIds) order by count(tc.steps) asc ")
})
public class TestCaseStatisticsServiceImpl implements TestCaseStatisticsService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TestCaseStatisticsService.class);

	@Inject private SessionFactory sessionFactory;
	
	
	
	@Override
	public TestCaseBoundRequirementsStatistics gatherBoundRequirementStatistics(Collection<Long> testCaseIds) {
		
		if (testCaseIds.isEmpty()){
			return new TestCaseBoundRequirementsStatistics();
		}

		Query query = sessionFactory.getCurrentSession().getNamedQuery("TestCaseStatistics.boundRequirements"); 
		query.setParameterList("testCaseIds", testCaseIds, LongType.INSTANCE);
		
		Integer nbBound = ((Long)query.uniqueResult()).intValue();
		
		int nbNotBound = testCaseIds.size() - nbBound;
		
		return new TestCaseBoundRequirementsStatistics(nbNotBound, nbBound);
		
	}

	
	
	@Override
	public TestCaseImportanceStatistics gatherTestCaseImportanceStatistics(Collection<Long> testCaseIds) {
		
		if (testCaseIds.isEmpty()){
			return new TestCaseImportanceStatistics();
		}
		
		Query query = sessionFactory.getCurrentSession().getNamedQuery("TestCaseStatistics.importanceStatistics");
		query.setParameterList("testCaseIds", testCaseIds);
		
		List<Object[]> tuples = query.list();
		
		
		// format the result
		TestCaseImportanceStatistics stats = new TestCaseImportanceStatistics();
		
		TestCaseImportance _importance;
		Integer _cardinality;
		for (Object[] tuple : tuples){
			_importance = TestCaseImportance.valueOf((String)tuple[0]);
			_cardinality = ((Long)tuple[1]).intValue();
			switch(_importance){
			case VERY_HIGH : 
				stats.setVeryHigh(_cardinality); 
				break;
			case HIGH : 
				stats.setHigh(_cardinality);
				break;
			case MEDIUM : 
				stats.setMedium(_cardinality);
				break;
			case LOW : 
				stats.setLow(_cardinality);
				break;
			default : LOGGER.warn("TestCaseStatisticsService cannot handle the following TestCaseImportance value : '"+(String)tuple[0]+"'");
			}
		}
		
		return stats;
	}

	
	
	@Override
	public TestCaseStatusesStatistics gatherTestCaseStatusesStatistics(Collection<Long> testCaseIds) {
		
		if (testCaseIds.isEmpty()){
			return new TestCaseStatusesStatistics();
		}
		
		Query query = sessionFactory.getCurrentSession().getNamedQuery("TestCaseStatistics.statusesStatistics");
		query.setParameterList("testCaseIds", testCaseIds);
		
		List<Object[]> tuples = query.list();
		
		// format the result
		TestCaseStatusesStatistics stats = new TestCaseStatusesStatistics();
		
		TestCaseStatus _status;
		Integer _cardinality;
		for (Object[] tuple : tuples){
			_status = TestCaseStatus.valueOf((String)tuple[0]);
			_cardinality = ((Long)tuple[1]).intValue();
			switch(_status){
			case WORK_IN_PROGRESS : 
				stats.setWorkInProgress(_cardinality);
				break;
			case APPROVED :
				stats.setApproved(_cardinality);
				break;
			case OBSOLETE : 
				stats.setObsolete(_cardinality);
				break;
			case TO_BE_UPDATED :
				stats.setToBeUpdated(_cardinality);
				break;
			case UNDER_REVIEW : 
				stats.setUnderReview(_cardinality);
				break;
			
			}
		}
		
		return stats;
	}

	@Override
	public TestCaseSizeStatistics gatherTestCaseSizeStatistics(Collection<Long> testCaseIds) {

		if (testCaseIds.isEmpty()){
			return new TestCaseSizeStatistics();
		}
		
		Query query = sessionFactory.getCurrentSession().getNamedQuery("TestCaseStatistics.sizeStatistics");
		query.setParameterList("testCaseIds", testCaseIds, LongType.INSTANCE);
		
		List<Long> stepsNumbers = query.list();
		
		
		TestCaseSizeStatistics stats = new TestCaseSizeStatistics();
		
		//the following code is ugly because it pleases me to loop only once over the result set. It relies on the fact that the result set is sorted.
		ListIterator<Long> iterator = stepsNumbers.listIterator();
		Long _currentStepNumber;
		
		int count=0;
		while (iterator.hasNext() && (_currentStepNumber=iterator.next())==0){
			count++;
		}
		stats.setZeroSteps(count);
		
		count=0;
		while (iterator.hasNext() && (_currentStepNumber=iterator.next())<=10){
			count++;
		}
		stats.setBetween0And10Steps(count);
		
		count=0;
		while (iterator.hasNext() && (_currentStepNumber=iterator.next())<=20){
			count++;
		}
		stats.setBetween11And20Steps(count);
		
		count=0;
		while (iterator.hasNext() && (_currentStepNumber=iterator.next())>20){
			count++;
		}
		stats.setAbove20Steps(count);
		
		return stats;
	}

	
	@Override
	public TestCaseStatisticsBundle gatherTestCaseStatisticsBundle(Collection<Long> testCaseIds) {
		
		TestCaseBoundRequirementsStatistics reqs = gatherBoundRequirementStatistics(testCaseIds);
		TestCaseImportanceStatistics imp = gatherTestCaseImportanceStatistics(testCaseIds);
		TestCaseStatusesStatistics status = gatherTestCaseStatusesStatistics(testCaseIds);
		TestCaseSizeStatistics size = gatherTestCaseSizeStatistics(testCaseIds);
		
		
		return new TestCaseStatisticsBundle(reqs, imp, status, size);
		
	}

}
