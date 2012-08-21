/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package squashtm.testautomation.repository.hibernate

import javax.inject.Inject;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.internal.repository.hibernate.DbunitDaoSpecification;
import org.unitils.dbunit.annotation.DataSet;

import spock.unitils.UnitilsSupport;
import squashtm.testautomation.domain.TestAutomationServer;
import squashtm.testautomation.repository.NonUniqueEntityException;
import squashtm.testautomation.repository.TestAutomationServerDao;

@UnitilsSupport
@Transactional
class HibernateTestAutomationServerDaoIT extends DbunitDaoSpecification {
	
	@Inject  TestAutomationServerDao serverDao

	@DataSet("HibernateTestAutomationDao.sandbox.xml")
	def "should refuse to perist a server having similar characteristic to an already existing server"(){
		given :
			TestAutomationServer newServer = new TestAutomationServer(new URL("http://www.roberto.com"), "roberto", "passroberto");
		when :
			serverDao.persist(newServer)
		then :
			thrown(NonUniqueEntityException)
	}
	
	@DataSet("HibernateTestAutomationDao.sandbox.xml")
	def "should find a server by id"(){
		
		when :
			def res = serverDao.findById(1l)
			
		then :
			res.id==1l
			res.baseURL.equals(new URL("http://www.roberto.com"))
			res.login == "roberto"
			res.password == "passroberto"
			res.kind=="jenkins"
		
	}
	
	
	@DataSet("HibernateTestAutomationDao.sandbox.xml")
	def "should find a server by example"(){
		given :
			TestAutomationServer example = new TestAutomationServer(new URL("http://www.roberto.com"), "roberto", "passroberto");
		when :
			def res = serverDao.findByExample(example)
			
		then :
			res.id==1l;
	}
	
	
	@DataSet("HibernateTestAutomationDao.sandbox.xml")
	def "should not find a project because of unmatched example"(){
		given :
			TestAutomationServer example = new TestAutomationServer(null, "bobinio", "passbobinio");
		
		when :
			def res = serverDao.findByExample(example)
		
		then :
			res == null
	}
	
	
	@DataSet("HibernateTestAutomationDao.sandbox.xml")
	def "should rant because too many matches for the given example"(){
		given :
			TestAutomationServer example = new TestAutomationServer(new URL("http://www.roberto.com"), null, null);
		
		when :
			def res = serverDao.findByExample(example)
		
		then :
			thrown(NonUniqueEntityException)
	}
	
	
	@DataSet("HibernateTestAutomationDao.sandbox.xml")
	def "should list the automation projects hosted on a given server"(){

		when :
			def res = serverDao.findAllHostedProjects(1l)
		
		then :
			res.size()==3
			res.collect{it.name} as Set == ["roberto1", "roberto2", "roberto3"] as Set
	}
	

}
