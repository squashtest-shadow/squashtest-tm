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
package org.squashtest.csp.tm.internal.service.customField

import org.apache.poi.hssf.record.formula.functions.T
import org.squashtest.csp.tm.domain.customfield.CustomField
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder
import org.squashtest.csp.tm.internal.repository.CustomFieldBindingDao
import org.squashtest.csp.tm.internal.repository.CustomFieldDao
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;

import spock.lang.Specification

class CustomCustomFieldManagerServiceImplTest extends Specification {

	CustomCustomFieldManagerServiceImpl service = new CustomCustomFieldManagerServiceImpl();
	CustomFieldDao customFieldDao = Mock()
	CustomFieldBindingDao customFieldBindingDao = Mock();
	
	def setup() {
		service.customFieldDao = customFieldDao
		service.customFieldBindingDao = customFieldBindingDao
	}

	def "should delete custom field"(){
		given:
		CustomField cuf = Mock()
		List<Long> bindingIds = new ArrayList<Long>();
		customFieldDao.findById(1L) >> cuf
		customFieldBindingDao.findAllForCustomField(1L) >> bindingIds;
			
		when :
		service.deleteCustomField(1L);
		
		then:
		1* customFieldDao.remove(cuf)
	}
	
	def "should find sorted "(){
		given :
		PagingAndSorting cs = Mock()
		List<CustomField> customFields = Mock()
		customFieldDao.findSortedCustomFields(cs)>> customFields
		
		and:
		def counted = 3
		customFieldDao.countCustomFields()>> counted

		when:
		PagedCollectionHolder<Collection<CustomField>> result = service.findSortedCustomFields(cs)

		then:
		result != null
		1* customFieldDao.findSortedCustomFields(cs)
		1* customFieldDao.countCustomFields()
	}
}
