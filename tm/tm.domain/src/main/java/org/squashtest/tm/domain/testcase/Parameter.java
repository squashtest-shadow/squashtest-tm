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
package org.squashtest.tm.domain.testcase;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.tm.domain.Identified;

@Entity
@Table(uniqueConstraints={@UniqueConstraint(columnNames={"NAME","TEST_CASE_ID"})})
public class Parameter implements Identified{
	
	public static final String CODE_REGEXP = "^[A-Za-z0-9_-]*$";
	public static final int MIN_CODE_SIZE = 1;
	public static final int MAX_CODE_SIZE = 255;
	
	@Id
	@GeneratedValue
	@Column(name = "PARAM_ID")
	private Long id;
	
	@NotBlank
	@Pattern(regexp = CODE_REGEXP)
	@Size(min = MIN_CODE_SIZE, max = MAX_CODE_SIZE)
	private String name;
	
	@Lob
	private String description="";
	
	
	@ManyToOne
	@JoinColumn(name = "TEST_CASE_ID", referencedColumnName = "TCLN_ID")
	private TestCase testCase;
	
	@OneToMany(mappedBy="parameter", cascade={CascadeType.REMOVE})
	private List<DatasetParamValue> datasetParamValues = new ArrayList<DatasetParamValue>();
	
	public Parameter(){
		super();
	}
	
	public Parameter(String name,@NotNull TestCase testCase) {
		super();
		this.name = name;
		this.testCase = testCase;
		this.testCase.addParameter(this);
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
	public void setDescription(@NotNull String description) {
		this.description = description;
	}
	public TestCase getTestCase() {
		return testCase;
	}
	public void setTestCase(@NotNull TestCase testCase) {
		this.testCase = testCase;
	}
	public Long getId() {
		return id;
	}
	
	
	
}
