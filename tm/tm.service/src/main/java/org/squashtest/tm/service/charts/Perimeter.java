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

import java.util.Collection;

/**
 * The Perimeter of a chart represents which data are encompassed by this charts before someone selects the axises, filters and aggregation.
 * It also defines a HQL/Criteria/SQL query, that may be tuned by a PerimeterQuery.
 * 
 * @author bsiri
 *
 */
public interface Perimeter {

	/**
	 * Each Perimeter is identified by an ID. This method returns it.
	 * 
	 * @return
	 */
	String getId();

	/**
	 * Must return an non null collection of Column, that are available for dataplot
	 * 
	 * @return
	 */
	Collection<Column> getAvailableColumns();

	/**
	 * Consumes a query that defines filters, aggregations etc and produces a response accordingly.
	 * 
	 * @param query
	 * @return
	 */
	PerimeterResponse process(PerimeterQuery query);

}
