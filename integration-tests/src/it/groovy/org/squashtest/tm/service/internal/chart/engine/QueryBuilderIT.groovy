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


package org.squashtest.tm.service.internal.chart.engine

import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.type.LongType;
import org.spockframework.util.NotThreadSafe;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.bugtracker.QIssue;
import org.squashtest.tm.domain.campaign.QCampaign;
import org.squashtest.tm.domain.campaign.QIteration;
import org.squashtest.tm.domain.campaign.QIterationTestPlanItem;
import org.squashtest.tm.domain.chart.ColumnType;
import org.squashtest.tm.domain.chart.ChartQuery;
import org.squashtest.tm.domain.chart.ColumnPrototype;
import org.squashtest.tm.domain.chart.Filter;
import org.squashtest.tm.domain.chart.MeasureColumn;
import org.squashtest.tm.domain.chart.AxisColumn;
import org.squashtest.tm.domain.chart.DataType;
import org.squashtest.tm.domain.chart.Operation;
import org.squashtest.tm.domain.chart.SpecializedEntityType;
import org.squashtest.tm.domain.execution.QExecution;
import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery;
import org.squashtest.tm.domain.requirement.QRequirement;
import org.squashtest.tm.domain.requirement.QRequirementVersion;
import org.squashtest.tm.domain.testcase.QRequirementVersionCoverage;
import org.squashtest.tm.domain.testcase.QTestCase;
import org.squashtest.tm.service.internal.batchimport.testcase.excel.ColumnDef;
import org.squashtest.tm.service.internal.repository.hibernate.DbunitDaoSpecification
import org.unitils.dbunit.annotation.DataSet;
import static org.squashtest.tm.domain.EntityType.*
import static org.squashtest.tm.domain.chart.ColumnType.*
import static org.squashtest.tm.domain.chart.DataType.*
import static org.squashtest.tm.domain.chart.Operation.*


import spock.lang.Unroll;
import spock.unitils.UnitilsSupport;

import static org.squashtest.tm.service.internal.chart.engine.ChartEngineTestUtils.*;

@NotThreadSafe
@UnitilsSupport
@Transactional
class QueryBuilderIT extends DbunitDaoSpecification {



	// fix the requirementVersion - requirement relation
	def setup(){
		def session = getSession()

		[ "-1" : [-11l, -12l, -13l], "-2" : [-21l], "-3" : [-31l, -32l]].each {
			reqid, versids ->
			Query qu = session.createSQLQuery("update REQUIREMENT_VERSION set requirement_id = :reqid where res_id in (:vids)")
			qu.setParameter("reqid", Long.valueOf(reqid), LongType.INSTANCE)
			qu.setParameterList("vids", versids, LongType.INSTANCE)
			qu.executeUpdate()

		}
	}


	@DataSet("QueryPlanner.dataset.xml")
	def "should use a calculated column in a select clause"(){

		// goal : select requiremend.id, s_count(requirement.versions) from Requirement
		// in a convoluted way of course.

		given :
		def measureProto = findByName("REQUIREMENT_NB_VERSIONS")

		and :
		def measure = new MeasureColumn(column : measureProto, operation : Operation.NONE)
		def axe = mkAxe(ATTRIBUTE, NUMERIC, NONE, EntityType.REQUIREMENT, "id")

		ChartQuery chartquery = new ChartQuery(
				measures : [measure],
				axis : [axe]
				)

		when :


		def _q = new QueryBuilder(new DetailedChartQuery(chartquery)).createQuery()
		def query = _q.clone(getSession())
		def res = query.fetch();


		then :
		res.collect{it.a} as Set == [ [-1l,3], [-2l,1], [-3l,2] ] as Set


	}


	@DataSet("QueryPlanner.dataset.xml")
	def "should use a calculated column in a where clause - via subquery having  "(){

		// goal : select requirement.id, requirement.id group by requirement having s_count(requirement.versions) > 1
		// but not as concisely

		given :
		def filterProto = findByName("REQUIREMENT_NB_VERSIONS")

		and :
		def measure = mkMeasure(ATTRIBUTE, NUMERIC, NONE, EntityType.REQUIREMENT, "id")
		def filter = new Filter(column : filterProto, operation : Operation.GREATER, values : ["1"])
		def axe = mkAxe(ATTRIBUTE, NUMERIC, NONE, EntityType.REQUIREMENT, "id")

		ChartQuery chartquery = new ChartQuery(
				measures : [measure],
				filters : [filter],
				axis : [axe]
				)

		when :

		def query = new QueryBuilder(new DetailedChartQuery(chartquery)).createQuery().clone(getSession())
		def res = query.fetch();


		then :
		res.collect{it.a} as Set == [ [-1l,-1l], [-3l, -3l] ] as Set
		query.toString().replaceAll(/_\d+/, "_sub") ==
				"""select distinct requirement.id, requirement.id
from Requirement requirement
where requirement.id in (select distinct requirement_sub.id
from Requirement requirement_sub
  inner join requirement_sub.versions as requirementVersion_sub
group by requirement_sub.id
having s_count(requirementVersion_sub.id) > ?1)
group by requirement.id"""

	}

