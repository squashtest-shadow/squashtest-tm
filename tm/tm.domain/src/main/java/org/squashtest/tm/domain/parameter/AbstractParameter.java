/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) Henix, henix.fr
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
package org.squashtest.tm.domain.parameter;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.Sizes;
import org.squashtest.tm.domain.testcase.DatasetParamValue;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * @author aguilhem
 */

@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "PARAM_TYPE", discriminatorType = DiscriminatorType.STRING)
@Entity
@Table(name = "PARAMETER")
public abstract class AbstractParameter implements Identified {

	private static final String PARAM_REGEXP = "[A-Za-z0-9_-]{1,255}";
	public static final String NAME_REGEXP = "^" + PARAM_REGEXP + "$";
	public static final int MIN_NAME_SIZE = 1;
	public static final int MAX_NAME_SIZE = Sizes.NAME_MAX;

	public static final String USAGE_PREFIX = "${";
	public static final String USAGE_SUFFIX = "}";
	public static final String USAGE_PATTERN = "\\Q" + USAGE_PREFIX + "\\E(" + PARAM_REGEXP + ")\\Q" + USAGE_SUFFIX
		+ "\\E";
	@Id
	@Column(name = "PARAM_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "parameter_param_id_seq")
	@SequenceGenerator(name = "parameter_param_id_seq", sequenceName = "parameter_param_id_seq", allocationSize = 1)
	protected Long id;

	@NotBlank
	@Pattern(regexp = NAME_REGEXP)
	@Size(min = MIN_NAME_SIZE, max = MAX_NAME_SIZE)
	protected String name;

	@Lob
	@Type(type="org.hibernate.type.TextType")
	protected String description = "";

	@OneToMany(mappedBy = "parameter", cascade = { CascadeType.REMOVE })
	private List<DatasetParamValue> datasetParamValues = new ArrayList<>();

	public String getName() {
		return name;
	}

	public void setName(String newName) {
		this.name = newName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(@NotNull String description) {
		this.description = description;
	}

	@Override
	public Long getId() {
		return id;
	}

	public GlobalParameter createGlobalParameterTypeCopy() {
		GlobalParameter copy = new GlobalParameter();
		copy.setName(this.name);
		copy.setDescription(this.description);
		return copy;
	}
}
