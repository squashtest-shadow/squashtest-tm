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
package org.squashtest.tm.domain.denormalizedfield;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OrderColumn;
import javax.validation.Valid;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.domain.customfield.CustomFieldOption;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.SingleSelectField;
import org.squashtest.tm.exception.WrongStringSizeException;
import org.squashtest.tm.exception.customfield.CodeAlreadyExistsException;
import org.squashtest.tm.exception.customfield.CodeDoesNotMatchesPatternException;
import org.squashtest.tm.exception.customfield.OptionAlreadyExistException;

/**
 * An Editable Denormalized Custom Field which stores a single option selected from a list.
 * 
 * @author Frederic Laurens
 */

@NamedQueries({ @NamedQuery(name = "denormalizedSingleSelectField.findById", query = "from DenormalizedSingleSelectField dssf where dssf.id = :id")})
@Entity
@DiscriminatorValue("SSF")
public class DenormalizedSingleSelectField extends DenormalizedFieldValue {

	@ElementCollection
	@CollectionTable(name = "DENORMALIZED_FIELD_OPTION", joinColumns = @JoinColumn(name = "DFV_ID"))
	@OrderColumn(name = "POSITION")
	@Valid
	private List<CustomFieldOption> options = new ArrayList<CustomFieldOption>();

	/**
	 * Created a SingleSelectField with a
	 */
	public DenormalizedSingleSelectField() {
		super();
	}

	public DenormalizedSingleSelectField(CustomFieldValue customFieldValue, Long denormalizedFieldHolderId,
			DenormalizedFieldHolderType denormalizedFieldHolderType) {
		super(customFieldValue, denormalizedFieldHolderId, denormalizedFieldHolderType);
		SingleSelectField singleSelectField = (SingleSelectField) customFieldValue.getCustomField();
		for(CustomFieldOption option : singleSelectField.getOptions()){
			this.addOption(option);
		}
	}

	public DenormalizedSingleSelectField(String value, CustomFieldBinding binding, Long denormalizedFieldHolderId,
			DenormalizedFieldHolderType denormalizedFieldHolderType) {
		super(value, binding, denormalizedFieldHolderId, denormalizedFieldHolderType);
		SingleSelectField singleSelectField = (SingleSelectField) binding.getCustomField();
		for(CustomFieldOption option : singleSelectField.getOptions()){
			this.addOption(option);
		}
	}

	public DenormalizedSingleSelectField(CustomFieldValue customFieldValue, int newBindingPosition,
			Long denormalizedFieldHolderId, DenormalizedFieldHolderType denormalizedFieldHolderType) {
		super(customFieldValue, newBindingPosition, denormalizedFieldHolderId, denormalizedFieldHolderType);
		SingleSelectField singleSelectField = (SingleSelectField) customFieldValue.getCustomField();
		for(CustomFieldOption option : singleSelectField.getOptions()){
			this.addOption(option);
		}
	}

	/**
	 * Will check if label and the code are available among the existing options. If so, will add the new option at the
	 * end of the list. Else will throw a NameAlreadyInUseException or CodeAlreadyExistsException.
	 * 
	 * @throws OptionAlreadyExistsException
	 * @param option
	 *            : the new option
	 */
	// made 'final' because it's used in the constructor (and SONAR complained about that)
	public final void addOption(CustomFieldOption option) {
		checkLabelAvailable(option.getLabel());
		checkCodeAvailable(option.getCode());
		// TODO fix [Task 1682] and remove this line
		checkCodeMatchesPattern(option.getCode());
		options.add(option);
	}

	// TODO fix [Task 1682] and remove this method
	private void checkCodeMatchesPattern(String code) {
		if (!code.matches(CustomField.CODE_REGEXP)) {
			throw new CodeDoesNotMatchesPatternException(code, CustomField.CODE_REGEXP, "optionCode");
		}
		if (code.length() > CustomField.MAX_CODE_SIZE || code.length() < CustomField.MIN_CODE_SIZE) {
			throw new WrongStringSizeException("code", CustomField.MIN_CODE_SIZE, CustomField.MAX_CODE_SIZE);
		}
	}

	private void checkCodeAvailable(String code) {
		if (!isCodeAvailable(code)) {
			throw new CodeAlreadyExistsException(null, code, CustomFieldOption.class);
		}
	}

	private void checkLabelAvailable(String label) {
		// TODO fix [Task 1682] and remove the first check
		if (label.length() > 255 || label.length() < 1) {
			throw new WrongStringSizeException("label", 1, 255);
		}

		if (!isLabelAvailable(label)) {
			throw new OptionAlreadyExistException(label);
		}
	}

	private boolean isLabelAvailable(String newlabel) {
		return findIndexOfLabel(newlabel) == -1;
	}

	private boolean isCodeAvailable(String newCode) {
		return findIndexOfCode(newCode) == -1;
	}

	private int findIndexOfCode(String newCode) {
		Iterator<CustomFieldOption> it = options.iterator();

		while (it.hasNext()) {
			CustomFieldOption option = it.next();
			if (newCode.equals(option.getCode())) {
				return options.indexOf(option);
			}
		}
		return -1;
	}

	private int findIndexOfLabel(String previousLabel) {
		Iterator<CustomFieldOption> it = options.iterator();

		while (it.hasNext()) {
			CustomFieldOption option = it.next();
			if (previousLabel.equals(option.getLabel())) {
				return options.indexOf(option);
			}
		}
		return -1;
	}

	public List<CustomFieldOption> getOptions() {
		return Collections.unmodifiableList(options);
	}

	public void accept(DenormalizedFieldVisitor visitor) {
		visitor.visit(this);
	}
}
