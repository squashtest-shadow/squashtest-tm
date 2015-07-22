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

import java.util.Collection;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.squashtest.tm.service.charts.Chart;
import org.squashtest.tm.service.charts.ChartBuilderService;
import org.squashtest.tm.service.charts.ChartInstance;
import org.squashtest.tm.service.charts.Perimeter;

@Service("squashtest.tm.service.ChartBuilderService")
public class ChartBuilderServiceImpl implements ChartBuilderService {

	@Autowired
	private Collection<Perimeter> perimeters;

	@Override
	public Collection<Perimeter> getAvailablePerimeters() {
		return Collections.unmodifiableCollection(perimeters);
	}

	@Override
	public ChartInstance buildChart(Chart chart) {
		// TODO Auto-generated method stub
		return null;
	}

}
