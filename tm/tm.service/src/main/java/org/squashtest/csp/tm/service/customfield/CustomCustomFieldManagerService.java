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
package org.squashtest.csp.tm.service.customfield;

import javax.validation.constraints.NotNull;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.customfield.CustomField;
import org.squashtest.csp.tm.domain.DuplicateNameException;
import org.squashtest.csp.tm.domain.customfield.CustomField;
import org.squashtest.csp.tm.domain.customfield.CustomFieldOption;

/**
 * Custom-Field manager services which cannot be dynamically generated.
 * 
 * @author mpagnon
 * 
 */
@Transactional
@PreAuthorize("hasRole('ROLE_ADMIN')")
public interface CustomCustomFieldManagerService extends CustomFieldFinderService {
	/** 
	 * Will delete the custom-field entity
	 * 
	 * @param customFieldId
	 *            : the id of the custom field to delete
	 */
	void deleteCustomField(long customFieldId);

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	void persist(@NotNull CustomField newCustomField);

	/**
	 * Will check if new name is available among all custom fields and, if so, will change the name of the concerned {@link CustomField}.
	 * 
	 * @param customFieldId
	 *            the id of the concerned {@link CustomField}
	 * @param newName
	 *            the {@link CustomField} potential new name
	 */
	public void changeName(long customFieldId, String newName);

	/**
	 * Will change the optional attribute of the concerned {@link CustomField}<br>
	 * If custom-field becomes mandatory, all necessary CustomFieldValues will be added. Otherwise, nothing special needs
	 * to be done.
	 *  
	 * @param customFieldId
	 *            the id of the concerned {@link CustomField}
	 * @param optional
	 *            : <code>true</code> if the custom-field changes to be optional<br>
	 *            <code>false</code> if it changes to be mandatory
	 */
	public void changeOptional(Long customFieldId, Boolean optional);
	

	/**
	 * Will check if the new label is available among all the concerned {@link CustomField}'s {@link CustomFieldOption}, 
	 * if so, will change the label of the concerned custom-field's option.
	 * 
	 * @throws DuplicateNameException
	 * @param customFieldId : the id of the concerned {@link CustomField}
	 * @param optionLabel : the current {@link CustomFieldOption}'s label
	 * @param newLabel : the potential new label for the concerned custom-field's option
	 */
	public void changeOptionLabel(Long customFieldId, String optionLabel, String newLabel);
	
	/**
	 * Will check if the new option's label is available among all the concerned {@link CustomField}'s {@link CustomFieldOption},
	 * if so, will add the new option at the bottom of the list.
	 * 
	 * @throws NameAlreadyInUseException
	 * @param customFieldId : the id of the concerned {@link CustomField}
	 * @param label : the label of the potential new option.
	 */
	public void addOption(Long customFieldId, String label);
}
