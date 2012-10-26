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

import javax.inject.Inject

import org.hibernate.Query
import org.squashtest.csp.tm.domain.customfield.CustomField
import org.squashtest.csp.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.csp.tm.domain.customfield.CustomFieldOption
import org.squashtest.csp.tm.domain.customfield.InputType
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting
import org.squashtest.csp.tm.internal.repository.CustomFieldDao
import org.squashtest.csp.tm.internal.repository.CustomFieldDeletionDao
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
class HibernateCustomFieldDeletionDaoIT_DISABLED extends DbunitDaoSpecification {
	
	@Inject
	CustomFieldDeletionDao customFieldDeletionDao

	@DataSet("HibernateCustomFieldDeletionDaoIT.should delete custom field.xml")
	def "should delete custom field" () {
		given:
		CustomField cuf =  findEntity(CustomField.class, 1L)
		when:
		customFieldDeletionDao.removeCustomField(cuf)
		then:
		!found(CustomField.class, 1L)
		found(CustomField.class, 2L)
	}
	
	@DataSet("HibernateCustomFieldDeletionDaoIT.should delete custom field and option.xml")
	def "should delete custom field and options" () {
		given:
		CustomField cuf =  findEntity(CustomField.class, 1L)
		when:
		customFieldDeletionDao.removeCustomField(cuf)
		then:
		!found(CustomField.class, 1L)
		!foundCustomFieldOption("first")
		!foundCustomFieldOption("second")
		!foundCustomFieldOption("third")
	}
	
	
	@DataSet("HibernateCustomFieldDeletionDaoIT.should delete custom field and binding.xml")
	def "should delete custom field and binding " () {
		given:
		CustomField cuf =  findEntity(CustomField.class, 1L)
		when:
		customFieldDeletionDao.removeCustomField(cuf)
		then:
		!found(CustomField.class, 1L)
		!found(CustomFieldBinding.class, 1L)
		!found(CustomFieldBinding.class, 2L)
	}
	
	
/* --------------------	private utilities --------------------------*/
	private foundCustomFieldOption(String label){
		Query query = session.createQuery("from CustomFieldOption cufo where cufo.label = :label")
		query.setParameter("label", label);
		return query.uniqueResult() != null;
	}
}
