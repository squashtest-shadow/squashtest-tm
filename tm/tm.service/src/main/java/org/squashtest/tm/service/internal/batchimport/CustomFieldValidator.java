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
package org.squashtest.tm.service.internal.batchimport;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.StringUtils;
import org.squashtest.tm.core.foundation.lang.DateUtils;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldOption;
import org.squashtest.tm.domain.customfield.InputType;
import org.squashtest.tm.domain.customfield.SingleSelectField;
import org.squashtest.tm.service.importer.ImportStatus;
import org.squashtest.tm.service.importer.LogEntry;
import org.squashtest.tm.service.importer.Target;

class CustomFieldValidator {

	private static final String TRUE = "TRUE";
	private static final String FALSE = "FALSE";

	private MultiValueMap optionsByListCode = new MultiValueMap();

	public CustomFieldValidator() {
		super();
	}

	/**
	 * check the custom fields for the import mode UPDATE. Empty values are legal.
	 * 
	 * @param cufs
	 * @param definitions
	 * @return
	 */
	LogTrain checkUpdateCustomFields(Target target, Map<String, String> cufs, Collection<CustomField> definitions) {

		LogTrain train = new LogTrain();

		for (CustomField cuf : definitions) {

			LogEntry check = null;
			String code = cuf.getCode();
			String value = cufs.get(code);

			// we only care if the value is not blank (blank is legal and means 'no change' )
			if (!StringUtils.isBlank(value)) {

				check = checkCustomField(target, code, value, cuf, Messages.IMPACT_NO_CHANGE);

				train.addEntry(check);
			}
		}

		return train;

	}

	/**
	 * a strong check does not tolerate empty values for custom fields when the said custom field is mandatory. Useful
	 * for modes create or replace
	 * 
	 * @param cufs
	 *            a map of (CODE, VALUE) for each custom fields read in the file
	 * @param definitions
	 *            the list of custom fields to check against
	 * @return
	 */
	LogTrain checkCreateCustomFields(Target target, Map<String, String> cufs, Collection<CustomField> definitions) {

		LogTrain train = new LogTrain();

		for (CustomField cuf : definitions) {

			LogEntry check = null;
			String code = cuf.getCode();
			String value = cufs.get(code);

			if (!StringUtils.isBlank(value)) {
				check = checkCustomField(target, code, value, cuf, Messages.IMPACT_DEFAULT_VALUE);
			} else if (!cuf.isOptional()) {
				check = new LogEntry(target, ImportStatus.WARNING, Messages.ERROR_MANDATORY_CUF, new String[] { code },
						Messages.IMPACT_DEFAULT_VALUE, null);
			}
			// else no big deal
			if (check != null) {
				train.addEntry(check);
			}
		}

		return train;
	}

	@SuppressWarnings("unchecked")
	private LogEntry checkCustomField(Target target, String inputCode, String inputValue, CustomField cuf,
			String impactmsg) {

		LogEntry check = null;
		InputType type = cuf.getInputType();

		switch (type) {

		case PLAIN_TEXT:
			if (inputValue.length() > 255) {
				String[] cufCodeArg = new String[] { inputCode };
				check = new LogEntry(target, ImportStatus.WARNING, Messages.ERROR_MAX_SIZE, cufCodeArg,
						Messages.IMPACT_MAX_SIZE, null);
			}
			break;

		case CHECKBOX:
			if (!(TRUE.equalsIgnoreCase(inputValue) || FALSE.equalsIgnoreCase(inputValue))) {
				String[] cufCodeArg = new String[] { inputCode };
				check = new LogEntry(target, ImportStatus.WARNING, Messages.ERROR_UNPARSABLE_CHECKBOX, cufCodeArg,
						impactmsg, null);
			}
			break;

		case DATE_PICKER:
			// if the weak check is not enough, swap for the string check
			if (!DateUtils.weakCheckIso8601Date(inputValue)) {
				String[] cufCodeArg = new String[] { inputCode };
				check = new LogEntry(target, ImportStatus.WARNING, Messages.ERROR_UNPARSABLE_DATE, cufCodeArg,
						impactmsg, null);
			}
			break;

		case DROPDOWN_LIST:
			// cache the options if needed
			registerOptions(cuf);
			Collection<String> options = (Collection<String>) optionsByListCode.getCollection(cuf.getCode());
			if (!options.contains(inputValue)) {
				String[] cufCodeArg = new String[] { inputCode };
				check = new LogEntry(target, ImportStatus.WARNING, Messages.ERROR_UNPARSABLE_OPTION, cufCodeArg,
						impactmsg, null);
			}
			break;

		default:
			check = new LogEntry(target, ImportStatus.WARNING, Messages.ERROR_UNKNOWN_CUF_TYPE, new String[] {
					inputCode, cuf.getInputType().toString() }, Messages.IMPACT_NO_CHANGE, null);
			break;
		}

		return check;
	}

	private void registerOptions(CustomField cuf) {
		String code = cuf.getCode();
		if (!optionsByListCode.containsKey(cuf.getCode())) {
			List<CustomFieldOption> options = ((SingleSelectField) cuf).getOptions();
			for (CustomFieldOption op : options) {
				optionsByListCode.put(code, op.getCode());
			}
		}
	}

}
