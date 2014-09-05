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

import javax.inject.Inject;

import org.hibernate.Query;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.service.internal.repository.CustomFieldBindingDao;
import org.squashtest.tm.service.internal.repository.hibernate.HibernateCustomCustomFieldBindingDao.NewBindingPosition;
import org.unitils.dbunit.annotation.DataSet;

import spock.unitils.UnitilsSupport;


@UnitilsSupport
@DataSet
class HibernateCustomCustomFieldBindingDaoIT extends DbunitDaoSpecification {

	@Inject
	HibernateCustomCustomFieldBindingDao dao
	
	@Inject
	CustomFieldBindingDao dynamicDao;

	
	def "should get correct indexes from a messed up table"(){

		when :
			List<NewBindingPosition> newPositions = dao.recomputeBindingPositions();
			
		then :
			def collected = newPositions.collect { return [ it.bindingId, it.formerPosition, it.newPosition] } 
			def expected = [
				[121l, 5, 3],
				[131l, 1 ,1],
				[111l, 2, 2],
				[221l, 8, 3],
				[122l, 10, 3],	
				[241l, 1, 1],
				[132l, 2, 2],
				[211l, 3, 2],
				[112l, 0, 1]
			] 
		
			collected as Set == expected as Set
	}
	
	def "should update the position of some cuf binding"(){
		
		given :
			def newPositions = [
					newPosition(241l, 1, 1l),
					newPosition(221l, 8, 3l),
					newPosition(211l, 3, 2l),
				]
		
		when :
			dao.updateBindingPositions(newPositions);
			
			Query q = getSession().createQuery("from CustomFieldBinding where id in (241, 221, 211)")
			List<CustomFieldBinding> bindings = q.list();
			
		then :
			bindings.collect{it.position} as Set == [1, 3, 2] as Set
	}
	
	
	def "should find all the cfb having the same project and bound entity as this one"(){
		
		when :
			def res = dynamicDao.findAllAlike(221l)
			
		then :
			res.collect{it.id} as Set == [211l, 221l, 241l] as Set 
			
			
		
	}
		
	
	NewBindingPosition newPosition(id, former, newp){
		return new NewBindingPosition(bindingId : id, formerPosition : former, newPosition : newp);
	}
	
}
