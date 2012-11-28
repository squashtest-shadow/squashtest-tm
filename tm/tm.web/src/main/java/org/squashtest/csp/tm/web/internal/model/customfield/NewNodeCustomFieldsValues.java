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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.squashtest.csp.tm.domain.customfield.CustomFieldValue;

public class NewNodeCustomFieldsValues implements Iterable<NewNodeCustomFieldsValues.Value>{

	private Map<String, String> customFieldValues = new HashMap<String, String>();
	private String modelName;
	private String ATTRIBUTE_PREFIX = "custom-field-";
	private BindingResult bindingResults;
	

	
	public NewNodeCustomFieldsValues() {
		super();
	}


	public NewNodeCustomFieldsValues(String modelName, Map<String, String> customFieldValues) {
		super();
		this.customFieldValues = customFieldValues;
		this.modelName = modelName;
	}




	public NewNodeCustomFieldsValues(String attributePrefix) {
		super();
		this.ATTRIBUTE_PREFIX = attributePrefix;
	}


	public void setCustomFieldValues(Map<String, String> customFieldValues) {
		this.customFieldValues = customFieldValues;
	}


	public String getAttributePrefix() {
		return ATTRIBUTE_PREFIX;
	}



	public void setAttributePrefix(String attributePrefix) {
		this.ATTRIBUTE_PREFIX = attributePrefix;
	}

	

	public boolean hasValidationErrors(){
		return (bindingResults!=null && bindingResults.hasErrors());
	}

	

	public void validate(){
		
		bindingResults = new MapBindingResult(customFieldValues, modelName);
		
		Pattern pattern = Pattern.compile("^"+ATTRIBUTE_PREFIX+".*");
		
		for (Entry<String, String > entry : customFieldValues.entrySet()){
			
			String name = entry.getKey();
			
			if (pattern.matcher(name).matches()){
				
				String value = entry.getValue().trim();
				
				if (value.isEmpty()){
					
					bindingResults.rejectValue(name, "NotBlank", "(*)");
					
				}
				
			}
			
		}
	}
	
	public String getValueFor(CustomFieldValue customFieldValue){
		
		String searchedKey = ATTRIBUTE_PREFIX+customFieldValue.getBinding().getId();
		
		return customFieldValues.get(searchedKey);
		
	}
	
	
	public boolean hasValueFor(CustomFieldValue customFieldValue){

		String searchedKey = ATTRIBUTE_PREFIX+customFieldValue.getBinding().getId();
		
		return customFieldValues.containsKey(searchedKey);
	}
	
	
	public void puke() throws BindException{
		throw new BindException(bindingResults);
	}
	
	
	@Override
	public Iterator<Value> iterator() {
		return new ValueIterator(ATTRIBUTE_PREFIX, customFieldValues.entrySet().iterator());		
	}
	
	public static class Value{
		Long bindingId;
		String value;
		
		public Value(Long id, String value){
			this.bindingId = id;
			this.value = value;
		}

		public Long getBindingId() {
			return bindingId;
		}

		public String getValue() {
			return value;
		}
		
	}
	
	
	private static class ValueIterator implements Iterator<Value>{

		private Pattern _attributeMatcher;

		private Value _currentValue=null;
		private Value _nextValue=null;
		
		private Iterator<Entry<String, String>> _iterator; 
		
		private ValueIterator(String prefix, Iterator<Entry<String, String>> iterator){
			_attributeMatcher = Pattern.compile("^"+prefix+"(\\d*)");
			_iterator = iterator;
		}
		
		@Override
		public boolean hasNext() {
			lookForNext();
			if (_nextValue == null){
				return false;
			}
			else{
				return true;
			}
		}

		@Override
		public Value next() {
			_currentValue = _nextValue;
			_nextValue = null;
			return _currentValue;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
		private void lookForNext(){
			
			_nextValue = null;
			
			while (_iterator.hasNext()){
				
				Entry<String, String> next = _iterator.next();
				String name = next.getKey();
				
				Matcher matcher = _attributeMatcher.matcher(name);
				
				if (matcher.matches()){
					
					Long bindingId = Long.valueOf(matcher.group(1));
					Value value = new Value(bindingId, next.getValue());
					
					_nextValue = value;
					
				}
			}
			
		}
		
	}
	
}

