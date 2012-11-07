
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
package org.squashtest.csp.tm.internal.repository.hibernate

import org.hibernate.Query;

import javax.inject.Inject;

import org.hibernate.SessionFactory;
import org.squashtest.csp.tm.domain.customfield.BindableEntity;
import org.squashtest.csp.tm.domain.customfield.CustomFieldValue;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.internal.repository.CustomFieldValueDao;
import org.unitils.dbunit.annotation.DataSet;

import spock.unitils.UnitilsSupport;


@UnitilsSupport
@DataSet
class HibernateCustomFieldValueDaoIT extends DbunitDaoSpecification{
	
	@Inject
	CustomFieldValueDao dao;
	
	@Inject
	SessionFactory factory;
	
	def "should find all the custom field values for test case 1"(){
		
		when :
			List<CustomFieldValue> values = dao.findAllCustomValues(111l, BindableEntity.TEST_CASE);
			
		then :
			values.size()==2
			values.collect {it.id} == [1111l, 1112l]
		
	}
	
	
	def "should find all the custom field values that are instances of a given custom field binding"(){
		
		when :
			List<CustomFieldValue> values = dao.findAllCustomValuesOfBinding(112l)
			
		then :
			values.size()==2
			values.collect {it.id} == [1112l, 1122l]
		
		
	}
	
	def "should copy the custom fields values from one test case to another test case by creating them"(){
		
		when :			
			dao.copyCustomFieldValues(112l, 113l, BindableEntity.TEST_CASE)			
			List<CustomFieldValue> values = dao.findAllCustomValues(113l, BindableEntity.TEST_CASE)
		
		then :
			values.collect{it.value} as Set == ["SEC-2", "false"] as Set
	}
	
	
	def "should copy the custom fields values from one test case to another test case by copying the values"(){
		
		when :
			dao.copyCustomFieldValuesContent(111l, 112l, BindableEntity.TEST_CASE)
			List<CustomFieldValue> values = dao.findAllCustomValues(112l, BindableEntity.TEST_CASE)
		
		then :
			values.collect{it.value} == ["", "true"]
	}
	
	def "should create all the custom field values for a new BoundEntity"(){
		
		given :
			def session = factory.getCurrentSession()
			def testcase = session.get(TestCase.class, 113l)
		
		when :
		
			def hql = 	""" insert into CustomFieldValue(boundEntityId, boundEntityType, binding, value)  
							select CAST(:destEntityId AS long), cfb.boundEntity, cfb, cf.defaultValue 
						  	from CustomFieldBinding cfb join cfb.customField cf 
							where cfb.boundEntity = :entityType 
							and cfb.boundProject = :boundProject 
						"""
			
			Query q = session.createQuery(hql)
			q.setParameter("destEntityId", 113l)
			q.setParameter("entityType", BindableEntity.TEST_CASE)
			q.setParameter("boundProject", testcase.project)
			
			q.executeUpdate()
			
			List<CustomFieldValue> values = dao.findAllCustomValues(113l, BindableEntity.TEST_CASE)
			
		then :
			values.collect{it.value} as Set == ["NOSEC", "false"] as Set
			
	}

	
}