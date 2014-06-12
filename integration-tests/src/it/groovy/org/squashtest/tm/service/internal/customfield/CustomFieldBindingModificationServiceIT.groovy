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
package org.squashtest.tm.service.internal.customfield


import javax.inject.Inject

import org.hibernate.SessionFactory
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.customfield.BindableEntity
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.service.DbunitServiceSpecification
import org.unitils.dbunit.annotation.DataSet
import org.squashtest.tm.service.customfield.CustomFieldBindingModificationService
import org.squashtest.tm.domain.customfield.CustomFieldBinding


import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
@DataSet("CustomFieldVariousIT.sandbox.xml")
class CustomFieldBindingModificationServiceIT extends DbunitServiceSpecification {

	@Inject
	CustomFieldBindingModificationService service



	def "when creating a new custom field binding, should cascade the create of cuf values for concerned entities"(){

		when :
		service.addNewCustomFieldBinding(1l, BindableEntity.TEST_CASE, 3l, new CustomFieldBinding())
		def customFields = listQuery("from CustomFieldValue")


		then :
		def allValuesDef = customFields
				.collect{ return [it.binding.customField.id,
						it.boundEntityType,
						it.boundEntityId]
				} as Set

		allValuesDef.containsAll([3l, BindableEntity.TEST_CASE, 111l],
		[3l, BindableEntity.TEST_CASE, 112l],
		[3l, BindableEntity.TEST_CASE, 113l]


		)


	}

	def "when removing a custom field binding, should cascade delete the concerned custom field values"(){

		when :
		service.removeCustomFieldBindings([111l, 112l])
		def customFields = listQuery("from CustomFieldValue")


		then :
		customFields.size() == 0

	}

	def "should reorder the bindings"(){

		when :
		service.moveCustomFieldbindings([112l], 0)
		def bindings = listQuery("from CustomFieldBinding")

		then :
		bindings.collect{return [it.id, it.position]} as Set == [[111l, 2], [112l, 1]] as Set

	}

	def listQuery={
		return getSession().createQuery(it).list()
	}

}