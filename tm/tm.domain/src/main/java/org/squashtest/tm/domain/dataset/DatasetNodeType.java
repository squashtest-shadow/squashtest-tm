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

/**
 * This interface just define the name of custom report entity. Please make names consistent with
 * {@link DatasetTreeDefinition} because hibernate have mapped values on these Strings in
 * {@link DatasetLibraryNode#getEntity()} AND in {@link DatasetLibraryNode#getEntityType()}.
 *
 * As these values are represented by columns data in database, PLEASE DON'T MODIFY these values.
 * You can however freely add new types here but you have to put a corresponding type in {@link DatasetTreeDefinition}
 * with a consistent name
 * @author aguilhem
 *
 */
public interface DatasetNodeType {
	String DATASET_NAME = "DATASET";
	String COMPOSITE_NAME = "COMPOSITE";
	String TEMPLATE_NAME = "TEMPLATE";
	String FOLDER_NAME = "FOLDER";
	String LIBRARY_NAME = "LIBRARY";
}
