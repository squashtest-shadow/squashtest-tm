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

public class Chart {

	private Perimeter perimeter;

	private ChartType chartType;

	/**
	 * What axes, picked among the columns available in the perimeter, will be plotted. Size of the collection must be consistent
	 * with the selected ChartType;
	 */
	private Collection<AxisColumn> axes;


	/**
	 * What data, picked among the columns available in the perimeter, will be plotted. Size of the collection must be consistent
	 * with the selected ChartType;
	 */
	private Collection<DataColumn> data;


	public Perimeter getPerimeter() {
		return perimeter;
	}


	public void setPerimeter(Perimeter perimeter) {
		this.perimeter = perimeter;
	}


	public ChartType getChartType() {
		return chartType;
	}


	public void setChartType(ChartType chartType) {
		this.chartType = chartType;
	}


	public Collection<AxisColumn> getAxes() {
		return axes;
	}


	public void setAxes(Collection<AxisColumn> axes) {
		this.axes = axes;
	}


	public Collection<DataColumn> getData() {
		return data;
	}


	public void setData(Collection<DataColumn> data) {
		this.data = data;
	}

}
