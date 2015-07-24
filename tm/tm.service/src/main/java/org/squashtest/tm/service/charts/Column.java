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

/**
 * <p>
 * A Column defines which data within a perimeter is useful for a chart. The signification of a column depends on the context :
 * </p>
 * <ul>
 * 	<li>for a Perimeter, it represents one of the many data it encompasses and proposes as candidate for Axis</li>
 * 	<li>for an Axis, it represents which data will be eventually plotted</li> *
 * </ul>
 * 
 * @author bsiri
 *
 */
public class Column {

	/**
	 * an identifier for this column.
	 * Must be unique among the columns proposed by a Perimeter
	 */
	private String id;

	/**
	 * The datatype of this column;
	 */
	private Datatype datatype;

	/**
	 * A default userfriendly name for this column. This is just a default : once embedded in an Axis,
	 * a column might change name
	 */
	private String defaultLabel;


	/**
	 * Used internally. Represents the alias of the column in an hql query.
	 */
	private String columnAlias;



	public String getId() {
		return id;
	}

	public Datatype getDatatype() {
		return datatype;
	}

	public String getDefaultLabel() {
		return defaultLabel;
	}

	public String getColumnAlias() {
		return columnAlias;
	}




	public void setId(String id) {
		this.id = id;
	}

	public void setDatatype(Datatype datatype) {
		this.datatype = datatype;
	}

	public void setDefaultLabel(String defaultLabel) {
		this.defaultLabel = defaultLabel;
	}

	public void setColumnAlias(String columnAlias) {
		this.columnAlias = columnAlias;
	}



	public Column() {
		super();
	}

	public Column(String id, Datatype datatype, String defaultLabel, String columnAlias) {
		super();
		this.id = id;
		this.datatype = datatype;
		this.defaultLabel = defaultLabel;
		this.columnAlias = columnAlias;
	}





}
