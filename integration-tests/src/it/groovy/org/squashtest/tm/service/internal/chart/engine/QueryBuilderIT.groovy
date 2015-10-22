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

import com.querydsl.jpa.hibernate.HibernateQuery;

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

		// goal : select requiremend.id, count(requirement.versions) from Requirement
		// in a convoluted way of course.

		given :
		def measureProto = findByName("REQUIREMENT_NB_VERSION")

		and :
		def measure = new MeasureColumn(column : measureProto, operation : Operation.NONE)
		def axe = mkAxe(ATTRIBUTE, NUMERIC, NONE, EntityType.REQUIREMENT, "id")

		ChartQuery chartquery = new ChartQuery(
				measures : [measure],
				axis : [axe]
				)

		when :

		def query = new QueryBuilder(new DetailedChartQuery(chartquery)).createQuery().clone(getSession())
		def res = query.fetch();


		then :
		res.collect{it.a} as Set == [ [-1l,3], [-2l,1], [-3l,2] ] as Set


	}


	@DataSet("QueryPlanner.dataset.xml")
	def "should use a calculated column in a where clause - via subquery having  "(){

		// goal : select requirement.id, requirement.id group by requirement having count(requirement.versions) > 1
		// but not as concisely

		given :
		def filterProto = findByName("REQUIREMENT_NB_VERSION")

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


	}

	/*
	 @DataSet("QueryPlanner.dataset.xml")
	 def "should use a calculated column in a where clause - via subquery where "(){
	 given :
	 false
	 when :
	 false
	 then :
	 false
	 }
	 */

	ColumnPrototype findByName(name){
		getSession().createQuery("from ColumnPrototype where label = '${name}'").uniqueResult();
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
