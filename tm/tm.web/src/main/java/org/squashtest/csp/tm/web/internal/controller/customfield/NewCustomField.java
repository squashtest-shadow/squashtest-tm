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

package org.squashtest.csp.tm.web.internal.controller.customfield;

import java.util.List;

import org.squashtest.csp.tm.domain.customfield.CustomField;
import org.squashtest.csp.tm.domain.customfield.InputType;
import org.squashtest.csp.tm.domain.customfield.SingleSelectField;

/**
 * @author Gregory Fouquet
 * 
 */
public class NewCustomField extends CustomField {
	private InputType inputType;
	private List<String> options;

	public NewCustomField() {
		super(InputType.PLAIN_TEXT);
	}

	public CustomField createTransientEntity() {
		CustomField res;
		switch (inputType) {
		case DROPDOWN_LIST:
			res = createSingleSelectField();
			
			break;
		default:
			res = new CustomField(inputType);
		}
		res.setCode(getCode());
		res.setLabel(getLabel());
		res.setName(getName());
		res.setOptional(isOptional());
		res.setDefaultValue(getDefaultValue());
		
		return res;
	}

	private CustomField createSingleSelectField() {
		CustomField res;
		SingleSelectField ssf = new SingleSelectField();
		 
		for(String option : options) {
			ssf.addOption(option);
		}
		
		res = ssf;
		return res;
	}

	/**
	 * @return the inputType
	 */
	@Override
	public InputType getInputType() {
		return inputType;
	}

	/**
	 * @param inputType
	 *            the inputType to set
	 */
	public void setInputType(InputType inputType) {
		this.inputType = inputType;
	}

	/**
	 * @return the options
	 */
	public List<String> getOptions() {
		return options;
	}

	/**
	 * @param options
	 *            the options to set
	 */
	public void setOptions(List<String> options) {
		this.options = options;
	}
}
