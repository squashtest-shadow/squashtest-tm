/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.testcase.steps;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.squashtest.tm.core.foundation.lang.IsoDateUtils;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.domain.testcase.TestStepVisitor;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;

/**
 * Builds a DataTable model for TestSteps table.
 * 
 * @author Gregory Fouquet
 * 
 */
public class TestStepsTableModelBuilder extends DataTableModelBuilder<TestStep> implements TestStepVisitor {
	/**
	 * 
	 */
	private static final int DEFAULT_MAP_CAPACITY = 16;

	private Map<Long, Map<String, CustomFieldValueTableModel>> customFieldValuesById;

	private final MessageSource messageSource;
	private final Locale locale;
	private Map<?, ?> lastBuiltItem;

	public TestStepsTableModelBuilder(MessageSource messageSource, Locale locale) {
		this.messageSource = messageSource;
		this.locale = locale;
	}

	/**
	 * 
	 * @see org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder#buildItemData(java.lang.Object)
	 */
	@Override
	protected Map<?, ?> buildItemData(TestStep item) {
		item.accept(this);
		return lastBuiltItem;
	}

	/**
	 * Creates a model row from the visited item and stores it as {@link #lastBuiltItem}
	 */
	@Override
	public void visit(ActionTestStep visited) {

		Map<Object, Object> item = new HashMap<Object, Object>(11);

		item.put("step-id", visited.getId());
		item.put("step-index", getCurrentIndex());
		item.put("attach-list-id", visited.getAttachmentList().getId());
		item.put("step-action", visited.getAction());
		item.put("step-result", visited.getExpectedResult());
		item.put("nb-attachments", visited.getAttachmentList().size());
		item.put("step-type", "action");
		item.put("called-tc-id", null);
		item.put("empty-requirements-holder", null);
		item.put("empty-browse-holder", null);
		item.put("empty-delete-holder", null);
		item.put("has-requirements", !visited.getRequirementVersionCoverages().isEmpty());
		item.put("nb-requirements", visited.getRequirementVersionCoverages().size());

		appendCustomFields(item);

		lastBuiltItem = item;

	}

	@Override
	public void visit(CallTestStep visited) {
		TestCase called = visited.getCalledTestCase();

		String action = messageSource.getMessage("test-case.call-step.action.template",
				new Object[] { called.getName() }, locale);

		Map<Object, Object> item = new HashMap<Object, Object>(11);

		item.put("step-id", visited.getId());
		item.put("step-index", getCurrentIndex());
		item.put("attach-list-id", null);
		item.put("step-action", action);
		item.put("step-result", null);
		item.put("nb-attachments", null);
		item.put("step-type", "call");
		item.put("called-tc-id", called.getId());
		item.put("empty-requirements-holder", null);
		item.put("empty-browse-holder", null);
		item.put("empty-delete-holder", null);
		item.put("has-requirements", false);
		item.put("nb-requirements", null);

		appendCustomFields(item);

		lastBuiltItem = item;

	}

	private void appendCustomFields(Map<Object, Object> item) {
		Map<String, CustomFieldValueTableModel> cufValues = getCustomFieldsFor((Long) item.get("step-id"));
		item.put("customFields", cufValues);

	}

	public void usingCustomFields(Collection<CustomFieldValue> cufValues, int nbFieldsPerEntity) {
		customFieldValuesById = new HashMap<Long, Map<String, CustomFieldValueTableModel>>();

		for (CustomFieldValue value : cufValues) {
			Long entityId = value.getBoundEntityId();
			Map<String, CustomFieldValueTableModel> values = customFieldValuesById.get(entityId);

			if (values == null) {
				values = new HashMap<String, CustomFieldValueTableModel>(nbFieldsPerEntity);
				customFieldValuesById.put(entityId, values);
			}

			values.put(value.getCustomField().getCode(), new CustomFieldValueTableModel(value));

		}
	}

	protected static class CustomFieldValueTableModel {
		private static final Logger LOGGER = LoggerFactory.getLogger(CustomFieldValueTableModel.class);

		private String value;
		private Long id;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public CustomFieldValueTableModel() {
			super();
		}

		public Date getValueAsDate() {
			try {
				return IsoDateUtils.parseIso8601Date(value);
			} catch (ParseException e) {
				LOGGER.debug("Unable to parse date {} of custom field #{}", value, id);
			}

			return null;
		}

		private CustomFieldValueTableModel(CustomFieldValue value) {
			this.id = value.getId();
			this.value = value.getValue();
		}

	}

	private Map<String, CustomFieldValueTableModel> getCustomFieldsFor(Long id) {
		if (customFieldValuesById == null) {
			return new HashMap<String, CustomFieldValueTableModel>();
		}

		Map<String, CustomFieldValueTableModel> values = customFieldValuesById.get(id);

		if (values == null) {
			values = new HashMap<String, CustomFieldValueTableModel>();
		}
		return values;

	}

	public void usingCustomFields(Collection<CustomFieldValue> cufValues) {
		usingCustomFields(cufValues, DEFAULT_MAP_CAPACITY);
	}

}
