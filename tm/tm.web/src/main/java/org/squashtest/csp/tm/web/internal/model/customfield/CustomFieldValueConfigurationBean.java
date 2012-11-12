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
package org.squashtest.csp.tm.web.internal.model.customfield;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.squashtest.csp.tm.domain.customfield.CustomFieldOption;
import org.squashtest.csp.tm.domain.customfield.CustomFieldValue;
import org.squashtest.csp.tm.domain.customfield.InputType;
import org.squashtest.csp.tm.domain.customfield.SingleSelectField;

public class CustomFieldValueConfigurationBean {

	
	private List<ConfigurationBean> configurationBeans = new LinkedList<ConfigurationBean>();
	
	
	public CustomFieldValueConfigurationBean(){
		super();
	}
	
	
	public CustomFieldValueConfigurationBean(
			List<ConfigurationBean> configuration) {
		super();
		this.configurationBeans = configuration;
	}
	
	
	public CustomFieldValueConfigurationBean(Collection<CustomFieldValue> values){
		
		for (CustomFieldValue value : values){
			
			InputType iType = value.getBinding().getCustomField().getInputType();
			
			ConfigurationBean newConf;
			
			switch (iType) {
			case DROPDOWN_LIST : newConf = new SingleSelectItem(value);
								break;
								
			case CHECKBOX : newConf = new CheckboxItem(value);
								break;
								
			default : newConf = new PlainTextItem(value);
								break;
			
			}
			
			configurationBeans.add(newConf);
		}
	}

	
	
	public List<ConfigurationBean> getConfigurationBeans() {
		return configurationBeans;
	}

	public void setConfigurationBeans(List<ConfigurationBean> configuration) {
		this.configurationBeans = configuration;
	}

	
	
	// ********************************* classes **************************
	
	
	public static interface ConfigurationBean{
		String getFieldName();
		Long getFieldId();
		String getFieldValue();
		String getType();
	}

	
	public static abstract class DefaultItem implements ConfigurationBean {
		
		private String name;
		private Long id;
		private String value;
		
		public DefaultItem() {
			super();
		}
		
		public DefaultItem(CustomFieldValue value){
			this.id = value.getId();
			this.value=value.getValue();
			this.name = value.getBinding().getCustomField().getName();
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
		public String getFieldName() {
			return name;
		}

		public void setFieldName(String name) {
			this.name = name;
		}

		@Override
		public abstract String getType();
		
	}
	
	
	public static class PlainTextItem extends DefaultItem{
		
		private static final String INPUT_TYPE="text";

		public PlainTextItem(){
			super();
		}
		
		public PlainTextItem(CustomFieldValue value){
			super(value);
		}

		@Override
		public String getType() {
			return INPUT_TYPE;
		}
		
	}
	

	public static class CheckboxItem extends DefaultItem{
		
		private static final String INPUT_TYPE = "checkbox";
		
		public CheckboxItem(){
			super();
		}
		
		public CheckboxItem(CustomFieldValue value){
			super(value);
		}
		
		@Override
		public String getType() {
			return INPUT_TYPE;
		}
		
		public boolean getValueAsBoolean(){
			return Boolean.valueOf(getFieldValue());
		}
		
	}
	
	
	public static class SingleSelectItem  extends DefaultItem{

		private static final String SELECTED = "selected";
		private static final String INPUT_TYPE="select";
		
		private Map<String,String> data = new LinkedHashMap<String, String>();

		public Map<String, String> getData() {
			return data;
		}
		
		public void setData(Map<String, String> data) {
			this.data = data;
		}
		
		public SingleSelectItem(){
			super();
		}
		
		public SingleSelectItem(CustomFieldValue value){
			super(value);
			
			SingleSelectField select = (SingleSelectField)value.getBinding().getCustomField();
			
			for (CustomFieldOption option : select.getOptions()){
				this.addOption(option);
			}
			
			this.setSelected(value.getValue());
		}
		

		public void addOption(String option){
			data.put(option, option);
		}
		
		public void setSelected(String option){
			data.put(SELECTED, option);
		}
		
		public void addOption(CustomFieldOption option){
			data.put(option.getLabel(), option.getLabel());
		}
		
		public void setSelected(CustomFieldOption option){
			data.put(SELECTED, option.getLabel());
		}
		
		@Override
		public String getType() {
			return INPUT_TYPE;
		}
		
	}
	
}
