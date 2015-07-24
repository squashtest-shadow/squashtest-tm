/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.charts;

import java.util.Collection;

import org.squashtest.tm.service.charts.Column;

public class JsonPerimeter {

	private String id;

	private String label;

	private Collection<Column> availableColumns;

	public JsonPerimeter(String id, String label, Collection<Column> availableColumns) {
		super();
		this.id = id;
		this.label = label;
		this.availableColumns = availableColumns;
	}

	public JsonPerimeter(){
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Collection<Column> getAvailableColumns() {
		return availableColumns;
	}

	public void setAvailableColumns(Collection<Column> availableColumns) {
		this.availableColumns = availableColumns;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}
