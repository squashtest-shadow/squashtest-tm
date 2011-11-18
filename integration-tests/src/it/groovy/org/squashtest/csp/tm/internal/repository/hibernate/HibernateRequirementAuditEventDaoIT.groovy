/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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

import javax.inject.Inject

import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.csp.tm.domain.event.*
import org.squashtest.csp.tm.domain.requirement.Requirement
import org.squashtest.csp.tm.internal.repository.RequirementAuditEventDao
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport


@NotThreadSafe
@UnitilsSupport
@Transactional
class HibernateRequirementAuditEventDaoIT extends DbunitDaoSpecification {

	@Inject
	RequirementAuditEventDao eventDao;

	
	def setupSpec(){
		List.metaClass.init = { howmany, item -> return (1..howmany).collect{return item} }
	}
	
	@DataSet("HibernateRequirementAuditEventDaoIT.should persist various events.xml")
	def "should persist a RequirementCreation event"(){
		given :
			def requirement = getSession().load(Requirement.class, 1l);
			
		when :
			def createEvent = new RequirementCreation(requirement, requirement.createdBy);			
			eventDao.persist(createEvent); 
			
			getSession().evict(createEvent);
			
			def revent = getSession().createQuery("from RequirementCreation rc where rc.requirement=:req")
										.setParameter("req", requirement)
										.uniqueResult();
			
		then :
			revent.id != null
			revent.requirement == requirement
		
	}
	
	@DataSet("HibernateRequirementAuditEventDaoIT.should persist various events.xml")
	def "should persist a RequirementPropertyChange event"(){
		given :
			def requirement = getSession().load(Requirement.class, 1l);
			
		when :
			def pptChangeEvent = new RequirementPropertyChange(requirement, requirement.createdBy, "property", "oldValue", "newValue");
			eventDao.persist(pptChangeEvent);
			
			getSession().evict(pptChangeEvent);
			
			def revent = getSession().createQuery("from RequirementPropertyChange rpc where rpc.requirement=:req")
										.setParameter("req", requirement)
										.uniqueResult();
			
		then :
			revent.id != null
			revent.requirement == requirement
			revent.propertyName == "property"
			revent.oldValue == "oldValue"
			revent.newValue == "newValue"
	}
	
	@DataSet("HibernateRequirementAuditEventDaoIT.should persist various events.xml")
	def "should persist a RequirementLargeProperty event"(){
		given :
			def requirement = getSession().load(Requirement.class, 1l);
			
		when :
			def pptChangeEvent = new RequirementLargePropertyChange(requirement, requirement.createdBy, "property", "oldValue", "newValue");
			eventDao.persist(pptChangeEvent);
			
			getSession().evict(pptChangeEvent);
			
			def revent = getSession().createQuery("from RequirementLargePropertyChange rpc where rpc.requirement=:req")
										.setParameter("req", requirement)
										.uniqueResult();
			
		then :
			revent.id != null
			revent.requirement == requirement
			revent.propertyName == "property"
			revent.oldValue == "oldValue"
			revent.newValue == "newValue"
	}
	
	
	@DataSet("HibernateRequirementAuditEventDaoIT.should fetch lists of events.xml")
	def "should fetch list of event for a requirement sorted by date"(){
		given :
			def requirementId=1l
			
		when :
			List<RequirementAuditEvent> events = eventDao.findAllByRequirementId(requirementId);
		
		then :
			events.size()==4
			
			
			events*.date == [ parse("2010-02-01"),parse("2010-04-02"), parse("2010-06-03"), parse("2010-08-04") ]
			events*.author == ["creator 1", "editor 11", "editor 12", "editor 13"]
			
			events*.class == [RequirementCreation.class, 
										RequirementPropertyChange.class, 
										RequirementPropertyChange.class, 
										RequirementLargePropertyChange.class]
			
			
	}
	
	
	@DataSet("HibernateRequirementAuditEventDaoIT.should fetch lists of events.xml")
	def "should fetch a list of event for a list of requirements, sorted by date and requirement"(){
		
		given :
			def ids =  [ 1l, 2l ]
		
		when :
			def events = eventDao.findAllByRequirementIds(ids)
			
			def eventsR1 = events.findAll {it.requirement.id==1l} 
			def eventsR2 = events.findAll {it.requirement.id==2l}
			
		then :
			//9 events...
			events.size()==9
			
			//...dispatched as follow
			eventsR1.size()==4
			eventsR2.size()==5
			
			
			//the events are contiguous
			events[0..3]*.requirement.id == [].init(4, 1l) && events[4..8].requirement.id == [].init(5, 2l)
			
			//content check			
			eventsR1*.date == [ parse("2010-02-01"),parse("2010-04-02"), parse("2010-06-03"), parse("2010-08-04") ]
			eventsR1*.author == ["creator 1", "editor 11", "editor 12", "editor 13"]
			eventsR1*.class == [RequirementCreation.class,
				RequirementPropertyChange.class,
				RequirementPropertyChange.class,
				RequirementLargePropertyChange.class]

			
			eventsR2*.author == ["creator 2", "editor 21", "editor 22", "editor 23", "editor 24"]
			eventsR2*.date == [ parse("2010-01-01"),parse("2010-03-02"), parse("2010-05-03"), parse("2010-07-04"), parse("2010-09-05") ]
			eventsR2*.class == [RequirementCreation.class,
				RequirementLargePropertyChange.class,
				RequirementPropertyChange.class,
				RequirementLargePropertyChange.class,
				RequirementPropertyChange.class,]


	}
	
	
	//the method parse looks deprecated for java.util.Date, but the actual Date class is provided by the Groovy JDK
	private Date parse(String arg){
		return Date.parse("yyyy-MM-dd", arg);
	}

	
	
}
