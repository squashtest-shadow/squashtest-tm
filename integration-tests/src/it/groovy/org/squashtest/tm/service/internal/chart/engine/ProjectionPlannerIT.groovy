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

import static org.squashtest.tm.domain.EntityType.*
import static org.squashtest.tm.domain.chart.ColumnType.*
import static org.squashtest.tm.domain.chart.DataType.*
import static org.squashtest.tm.domain.chart.Operation.*

import org.hibernate.Query
import org.hibernate.type.LongType
import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.EntityType
import org.squashtest.tm.domain.campaign.QCampaign
import org.squashtest.tm.domain.campaign.QIteration
import org.squashtest.tm.domain.campaign.QIterationTestPlanItem
import org.squashtest.tm.domain.chart.ColumnType
import org.squashtest.tm.domain.chart.AxisColumn
import org.squashtest.tm.domain.chart.ColumnPrototype
import org.squashtest.tm.domain.chart.DataType
import org.squashtest.tm.domain.chart.MeasureColumn
import org.squashtest.tm.domain.chart.Operation
import org.squashtest.tm.domain.execution.QExecution
import org.squashtest.tm.domain.requirement.QRequirement
import org.squashtest.tm.domain.requirement.QRequirementVersion
import org.squashtest.tm.domain.testcase.QRequirementVersionCoverage
import org.squashtest.tm.domain.testcase.QTestCase
import org.squashtest.tm.domain.bugtracker.QIssue
import org.squashtest.tm.service.internal.repository.hibernate.DbunitDaoSpecification
import org.unitils.dbunit.annotation.DataSet

import spock.lang.Unroll
import spock.unitils.UnitilsSupport

import com.querydsl.jpa.hibernate.HibernateQuery

@NotThreadSafe
@UnitilsSupport
@Transactional
class ProjectionPlannerIT extends DbunitDaoSpecification{

	static QTestCase tc = QTestCase.testCase
	static QRequirementVersionCoverage cov = QRequirementVersionCoverage.requirementVersionCoverage
	static QRequirementVersion v = QRequirementVersion.requirementVersion
	static QRequirement r = QRequirement.requirement
	static QIterationTestPlanItem itp = QIterationTestPlanItem.iterationTestPlanItem
	static QIteration ite = QIteration.iteration
	static QCampaign cp = QCampaign.campaign
	static QExecution exec = QExecution.execution
	static QIssue iss = QIssue.issue


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
	def "should generate simple select and group by clauses"(){

		given : "basequery"

		QTestCase testCase = QTestCase.testCase
		QRequirementVersionCoverage cov = QRequirementVersionCoverage.requirementVersionCoverage
		QRequirementVersion versions = QRequirementVersion.requirementVersion

		HibernateQuery query = new HibernateQuery().from(testCase)
				.join(testCase.requirementVersionCoverages, cov)
				.join(cov.verifiedRequirementVersion, versions)


		and : "definition"

		DetailedChartQuery definition = new DetailedChartQuery(
				measures : [mkMeasure(ATTRIBUTE, NUMERIC, COUNT, REQUIREMENT_VERSION, "id")],
				axis : [mkAxe(ATTRIBUTE, NUMERIC, NONE, TEST_CASE, "id")]
				)

		when :
		ProjectionPlanner planner = new ProjectionPlanner(definition, query)
		planner.modifyQuery()

		HibernateQuery concrete = query.clone(getSession())
		def res = concrete.fetch()

		then :
		def formatedRes = res.collect{it.a } as Set
		formatedRes == [ [-1l, 3] , [-2l, 2]] as Set

	}

