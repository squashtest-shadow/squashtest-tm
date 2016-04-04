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
package org.squashtest.tm.service.statistics.campaign
import javax.persistence.EntityManager

import org.hibernate.Query;
import org.hibernate.Session;
import org.squashtest.tm.service.internal.campaign.CampaignStatisticsServiceImpl;
import static org.squashtest.tm.domain.execution.ExecutionStatus.*;
import spock.lang.Specification


class CampaignStatisticsServiceImplTest extends Specification {

	CampaignStatisticsServiceImpl service = new CampaignStatisticsServiceImpl()
	EntityManager em = Mock()

	def sessionMocks = []


	def setup(){
		service.em = em
	}



	//def "should say that campaign progression statistics cannot be computed because some dates are "


	def "should return test inventory"(){

		given :
		addMockQuery([
			[1l, "bob", READY, 2l] as Object[],
			[1l, "bob", BLOCKED, 5l] as Object[],
			[2l, "mike", READY, 2l] as Object[],
			[2l, "mike", FAILURE, 1l] as Object[],
			[3l, "robert", SUCCESS, 8l] as Object[]
		])

		em.unwrap(_) >>> sessionMocks

		when :
		List<IterationTestInventoryStatistics> res = service.gatherCampaignTestInventoryStatistics([1l])

		then :
		res.size() == 3
		res*.iterationName == ["bob", "mike", "robert"]
		res*.nbReady == [2, 2, 0]
		res*.nbRunning == [0, 0, 0]
		res*.nbSuccess == [0, 0, 8]
		res*.nbFailure == [0, 1, 0]
		res*.nbBlocked == [5, 0, 0]
		res*.nbUntestable == [0, 0, 0]
		res*.nbWarning == [0, 0, 0]
		res*.nbError == [0, 0, 0]

	}


	def addMockQuery(result){
		Session session = Mock()
		Query q = Mock()

		q.list() >> result
		session.getNamedQuery(_) >> q

		sessionMocks << session
	}
}
