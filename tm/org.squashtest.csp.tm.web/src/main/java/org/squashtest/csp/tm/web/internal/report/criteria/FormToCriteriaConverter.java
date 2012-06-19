/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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

package org.squashtest.csp.tm.web.internal.report.criteria;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.api.report.criteria.Criteria;
import org.squashtest.tm.api.report.form.InputType;

/**
 * This class builds a map of {@link Criteria} from the map of objects which was submitted by a report form.
 * 
 * @author Gregory Fouquet
 * 
 */
public class FormToCriteriaConverter {

	/**
	 * 
	 */
	private static final String INPUT_SELECTED = "selected";
	/**
	 * 
	 */
	private static final String INPUT_VALUE = "value";
	/**
	 * 
	 */
	private static final String INPUT_TYPE = "type";
	private static final Logger LOGGER = LoggerFactory.getLogger(FormToCriteriaConverter.class);

	@SuppressWarnings("unchecked")
	public Map<String, Criteria<?>> convert(Map<String, Object> formValues) {
		HashMap<String, Criteria<?>> res = new HashMap<String, Criteria<?>>();

		for (Map.Entry<String, Object> entry : formValues.entrySet()) {
			String name = entry.getKey();
			Object inputValue = entry.getValue();

			if (inputValue instanceof Collection) {
				Collection<Map<String, Object>> optionValues = (Collection<Map<String, Object>>) inputValue;
				Criteria<?> crit = convertMultiValuedEntry(name, optionValues);
				res.put(name, crit);
			} else if (inputValue instanceof Map) {
				Map<String, Object> map = (Map<String, Object>) inputValue;
				Criteria<?> crit = convertSimpleEntry(name, map);
				res.put(name, crit);

			} else {
				LOGGER.error("Form {} contains non convertible entry {}", formValues, entry);
				throw new FormEntryNotConvertibleException(entry);
			}
		}

		return res;
	}

	/**
	 * @param name
	 * @param multiValued
	 * @return
	 */
	private Criteria<?> convertMultiValuedEntry(String name, Collection<Map<String, Object>> multiValued) {
		InputType inputType = extractInputType(multiValued);
		return convertMultiValuedEntry(name, multiValued, inputType);
	}

	/**
	 * @param name
	 * @param multiValued
	 * @param inputType
	 * @return
	 */
	private Criteria<?> convertMultiValuedEntry(String name, Collection<Map<String, Object>> multiValued,
			InputType inputType) {
		Criteria<?> res;
		switch (inputType) {
		case CHECKBOXES_GROUP:
			res = createMultiOptionsCriteria(name, multiValued, inputType);
			break;
		case RADIO_BUTTONS_GROUP:
		case DROPDOWN_LIST:
			res = createSingleOptionCriteria(name, multiValued, inputType);
			break;
		default :
			res = EmptyCriteria.createEmptyCriteria(name, inputType); 
		}
		return res;
	}

	private Criteria<?> createSingleOptionCriteria(String name, Collection<Map<String, Object>> multiValued,
			InputType inputType) {
		for (Map<String, Object> valueItem : multiValued) {
			Boolean selected = (Boolean) valueItem.get(INPUT_SELECTED);
			if (selected) {
				return new SimpleCriteria<String>(name, (String) valueItem.get(INPUT_VALUE), inputType);
			}
		}
		return new EmptyCriteria(name, inputType);
	}

	private Criteria<?> createMultiOptionsCriteria(String name, Collection<Map<String, Object>> multiValued, InputType inputType) {
		MultiOptionsCriteria crit = new MultiOptionsCriteria(name, inputType);
		for (Map<String, Object> valueItem : multiValued) {
			Boolean selected = (Boolean) valueItem.get(INPUT_SELECTED);
			String value = (String) valueItem.get(INPUT_VALUE);
			crit.addOption(value, selected);
		}
		return crit;
	}

	/**
	 * @param multiValued
	 * @return
	 */
	private InputType extractInputType(Collection<Map<String, Object>> multiValued) {
		String type = null;

		for (Map<String, Object> valueItem : multiValued) {
			if (type == null) {
				type = (String) valueItem.get(INPUT_TYPE);
			} else {
				if (!type.equals(valueItem.get(INPUT_TYPE))) {
					throw new InconsistentMultiValuedEntryException(multiValued);
				}
			}
		}
		return InputType.valueOf(type);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Criteria<?> convertSimpleEntry(String name, Map<String, Object> entry) {
		String type = (String) entry.get(INPUT_TYPE);
		InputType inputType = InputType.valueOf(type);
		Object value = convertValue(entry, inputType);

		return new SimpleCriteria(name, value, inputType);
	}

	private Object convertValue(Map<String, Object> entry, InputType inputType) {
		Object converted;
		switch (inputType) {
		case CHECKBOX:
			converted = Boolean.parseBoolean((String) entry.get(INPUT_SELECTED));
			break;
		case TEXT:
		default:
			converted = entry.get(INPUT_VALUE);
		}
		return converted;
	}
}