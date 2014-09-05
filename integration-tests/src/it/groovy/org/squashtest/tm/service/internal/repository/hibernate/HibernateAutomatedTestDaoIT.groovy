/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.service.internal.repository.hibernate;

import javax.inject.Inject;

import static org.squashtest.tm.domain.execution.ExecutionStatus.*;

import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.service.internal.repository.AutomatedSuiteDao;
import org.squashtest.tm.service.internal.repository.AutomatedTestDao;
import org.unitils.dbunit.annotation.DataSet;

import spock.lang.Unroll;
import spock.unitils.UnitilsSupport;


@UnitilsSupport
@DataSet("HibernateAutomatedTestDaoIT.sandbox.xml")
public class HibernateAutomatedTestDaoIT extends DbunitDaoSpecification {

	@Inject
	AutomatedTestDao testDao;

	def "should persist a new test"(){

		given :
		def newtest = new AutomatedTest("new test", getProject(1l))

		when :
		def persisted = testDao.persistOrAttach(newtest)

		then :
		persisted.id != null

	}

	def "should attach a new test because another test matches it exactly"(){

		given :
		def exist= new AutomatedTest("both", getProject(2l))

		when :
		def persisted = testDao.persistOrAttach(exist)

		then :
		persisted.id == 22l
	}


	@Unroll("for test #id, should find #cnt inbound references")
	def "should count references"(){

		expect :
		testDao.countReferences(id) == cnt

		where :
		id	| cnt
		11l	| 1l
		12l | 3l
		13l | 1l
		21l | 1l
		22l	| 2l
		23l	| 0l
		14l	| 0l
	}

	@Unroll("should #_neg remove automated test #id because it is referenced by #_referers")
	def "should remove (or not) an AutomatedTest"(){

		given :
		def test = getTest(id)

		when  :
		testDao.removeIfUnused(test)

		then :
		getSession().flush()
		found("AUTOMATED_TEST", "TEST_ID", id) == res

		where :

		_neg	| 	res		|	id	|	_referers
		"not"	|	true	|	11l	|	"1 execution"
		"not"	|	true	|	12l	|	"1 execution and 2 test cases"
		"not"	|	true	|	21l	|	"1 test case"
		""		|	false	|	23l	|	"nothing"

	}


	def "should remove automated tests referenced by nothing"(){
		when :
		testDao.pruneOrphans()
		getSession().flush()
		then :
		! found("AUTOMATED_TEST", "TEST_ID", 23l)
		! found("AUTOMATED_TEST", "TEST_ID", 14l)
		getSession().createQuery("from AutomatedTest").list().collect{it.id} as Set == [11l, 12l, 13l, 21l, 22l, ] as Set
	}


	def getServer(id){
		getSession().get(TestAutomationServer.class, id)
	}

	def getProject(id){
		getSession().get(TestAutomationProject.class, id)
	}

	def getTest(id){
		getSession().get(AutomatedTest.class, id)
	}

}
