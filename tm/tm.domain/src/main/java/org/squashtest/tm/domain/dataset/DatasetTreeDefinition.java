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

import org.squashtest.tm.domain.tree.TreeEntityDefinition;

public enum DatasetTreeDefinition implements TreeEntityDefinition {

	DATASET(false, DatasetNodeType.DATASET_NAME),
	COMPOSITE(false, DatasetNodeType.COMPOSITE_NAME),
	TEMPLATE(false, DatasetNodeType.TEMPLATE_NAME),
	FOLDER(true, DatasetNodeType.FOLDER_NAME),
	LIBRARY(true, DatasetNodeType.LIBRARY_NAME);

	private boolean container;

	private final String typeIdentifier;

	DatasetTreeDefinition(boolean container, String typeIdentifier){
		this.container = container;
		this.typeIdentifier = typeIdentifier;
	}

	@Override
	public String getTypeName() {
		return null;
	}

	@Override
	public boolean isContainer() {
		return false;
	}
}
