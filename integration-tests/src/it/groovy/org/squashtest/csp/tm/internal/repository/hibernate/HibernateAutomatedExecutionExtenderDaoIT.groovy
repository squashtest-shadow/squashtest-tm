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
package org.squashtest.csp.tm.internal.repository.hibernate

import javax.inject.Inject;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.csp.tm.internal.repository.testautomation.AutomatedExecutionExtenderDao;
import org.squashtest.csp.tm.internal.repository.testautomation.HibernateAutomatedExecutionExtenderDao;
import org.unitils.dbunit.annotation.DataSet;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.classic.Session;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import spock.unitils.UnitilsSupport;


@UnitilsSupport
@DataSet("HibernateAutomatedSuiteDaoIT.sandbox.xml")
@Transactional(isolation=Isolation.READ_UNCOMMITTED)
class HibernateAutomatedExecutionExtenderDaoIT extends DbunitDaoSpecification{
/*
	@Inject
	SessionFactory factory
	
	
	def "should get all the stuff"(){
		
		given :
			def strquery = """select ext from AutomatedExecutionExtender ext join fetch ext.automatedSuite suite join fetch ext.automatedTest
							 test join fetch test.project project join fetch project.server server
							""" 
			def ids = [110l, 120l, 130l, 210l, 220l]
		
		when :
			StatelessSession session = factory.openStatelessSession();
			def tx = session.beginTransaction()
			
			
			def res = []
			
			try{
				Query query = session.createQuery(strquery)
				//query.setParameter(0, 110l)
				res = query.list()
				tx.commit()
			}
			catch(Exception ex){
				println ex.message
				ex.printStackTrace()
				tx.rollback()
			}
			finally{
				session.close();
			}
		
		then :
			res*.id == ids
			res.collect{ it.automatedTest.name} as Set == 
				["test_11", 
				"test_12", 
				"test_13", 
				"test_21", 
				"test_22"
				] as Set
		
			res.collect {it.automatedTest.project.name} as Set ==
				[
					"first project",
					"first project",
					"first project",
					"second project",
					"second project"
				]
	}
	*/
	
/*
	@Inject
	AutomatedExecutionExtenderDao dao
	

	def "should init a detached version of a collection of extenders"(){
		
		when : 
		
			def ids = [110l, 120l, 130l, 210l, 220l]
			def extenders = []	
		
			ids.each{
					extenders << dao.findById(it)
			}
		
		when :
			def res = dao.getInitializedAndDetachedExtenders(extenders)
			
		then :
			extenders*.id == ids
			extenders*.automatedSuite.unique()*.id == [12345]
	}
*/
}