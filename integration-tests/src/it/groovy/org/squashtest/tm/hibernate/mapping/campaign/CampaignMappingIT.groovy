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
package org.squashtest.tm.hibernate.mapping.campaign

import org.squashtest.tm.core.foundation.exception.NullArgumentException
import org.apache.poi.hssf.record.formula.functions.T
import org.hibernate.Hibernate
import org.squashtest.tm.hibernate.mapping.HibernateMappingSpecification
import org.squashtest.csp.tools.unittest.hibernate.HibernateOperationCategory
import org.squashtest.tm.domain.campaign.Campaign
import org.squashtest.tm.domain.campaign.CampaignTestPlanItem
import org.squashtest.tm.domain.campaign.Iteration
import org.squashtest.tm.domain.testcase.TestCase


class CampaignMappingIT extends HibernateMappingSpecification {
	def "should persist ordered iterations"() {
		given:
		Campaign campaign = new Campaign(name: "camp")

		and:
		Iteration it1 = new Iteration(name: "it1")
		campaign.iterations << it1

		and:
		Iteration it2 = new Iteration(name: "it2")
		campaign.iterations << it2

		when:
		persistFixture it1, it2, campaign
		def loadedIters = use (HibernateOperationCategory) {
			sessionFactory.doInSession {
				def loadedCamp = it.get(Campaign, campaign.id)
				Hibernate.initialize(loadedCamp.iterations)
				return loadedCamp.iterations
			}
		}

		then:
		loadedIters[0].name == "it1"
		loadedIters[1].name == "it2"

		cleanup:
		deleteFixture it1
		deleteFixture it2
		deleteFixture campaign
	}
	def "should not add null iteration top campaign"() {
		given:
		Campaign campaign = new Campaign()
		when:
		campaign.addIteration null

		then:
		thrown NullArgumentException
	}

	/*def "Should add a test case to a campaign"(){
	 given :
	 def tc1 = new TestCase(name: "tc1")
	 persistFixture tc1
	 and:
	 def cpgn1 = new Campaign(name: "cpgn1")
	 persistFixture cpgn1
	 when :
	 doInTransaction{
	 def tc2 = it.get(TestCase, tc1.id)
	 Campaign cpgn2 = it.get(Campaign, cpgn1.id)
	 CampaignTestPlanItem item = new CampaignTestPlanItem(referencedTestCase: tc2)
	 it.persist item
	 cpgn2.addToTestPlan(item)
	 }
	 Campaign cmpgn = doInTransaction {
	 Campaign cmpgns = it.get(Campaign, cpgn1.id)
	 Hibernate.initialize(cmpgns.testPlan)
	 return cmpgns
	 }
	 then :
	 cmpgn.testPlan.size() == 1
	 }
	 */





}