	@DataSet("QueryPlanner.dataset.xml")
	// this one demonstrates the "left where join" in subqueries
	def "should select how many times a test case appears in an iteration"(){
		given :
		def measureProto = findByName('TEST_CASE_ITERCOUNT');
		def axisProto = findByName("TEST_CASE_ID");

		and :
		def measure = new MeasureColumn(column : measureProto, operation : Operation.SUM)
		def axe = new AxisColumn(column : axisProto, operation : Operation.NONE)

		ChartQuery chartQuery = new ChartQuery(
				measures : [measure],
				axis : [axe]
				)


		when :
		def query = new QueryBuilder(new DetailedChartQuery(chartQuery)).createQuery().clone(getSession())
		def res = query.fetch()

		then :
		res.collect {it.a} as Set == [[-1l, 2], [-2l, 1], [-3l, 0]] as Set
		query.toString().replaceAll(/_\d+/, "_sub") ==
				"""select distinct testCase.id, s_sum((select distinct s_count(iteration_sub.id)
from Iteration iteration_sub
  left join iteration_sub.testPlans as iterationTestPlanItem_sub
  left join iterationTestPlanItem_sub.referencedTestCase as testCase_sub
where testCase = testCase_sub))
from TestCase testCase
group by testCase.id"""

	}



	// ***************** Tests on attribute 'class' ***********************************

	@DataSet("QueryPlanner.dataset.xml")
	def "should count the call steps per test case "(){
		given :
		def measureProto = findByName("TEST_CASE_CALLSTEPCOUNT")
		def axisProto = findByName("TEST_CASE_ID")

		and :
		def measure = new MeasureColumn(column : measureProto, operation : Operation.SUM)
		def axis = new AxisColumn(column : axisProto, operation : Operation.NONE)

		ChartQuery chartQuery = new ChartQuery(
				measures : [measure],
				axis : [axis]
				)

		when :
		def query = new QueryBuilder(new DetailedChartQuery(chartQuery)).createQuery()
		println query
		println "**********"

		def clone = query.clone(getSession())
		def res = clone.fetch()

		then :
		res.collect {it.a} as Set == [[-3l, 0], [-2l,0], [-1l,1]] as Set
	}


	@DataSet("QueryPlanner.dataset.xml")
	def "should count how many test case per requirements have at least 1 call step"(){
		given :
		def measureProto = findByName("TEST_CASE_CALLSTEPCOUNT")
		def axisProto = findByName("TEST_CASE_ID")

		and :
		def measure = new MeasureColumn(column : measureProto, operation : Operation.SUM)
		def axis = new AxisColumn(column : axisProto, operation : Operation.NONE)

		ChartQuery chartQuery = new ChartQuery(
				measures : [measure],
				axis : [axis]
				)

		when :
		def query = new QueryBuilder(new DetailedChartQuery(chartQuery)).createQuery()
		println query
		println "**********"

		def clone = query.clone(getSession())
		def res = clone.fetch()

		then :
		// the requirement -2l isn't verified by tc2 and thus
		// is filtered out because of inner join
		res.collect {it.a} as Set == [[-1l,1], [-3l, 1]] as Set
	}


	// ******** Tests on EXISTENCE datatype and usage of is null/not null*******************

	@DataSet("QueryPlanner.dataset.xml")
	def "should filter on which test cases that have automated scripts"(){

		given :
		def measureProto = findByName("TEST_CASE_ID")
		def filterProto = findByName("TEST_CASE_HASAUTOSCRIPT")
		def axisProto = findByName("TEST_CASE_ID")

		and :
		def measure = new MeasureColumn(column : measureProto, operation : Operation.COUNT)
		def filter = new Filter(column : filterProto, operation : Operation.EQUALS, values : ["TRUE"])
		def axis = new AxisColumn(column : axisProto, operation : Operation.NONE)

		ChartQuery chartQuery = new ChartQuery(
				measures : [measure],
				filters : [filter],
				axis : [axis]
				)

		when :
		def query = new QueryBuilder(new DetailedChartQuery(chartQuery)).createQuery()
		println query
		println "**********"

		def clone = query.clone(getSession())
		def res = clone.fetch()

		then :
		res.collect {it.a} as Set == [[-3l, 1]] as Set
	}


