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
package org.squashtest.tm.service.internal.repository.hibernate;

import java.util.List;

import javax.inject.Inject;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.oauth2.Client;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.service.internal.repository.ClientDao;
import org.squashtest.tm.service.internal.repository.CustomClientDao;


@Repository("CustomClientDao")
public class HibernateClientDao implements CustomClientDao{

	@Inject
	private SessionFactory sessionFactory;

	@Override
	public List<Client> findClientsOrderedByName() {
		Query query = sessionFactory.getCurrentSession().getNamedQuery("client.findAllOrderedByName");
		return (List<Client>) query.list();
	}

	@Override
	public Client findClientByName(String name) {
		Query query = sessionFactory.getCurrentSession().getNamedQuery("client.findClientByName");
		query.setParameter("name", name);
		return (Client) query.uniqueResult();
	}

	@Override
	public void persist(Client client) {
		sessionFactory.getCurrentSession().persist(client);
		sessionFactory.getCurrentSession().flush();
	}

	@Override
	public void remove(Client client) {
		sessionFactory.getCurrentSession().delete(client);
		sessionFactory.getCurrentSession().flush();
	}
}
