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
import static org.squashtest.tm.domain.chart.AttributeType.*
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
import org.squashtest.tm.domain.chart.AttributeType
import org.squashtest.tm.domain.chart.AxisColumn
import org.squashtest.tm.domain.chart.ColumnPrototype
import org.squashtest.tm.domain.chart.DataType
import org.squashtest.tm.domain.chart.Filter;
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
import com.querydsl.core.types.dsl.Expressions;
import spock.lang.Unroll
import spock.unitils.UnitilsSupport

import com.querydsl.core.types.Projections
import com.querydsl.core.types.Ops.AggOps;
import com.querydsl.jpa.hibernate.HibernateQuery


@NotThreadSafe
@UnitilsSupport
@Transactional
class FilterPlannerIT extends DbunitDaoSpecification {

	static QTestCase tc = QTestCase.testCase
	static QRequirementVersionCoverage cov = QRequirementVersionCoverage.requirementVersionCoverage
	static QRequirementVersion v = QRequirementVersion.requirementVersion
	static QRequirement r = QRequirement.requirement
	static QIterationTestPlanItem itp = QIterationTestPlanItem.iterationTestPlanItem
	static QIteration ite = QIteration.iteration
	static QCampaign cp = QCampaign.campaign
	static QExecution exec = QExecution.execution
	static QIssue iss = QIssue.issue


	// TODO : test the AND/OR mechanism


	@DataSet("QueryPlanner.dataset.xml")
	def "should retain the requirement versions only for test case 1"(){

		given : "the query"

		HibernateQuery query = new HibernateQuery()
		query.from(v).join(v.requirementVersionCoverages, cov)
				.join(cov.verifyingTestCase, tc)
				.select(Projections.tuple(v.id,  v.name.countDistinct() ))
				.groupBy(v.id)

		and : "the definition"
		DetailedChartDefinition definition = new DetailedChartDefinition(
				filters : [mkFilter(ATTRIBUTE, NUMERIC, EQUALS, TEST_CASE, "id", ["-1"])]
				)

		when :
		FilterPlanner planner = new FilterPlanner(definition, query)
		planner.modifyQuery()
		HibernateQuery concrete = query.clone(getSession())

		def res = concrete.fetch()

		then :

		def formatedRes = res.collect{ it.a } as Set
		formatedRes == [ [-12l, 1] , [-21l, 1], [-31l, 1]] as Set

	}

	def mkMeasure(AttributeType attrType, DataType datatype, Operation operation, EntityType eType, String attributeName){
		def proto = new ColumnPrototype(entityType : eType, dataType : datatype, attributeType : attrType, attributeName : attributeName)
		def meas = new MeasureColumn(column : proto, operation : operation)

		return meas

	}

	def mkAxe(AttributeType attrType, DataType datatype, Operation operation, EntityType eType, String attributeName){
		def proto = new ColumnPrototype(entityType : eType, dataType : datatype, attributeType : attrType, attributeName : attributeName)
		def meas = new AxisColumn(column : proto, operation : operation)

		return meas

	}

	def mkFilter(AttributeType attrType, DataType datatype, Operation operation, EntityType eType, String attributeName, List<String> values){
		def proto = new ColumnPrototype(entityType : eType, dataType : datatype, attributeType : attrType, attributeName : attributeName)
		def filter = new Filter(column : proto, operation : operation, values : values)

		return filter

	}


	def HibernateQuery from(clz){
		return new HibernateQuery().from(clz)
	}




	class ManyQueryPojo {
		HibernateQuery query
		DetailedChartDefinition definition
		Set<?> expected
	}
}
