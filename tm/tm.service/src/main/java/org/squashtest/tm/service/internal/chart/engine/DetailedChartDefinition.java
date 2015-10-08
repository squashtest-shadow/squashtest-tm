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
package org.squashtest.tm.service.internal.chart.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.chart.AxisColumn;
import org.squashtest.tm.domain.chart.ChartDefinition;
import org.squashtest.tm.domain.chart.ColumnRole;
import org.squashtest.tm.domain.chart.Filter;
import org.squashtest.tm.domain.chart.MeasureColumn;


/**
 * see javadoc on {@link ChartDataFinder}
 *
 * @author bsiri
 *
 */
class DetailedChartDefinition extends ChartDefinition{

	private InternalEntityType rootEntity;

	private List<InternalEntityType> targetEntities;


	// for testing purposes - do not use
	DetailedChartDefinition(){
		super();
	}

	DetailedChartDefinition(ChartDefinition parent){

		super();

		// todo : better merge for main attributes with the ChartDefinition
		getAxis().addAll(parent.getAxis());

		getFilters().addAll(parent.getFilters());

		getMeasures().addAll(parent.getMeasures());

		getScope().addAll(parent.getScope());


		// find the root entity
		rootEntity = InternalEntityType.fromDomainType(parent.getMeasures().get(0).getEntityType());

		// find all the target entities
		Map<ColumnRole, Set<EntityType>> entitiesByRole = parent.getInvolvedEntities();

		targetEntities = new ArrayList<>();
		for (Set<EntityType> types : entitiesByRole.values()){
			for (EntityType type : types){
				targetEntities.add(InternalEntityType.fromDomainType(type));
			}
		}

	}


	protected InternalEntityType getRootEntity() {
		return rootEntity;
	}


	protected void setRootEntity(InternalEntityType rootEntity) {
		this.rootEntity = rootEntity;
	}


	protected List<InternalEntityType> getTargetEntities() {
		return targetEntities;
	}


	protected void setTargetEntities(List<InternalEntityType> targetEntities) {
		this.targetEntities = targetEntities;
	}


	protected void setMeasures(List<MeasureColumn> measures){
		getMeasures().addAll(measures);
	}

	protected void setAxis(List<AxisColumn> axes){
		getAxis().addAll(axes);
	}



}
