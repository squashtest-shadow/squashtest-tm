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

import org.squashtest.tm.domain.dataset.AbstractDataset;
import org.squashtest.tm.domain.dataset.DatasetWorkspaceElement;
import org.squashtest.tm.domain.testcase.Dataset;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import static org.squashtest.tm.domain.parameter.GlobalParameter.PARAM_TYPE;

/**
 * Class representing an {@link AbstractParameter} linked to a {@link org.squashtest.tm.domain.dataset.AbstractDataset} created in the Dataset Workspace.
 * @author aguilhem
 */
@Entity
@DiscriminatorValue(PARAM_TYPE)
public class GlobalParameter extends AbstractParameter {

	static final String PARAM_TYPE = "GLOBAL";

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinTable(name = "DATASET_PARAMETER", joinColumns = @JoinColumn(name = "PARAM_ID", insertable = false, updatable = false), inverseJoinColumns = @JoinColumn(name = "DATASET_ID", insertable = false, updatable = false))
	private AbstractDataset dataset;

	public GlobalParameter(){super();}

	public GlobalParameter(String name){
		this();
		this.name = name;
	}

	public GlobalParameter(String name, @NotNull DatasetWorkspaceElement dataset){
		this(name);
		dataset.addGlobalParameter(this);
	}

	public AbstractDataset getDataset() {
		return dataset;
	}

	public void setDataset(DatasetWorkspaceElement dataset) {
		dataset.addGlobalParameter(this);
	}
}
