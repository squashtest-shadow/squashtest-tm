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
package org.squashtest.tm.web.internal.model.json;

import java.util.HashSet;
import java.util.Set;

/**
 * JSON used to forward to client side all the data needed to render a complete dashboard.
 * Avoids n requests where n = number of charts in dashboard, as all charts datas are retrieved in on request
 * @author jthebault
 *
 */
public class JsonCustomReportDashboard {
	
	private Long id;
	
	private String name;
	
	private Set<JsonCustomReportChartBinding> chartBindings = new HashSet<JsonCustomReportChartBinding>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<JsonCustomReportChartBinding> getChartBindings() {
		return chartBindings;
	}

	public void setChartBindings(Set<JsonCustomReportChartBinding> chartBindings) {
		this.chartBindings = chartBindings;
	}
	
}
