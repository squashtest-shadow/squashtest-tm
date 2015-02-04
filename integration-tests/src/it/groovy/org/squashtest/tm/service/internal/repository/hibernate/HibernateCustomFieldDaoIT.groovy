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
package org.squashtest.tm.service.internal.repository.hibernate

import javax.inject.Inject

import org.apache.poi.hssf.record.formula.functions.T
import org.squashtest.tm.domain.customfield.CustomField
import org.squashtest.tm.domain.customfield.InputType
import org.squashtest.tm.service.internal.repository.CustomFieldDao
import org.squashtest.tm.core.foundation.collection.PagingAndSorting
import org.squashtest.tm.core.foundation.collection.SortOrder
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
class HibernateCustomFieldDaoIT extends DbunitDaoSpecification {
	@Inject
	CustomFieldDao customFieldDao

	@DataSet("HibernateCustomFieldDaoIT.should return list of cuf ordered by name.xml")
	def "should return list of cuf ordered by name" () {
		when:
		List<CustomField> list = customFieldDao.findAllOrderedByName()

		then:
		list.size() == 3
		list.get(0).name == "abc"
		list.get(1).name == "cde"
		list.get(2).name == "fde"
	}
	
	@DataSet("HibernateCustomFieldDaoIT.should return sorted list of cuf.xml")
	def "should return sorted list of cuf"(){
		when:
		List<CustomField> list = customFieldDao.findSortedCustomFields(new InputTypeCollectionFilter())
		
		then: 
		list.size() == 2
		list.get(0).inputType == InputType.DROPDOWN_LIST
		list.get(1).inputType == InputType.PLAIN_TEXT

	}
	
	private class InputTypeCollectionFilter implements PagingAndSorting	{
			@Override
			String getSortedAttribute(){
				return "inputType"
			}
			@Override
			public int getFirstItemIndex() {
				return 1;
			}
			@Override
			public int getPageSize() {
				return 2;
			}
			/* (non-Javadoc)
			 * @see org.squashtest.tm.core.foundation.collection.Sorting#getSortOrder()
			 */
			@Override
			public SortOrder getSortOrder() {
				return SortOrder.ASCENDING;
			}
			
			@Override
			public boolean shouldDisplayAll() {
				return false;
			}
	}
	
	//TODO un-comment when deletion is handled
//	@DataSet("HibernateCustomFieldDeletionDaoIT.should delete custom field.xml")
//	def "should delete custom field" () {
//		given:
//		CustomField cuf =  findEntity(CustomField.class, -1L)
//		when:
//		customFieldDao.remove(cuf)
//		then:
//		!found(CustomField.class, -1L)
//		found(CustomField.class, -2L)
//	}
//	
//	@DataSet("HibernateCustomFieldDeletionDaoIT.should delete custom field and option.xml")
//	def "should delete custom field and options" () {
//		given:
//		CustomField cuf =  findEntity(CustomField.class, -1L)
//		when:
//		customFieldDao.remove(cuf)
//		then:
//		!found(CustomField.class, -1L)
//		!foundCustomFieldOption("first")
//		!foundCustomFieldOption("second")
//		!foundCustomFieldOption("third")
//	}
//	
//	
//	@DataSet("HibernateCustomFieldDeletionDaoIT.should delete custom field and binding.xml")
//	def "should delete custom field and binding " () {
//		given:
//		CustomField cuf =  findEntity(CustomField.class, -1L)
//		when:
//		customFieldDao.remove(cuf)
//		then:
//		!found(CustomField.class, -1L)
//		!found(CustomFieldBinding.class, -1L)
//		!found(CustomFieldBinding.class, -2L)
//	}
//	
//	
///* --------------------	private utilities --------------------------*/
//	private foundCustomFieldOption(String label){
//		Query query = session.createQuery("from CustomFieldOption cufo where cufo.label = :label")
//		query.setParameter("label", label);
//		return query.uniqueResult() != null;
//	}
	
}
