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

import org.squashtest.tm.domain.parameter.GlobalParameter;

import java.util.List;

/**
 * Interface for sub-class of {@link AbstractDataset} who appears in DatasetWorkspace tree.
 * @author aguilhem
 */
public interface DatasetWorkspaceElement {

	void addGlobalParameter(GlobalParameter globalParameter);

	void removeGlobalParameter(GlobalParameter globalParameter);

	List<GlobalParameter> getGlobalParameters();

	String getReference();

	void setReference(String reference);

	String getDescription();

	void setDescription(String description);
}
