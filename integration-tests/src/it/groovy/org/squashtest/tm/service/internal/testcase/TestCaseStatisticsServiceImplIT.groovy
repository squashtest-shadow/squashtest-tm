/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package org.squashtest.tm.service.internal.testcase

import javax.inject.Inject

import org.hibernate.type.LongType;
import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.service.DbunitServiceSpecification
import org.squashtest.tm.service.testcase.TestCaseStatisticsBundle.TestCaseSizeStatistics;
import org.squashtest.tm.service.testcase.TestCaseStatisticsService
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@NotThreadSafe
@UnitilsSupport
@Transactional
class TestCaseStatisticsServiceImplIT extends DbunitServiceSpecification {

	@Inject TestCaseStatisticsService service;
	

	
	/*
	 * should find : 0 -> 3, 0-10 -> 5, 10-20 -> 3, 20+ -> 5 
	 */
	@DataSet("TCStatisticsService.sizeStatistics.xml")
	def "should count how many test cases have 0 steps, upto 10, upto 20, then above"(){
		
		given :
			def tcIds = 238L..253L as List
		
		when :
			TestCaseSizeStatistics stats = service.gatherTestCaseSizeStatistics(tcIds)
			
		then :
			stats.zeroSteps == 3
			stats.between0And10Steps == 5
			stats.between11And20Steps == 3
			stats.above20Steps == 5
		
	}
	
}
