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

import org.squashtest.tm.domain.Sizes;
import org.squashtest.tm.domain.customreport.TreeEntityVisitor;
import org.squashtest.tm.domain.parameter.GlobalParameter;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.tree.TreeEntity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import java.util.List;

import static org.squashtest.tm.domain.dataset.CompositeDataset.DATASET_TYPE;

/**
 * @author aguilhem
 */
@Entity
@DiscriminatorValue(DATASET_TYPE)
public class CompositeDataset extends AbstractDataset implements TreeEntity {

	public static final int MAX_NAME_SIZE = Sizes.NAME_MAX;
	static final String DATASET_TYPE = "COMPOSITE";

	@Override
	public void accept(TreeEntityVisitor visitor) {

	}

	@Override
	public Project getProject() {
		return null;
	}

	@Override
	public void setProject(Project project) {

	}

	@Override
	public TreeEntity createCopy() {
		return null;
	}

	@Override
	public List<GlobalParameter> getGlobalParameters() {
		return globalParameters;
	}

	@Override
	public void addGlobalParameter(GlobalParameter globalParameter) {
		this.globalParameters.add(globalParameter);
	}

	@Override
	public void removeGlobalParameter(GlobalParameter globalParameter) {
		this.globalParameters.remove(globalParameter);
	}
}
