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
package org.squashtest.tm.hibernate.mapping.campaign

import org.apache.poi.hssf.record.formula.functions.T
import org.squashtest.tm.hibernate.mapping.HibernateMappingSpecification
import org.squashtest.csp.tools.unittest.hibernate.HibernateOperationCategory
import org.squashtest.tm.domain.campaign.Iteration

class IterationMappingIT extends HibernateMappingSpecification {

	def "hibernate should populate creation info when iteration persisted"() {
		given:
		Iteration iteration = new Iteration(name: "it")
		persistFixture iteration

		when:
		def res = use (HibernateOperationCategory) {
			sessionFactory.doInSession { it.get(Iteration, iteration.id) }
		}

		then:
		res.createdOn != null
		res.createdBy != null
	}

	def "should persist actual and scheduled date periods"() {
		given:
		Iteration iteration = new Iteration(name: "it")
		iteration.actualEndDate = new Date()
		iteration.actualStartDate = new Date()
		iteration.scheduledEndDate = new Date()
		iteration.scheduledStartDate = new Date()
		persistFixture iteration

		when:
		def res = use (HibernateOperationCategory) {
			sessionFactory.doInSession { it.get(Iteration, iteration.id) }
		}

		then:
		res.actualStartDate != null
		res.actualEndDate != null
		res.scheduledStartDate != null
		res.scheduledEndDate != null
	}

	def "hibernate should not nullify scheduled date when its fields are null"() {
		given:
		Iteration iteration = new Iteration(name: "it")
		persistFixture iteration

		when:
		def res = use (HibernateOperationCategory) {
			sessionFactory.doInSession {
				it.get(Iteration, iteration.id)
			}
		}

		then:
		res.scheduledPeriod != null
	}
}