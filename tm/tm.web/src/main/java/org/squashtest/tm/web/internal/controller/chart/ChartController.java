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
package org.squashtest.tm.web.internal.controller.chart;

import javax.inject.Inject;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.domain.chart.ChartDefinition;
import org.squashtest.tm.domain.chart.ChartInstance;
import org.squashtest.tm.service.chart.ChartModificationService;
import org.squashtest.tm.service.user.UserAccountService;
import org.squashtest.tm.web.internal.http.ContentTypes;
import org.squashtest.tm.web.internal.model.json.JsonChartWizardData;

@Controller
@RequestMapping("charts")
public class ChartController {

	@Inject
	private UserAccountService userService;

	@Inject
	private ChartModificationService chartService;

	@RequestMapping(method = RequestMethod.GET, produces = ContentTypes.APPLICATION_JSON)
	@ResponseBody
	public JsonChartWizardData getWizardData() {
		return new JsonChartWizardData(chartService.getColumnPrototypes());
	}

	@RequestMapping(value = "/wizard/{parentId}", method = RequestMethod.GET)
	public String getWizard(@PathVariable Long parentId) {
		return "charts/wizard/wizard.html";
	}

	@RequestMapping(value = "/{definitionId}/instance", method = RequestMethod.GET)
	public @ResponseBody JsonChartInstance generate(@PathVariable("definitionId") Long definitionId){
		ChartInstance instance = chartService.generateChart(definitionId);
		return new JsonChartInstance(instance);
	}

	@RequestMapping(value = "/instance", method = RequestMethod.POST)
	public @ResponseBody JsonChartInstance generate(@RequestBody @Valid ChartDefinition definition) {
		ChartInstance instance = chartService.generateChart(definition);
		return new JsonChartInstance(instance);
	}

	// ******************* TEMPORARY TEST CODE BELOW *********************

	@RequestMapping(value = "/test-page", method = RequestMethod.GET)
	public String getTestPage(){
		return "charts-render-test.html";
	}

	@RequestMapping(value = "/new", method = RequestMethod.POST, consumes = ContentTypes.APPLICATION_JSON)
	public @ResponseBody void createNewChartDefinition(@RequestBody @Valid ChartDefinition definition) {

		definition.setOwner(userService.findCurrentUser());
		chartService.persist(definition);
	}

}
