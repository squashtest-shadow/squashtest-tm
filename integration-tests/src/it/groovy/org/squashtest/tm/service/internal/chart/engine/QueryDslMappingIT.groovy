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

import org.spockframework.util.NotThreadSafe;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.execution.QExecution;
import org.squashtest.tm.domain.testcase.QTestCase;
import org.squashtest.tm.domain.testcase.QTestStep;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.service.DbunitServiceSpecification
import org.squashtest.tm.service.internal.repository.hibernate.DbunitDaoSpecification;
import org.unitils.dbunit.annotation.DataSet;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.hibernate.HibernateQuery;

import spock.unitils.UnitilsSupport;


@NotThreadSafe
@UnitilsSupport
@Transactional
class QueryDslMappingIT extends DbunitDaoSpecification {

	@DataSet("MainQueryPlanner.dataset.xml")
	def "should fetch test step ids using querydsl Qtypes"(){

		given :
		HibernateQuery q = new HibernateQuery()

		QTestCase testCase = QTestCase.testCase
		QTestStep allsteps = QTestStep.testStep

		q.select(allsteps.id).from(testCase).join(testCase.steps, allsteps).where(testCase.id.eq(-1l))

		when :
		HibernateQuery attached = q.clone(getSession())
		def res = attached.fetch();

		then :
		res as Set == [-11l, -12l, -13l] as Set

	}



	@DataSet("MainQueryPlanner.dataset.xml")
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

		HibernateQuery q = new HibernateQuery();

		q.from(testcase);

		q.join(stepjoin, tcsteps);
		q.select(stepid);
		q.where(tcid.eq(-1l));

		when :
		HibernateQuery attached = q.clone(getSession())
		def res = attached.fetch();

		then :
		res as Set == [-11l, -12l, -13l] as Set

	}



}
