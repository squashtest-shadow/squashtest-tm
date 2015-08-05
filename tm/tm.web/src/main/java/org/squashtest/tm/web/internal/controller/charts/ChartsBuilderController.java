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

import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.service.charts.Chart;
import org.squashtest.tm.service.charts.ChartBuilderService;
import org.squashtest.tm.service.charts.ChartInstance;
import org.squashtest.tm.service.charts.ChartType;
import org.squashtest.tm.service.charts.Perimeter;


@Controller
@RequestMapping("/charts-workspace")
public class ChartsBuilderController {


	@Inject
	private ChartBuilderService service;


	@RequestMapping(value="/builder")
	public String getBuilder(Model model){

		ChartsBuilderModel builderModel = buildModel();
		model.addAttribute("builderModel", builderModel);

		return "chartsbuilder-test.html";
	}


	@RequestMapping(value="/processor", consumes="application/json", produces="application/json", method = RequestMethod.POST)
	@ResponseBody
	public JsonChart getChart(@RequestBody JsonChart jsonChart){

		String perimeterId = jsonChart.getPerimeterId();
		Perimeter perimeter = findPerimeterById(perimeterId);

		Chart c = new Chart();
		c.setPerimeter(perimeter);
		c.setAxes(jsonChart.getAxes());
		c.setData(jsonChart.getData());
		c.setChartType(ChartType.valueOf(jsonChart.getChartType()));

		ChartInstance instance = service.buildChart(c);

		jsonChart.setResultSet(instance.getResponse().getData());

		return jsonChart;

	}









	private Perimeter findPerimeterById(String id){

		Collection<Perimeter> perimeters = service.getAvailablePerimeters();
		for (Perimeter p : perimeters){
			if (p.getId().equals(id)){
				return p;
			}
		}

		throw new NoSuchElementException();
	}


	private ChartsBuilderModel buildModel(){

		ChartsBuilderModel builderModel = new ChartsBuilderModel();

		// the perimeters
		Collection<Perimeter> perimeters = service.getAvailablePerimeters();
		Collection<JsonPerimeter> jperimeters = new ArrayList<>();

		for (Perimeter p : perimeters){
			JsonPerimeter jp = new JsonPerimeter(p.getId(), p.getLabel(), p.getAvailableColumns());
			jperimeters.add(jp);
		}

		builderModel.setPerimeters(jperimeters);


		return builderModel;

	}



}
