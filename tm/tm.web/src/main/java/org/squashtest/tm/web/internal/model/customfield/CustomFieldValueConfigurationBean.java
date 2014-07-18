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
package org.squashtest.tm.web.internal.model.customfield;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.core.foundation.lang.DateUtils;
import org.squashtest.tm.domain.customfield.CustomFieldOption;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.InputType;
import org.squashtest.tm.domain.customfield.SingleSelectField;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldValue;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedSingleSelectField;

@Deprecated
public class CustomFieldValueConfigurationBean {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomFieldValueConfigurationBean.class);

	public static CustomFieldValueConfigurationBean createFromValues(Collection<CustomFieldValue> values) {
		List<ConfigurationBean> configurationBeans = new ArrayList<CustomFieldValueConfigurationBean.ConfigurationBean>(
				values.size());

		for (CustomFieldValue value : values) {
			InputType iType = value.getBinding().getCustomField().getInputType();
			ConfigurationBean newConf;

			switch (iType) {
			case DROPDOWN_LIST:
				newConf = new SingleSelectItem(value);
				break;
			case CHECKBOX:
				newConf = new CheckboxItem(value);
				break;
			case DATE_PICKER:
				newConf = new DatePickerItem(value);
				break;
			case RICH_TEXT :
				newConf = new RichTextItem(value);
				break;
			default:
				newConf = new PlainTextItem(value);
				break;

			}

			configurationBeans.add(newConf);
		}

		return new CustomFieldValueConfigurationBean(configurationBeans);
	}

	public static CustomFieldValueConfigurationBean createFromDenormalized(Collection<DenormalizedFieldValue> values) {
		List<ConfigurationBean> configurationBeans = new ArrayList<CustomFieldValueConfigurationBean.ConfigurationBean>(
				values.size());

		for (DenormalizedFieldValue value : values) {
			InputType iType = value.getInputType();
			ConfigurationBean newConf;

			switch (iType) {
			case DROPDOWN_LIST:
				newConf = new SingleSelectItem((DenormalizedSingleSelectField)value);
				break;
			case CHECKBOX:
				newConf = new CheckboxItem(value);
				break;
			case DATE_PICKER:
				newConf = new DatePickerItem(value);
				break;
			case RICH_TEXT :
				newConf = new RichTextItem(value);
				break;
			default:
				newConf = new PlainTextItem(value);
				break;

			}

			configurationBeans.add(newConf);
		}

		return new CustomFieldValueConfigurationBean(configurationBeans);
	}

	private List<ConfigurationBean> configurationBeans;

	public CustomFieldValueConfigurationBean() {
		super();
		configurationBeans = new ArrayList<CustomFieldValueConfigurationBean.ConfigurationBean>();
	}

	public CustomFieldValueConfigurationBean(@NotNull List<ConfigurationBean> configuration) {
		super();
		this.configurationBeans = configuration;
	}

	public List<ConfigurationBean> getConfigurationBeans() {
		return configurationBeans;
	}

	public void setConfigurationBeans(List<ConfigurationBean> configuration) {
		this.configurationBeans = configuration;
	}

	// ********************************* classes **************************

	public interface ConfigurationBean {
		String getFieldLabel();

		Long getFieldId();

		String getFieldValue();

		String getType();
	}

	/**
	 * This has to be public for it to be processable by thymeleaf
	 */
	public static abstract class DefaultItem implements ConfigurationBean {

		private String label;
		private Long id;
		protected String value;
		private boolean optional;

		public DefaultItem() {
			super();
		}

		public DefaultItem(DenormalizedFieldValue value) {
			this.id = value.getId();
			this.value = value.getValue();
			this.label = value.getLabel();
			this.optional = true;
		}

		public DefaultItem(CustomFieldValue value) {
			this.id = value.getId();
			this.value = value.getValue();
			this.label = value.getBinding().getCustomField().getLabel();
			this.optional = value.getCustomField().isOptional();
		}

		@Override
		public Long getFieldId() {
			return id;
		}

		public void setFieldId(Long id) {
			this.id = id;
		}

		@Override
		public String getFieldValue() {
			return value;
		}

		public void setFieldValue(String value) {
			this.value = value;
		}

		@Override
		public String getFieldLabel() {
			return label;
		}

		public void setFieldLabel(String label) {
			this.label = label;
		}

		public boolean isOptional() {
			return optional;
		}

		public void setOptional(boolean optional) {
			this.optional = optional;
		}

		@Override
		public abstract String getType();

	}

	/**
	 * This has to be public for it to be processable by thymeleaf
	 */
	public static class PlainTextItem extends DefaultItem {

		private static final String INPUT_TYPE = "text";

		public PlainTextItem() {
			super();
		}

		public PlainTextItem(DenormalizedFieldValue value) {
			super(value);
		}

		public PlainTextItem(CustomFieldValue value) {
			super(value);
		}

		@Override
		public String getType() {
			return INPUT_TYPE;
		}

	}

	public static class RichTextItem extends DefaultItem{

		private static final String INPUT_TYPE = "ckeditor";

		public RichTextItem(){
			super();
		}

		public RichTextItem(CustomFieldValue value){
			super(value);
		}

		public RichTextItem(DenormalizedFieldValue value){
			super(value);
		}

		@Override
		public String getType(){
			return INPUT_TYPE;
		}

	}

	/**
	 * This has to be public for it to be processable by thymeleaf
	 */
	public static class CheckboxItem extends DefaultItem {

		private static final String INPUT_TYPE = "checkbox";

		public CheckboxItem() {
			super();
		}

		public CheckboxItem(DenormalizedFieldValue value) {
			super(value);
		}

		public CheckboxItem(CustomFieldValue value) {
			super(value);
		}

		@Override
		public String getType() {
			return INPUT_TYPE;
		}

		public boolean getValueAsBoolean() {
			return Boolean.valueOf(getFieldValue());
		}

	}

	/**
	 * This has to be public for it to be processable by thymeleaf
	 */
	public static class DatePickerItem extends DefaultItem {

		private static final String INPUT_TYPE = "datepicker";

		public DatePickerItem() {
			super();
		}

		public DatePickerItem(DenormalizedFieldValue value) {
			super(value);

		}

		public DatePickerItem(CustomFieldValue value) {
			super(value);

		}

		@Override
		public String getType() {
			return INPUT_TYPE;
		}

		public Date getValueAsDate() {
			if (value == null || "".equals(value)) {
				return null;
			}

			Date date = null;

			try {
				date = DateUtils.parseIso8601Date(value);
			} catch (ParseException e) {
				LOGGER.warn(e.getMessage(), e);
			}

			return date;
		}

	}

	/**
	 * This has to be public for it to be processable by thymeleaf
	 */
	public static class SingleSelectItem extends DefaultItem {

		private static final String SELECTED = "selected";
		private static final String INPUT_TYPE = "select";

		private Map<String, String> data = new LinkedHashMap<String, String>();

		public Map<String, String> getData() {
			return data;
		}

		public void setData(Map<String, String> data) {
			this.data = data;
		}

		public SingleSelectItem() {
			super();
		}

		public SingleSelectItem(DenormalizedSingleSelectField value) {

			super(value);

			for (CustomFieldOption option : value.getOptions()) {
				this.addOption(option);
			}

			this.setSelected(value.getValue());
		}

		public SingleSelectItem(CustomFieldValue value) {
			super(value);

			SingleSelectField select = (SingleSelectField) value.getBinding().getCustomField();

			for (CustomFieldOption option : select.getOptions()) {
				this.addOption(option);
			}

			this.setSelected(value.getValue());
		}

		public final void addOption(String option) {
			data.put(option, option);
		}

		public final void setSelected(String option) {
			data.put(SELECTED, option);
		}

		public final void addOption(CustomFieldOption option) {
			data.put(option.getLabel(), option.getLabel());
		}

		public final void setSelected(CustomFieldOption option) {
			data.put(SELECTED, option.getLabel());
		}

		@Override
		public String getType() {
			return INPUT_TYPE;
		}

	}

}
