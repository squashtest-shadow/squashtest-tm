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
package org.squashtest.tm.service.internal.campaign;

import org.hibernate.*;
import org.springframework.beans.factory.annotation.Configurable;
import org.squashtest.tm.service.annotation.IdsCoercer;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @author Gregory Fouquet
 * @since 1.11.6
 */
@Configurable
public class IterationToCampaignIdsCoercer implements IdsCoercer {
	@Inject
	private SessionFactory sessionFactory;

	@Override
	public Collection<? extends Serializable> coerce(Collection<? extends Serializable> ids) {
		StatelessSession s = sessionFactory.openStatelessSession();
		Transaction tx = s.beginTransaction();

		try {
			Query q = sessionFactory.getCurrentSession().createQuery("select distinct c.id from Iteration i join i.campaign c where i.id in (:iterIds)");
			q.setParameterList("iterIds", ids);
			return q.list();

		} finally {
			tx.commit();
			s.close();
		}
	}
}