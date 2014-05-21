/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.service.internal.foundation.collection.PagingUtils;
import org.squashtest.tm.service.internal.foundation.collection.SortingUtils;
import org.squashtest.tm.service.internal.repository.TestAutomationServerDao;


@Repository
public class HibernateTestAutomationServerDao implements
TestAutomationServerDao {

	@Inject
	private SessionFactory sessionFactory;


	@Override
	public void persist(TestAutomationServer server) {
		sessionFactory.getCurrentSession().persist(server);
	}

	@Override
	public List<TestAutomationServer> findAllOrderedByName() {
		Session session = sessionFactory.getCurrentSession();
		Query q = session.getNamedQuery("testAutomationServer.findAllOrderedByName");
		return q.list();
	}


	@Override
	public long countAll() {
		Query q = sessionFactory.getCurrentSession().getNamedQuery("testAutomationServer.countAll");
		return ((Long)q.iterate().next()).longValue();
	}


	@Override
	public List<TestAutomationServer> findPagedServers(PagingAndSorting pas) {

		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(TestAutomationServer.class, "TestAutomationServer");
		SortingUtils.addOrder(criteria, pas);
		PagingUtils.addPaging(criteria, pas);
		criteria.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);

		List<Map<String, ?>> raw = criteria.list();
		List<TestAutomationServer> res = new ArrayList<TestAutomationServer>(raw.size());

		for (Map<String, ?> r : raw){
			res.add((TestAutomationServer)r.get("TestAutomationServer"));
		}

		return res;

	}

	@Override
	public boolean hasBoundProjects(long serverId) {
		Session session = sessionFactory.getCurrentSession();
		Query q = session.getNamedQuery("testAutomationServer.hasBoundProjects");
		int count = ((Integer)q.iterate().next()).intValue();
		return (count > 0);
	}

	@Override
	public TestAutomationServer findById(Long id) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.getNamedQuery("testAutomationServer.findById");
		query.setParameter("serverId", id);
		return (TestAutomationServer)query.uniqueResult();
	}

	@Override
	public TestAutomationServer findByName(String serverName){
		Session session = sessionFactory.getCurrentSession();
		Query query = session.getNamedQuery("testAutomationServer.findByName");
		query.setParameter("serverName", serverName);
		return (TestAutomationServer)query.uniqueResult();
	}




	@SuppressWarnings("unchecked")
	@Override
	public List<TestAutomationProject> findAllHostedProjects(long serverId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.getNamedQuery("testAutomationServer.findAllHostedProjects");
		query.setParameter("serverId", serverId);
		return (List<TestAutomationProject>)query.list();
	}


	@Override
	public void deleteServer(long serverId) {
		dereferenceProjects(serverId);
		deleteServerById(serverId);
	}

	// ***************** private stuffs ***************

	private void dereferenceProjects(long serverId){
		Query q = sessionFactory.getCurrentSession().getNamedQuery("testAutomationServer.dereferenceProjects");
		q.setParameter("serverId", serverId, LongType.INSTANCE);
		q.executeUpdate();
	}

	private void deleteServerById(long serverId){
		Query q = sessionFactory.getCurrentSession().getNamedQuery("testAutomationServer.deleteServer");
		q.setParameter("serverId", serverId, LongType.INSTANCE);
		q.executeUpdate();
	}

}
