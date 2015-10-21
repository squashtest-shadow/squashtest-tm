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

import org.squashtest.tm.domain.chart.AxisColumn;
import org.squashtest.tm.domain.chart.ChartQuery;
import org.squashtest.tm.domain.chart.ColumnRole;
import org.squashtest.tm.domain.chart.Filter;
import org.squashtest.tm.domain.chart.MeasureColumn;
import org.squashtest.tm.domain.chart.SpecializedEntityType;


/**
 * see javadoc on {@link ChartDataFinder}
 *
 * @author bsiri
 *
 */
class DetailedChartQuery extends ChartQuery{

	private InternalEntityType rootEntity;

	private List<InternalEntityType> targetEntities;


	// for testing purposes - do not use
	DetailedChartQuery(){
		super();
	}

	DetailedChartQuery(ChartQuery parent){

		super();

		getAxis().addAll(parent.getAxis());

		getFilters().addAll(parent.getFilters());

		getMeasures().addAll(parent.getMeasures());

		setJoinStyle(parent.getJoinStyle());

		setStrategy(parent.getStrategy());

		// find the root entity
		rootEntity = InternalEntityType.fromSpecializedType(parent.getAxis().get(0).getSpecializedType());

		// find all the target entities
		Map<ColumnRole, Set<SpecializedEntityType>> entitiesByRole = parent.getInvolvedEntities();

		targetEntities = new ArrayList<>();
		for (Set<SpecializedEntityType> types : entitiesByRole.values()){
			for (SpecializedEntityType type : types){
				targetEntities.add(InternalEntityType.fromSpecializedType(type));
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


	public void setMeasures(List<MeasureColumn> measures){
		getMeasures().addAll(measures);
	}

	public void setAxis(List<AxisColumn> axes){
		getAxis().addAll(axes);
	}

	public void setFilters(List<Filter> filters){
		getFilters().addAll(filters);
	}



}
