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
package org.squashtest.tm.web.internal.controller.search;

import java.util.List;

public class SearchInputFieldModel {

	private String name;
	
	private String inputType;

	private List<SearchInputPossibleValueModel> possibleValues;
	
	public SearchInputFieldModel(){
		
	}
	
	public SearchInputFieldModel(String name, String inputType){
		this.name = name;
		this.inputType = inputType;
	}

	public SearchInputFieldModel(String name, String inputType, List<SearchInputPossibleValueModel> possibleValues){
		this(name,inputType);
		this.possibleValues = possibleValues;
	}
	
	public void addPossibleValue(SearchInputPossibleValueModel value){
		this.possibleValues.add(value);
	}
	
	public void addPossibleValue(String value, String code){
		this.possibleValues.add(new SearchInputPossibleValueModel(value, code));
	}

	public String getInputType() {
		return inputType;
	}

	public void setInputType(String inputType) {
		this.inputType = inputType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<SearchInputPossibleValueModel> getPossibleValues() {
		return possibleValues;
	}

	public void setPossibleValues(List<SearchInputPossibleValueModel> possibleValues) {
		this.possibleValues = possibleValues;
	}
}
