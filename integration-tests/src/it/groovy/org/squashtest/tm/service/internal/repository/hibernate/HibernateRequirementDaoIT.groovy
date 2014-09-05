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
package org.squashtest.tm.service.internal.repository.hibernate

import javax.inject.Inject

import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.requirement.RequirementCategory
import org.squashtest.tm.domain.requirement.RequirementCriticality
import org.squashtest.tm.domain.requirement.RequirementSearchCriteria
import org.squashtest.tm.domain.requirement.VerificationCriterion
import org.squashtest.tm.service.internal.repository.RequirementDao
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
class HibernateRequirementDaoIT extends DbunitDaoSpecification {
	@Inject RequirementDao requirementDao

	@DataSet("HibernateRequirementDaoIT.should find requirements by name token.xml")
	def "should find requirements by name token"() {
		given:
		def req = aLabelBasedCriteria()
		req.name >> "token"

		when:
		def res = requirementDao.findAllBySearchCriteria(req)

		then:
		res.size() == 1
		res[0].id == 10
	}
	

	@DataSet("HibernateRequirementDaoIT.should find requirements by reference token.xml")
	def "should find requirements by reference token"() {
		given:
		def req = aLabelBasedCriteria()
		req.reference >> "token"
		
		when:
		def res = requirementDao.findAllBySearchCriteria(req)

		then:
		res.size() == 1
		res[0].id == 10
	}

	@DataSet("HibernateRequirementDaoIT.should find requirements by reference and name token.xml")
	def "should find requirements by reference and name token"() {
		given:
		def req = aLabelBasedCriteria()
		req.name >> "token"
		req.reference >> "token"

		when:
		def res = requirementDao.findAllBySearchCriteria(req)

		then:
		res.size() == 1
		res[0].id == 10
	}

	@DataSet("HibernateRequirementDaoIT.should find requirements by criticalities.xml")
	def "should find requirements by criticalities"() {
		given:
		RequirementSearchCriteria req = Mock()
		req.criticalities >> [
			RequirementCriticality.MINOR,
			RequirementCriticality.MAJOR
		]
		req.categories >> []
		req.verificationCriterion >> VerificationCriterion.ANY

		when:
		def res = requirementDao.findAllBySearchCriteria(req)

		then:
		res.size() == 2
		!res.collect({ it.id }).contains(30)
	}
	@DataSet("HibernateRequirementDaoIT.should find requirements by categories.xml")
	def "should find requirements by categories"() {
		given:
		RequirementSearchCriteria req = Mock()
		req.criticalities >> []
		req.categories >> [RequirementCategory.UNDEFINED, RequirementCategory.FUNCTIONAL]
		req.verificationCriterion >> VerificationCriterion.ANY

		when:
		def res = requirementDao.findAllBySearchCriteria(req)

		then:
		res.size() == 2
		!res.collect({ it.id }).contains(30)
	}

	@DataSet("HibernateRequirementDaoIT.should find verified requirements.xml")
	def "should find verified requirements"() {
		given:
		RequirementSearchCriteria req = Mock()
		req.criticalities >> []
		req.categories >> []
		req.verificationCriterion >> VerificationCriterion.SHOULD_BE_VERIFIED

		when:
		def res = requirementDao.findAllBySearchCriteria(req)

		then:
		res.size() == 1
		res.collect({ it.id }).contains(10L)
	}

	@DataSet("HibernateRequirementDaoIT.should find verified requirements.xml")
	def "should find non verified requirements"() {
		given:
		RequirementSearchCriteria req = Mock()
		req.criticalities >> []
		req.categories >> []
		req.verificationCriterion >> VerificationCriterion.SHOULD_NOT_BE_VERIFIED

		when:
		def res = requirementDao.findAllBySearchCriteria(req)

		then:
		res.size() == 1
		res.collect({ it.id }).contains(20L)
	}

	@DataSet("HibernateRequirementDaoIT.should not find folders when searching non verified requirements.xml")
	def "should not find folders when searching non verified requirements"() {
		given:
		RequirementSearchCriteria req = Mock()
		req.criticalities >> []
		req.categories >> []
		req.verificationCriterion >> VerificationCriterion.SHOULD_NOT_BE_VERIFIED

		when:
		def res = requirementDao.findAllBySearchCriteria(req)

		then:
		res.size() == 0
	}

	@DataSet("HibernateRequirementDaoIT.should not find folders when searching non verified requirements.xml")
	def "should find folders when searching regardless verified or not requirements"() {
		given:
		RequirementSearchCriteria req = Mock()
		req.criticalities >> []
		req.categories >> []
		req.verificationCriterion >> VerificationCriterion.ANY
		req.libeleIsOnlyCriteria() >> true;
		when:
		def res = requirementDao.findAllBySearchCriteria(req)

		then:
		res.size() == 1
	}

	@DataSet("HibernateRequirementDaoIT.should find verified requirements.xml")
	def "should find requirements regardless verified or not"() {
		given:
		RequirementSearchCriteria req = Mock()
		req.criticalities >> []
		req.categories >> []
		req.verificationCriterion >> VerificationCriterion.ANY

		when:
		def res = requirementDao.findAllBySearchCriteria(req)

		then:
		res.size() == 2
	}

	@DataSet("HibernateRequirementDaoIT.should find folders by name token.xml")
	def "should find folders by name token"() {
		given:
		def req = aLabelBasedCriteria()
		req.name >> "token"
		req.libeleIsOnlyCriteria() >> true
		
		when:
		def res = requirementDao.findAllBySearchCriteria(req)

		then:
		res.size() == 1
		res[0].id == 50
	}

	def aLabelBasedCriteria() {
		RequirementSearchCriteria req = Mock()
		req.categories >> []
		req.criticalities >> []
		req.verificationCriterion >> VerificationCriterion.ANY
		return req
	}

}
