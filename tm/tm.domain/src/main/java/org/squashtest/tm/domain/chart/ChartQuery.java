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
package org.squashtest.tm.domain.chart;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * <p>This class represents a query. It is composed of  AxisColumn, Filter and MeasureColumns.</p>
 * 
 * <p>
 * 	Most queries belong to a chart definition, but not all of them : when a ColumnPrototype is
 * 	itself a subquery, this column prototype will reference a query.
 * </p>
 * 
 * @author bsiri
 *
 */
@Entity
@Table(name = "CHART_QUERY")
public class ChartQuery {

	@Id
	@Column(name = "CHART_QUERY_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "chart_query_chart_query_id_seq")
	@SequenceGenerator(name = "chart_query_chart_query_id_seq", sequenceName = "chart_query_chart_query_id_seq")
	private long id;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "QUERY_ID", nullable = false)
	private List<Filter> filters = new ArrayList<>();

	@ElementCollection
	@CollectionTable(name = "CHART_AXIS_COLUMN", joinColumns = @JoinColumn(name = "QUERY_ID") )
	@OrderColumn(name = "AXIS_RANK")
	private List<AxisColumn> axis = new ArrayList<>();

	@ElementCollection
	@CollectionTable(name = "CHART_MEASURE_COLUMN", joinColumns = @JoinColumn(name = "QUERY_ID") )
	@OrderColumn(name = "MEASURE_RANK")
	private List<MeasureColumn> measures = new ArrayList<>();


	public List<Filter> getFilters() {
		return filters;
	}

	public List<AxisColumn> getAxis() {
		return axis;
	}

	public List<MeasureColumn> getMeasures() {
		return measures;
	}

}
