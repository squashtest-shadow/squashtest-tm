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
package org.squashtest.tm.bugtracker.advanceddomain;

import java.util.HashMap;
import java.util.Map;

import org.squashtest.tm.bugtracker.definition.RemoteCategory;
import org.squashtest.tm.bugtracker.definition.RemoteIssue;
import org.squashtest.tm.bugtracker.definition.RemotePriority;
import org.squashtest.tm.bugtracker.definition.RemoteStatus;
import org.squashtest.tm.bugtracker.definition.RemoteUser;
import org.squashtest.tm.bugtracker.definition.RemoteVersion;

public class AdvancedIssue implements RemoteIssue {
	
	//maps a fieldId to a FieldValue
	private Map<String, FieldValue> fieldValues = new HashMap<String, FieldValue>();
	
	private AdvancedProject project ;
	
	private String key;
	
	private String btName;
	
	//the name of the fields scheme currently used, see AdvancedProject#schemes
	private String currentScheme;
	
	public void setId(String key){
		this.key = key;
	}
	
	@Override
	public String getId() {
		return key;
	}

	@Override
	public String getSummary() {
		return findFieldValueName("summary");
	}

	@Override
	public String getDescription() {
		return findFieldValueName("description");
	}

	@Override
	public void setDescription(String description) {
		setFieldValueName("description", description);
	}

	@Override
	public String getComment() {
		return findFieldValueName("comment");
	}
	
	@Override
	public void setComment(String comment) {
		setFieldValueName("comment", comment);	
	}
	
	@Override
	public AdvancedProject getProject() {
		return project;
	}

	@Override
	public RemoteStatus getStatus() {
		return fieldValues.get("status");
	}

	@Override
	public RemoteUser getAssignee() {
		return fieldValues.get("assignee");
	}

	@Override
	public RemotePriority getPriority() {
		return fieldValues.get("priority");
	}

	@Override
	public RemoteCategory getCategory() {
		return fieldValues.get("category");
	}

	@Override
	public RemoteVersion getVersion() {
		return fieldValues.get("version");
	}

	@Override
	public void setBugtracker(String btName) {
		this.btName = btName;
	}

	@Override
	public String getBugtracker() {
		return btName;
	}
	
	public void setFieldValue(String fieldName, FieldValue fieldValue){
		fieldValues.put(fieldName, fieldValue);
	}
	
	public FieldValue getFieldValue(String fieldName){
		return fieldValues.get(fieldName);
	}
	
	public void setFieldValues(Map<String, FieldValue> fieldValues){
		this.fieldValues = fieldValues;
	}
	
	public Map<String, FieldValue> getFieldValues(){
		return fieldValues;
	}
	
	public String getCurrentScheme() {
		return currentScheme;
	}

	public void setCurrentScheme(String currentScheme) {
		this.currentScheme = currentScheme;
	}

	public void setProject(AdvancedProject project) {
		this.project = project;
	}

	
	
	// ********************* private stuffs ***************************


	private String findFieldValueName(String fieldId){
		FieldValue value = fieldValues.get("fieldId");
		return (value!=null) ? value.getName() : "";			
	}

	private void setFieldValueName(String fieldId, String newName){
		FieldValue value = fieldValues.get(fieldId);
		if (value!=null){
			value.setScalar(newName);
		}
	}
}
