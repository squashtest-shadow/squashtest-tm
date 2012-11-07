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
package org.squashtest.csp.tm.internal.service.customField;

import java.util.List;

import javax.inject.Inject;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.squashtest.csp.tm.domain.DuplicateNameException;
import org.squashtest.csp.tm.domain.customfield.CustomField;
import org.squashtest.csp.tm.domain.customfield.SingleSelectField;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.csp.tm.internal.repository.CustomFieldDao;
import org.squashtest.csp.tm.service.customfield.CustomCustomFieldManagerService;

/**
 * Implementations for (non dynamically generated) custom-field management services.
 * 
 * @author mpagnon
 * 
 */
@Service("CustomCustomFieldManagerService")
public class CustomCustomFieldManagerServiceImpl implements CustomCustomFieldManagerService {

	@Inject
	private CustomFieldDao customFieldDao;

	/**
	 * @see org.squashtest.csp.tm.service.customfield.CustomFieldFinderService#findAllOrderedByName()
	 */
	@Override
	public List<CustomField> findAllOrderedByName() {
		return customFieldDao.finAllOrderedByName();
	}

	/**
	 * @see org.squashtest.csp.tm.service.customfield.CustomFieldFinderService#findSortedCustomFields(CollectionSorting)
	 */
	@Override
	public FilteredCollectionHolder<List<CustomField>> findSortedCustomFields(CollectionSorting filter) {
		List<CustomField> customFields = customFieldDao.findSortedCustomFields(filter);
		long count = customFieldDao.countCustomFields();
		return new FilteredCollectionHolder<List<CustomField>>(count, customFields);
	}

	/**
	 * @see org.squashtest.csp.tm.service.customfield.CustomCustomFieldManagerService#deleteCustomField(long)
	 */
	@Override
	public void deleteCustomField(long customFieldId) {
		CustomField customField = customFieldDao.findById(customFieldId);
		customFieldDao.remove(customField);

	}

	/**
	 * @see org.squashtest.csp.tm.service.customfield.CustomCustomFieldManagerService#persist(org.squashtest.csp.tm.domain.customfield.CustomField)
	 */
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void persist(CustomField newCustomField) {
		CustomField cf = customFieldDao.findByName(newCustomField.getName());

		if (cf != null) {
			throw new NameAlreadyInUseException("CustomField", newCustomField.getName());
		} else {
			customFieldDao.persist(newCustomField);
		}
	}

	/**
	 * @see org.squashtest.csp.tm.service.customfield.CustomCustomFieldManagerService#changeName(long, String)
	 */
	@Override
	public void changeName(long customFieldId, String newName) {
		CustomField customField = customFieldDao.findById(customFieldId);
		String oldName = customField.getName();
		if (customFieldDao.findByName(newName) != null) {
			throw new DuplicateNameException(oldName, newName);
		} else {
			customField.setName(newName);
		}

	}

	/**
	 * @see org.squashtest.csp.tm.service.customfield.CustomCustomFieldManagerService#changeOptional(Long, Boolean)
	 */
	@Override
	public void changeOptional(Long customFieldId, Boolean optional) {
		CustomField customField = customFieldDao.findById(customFieldId);
		if (!optional) {
			checkDefaultValueExists(customField);
			// TODO add all necessary customFieldValues
		}
		customField.setOptional(optional);
	}

	private void checkDefaultValueExists(CustomField customField) {
		if (customField.getDefaultValue() == null || customField.getDefaultValue().equals("")) {
			throw new MandatoryCufNeedsDefaultValueException();
		}

	}

	/**
	 * @see org.squashtest.csp.tm.service.customfield.CustomCustomFieldManagerService#changeOptionLabel(Long, String,
	 *      String)
	 */
	@Override
	public void changeOptionLabel(Long customFieldId, String optionLabel, String newLabel) {
		SingleSelectField customField = customFieldDao.findSingleSelectFieldById(customFieldId);
		customField.changeOption(optionLabel, newLabel);
	}

	/**
	 * @see org.squashtest.csp.tm.service.customfield.CustomCustomFieldManagerService#addOption(Long, String)
	 */
	@Override
	public void addOption(Long customFieldId, String label) {
		SingleSelectField customField = customFieldDao.findSingleSelectFieldById(customFieldId);
		customField.addOption(label);

	}

	/**
	 * @see org.squashtest.csp.tm.service.customfield.CustomFieldFinderService#findSingleSelectFieldById(Long)
	 */
	@Override
	public SingleSelectField findSingleSelectFieldById(Long customFieldId) {
		return customFieldDao.findSingleSelectFieldById(customFieldId);
	}

	/**
	 * @see org.squashtest.csp.tm.service.customfield.CustomCustomFieldManagerService#removeOption(long, String)
	 */
	@Override
	public void removeOption(long customFieldId, String optionLabel) {
		SingleSelectField customField = customFieldDao.findSingleSelectFieldById(customFieldId);
		customField.removeOption(optionLabel);
	}

	/**
	 * @see org.squashtest.csp.tm.service.customfield.CustomCustomFieldManagerService#changeOptionsPositions(long, int,
	 *      List)
	 */
	@Override
	public void changeOptionsPositions(long customFieldId, int newIndex, List<String> optionsLabels) {
		SingleSelectField customField = customFieldDao.findSingleSelectFieldById(customFieldId);
		customField.moveOptions(newIndex, optionsLabels);
	}

}
