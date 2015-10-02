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

import static org.squashtest.tm.domain.EntityType.CAMPAIGN;
import static org.squashtest.tm.domain.EntityType.EXECUTION;
import static org.squashtest.tm.domain.EntityType.ISSUE;
import static org.squashtest.tm.domain.EntityType.ITEM_TEST_PLAN;
import static org.squashtest.tm.domain.EntityType.ITERATION;
import static org.squashtest.tm.domain.EntityType.REQUIREMENT;
import static org.squashtest.tm.domain.EntityType.REQUIREMENT_VERSION;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.chart.ChartType;
import org.squashtest.tm.domain.chart.ColumnPrototype;

public class JsonChartWizardData {

	private Map<EntityType, Set<ColumnPrototype>> columnPrototypes;

	private EnumSet<ChartType> chartTypes = EnumSet.allOf(ChartType.class);

	private EnumSet<EntityType> entityTypes = EnumSet.of(CAMPAIGN, EXECUTION, ISSUE, ITEM_TEST_PLAN, ITERATION,
			REQUIREMENT, REQUIREMENT, REQUIREMENT_VERSION);

	public JsonChartWizardData(Map<EntityType, Set<ColumnPrototype>> columnPrototypes) {

		this.columnPrototypes = columnPrototypes;

	}

	public Map<EntityType, Set<ColumnPrototype>> getColumnPrototypes() {
		return columnPrototypes;
	}

	public EnumSet<ChartType> getChartTypes() {
		return chartTypes;
	}

	public EnumSet<EntityType> getEntityTypes() {
		return entityTypes;
	}

}
