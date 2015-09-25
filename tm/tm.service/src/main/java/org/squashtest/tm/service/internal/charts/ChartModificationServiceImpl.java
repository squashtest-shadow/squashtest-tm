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

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.chart.ChartDefinition;
import org.squashtest.tm.domain.chart.ColumnPrototype;
import org.squashtest.tm.domain.chart.EntityType;
import org.squashtest.tm.domain.chart.QColumnPrototype;
import org.squashtest.tm.service.charts.ChartModificationService;

import com.querydsl.jpa.hibernate.HibernateQueryFactory;
import static com.querydsl.core.group.GroupBy.*;

@Service("squashtest.tm.service.ChartModificationService")
public class ChartModificationServiceImpl implements ChartModificationService {

	@Inject
	private SessionFactory sessionFactory;

	@Override
	public void persist(ChartDefinition newChartDefinition) {
		session().persist(newChartDefinition);
	}

	@Override
	public ChartDefinition findById(long id) {
		return (ChartDefinition)(session().get(ChartDefinition.class, id));
	}

	@Override
	public Map<EntityType, Set<ColumnPrototype>> getColumnPrototypes() {

		HibernateQueryFactory factory = new HibernateQueryFactory(session());
		QColumnPrototype prototype = QColumnPrototype.columnPrototype;

		Map<EntityType, Set<ColumnPrototype>> prototypes;

		prototypes = factory.from(prototype).transform(groupBy(prototype.entityType).as(set(prototype)));

		return prototypes;
	}

	@Override
	public void update(ChartDefinition chartDef) {
		session().saveOrUpdate(chartDef);
	}


	private Session session(){
		return sessionFactory.getCurrentSession();
	}
}
