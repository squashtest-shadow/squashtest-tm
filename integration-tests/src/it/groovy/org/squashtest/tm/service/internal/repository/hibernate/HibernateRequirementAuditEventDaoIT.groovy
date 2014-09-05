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

import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.domain.event.*
import org.squashtest.tm.domain.requirement.Requirement
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.service.internal.repository.RequirementAuditEventDao
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport


@NotThreadSafe
@UnitilsSupport
@Transactional
class HibernateRequirementAuditEventDaoIT extends DbunitDaoSpecification {

	@Inject
	RequirementAuditEventDao eventDao;


	def setupSpec(){
		List.metaClass.init = { howmany, item -> return (1..howmany).collect{return item
			} }
	}

	@DataSet("HibernateRequirementAuditEventDaoIT.should persist various events.xml")
	def "should persist a RequirementCreation event"(){
		given :
		RequirementVersion requirement = getSession().load(RequirementVersion.class, 1l);

		when :
		def createEvent = new RequirementCreation(requirement, requirement.createdBy);
		eventDao.persist(createEvent);

		getSession().evict(createEvent);

		def revent = getSession().createQuery("from RequirementCreation rc where rc.requirementVersion=:req")
				.setParameter("req", requirement)
				.uniqueResult();

		then :
		revent.id != null
		revent.requirementVersion == requirement
	}

	@DataSet("HibernateRequirementAuditEventDaoIT.should persist various events.xml")
	def "should persist a RequirementPropertyChange event"(){
		given :
		RequirementVersion requirement = getSession().load(RequirementVersion.class, 1l);

		when :
		RequirementPropertyChange pptChangeEvent = RequirementPropertyChange.builder()
		.setSource(requirement)
		.setAuthor(requirement.createdBy)
		.setModifiedProperty("property")
		.setOldValue("oldValue")
		.setNewValue("newValue")
		.build()
		eventDao.persist(pptChangeEvent);

		getSession().evict(pptChangeEvent);

		def revent = getSession().createQuery("from RequirementPropertyChange rpc where rpc.requirementVersion=:req")
				.setParameter("req", requirement)
				.uniqueResult();

		then :
		revent.id != null
		revent.requirementVersion == requirement
		revent.propertyName == "property"
		revent.oldValue == "oldValue"
		revent.newValue == "newValue"
	}

	@DataSet("HibernateRequirementAuditEventDaoIT.should persist various events.xml")
	def "should persist a RequirementLargeProperty event"(){
		given :
		RequirementVersion requirement = getSession().load(RequirementVersion.class, 1l);

		when :
		def pptChangeEvent = RequirementLargePropertyChange.builder()
		.setSource(requirement)
		.setAuthor(requirement.createdBy)
		.setModifiedProperty("property")
		.setOldValue("oldValue")
		.setNewValue("newValue")
		.build()
		eventDao.persist(pptChangeEvent);

		getSession().evict(pptChangeEvent);

		def revent = getSession().createQuery("from RequirementLargePropertyChange rpc where rpc.requirementVersion=:req")
				.setParameter("req", requirement)
				.uniqueResult();

		then :
		revent.id != null
		revent.requirementVersion == requirement
		revent.propertyName == "property"
		revent.oldValue == "oldValue"
		revent.newValue == "newValue"
	}


	@DataSet("HibernateRequirementAuditEventDaoIT.should fetch lists of events.xml")
	def "should fetch list of event for a requirement sorted by date"(){
		given :
		def requirementId=1l

		and:
		Paging paging = Mock()
		paging.getPageSize() >> 10
		paging.getFirstItemIndex() >> 0

		when :
		List<RequirementAuditEvent> events = eventDao.findAllByRequirementVersionIdOrderedByDate(requirementId, paging);

		then :
		events.size()==4


		events*.date == [
			parse("2010-08-04"),
			parse("2010-06-03"),
			parse("2010-04-02"),
			parse("2010-02-01")
			
			
			
		]
		
		events*.author == [
			"editor 13",
			"editor 12",
			"editor 11",
			"creator 1"
		]

		events*.class == [
			RequirementLargePropertyChange.class,
			RequirementPropertyChange.class,
			RequirementPropertyChange.class,
			RequirementCreation.class
		]
	}

	@DataSet("HibernateRequirementAuditEventDaoIT.should fetch lists of events.xml")
	def "should fetch paged list of event for a requirement"(){
		given :
		def requirementId=1L

		and:
		Paging paging = Mock()
		paging.getPageSize() >> 2
		paging.getFirstItemIndex() >> 1

		when :
		List<RequirementAuditEvent> events = eventDao.findAllByRequirementVersionIdOrderedByDate(requirementId, paging);

		then :
		events.collect { it.id } == [ 12L, 14L ]
	}

	@DataSet("HibernateRequirementAuditEventDaoIT.should fetch lists of events.xml")
	def "should count events for a requirement"(){
		given :
		def requirementId=1L

		when :
		def res = eventDao.countByRequirementVersionId(1L)
		

		then :
		res == 4
	}

	//the method parse looks deprecated for java.util.Date, but the actual Date class is provided by the Groovy JDK
	private Date parse(String arg){
		return Date.parse("yyyy-MM-dd", arg);
	}



}
