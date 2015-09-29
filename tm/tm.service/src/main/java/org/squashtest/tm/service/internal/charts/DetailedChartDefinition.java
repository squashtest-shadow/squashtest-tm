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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.squashtest.tm.domain.chart.ChartDefinition;
import org.squashtest.tm.domain.chart.ColumnRole;
import org.squashtest.tm.domain.chart.EntityType;
import org.squashtest.tm.domain.chart.Filter;


/**
 * see javadoc on {@link ChartDataFinder}
 * 
 * @author bsiri
 *
 */
class DetailedChartDefinition extends ChartDefinition{

	private EntityType rootEntity;

	private List<EntityType> targetEntities;

	private List<Filter> scopeFilters;


	DetailedChartDefinition(ChartDefinition parent){

		super();

		// todo : merge main attributes with the ChartDefinition

		// find the root entity
		rootEntity = parent.getMeasures().get(0).getEntityType();

		// find all the target entities
		Map<ColumnRole, Set<EntityType>> entitiesByRole = parent.getInvolvedEntities();

		targetEntities = new ArrayList<>();
		for (Set<EntityType> types : entitiesByRole.values()){
			targetEntities.addAll(types);
		}

	}


	protected EntityType getRootEntity() {
		return rootEntity;
	}


	protected void setRootEntity(EntityType rootEntity) {
		this.rootEntity = rootEntity;
	}


	protected List<EntityType> getTargetEntities() {
		return targetEntities;
	}


	protected void setTargetEntities(List<EntityType> targetEntities) {
		this.targetEntities = targetEntities;
	}


	protected List<Filter> getScopeFilters() {
		return scopeFilters;
	}


	protected void setScopeFilters(List<Filter> scopeFilters) {
		this.scopeFilters = scopeFilters;
	}



}
