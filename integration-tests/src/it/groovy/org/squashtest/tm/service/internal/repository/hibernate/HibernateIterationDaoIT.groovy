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
package org.squashtest.tm.service.internal.repository.hibernate

import javax.inject.Inject

import org.apache.poi.hssf.record.formula.functions.T
import org.squashtest.tm.domain.campaign.TestPlanStatistics
import org.squashtest.tm.domain.campaign.TestPlanStatus
import org.squashtest.tm.service.internal.repository.IterationDao
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
class HibernateIterationDaoIT extends DbunitDaoSpecification {
	@Inject IterationDao iterationDao
	
	@DataSet("HibernateIterationDaoIT.should return list of executions.xml")
	def "should return list of executions"(){
		when:
		def result = iterationDao.findAllExecutionByIterationId (2l)
		
		then:
		result.size() == 3
		result.each {it.name == "iteration2-execution"}
	}
	
	@DataSet("HibernateIterationDaoIT.should find iteration statistics.xml")
	def "should find test suite statistics READY"(){
		when:
		TestPlanStatistics result = iterationDao.getIterationStatistics(1L)
		
		then:
		result != null
		result.nbBlocked == 0
		result.nbSuccess == 0
		result.nbReady == 3
		result.nbDone == 0
		result.nbRunning == 0
		result.nbTestCases == 3
		result.nbUntestable == 0
		result.progression == 0
		result.nbFailure == 0
		result.status == TestPlanStatus.READY
	}
	
}	