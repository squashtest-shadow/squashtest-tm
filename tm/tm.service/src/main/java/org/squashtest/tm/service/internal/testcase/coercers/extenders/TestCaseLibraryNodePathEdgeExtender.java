/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.service.internal.testcase.coercers.extenders;

import java.io.Serializable;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Named;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Configurable;
import org.squashtest.tm.service.annotation.BatchPreventConcurrent;
import org.squashtest.tm.service.annotation.IdsCoercerExtender;
import org.squashtest.tm.service.annotation.PreventConcurrents;
import org.squashtest.tm.service.internal.hibernate.HibernateStatelessSessionHelper;

/**
 * {@link IdsCoercerExtender} used for move operations. This class is used with {@link PreventConcurrents} and {@link BatchPreventConcurrent} annotations.
 *
 * Will give the ids of the nodes and the ids of the parents.
 * Note that another coercer will be used for retrieve the ids of LIBRARY parents.
 *
 * @author Julien Thebault
 * @since 1.13
 */
@Configurable
@Named("testCaseLibraryNodePathEdgeExtender")
public class TestCaseLibraryNodePathEdgeExtender implements IdsCoercerExtender {

	@Inject
	private HibernateStatelessSessionHelper hibernateStatelessSessionHelper;

	@Override
	public Collection<? extends Serializable> doCoerce(Collection<? extends Serializable> coercedIds) {
		StatelessSession s = hibernateStatelessSessionHelper.openStatelessSession();
		Transaction tx = s.beginTransaction();

		try {
			Query q = s.createQuery("select distinct edge.ancestorId from TestCasePathEdge edge where edge.descendantId in (:tclnIds) and depth=1");
			q.setParameterList("tclnIds", coercedIds);
			coercedIds.addAll(q.list());
			return coercedIds;

		} finally {
			tx.commit();
			s.close();
		}
	}
}
