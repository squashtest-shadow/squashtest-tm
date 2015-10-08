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

import org.hibernate.Query
import org.hibernate.type.LongType
import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.chart.AttributeType
import static org.squashtest.tm.domain.EntityType.*;
import static org.squashtest.tm.domain.chart.AttributeType.*;
import org.squashtest.tm.domain.chart.AxisColumn;
import org.squashtest.tm.domain.chart.ColumnPrototype;
import org.squashtest.tm.domain.chart.DataType;
import static org.squashtest.tm.domain.chart.DataType.*;
import org.squashtest.tm.domain.chart.MeasureColumn;
import org.squashtest.tm.domain.chart.Operation;
import static org.squashtest.tm.domain.chart.Operation.*;
import org.squashtest.tm.domain.requirement.QRequirementVersion;
import org.squashtest.tm.domain.testcase.QRequirementVersionCoverage
import org.squashtest.tm.domain.testcase.QTestCase
import org.squashtest.tm.service.internal.repository.hibernate.DbunitDaoSpecification
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

import com.querydsl.jpa.hibernate.HibernateQuery

@NotThreadSafe
@UnitilsSupport
@Transactional
class ProjectionPlannerIT extends DbunitDaoSpecification{

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


	@DataSet("MainQueryPlanner.dataset.xml")
	def "should generate simple select and group by clauses"(){

		given : "basequery"

		QTestCase testCase = QTestCase.testCase
		QRequirementVersionCoverage cov = QRequirementVersionCoverage.requirementVersionCoverage
		QRequirementVersion versions = QRequirementVersion.requirementVersion

		HibernateQuery query = new HibernateQuery().from(testCase)
				.join(testCase.requirementVersionCoverages, cov)
				.join(cov.verifiedRequirementVersion, versions)


		and : "definition"

		DetailedChartDefinition definition =
				new DetailedChartDefinition(measures : [
					mkMeasure(ATTRIBUTE, NUMERIC, COUNT, REQUIREMENT_VERSION, "id")
				],
				axis : [
					mkAxe(ATTRIBUTE, NUMERIC, NONE, TEST_CASE, "id")
				]
				)

		when :
		ProjectionPlanner planner = new ProjectionPlanner(definition, query)
		planner.modifyQuery()

		HibernateQuery concrete = query.clone(getSession())
		def res = concrete.fetch()

		then :
		def formatedRes = res.collect{ return [ it.get(0, Object.class), it.get(1, Object.class) ] } as Set
		formatedRes == [ [-1l, 3] , [-2l, 2]] as Set

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

}