	@DataSet("QueryPlanner.dataset.xml")
	def "should filter on which test cases that don't have automated scripts"(){

		given :
		def measureProto = findByName("TEST_CASE_ID")
		def filterProto = findByName("TEST_CASE_HASAUTOSCRIPT")
		def axisProto = findByName("TEST_CASE_ID")

		and :
		def measure = new MeasureColumn(column : measureProto, operation : Operation.COUNT)
		def filter = new Filter(column : filterProto, operation : Operation.EQUALS, values : ["FALSE"])
		def axis = new AxisColumn(column : axisProto, operation : Operation.NONE)

		ChartQuery chartQuery = new ChartQuery(
				measures : [measure],
				filters : [filter],
				axis : [axis]
				)

		when :
		def query = new QueryBuilder(new DetailedChartQuery(chartQuery)).createQuery()
		println query
		println "**********"

		def clone = query.clone(getSession())
		def res = clone.fetch()

		then :
		res.collect {it.a} as Set == [[-1l, 1], [-2l, 1]] as Set
	}





	@DataSet("QueryPlanner.dataset.xml")
	def "should count test cases grouped by whether automated or not"(){

		given :
		def measureProto = findByName("TEST_CASE_ID")
		def axisProto = findByName("TEST_CASE_HASAUTOSCRIPT")

		and :
		def measure = new MeasureColumn(column : measureProto, operation : Operation.COUNT)
		def axis = new AxisColumn(column : axisProto, operation : Operation.NONE)

		ChartQuery chartQuery = new ChartQuery(
				measures : [measure],
				axis : [axis]
				)

		when :
		def query = new QueryBuilder(new DetailedChartQuery(chartQuery)).createQuery()
		println query
		println "**********"

		def clone = query.clone(getSession())
		def res = clone.fetch()

		then :
		res.collect {it.a} as Set == [[false, 2], [true, 1]] as Set
	}





	@DataSet("QueryPlanner.dataset.xml")
	def "should count how many test plans have been executed for each iteration"(){
		given :
		def measureProto= findByName('ITEM_TEST_PLAN_ID')
		def filterProto = findByName("ITEM_TEST_PLAN_IS_EXECUTED")
		def axisProto = findByName('ITERATION_ID')

		and :
		def measure = new MeasureColumn(column : measureProto, operation : Operation.COUNT)
		def filter = new Filter(column : filterProto, operation : Operation.EQUALS, values : ["TRUE"])
		def axis = new AxisColumn(column : axisProto, operation : Operation.NONE)

		ChartQuery chartQuery = new ChartQuery(
				measures : [measure],
				filters : [filter],
				axis : [axis]
				)

		when :
		def query = new QueryBuilder(new DetailedChartQuery(chartQuery)).createQuery()
		println query
		println "**********"
		def clone = query.clone(getSession())
		def res = clone.fetch()

		then :
		res.collect {it.a} as Set == [[-11l, 1], [-12l,2]] as Set

	}


	@DataSet("QueryPlanner.dataset.xml")
	def "should count how many test plans have not been executed for each iteration"(){
		given :
		def measureProto= findByName('ITEM_TEST_PLAN_ID')
		def filterProto = findByName("ITEM_TEST_PLAN_IS_EXECUTED")
		def axisProto = findByName('ITERATION_ID')

		and :
		def measure = new MeasureColumn(column : measureProto, operation : Operation.COUNT)
		def filter = new Filter(column : filterProto, operation : Operation.EQUALS, values : ["FALSE"])
		def axis = new AxisColumn(column : axisProto, operation : Operation.NONE)

		ChartQuery chartQuery = new ChartQuery(
				measures : [measure],
				filters : [filter],
				axis : [axis]
				)

		when :
		def query = new QueryBuilder(new DetailedChartQuery(chartQuery)).createQuery()
		println query
		println "**********"
		def clone = query.clone(getSession())
		def res = clone.fetch()


		then :
		// alas, the other iteration has no not-executed items so
		// it is filtered out because of of inner join mechanics
		res.collect {it.a} as Set == [[-11l, 1]] as Set

	}
	// ********* utilities ***************************


	ColumnPrototype findByName(name){
		getSession().createQuery("from ColumnPrototype where label = '${name}'").uniqueResult();
	}

	def ExtendedHibernateQuery from(clz){
		return new ExtendedHibernateQuery().from(clz)
	}


	class ManyQueryPojo {
		ExtendedHibernateQuery query
		DetailedChartQuery definition
		Set<?> expected
	}
}
