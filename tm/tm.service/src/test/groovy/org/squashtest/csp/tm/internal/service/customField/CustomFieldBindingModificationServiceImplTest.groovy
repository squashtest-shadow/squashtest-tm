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
import org.squashtest.csp.tm.domain.customfield.BindableEntity;
import org.squashtest.csp.tm.domain.customfield.CustomField
import org.squashtest.csp.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.project.ProjectTemplate;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder
import org.squashtest.csp.tm.internal.repository.CustomFieldBindingDao
import org.squashtest.csp.tm.internal.repository.CustomFieldDao
import org.squashtest.csp.tm.internal.repository.GenericProjectDao;
import org.squashtest.csp.tm.internal.repository.ProjectDao;
import org.squashtest.csp.tm.service.customfield.CustomFieldBindingModificationService
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder
import org.squashtest.tm.core.foundation.collection.PagingAndSorting

import spock.lang.Specification

class CustomFieldBindingModificationServiceImplTest extends Specification {

	CustomFieldBindingModificationServiceImpl service = new CustomFieldBindingModificationServiceImpl()
	CustomFieldDao customFieldDao = Mock()
	CustomFieldBindingDao customFieldBindingDao = Mock()
	ProjectDao projectDao = Mock()
	GenericProjectDao genericProjectDao = Mock()
	PrivateCustomFieldValueService customValueService = Mock()
	
	def setup() {
		service.customFieldDao = customFieldDao
		service.customFieldBindingDao = customFieldBindingDao
		service.projectDao = projectDao
		service.genericProjectDao = genericProjectDao
		service.customValueService = customValueService
	}

	def "should copy paste cuf binding from template"(){ 
		given:"a project"
		Project project = Mock()
		project.getId()>>3L
		projectDao.findById(3L)>>project
		genericProjectDao.findById(3L)>>project
		and:"a template"
		ProjectTemplate template = Mock()
		template.getId()>>2L
		CustomField cuf = Mock()
		cuf.getId() >> 4L
		customFieldDao.findById(4L)>>cuf
		BindableEntity entity1 = Mock()
		customFieldBindingDao.countAllForProjectAndEntity(3L, entity1) >> 1
		BindableEntity entity2 = Mock()
		customFieldBindingDao.countAllForProjectAndEntity(3L, entity2) >> 2
		CustomFieldBinding binding1 = Mock()
		binding1.getBoundEntity() >> entity1
		binding1.getCustomField() >> cuf
		CustomFieldBinding binding2 = Mock()
		binding2.getBoundEntity() >> entity2
		binding2.getCustomField() >> cuf
		List<CustomFieldBinding> bindings = [binding1, binding2]
		customFieldBindingDao.findAllForGenericProject(2L)>> bindings
		
		when:
		service.copyCustomFieldsSettingsFromTemplate(project, template)
		then:
		2*customFieldBindingDao.persist(_)
		2*customValueService.cascadeCustomFieldValuesCreation(_)
	}
}
