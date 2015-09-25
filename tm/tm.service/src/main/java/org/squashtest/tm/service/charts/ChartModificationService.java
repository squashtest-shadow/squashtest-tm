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

import java.util.Map;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.chart.ChartDefinition;
import org.squashtest.tm.domain.chart.ColumnPrototype;
import org.squashtest.tm.domain.chart.EntityType;

@Transactional
public interface ChartModificationService {

	void persist(ChartDefinition newChartDefinition);

	ChartDefinition findById(long id);

	/**
	 * Returns all the ColumnPrototypes known in the database, indexed by EntityType.
	 * 
	 * @return
	 */
	Map<EntityType, Set<ColumnPrototype>> getColumnPrototypes();


	/**
	 * Will update the chart definition in the persistence layer. The detached ChartDefinition argument must have a persisted
	 * counterpart (ie a non null ID that reference something in the database).
	 * 
	 * @param chartDef a detached instance of a ChartDefinition
	 */
	void update(ChartDefinition chartDef);

}
