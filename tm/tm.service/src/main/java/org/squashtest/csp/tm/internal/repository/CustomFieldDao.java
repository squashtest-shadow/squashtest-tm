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
package org.squashtest.csp.tm.internal.repository;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.squashtest.csp.tm.domain.customfield.BindableEntity;
import org.squashtest.csp.tm.domain.customfield.CustomField;

public interface CustomFieldDao extends CustomCustomFieldDao {

	List<CustomField> findAll();

	/**
	 * Will return the list of custom fields that can be bound to the given project and the given bindable entity (ie,
	 * those who aren't bound yet).
	 * 
	 * 
	 * @param projectId
	 * @param bindableEntity
	 * @return
	 */
	List<CustomField> findAllBindableCustomFields(Long projectId, BindableEntity bindableEntity);

	void persist(CustomField customField);

	CustomField findById(long id);

	
	void remove(CustomField customField);

	

	/**
	 * Returns the field matching the name if it exists.
	 * 
	 * @param name
	 * @return
	 */
	CustomField findByName(@NotNull String name);
}
