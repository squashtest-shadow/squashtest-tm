/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.service.internal.repository.hibernate;

import javax.inject.Inject;

import static org.squashtest.tm.domain.execution.ExecutionStatus.*;
import org.squashtest.tm.service.internal.repository.AutomatedSuiteDao;
import org.unitils.dbunit.annotation.DataSet;

import spock.unitils.UnitilsSupport;


@UnitilsSupport
@DataSet("HibernateAutomatedSuiteDaoIT.sandbox.xml")
public class HibernateAutomatedSuiteDaoIT extends DbunitDaoSpecification {

	@Inject
	AutomatedSuiteDao suiteDao;
	
	def suiteid = "12345"
	
	
	def "should create a new suite"(){
		
		expect :
			def suite = suiteDao.createNewSuite()
			suite.id != null
		
	}
	
	def "should find all the extenders associated to that suite"(){
		
		when :
			def extenders = suiteDao.findAllExtenders(suiteid)
			
		then :
			extenders.collect { it.id } as Set == [110l, 120l, 130l, 210l, 220l] as Set
		
	}
	
	def "should find all the extenders of executions waiting to be run"(){
		
		expect :
			suiteDao.findAllWaitingExtenders(suiteid).collect { it.id } == [ 210l ]
		
	}
	
	def "should find all the extenders of executions currently running"(){
		
		expect :
		suiteDao.findAllRunningExtenders(suiteid).collect { it.id } == [ 130l ]
		
	}
	
	def "should find all completed executions"(){
		
		expect :
			suiteDao.findAllCompletedExtenders(suiteid).collect{ it.id } as Set == [ 110l, 120l, 220l] as Set
		
	}
	
	def "should find all extenders by statys"(){
		
		expect :
		suiteDao.findAllExtendersByStatus(suiteid, [FAILURE, RUNNING]).collect {it.id} as Set == [220l, 130l] as Set
		
	}
	
}