	@DataSet("QueryPlanner.dataset.xml")
	def "should count executions by yearmonth"(){

		given : "query"
		HibernateQuery query = new HibernateQuery()
		QExecution exec = QExecution.execution

		query.from(exec)

		and : "definition"

		DetailedChartQuery definition =
				new DetailedChartQuery(
				measures : [mkMeasure(ATTRIBUTE, NUMERIC, COUNT, EXECUTION, "id")],
				axis : [mkAxe(ATTRIBUTE, DATE, BY_MONTH, EXECUTION, "lastExecutedOn")]
				)

		when :
		ProjectionPlanner planner = new ProjectionPlanner(definition, query)
		planner.modifyQuery()

		HibernateQuery concrete = query.clone(getSession())
		def res = concrete.fetch()

		then :
		def formatedRes = res.collect{ it.a } as Set
		formatedRes == [ [201510, 3] , [201511, 2]] as Set


	}

	@Unroll
	@DataSet("QueryPlanner.dataset.xml")
	def "should perform many queries"(){

		expect :
		def q = conf.query
		def definition = conf.definition
		def expected = conf.expected

		ProjectionPlanner planner = new ProjectionPlanner(definition, q)
		planner.modifyQuery()

		HibernateQuery query = q.clone(getSession())

		def res = query.fetch()
		def refined = res.collect{ it.a }

		refined as Set == expected as Set

		where :

		conf << [
			configureManyQuery(1),
			configureManyQuery(2),
			configureManyQuery(3)
		]



	}


	def ManyQueryPojo configureManyQuery(dsNum){

		def query
		def definition
		def expected

		switch (dsNum){
			case 1 : // case 1 -> select count(tc) and count(rv) by requirement id

				query = from(tc)
				.join(tc.requirementVersionCoverages, cov)
				.join(cov.verifiedRequirementVersion, v)
				.join(v.requirement, r);

				definition = new DetailedChartQuery(
						measures : [mkMeasure(ATTRIBUTE, NUMERIC, COUNT, TEST_CASE, "id"),
							mkMeasure(ATTRIBUTE, NUMERIC, COUNT, REQUIREMENT_VERSION, "id")
						],
						axis : [mkAxe(ATTRIBUTE, NUMERIC, NONE, REQUIREMENT, "id")]
						)

				expected = [[-1l, 2, 2],  [-2l, 1, 1], [-3l, 2, 2]]
				break;

			case 2 : // case 2 -> select count(exec) group by it id and referenced tc id
				query = from(exec).join(exec.testPlan, itp).join(itp.iteration, ite)

				definition = new DetailedChartQuery(
						measures : [mkMeasure(ATTRIBUTE, NUMERIC, COUNT, EXECUTION, "id")],
						axis : [mkAxe(ATTRIBUTE, NUMERIC, NONE, ITERATION, "id"),
							mkAxe(ATTRIBUTE, NUMERIC, NONE, EXECUTION, "referencedTestCase.id")]
						)

				expected = [[-11l, -1l, 3], [-12l, -1l, 1], [-12l, null, 1]]
				break;

			case 3 : // case 3 -> select count(req) group by year of creation

				query = from(r)

				definition = new DetailedChartQuery(
						measures : [mkMeasure(ATTRIBUTE, NUMERIC, COUNT, REQUIREMENT, "id")],
						axis : [mkAxe(ATTRIBUTE, DATE, BY_YEAR, REQUIREMENT, "audit.createdOn")]
						)

				expected = [[2015, 2], [2016, 1]]

		}

		return new ManyQueryPojo(query : query, definition : definition, expected : expected)


	}

	def mkMeasure(ColumnType attrType, DataType datatype, Operation operation, EntityType eType, String attributeName){
		def proto = new ColumnPrototype(entityType : eType, dataType : datatype, columnType : attrType, attributeName : attributeName)
		def meas = new MeasureColumn(column : proto, operation : operation)

		return meas

	}

	def mkAxe(ColumnType attrType, DataType datatype, Operation operation, EntityType eType, String attributeName){
		def proto = new ColumnPrototype(entityType : eType, dataType : datatype, columnType : attrType, attributeName : attributeName)
		def meas = new AxisColumn(column : proto, operation : operation)

		return meas

	}


	def HibernateQuery from(clz){
		return new HibernateQuery().from(clz)
	}




	class ManyQueryPojo {
		HibernateQuery query
		DetailedChartQuery definition
		Set<?> expected
	}

}
