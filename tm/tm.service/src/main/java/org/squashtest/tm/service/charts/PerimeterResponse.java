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
package org.squashtest.tm.service.charts;

import java.util.List;

/**
 * Basically represents the dataset created by a Perimeter once it has processed a PerimeterQuery
 * 
 * @author bsiri
 *
 */
public class PerimeterResponse {

	/**
	 * The ordered list of columns that this dataset represents.
	 */
	private List<Column> headers;

	/**
	 * The actual data. The header above should hint you about how the data are structured.
	 */
	private List<Object[]> data;

	public List<Column> getHeaders() {
		return headers;
	}

	public void setHeaders(List<Column> headers) {
		this.headers = headers;
	}

	public List<Object[]> getData() {
		return data;
	}

	public void setData(List<Object[]> data) {
		this.data = data;
	}

	public void addData(Object[] tuple){
		this.data.add(tuple);
	}

}
