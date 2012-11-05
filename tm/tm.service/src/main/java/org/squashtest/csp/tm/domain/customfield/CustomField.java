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
package org.squashtest.csp.tm.domain.customfield;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @author Gregory Fouquet
 */

@NamedQueries({
	@NamedQuery(name="customField.findAllOrderedByName", query="from CustomField cf order by cf.name"),
	@NamedQuery(name="customField.count", query="select count(*) from CustomField")
})
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "FIELD_TYPE", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("CF")
public class CustomField {
	@Id
	@GeneratedValue
	@Column(name = "CF_ID")
	private Long id;

	@NotBlank
	@Size(min = 0, max = 255)
	private String name;

	@NotBlank
	@Size(min = 0, max = 255)
	private String label = "";

	private boolean optional = true;

	@Size(min = 0, max = 255)
	private String defaultValue;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(updatable = false)
	private InputType inputType = InputType.PLAIN_TEXT;

	/**
	 * For ORM purposes.
	 */
	protected CustomField() {
		super();
	}

	public CustomField(@NotNull InputType inputType) {
		super();
		this.inputType = inputType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isOptional() {
		return optional;
	}

	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public Long getId() {
		return id;
	}

	public InputType getInputType() {
		return inputType;
	}
}
