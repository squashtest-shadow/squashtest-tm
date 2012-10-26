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

import org.squashtest.csp.tm.domain.customfield.CustomField;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.internal.repository.CustomFieldDao
import org.squashtest.csp.tm.internal.repository.CustomFieldDeletionDao
import org.squashtest.csp.tm.service.customfield.CustomCustomFieldManagerService

import spock.lang.Specification

class CustomCustomFieldManagerServiceImplTest extends Specification {
	
	CustomCustomFieldManagerServiceImpl service = new CustomCustomFieldManagerServiceImpl();
	CustomFieldDao customFieldDao = Mock()
	CustomFieldDeletionDao customFieldDeletionDao = Mock()
	
	def setup() {
		service.customFieldDao = customFieldDao
		service.customFieldDeletionDao = customFieldDeletionDao
	}
	
	def "should delete custom field"(){
		given:
		CustomField cuf = Mock()
		customFieldDao.findById(1L) >> cuf
		when :
		service.deleteCustomField(1L);
		then:
		1* customFieldDeletionDao.removeCustomField(cuf)
	}
	
	def "should find all ordered by name"(){
		when :
		service.findAllOrderedByName()
		then :
		1* customFieldDao.finAllOrderedByName()
	}
	def "should find sorted "(){
		given : 
		CollectionSorting cs = Mock()
		when:
		service.findSortedCustomFields()
		then:
		1* customFieldDao.findSortedCustomFields(cs)
	}
}
