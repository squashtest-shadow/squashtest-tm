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
package org.squashtest.tm.service.internal.campaign.coercers.extenders;

import java.io.Serializable;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Named;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Configurable;
import org.squashtest.tm.service.annotation.IdsCoercerExtender;

/**
 * @author Julien Thebault
 * @since 1.13
 */
@Configurable
@Named("testSuiteToIterationExtender")
public class TestSuiteToIterationExtender implements IdsCoercerExtender {
	@Inject
	private SessionFactory sessionFactory;

	@Override
	public Collection<? extends Serializable> doCoerce (Collection<? extends Serializable>  ids) {
		StatelessSession s = sessionFactory.openStatelessSession();
		Transaction tx = s.beginTransaction();

		try {
			Query q = sessionFactory.getCurrentSession()
					.createQuery("select distinct i.id from Iteration where i.testSuites.id in (:tsids)");
			q.setParameterList("tsids", ids);
			return q.list();

		} finally {
			tx.commit();
			s.close();
		}
	}
}
