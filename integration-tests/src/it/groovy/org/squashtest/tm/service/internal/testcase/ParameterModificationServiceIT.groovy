/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package org.squashtest.tm.service.internal.testcase

import javax.inject.Inject

import org.hibernate.SessionFactory
import org.springframework.transaction.annotation.Transactional
import org.squashtest.csp.tm.internal.service.DbunitServiceSpecification
import org.unitils.dbunit.annotation.DataSet

import org.squashtest.tm.service.testcase.ParameterFinder
import org.squashtest.tm.service.testcase.ParameterModificationService
import org.squashtest.tm.service.internal.repository.ParameterDao
import org.squashtest.tm.domain.testcase.Parameter
import org.squashtest.tm.service.internal.repository.RequirementAuditEventDao

import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
class ParameterModificationServiceIT extends DbunitServiceSpecification {

	@Inject
	ParameterModificationService service;
	
	@Inject
	ParameterFinder finder;
	
	@Inject
	ParameterDao parameterDao;

	@DataSet("ParameterModificationServiceIT.xml")
	def "should return the parameter list for a given test case"(){

		when :
			List<Parameter> params = service.getAllforTestCase(100L);
		then : 
			params.size() == 1;
	}
	
	@DataSet("ParameterModificationServiceIT.xml")
	def "should return the parameter list for a given test case with call step"(){
		
		
		when :
			List<Parameter> params = service.getAllforTestCase(101L);
		then :
			params.size() == 3;
	}
}
