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
package org.squashtest.tm.service.internal.chart

import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.service.DbunitServiceSpecification

import javax.inject.Inject;
import org.springframework.context.ApplicationEventPublisher;
import org.squashtest.tm.domain.customfield.CustomFieldBinding
import org.squashtest.tm.event.CreateCustomFieldBindingEvent;
import org.squashtest.tm.event.DeleteCustomFieldBindingEvent;
import org.squashtest.tm.domain.customfield.BindableEntity
import org.squashtest.tm.domain.chart.ColumnPrototype
import org.squashtest.tm.event.ChangeCustomFieldCodeEvent

import spock.unitils.UnitilsSupport

import org.squashtest.tm.domain.customfield.CustomField
import org.unitils.dbunit.annotation.DataSet

@UnitilsSupport
@Transactional
@DataSet("ColumnPrototypeModification.dataset.xml")
class ColumnPrototypeModificationIT extends DbunitServiceSpecification{

	@Inject
	ApplicationEventPublisher eventPublisher;
	
	def "should not create columnPrototype that already exist when new cufbinding is added" (){
		
		given:
		
		CustomField cuf = new CustomField(code:code)
		CustomFieldBinding cufBinding = new CustomFieldBinding(customField:cuf, boundEntity :  entityType )
		
		when :
		eventPublisher.publishEvent(new CreateCustomFieldBindingEvent(cufBinding));

		def result = findAll("ColumnPrototype");
		then :
		result.size == 5
		where :
		code        |        entityType                     || _
		"xx"        |        BindableEntity.TEST_CASE       || _
		"xx"        |        BindableEntity.CAMPAIGN        || _
	}
	
	def "should  create columnPrototype  when new cufbinding is added" (){
		
		given:
		
		CustomField cuf = new CustomField(code:code)
		CustomFieldBinding cufBinding = new CustomFieldBinding(customField:cuf, boundEntity :  entityType )
		
		when :
		eventPublisher.publishEvent(new CreateCustomFieldBindingEvent(cufBinding));

		def result = findAll("ColumnPrototype");
		then :
		result.size == 6
		
		where :
		code        |        entityType                     || _
		"xx"        |        BindableEntity.ITERATION       || _
		"ww"        |        BindableEntity.TEST_CASE       || _
		"ww"        |        BindableEntity.CAMPAIGN        || _
	
	}
	
	
	
		def "should delete columnPrototype when cufbinding is removed and no other binding are using this columnPrototype" (){
			
			when :
			eventPublisher.publishEvent(new DeleteCustomFieldBindingEvent(ids));
	
			then :
			def result = findAll("ColumnPrototype");
			result.size == remaining
			where :
			ids                                      || remaining
			[-1L]                                    ||     5
			[-2L]                                    ||     5
			[-3L]                                    ||     4
			[-4L]                                    ||     5
			[-5L]                                    ||     5
			[-6L]                                    ||     5
			[-7L]                                    ||     5
			[-8L]                                    ||     4
			[-1L, -2L]                               ||     4
			[-1L, -2L, -3L]                          ||     3
			[-1L, -4L, -6L]                          ||     5
			[-1L, -4L, -7L]                          ||     5
			[-1L, -5L, -6L]                          ||     5
			[-1L, -5L, -7L]                          ||     5
			[-3L, -8L]                               ||     3
			[-1L, -2L, -3L, -4L, -5L]                ||     2
			[-1L, -2L, -3L, -4L, -5L, -6L, -8L]      ||     1
			[-1L, -2L, -3L, -4L, -5L, -6L, -7L]      ||     1
			[-1L, -2L, -3L, -4L, -5L, -6L, -7L, -8L] ||     0
		}
		
		
		def "should update column prototype when cuf code is changed" () {
			when :
			eventPublisher.publishEvent(new ChangeCustomFieldCodeEvent((String[]) codes));
			def result = findAll("ColumnPrototype").findAll {return it.attributeName == codes[1]};
			then :
			result.size == number
			where :
			codes             || number
			["xx", "AAAA"]    ||   2
			["aa", "AAAA"]    ||   3
			["ww", "AAAA"]    ||   0
		}
	
}
