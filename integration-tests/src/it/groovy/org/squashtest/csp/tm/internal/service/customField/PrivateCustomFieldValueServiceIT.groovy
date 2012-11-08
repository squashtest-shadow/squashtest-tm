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


import javax.inject.Inject

import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Transactional
import org.squashtest.csp.tm.domain.customfield.BindableEntity;
import org.squashtest.csp.tm.domain.project.Project
import org.squashtest.csp.tm.internal.service.DbunitServiceSpecification;
import org.unitils.dbunit.annotation.DataSet
import org.squashtest.csp.tm.service.customfield.CustomFieldBindingModificationService
import org.squashtest.csp.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.csp.tm.internal.repository.BoundEntityDao
import org.squashtest.csp.tm.internal.repository.CustomFieldValueDao;
import org.squashtest.csp.tm.internal.repository.TestCaseDao;


import spock.lang.Specification;
import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
@DataSet("CustomFieldVariousIT.sandbox.xml")
public class PrivateCustomFieldValueServiceIT extends DbunitServiceSpecification{
	
	
	@Inject
	PrivateCustomFieldValueService service

	@Inject
	SessionFactory factory
	
	@Inject
	TestCaseDao tcDao
	
	@Inject
	CustomFieldValueDao cfvDao
	
	def "should create and copy the custom fields from one entity to another"(){
		
		given :
			def src = tcDao.findById(112l)
			def dest = tcDao.findById(113l)
		
		when :
			service.copyCustomFieldValues(src, dest)
			def fields = cfvDao.findAllCustomValues(113l, BindableEntity.TEST_CASE)
			
		then :
			fields.collect{it.value} == ["SEC-2", "false"]
		
	}
	
	def "should copy content of the custom fields from one entity to another"(){
		
		given :
			def src = tcDao.findById(112l)
			def dest = tcDao.findById(111l)
		
		when :
			service.copyCustomFieldValuesContent(src, dest)
			def fields = cfvDao.findAllCustomValues(111l, BindableEntity.TEST_CASE)
			
		then :
			fields.collect{it.value} == ["SEC-2", "false"]
		
		
	}
	

	
}