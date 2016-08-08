/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.service.statistics.requirement

import javax.persistence.EntityManager
import javax.persistence.Query;

import org.squashtest.tm.service.internal.campaign.CampaignStatisticsServiceImpl;
import org.squashtest.tm.service.internal.requirement.RequirementStatisticsServiceImpl
import static org.squashtest.tm.domain.execution.ExecutionStatus.*;
import spock.lang.Specification


class RequirementStatisticsServiceImplTest extends Specification {

	RequirementStatisticsServiceImpl service = new RequirementStatisticsServiceImpl()
	
	EntityManager em = Mock()

	def setup() {
		service.entityManager = em
	}

	//def "should say that campaign progression statistics cannot be computed because some dates are "

	def "should return bound testCases statistics"(){

		given :
		addMockQuery([
			[0, , new BigInteger(8)] as Object[],
			[1, new BigInteger(15)] as Object[],
			[2, new BigInteger(26)] as Object[],
		])

		when :
		RequirementBoundTestCasesStatistics res = service.gatherBoundTestCaseStatistics([1l])

		then :
			res.getZeroTestCases() == 8;
			res.getOneTestCase() == 15;
			res.getManyTestCases() == 26;

	}

	def addMockQuery(result) {
		Query q = Mock()
		em.createNativeQuery(_) >> q
		q.getResultList() >> result
	}
	
}
