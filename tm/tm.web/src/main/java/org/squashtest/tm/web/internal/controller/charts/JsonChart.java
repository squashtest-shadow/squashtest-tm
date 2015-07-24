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
import java.util.List;

import org.squashtest.tm.service.charts.AxisColumn;
import org.squashtest.tm.service.charts.DataColumn;

public class JsonChart {

	private String perimeterId;

	private String chartType;

	private Collection<AxisColumn> axes;

	private Collection<DataColumn> data;

	private List<Object[]> resultSet;

	public String getPerimeterId() {
		return perimeterId;
	}

	public void setPerimeterId(String perimeterId) {
		this.perimeterId = perimeterId;
	}

	public String getChartType() {
		return chartType;
	}

	public void setChartType(String chartType) {
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

	public List<Object[]> getResultSet() {
		return resultSet;
	}

	public void setResultSet(List<Object[]> resultSet) {
		this.resultSet = resultSet;
	}

}
