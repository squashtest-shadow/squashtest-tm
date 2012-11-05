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


import javax.inject.Inject;

import org.squashtest.csp.tm.domain.customfield.BindableEntity;
import org.squashtest.csp.tm.domain.customfield.CustomFieldValue;
import org.squashtest.csp.tm.internal.repository.CustomFieldValueDao;
import org.unitils.dbunit.annotation.DataSet;

import spock.unitils.UnitilsSupport;


@UnitilsSupport
@DataSet
class HibernateCustomFieldValueDaoIT extends DbunitDaoSpecification{
	
	@Inject
	CustomFieldValueDao dao;
	
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

}