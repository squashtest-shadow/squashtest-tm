/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.service.internal.repository.hibernate

import org.hibernate.Query;

import javax.inject.Inject;

import org.hibernate.SessionFactory;
import org.hibernate.type.LongType;
import org.springframework.test.context.ContextConfiguration;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.internal.repository.CustomFieldValueDao;
import org.unitils.dbunit.annotation.DataSet;
import org.squashtest.tm.service.internal.repository.CustomFieldValueDao.CustomFieldValuesPair; 

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
	
	def "should find pairs of custom field values"(){
		
		when :
			List<CustomFieldValuesPair> pairs = dao.findPairedCustomFieldValues(BindableEntity.TEST_CASE, 111l,112l )
			
		then :
			pairs.collect{ return [it.original.id, it.recipient.id] } as Set == [ [1111l, 1121l], [1112l, 1122l] ] as Set
			
		
	}

}