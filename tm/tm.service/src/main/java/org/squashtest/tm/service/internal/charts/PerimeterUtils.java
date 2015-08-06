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
package org.squashtest.tm.service.internal.charts;

import java.util.Collection;
import java.util.List;

import org.squashtest.tm.service.charts.Column;
import org.squashtest.tm.service.charts.PerimeterQuery;


public class PerimeterUtils {


	public String getHQL(PerimeterQuery perimeter, String fromClause){

		StringBuilder hqlbuilder = new StringBuilder();

		// phase 1 : generate the select clause
		addSelectClause(hqlbuilder, perimeter);

		// phase 2 : fromClause
		hqlbuilder.append(" "+fromClause);

		// phase 3 : filters -- TODO

		// phase 4 : group by
		addGroupByClause(hqlbuilder, perimeter);

		// phase 5 : order by
		addOrderBy(hqlbuilder, perimeter);

		return hqlbuilder.toString();
	}

	/**
	 * <p>Generate a select clause with appropriate aggregations.</p>
	 * 
	 * <p>
	 * 	Rules are :
	 * 
	 * 	<ul>
	 * 		<li>Axes column will be grouped upon, no aggregation but might change according to the hierarchy</li>
	 * 		<li>Data columns will be aggregated according to the aggregation defined in the perimeter (only aggregation available for now)</li>
	 * 		<li>other columns will not be considered at all</li>
	 * 	</ul>
	 * 
	 * 
	 * </p>
	 * 
	 * @param axes
	 * @param data
	 * @return
	 */
	private void addSelectClause(StringBuilder builder, PerimeterQuery perimeter){

		Collection<Column> axes = perimeter.getAxes();
		Collection<Column> data = perimeter.getData();

		builder.append("select ");

		for (Column axe : axes){
			builder.append(axe.getColumnAlias()+", ");
		}

		for (Column d : data){
			builder.append("count(distinct "+d.getColumnAlias()+"), ");
		}

		//strip extra comma. We assume there is always one because no chart
		// would make sense with no axis and no data
		builder.replace(builder.length()-2, builder.length(), "");
	}


	/**
	 * <p>Rules for group by are follow :</p>
	 * 
	 *  <ul>
	 *  	<li>Axis column MUST be grouped upon</li>
	 *  	<li>Data column will NOT be grouped upon</li>
	 *  </ul>
	 * 
	 * 
	 * @param builder
	 * @param perimeter
	 */
	private void addGroupByClause(StringBuilder builder, PerimeterQuery perimeter){

		Collection<Column> axes = perimeter.getAxes();

		builder.append(" group by ");

		for (Column axe : axes){
			builder.append(axe.getColumnAlias()+", ");
		}

		//strip extra comma. We assume there is always one because no chart
		// would make sense with no axis and no data
		builder.replace(builder.length()-2, builder.length(), "");
	}

	/**
	 * That one is simple : we order everything ascending. However depending on the datatype of the column
	 * the definition of the order might change. For instance a requirement criticality should certainly not
	 * be ordered lexicographically.
	 * 
	 * TODO : implement that last comment about special ordering
	 * 
	 * @param builder
	 * @param perimeter
	 */
	private void addOrderBy(StringBuilder builder, PerimeterQuery perimeter){

		Collection<Column> axes = perimeter.getAxes();

		builder.append(" order by ");

		for (Column axe : axes){
			builder.append(axe.getColumnAlias()+" asc, ");
		}

		//strip extra comma. We assume there is always one because no chart
		// would make sense with no axis and no data
		builder.replace(builder.length()-2, builder.length(), "");

	}


}
