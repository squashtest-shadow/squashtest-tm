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
package org.squashtest.tm.web.internal.model.rest;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.squashtest.tm.domain.testcase.TestCase;

@XmlRootElement(name="testcase")
public class RestTestCase {

	@XmlElement(name = "id")
	private Long id;
	
	@XmlElement(name = "name")
	private String name;
	
	@XmlElement(name = "description")
	private String description;
	
	@XmlElement(name = "reference")
	private String reference;
	
	@XmlElement(name = "prerequisite")
	private String prerequisite;
	
	@XmlElement(name = "importance")
	private String importance;
	
	@XmlElement(name = "nature")
	private String nature;
	
	@XmlElement(name = "type")
	private String type;
	
	@XmlElement(name = "status")
	private String status;
	
	@XmlElement(name = "execution-mode")
	private String executionMode;
	
	public RestTestCase(){
		
	}
	
	public RestTestCase(TestCase testCase) {
		this.setId(testCase.getId());
		this.name = testCase.getName();
		this.description = testCase.getDescription();
		this.reference = testCase.getReference();
		this.prerequisite = testCase.getPrerequisite();
		this.importance = testCase.getImportance().name();
		this.nature = testCase.getNature().name();
		this.type = testCase.getType().name();
		this.status = testCase.getStatus().name();
		this.executionMode = testCase.getExecutionMode().name();
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getPrerequisite() {
		return prerequisite;
	}

	public void setPrerequisite(String prerequisite) {
		this.prerequisite = prerequisite;
	}

	public String getImportance() {
		return importance;
	}

	public void setImportance(String importance) {
		this.importance = importance;
	}

	public String getNature() {
		return nature;
	}

	public void setNature(String nature) {
		this.nature = nature;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getExecutionMode() {
		return executionMode;
	}

	public void setExecutionMode(String executionMode) {
		this.executionMode = executionMode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	
}
