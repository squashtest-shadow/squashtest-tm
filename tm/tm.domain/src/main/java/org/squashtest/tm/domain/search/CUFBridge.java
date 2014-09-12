/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.domain.search;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.ParameterizedBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.InputType;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;

public class CUFBridge extends SessionFieldBridge implements ParameterizedBridge {
	private static final Logger LOGGER = LoggerFactory.getLogger(CUFBridge.class);

	private SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	private String type = "";
	private String inputType = "";

	@SuppressWarnings("unchecked")
	private List<CustomFieldValue> findCufValuesForType(Session session, Object value) {

		BindableEntity entityType = null;
		Long id = null;

		if ("testcase".equals(type)) {
			TestCase testcase = (TestCase) value;
			id = testcase.getId();
			entityType = BindableEntity.TEST_CASE;

		} else if ("requirement".equals(type)) {
			RequirementVersion requirement = (RequirementVersion) value;
			id = requirement.getId();
			entityType = BindableEntity.REQUIREMENT_VERSION;
		}
		Criteria crit = session.createCriteria(CustomFieldValue.class).createAlias("binding", "binding")
				.createAlias("binding.customField", "cuf");
		crit.add(Restrictions.eq("boundEntityId", id)).add(Restrictions.eq("boundEntityType", entityType));

		SimpleExpression typeDropDownList = Restrictions.eq("cuf.inputType", InputType.DROPDOWN_LIST);
		if (inputType != null && inputType.equals(InputType.DROPDOWN_LIST.name())) {
			crit.add(typeDropDownList);
		} else {
			crit.add(Restrictions.not(typeDropDownList));
		}
		return crit.list();

	}

	@Override
	public void setParameterValues(Map<String, String> parameters) {
		if (parameters.containsKey("type")) {
			this.type = (String) parameters.get("type");
		}
		if (parameters.containsKey("inputType")) {
			this.inputType = (String) parameters.get("inputType");
		}
	}

	@Override
	protected void writeFieldToDocument(String name, Session session, Object value, Document document,
			LuceneOptions luceneOptions) {

		List<CustomFieldValue> cufValues = findCufValuesForType(session, value);

		for (CustomFieldValue cufValue : cufValues) {

			InputType inputType = cufValue.getBinding().getCustomField().getInputType();
			String code = cufValue.getBinding().getCustomField().getCode();
			String val = null;

			switch (inputType) {
			case DATE_PICKER:
				val = formatDate(cufValue);
				break;
			case DROPDOWN_LIST:
				val = cufValue.getValue();
				if ("".equals(val)) {
					val = "$NO_VALUE";
				}
				break;
			default:
				val = cufValue.getValue();
			}

			if (val != null) {
				Field field = new Field(code, val, luceneOptions.getStore(), luceneOptions.getIndex(),
						luceneOptions.getTermVector());
				field.setBoost(luceneOptions.getBoost());
				document.add(field);
			}
		}
	}

	/**
	 * Formats a DATE cuf value
	 * 
	 * @param fieldValue
	 * @return
	 */
	private String formatDate(CustomFieldValue fieldValue) {
		String value = fieldValue.getValue();
		
		if (StringUtils.isEmpty(value)) {
			// wont parse as date, early exit
			return null;
		}
		
		try {
			Date inputDate = inputFormat.parse(fieldValue.getValue());
			return dateFormat.format(inputDate);
		} catch (ParseException e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Cannot parse as date custom field of value '" + fieldValue.getValue() + '\'', e);
			}
		}
		
		return null;
	}

}
