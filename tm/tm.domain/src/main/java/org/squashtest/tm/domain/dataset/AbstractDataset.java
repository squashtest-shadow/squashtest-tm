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
package org.squashtest.tm.domain.dataset;

import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.tm.domain.Sizes;
import org.squashtest.tm.domain.audit.Auditable;
import org.squashtest.tm.domain.testcase.DatasetParamValue;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.*;

@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DATASET_TYPE", discriminatorType = DiscriminatorType.STRING)
@Entity
@Auditable
@Table(name = "DATASET")
public abstract class AbstractDataset {

	public static final int MAX_NAME_SIZE = Sizes.NAME_MAX;
	public static final int MAX_REF_SIZE = 50;

	@Id
	@Column(name = "DATASET_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "dataset_dataset_id_seq")
	@SequenceGenerator(name = "dataset_dataset_id_seq", sequenceName = "dataset_dataset_id_seq", allocationSize = 1)
	protected Long id;

	@NotBlank
	@Size(max = MAX_NAME_SIZE)
	@Column
	protected String name;

	@NotNull
	@OneToMany(cascade = { CascadeType.ALL }, mappedBy="dataset")
	protected Set<DatasetParamValue> parameterValues = new HashSet<>(0);

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<DatasetParamValue> getParameterValues() {
		return Collections.unmodifiableSet(this.parameterValues);
	}

	public void addParameterValue(@NotNull DatasetParamValue datasetParamValue) {
		this.parameterValues.add(datasetParamValue);
	}

	public void removeParameterValue(@NotNull DatasetParamValue datasetParamValue) {
		this.parameterValues.remove(datasetParamValue);
	}

}
