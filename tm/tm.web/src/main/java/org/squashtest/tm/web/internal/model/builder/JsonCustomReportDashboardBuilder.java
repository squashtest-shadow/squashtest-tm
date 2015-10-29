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
package org.squashtest.tm.web.internal.model.builder;

import java.util.Set;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.chart.ChartInstance;
import org.squashtest.tm.domain.customreport.CustomReportChartBinding;
import org.squashtest.tm.domain.customreport.CustomReportDashboard;
import org.squashtest.tm.service.chart.ChartModificationService;
import org.squashtest.tm.web.internal.controller.chart.JsonChartInstance;
import org.squashtest.tm.web.internal.model.json.JsonCustomReportChartBinding;
import org.squashtest.tm.web.internal.model.json.JsonCustomReportDashboard;

@Component
@Scope("prototype")
public class JsonCustomReportDashboardBuilder {
	
	private ChartModificationService chartService;
	
	private JsonCustomReportDashboard json = new JsonCustomReportDashboard();
	
	private CustomReportDashboard dashboard;
	
	public JsonCustomReportDashboardBuilder(ChartModificationService chartService) {
		super();
		this.chartService = chartService;
	}
	
	public JsonCustomReportDashboard build(CustomReportDashboard dashboard){
		this.dashboard = dashboard;
		doBaseAttributes();
		doBindings();
		return json;
	}

	private void doBindings() {
		Set<CustomReportChartBinding> bindings = dashboard.getChartBindings();
		for (CustomReportChartBinding binding : bindings) {
			JsonCustomReportChartBinding jsonBinding = new JsonCustomReportChartBinding();
			jsonBinding.setId(binding.getId());
			jsonBinding.setChartDefinitionId(binding.getChart().getId());
			jsonBinding.setPosX(binding.getPosX());
			jsonBinding.setPosY(binding.getPosY());
			jsonBinding.setSizeX(binding.getSizeX());
			jsonBinding.setSizeY(binding.getSizeY());
			ChartInstance chartInstance = chartService.generateChart(binding.getChart());
			jsonBinding.setChartInstance(new JsonChartInstance(chartInstance));
			json.getChartBindings().add(jsonBinding);
		}
	}

	private void doBaseAttributes() {
		json.setId(dashboard.getId());
		json.setName(dashboard.getName());
	}
}
