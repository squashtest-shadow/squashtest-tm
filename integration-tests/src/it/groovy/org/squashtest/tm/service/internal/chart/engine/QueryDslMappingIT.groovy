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

import javax.inject.Inject;

import org.hibernate.Query;
import org.hibernate.type.LongType;
import org.spockframework.util.NotThreadSafe;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.bugtracker.QIssue;
import org.squashtest.tm.domain.campaign.QCampaign;
import org.squashtest.tm.domain.campaign.QIteration;
import org.squashtest.tm.domain.campaign.QIterationTestPlanItem;
import org.squashtest.tm.domain.execution.QExecution;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.jpql.ExtOps;
import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery;
import org.squashtest.tm.domain.requirement.QRequirement;
import org.squashtest.tm.domain.requirement.QRequirementVersion;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.QRequirementVersionCoverage;
import org.squashtest.tm.domain.testcase.QTestCase;
import org.squashtest.tm.domain.testcase.QTestStep;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.service.DbunitServiceSpecification
import org.squashtest.tm.service.internal.repository.hibernate.DbunitDaoSpecification;
import org.unitils.dbunit.annotation.DataSet;
import org.squashtest.tm.domain.testautomation.QAutomatedTest;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Ops.AggOps;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.Ops.MathOps;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPASubQuery;

import spock.unitils.UnitilsSupport;


@NotThreadSafe
@UnitilsSupport
@Transactional
class QueryDslMappingIT extends DbunitDaoSpecification {


	static QTestCase tc = QTestCase.testCase
	static QTestStep st = QTestStep.testStep
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
	def "should fetch test step ids using querydsl Qtypes"(){

		given :
		ExtendedHibernateQuery q = new ExtendedHibernateQuery()

		QTestCase testCase = QTestCase.testCase
		QTestStep allsteps = QTestStep.testStep

		q.select(allsteps.id).from(testCase).join(testCase.steps, allsteps).where(testCase.id.eq(-1l))

		when :
		ExtendedHibernateQuery attached = q.clone(getSession())
		def res = attached.fetch();

		then :
		res as Set == [-11l, -12l, -13l] as Set

	}



	@DataSet("QueryPlanner.dataset.xml")
	def "should fetch test step ids using join over dynamic path (instead of the natural way)"(){

		given : "the building parts"

		String tcAlias = "tc";
		String stepAlias = "st";


		EntityPathBase testcase = new QTestCase(tcAlias);
		EntityPathBase tcsteps = new QTestStep(stepAlias);


		PathBuilder stepjoin = new PathBuilder<>(TestCase.class, tcAlias)
				.get("steps", TestStep.class);

		PathBuilder stepid = new PathBuilder(TestStep.class, stepAlias).get("id");

		PathBuilder tcid = new PathBuilder(TestCase.class, tcAlias).get("id");

		and : "the assembly"

		ExtendedHibernateQuery q = new ExtendedHibernateQuery();

		q.from(testcase);

		q.join(stepjoin, tcsteps);
		q.select(stepid);
		q.where(tcid.eq(-1l));

		when :
		ExtendedHibernateQuery attached = q.clone(getSession())
		def res = attached.fetch();

		then :
		res as Set == [-11l, -12l, -13l] as Set

	}


	@DataSet("QueryPlanner.dataset.xml")
	def "should fetch step ids in an even less natural way"(){

		given : "the building parts"

		String tcAlias = "tc";
		String stepAlias = "st";


		EntityPathBase testcase = new EntityPathBase(TestCase.class, tcAlias);
		EntityPathBase tcsteps = new EntityPathBase(TestStep.class, stepAlias);


		PathBuilder stepjoin = new PathBuilder<>(TestCase.class, tcAlias)
				.get("steps", TestStep.class);

		PathBuilder stepid = new PathBuilder(TestStep.class, stepAlias).get("id");

		PathBuilder tcid = new PathBuilder(TestCase.class, tcAlias).get("id");

		and : "the assembly"

		ExtendedHibernateQuery q = new ExtendedHibernateQuery();

		q.from(testcase);

		q.join(stepjoin, tcsteps);
		q.select(stepid);
		q.where(tcid.eq(-1l));

		when :
		ExtendedHibernateQuery attached = q.clone(getSession())
		def res = attached.fetch();

		then :
		res as Set == [-11l, -12l, -13l] as Set
	}

	@DataSet("QueryPlanner.dataset.xml")
	def "test the BOOLEAN_CASE thing"(){

		when :
		QTestCase tc = new QTestCase("tc")
		QAutomatedTest auto = new QAutomatedTest("auto")

		ExtendedHibernateQuery q = new ExtendedHibernateQuery(getSession())

		q.from(tc).leftJoin(tc.automatedTest, auto)

		def ex = Expressions.simpleOperation(ExtAggOps.BOOLEAN_CASE.getType(), ExtAggOps.BOOLEAN_CASE, auto.id.isNotNull())
		q.select(ex)


		then :
		def res = q.fetch()
		true
	}

}
