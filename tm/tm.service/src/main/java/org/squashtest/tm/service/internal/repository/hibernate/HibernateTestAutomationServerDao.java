/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.service.internal.repository.TestAutomationServerDao;


@Repository
public class HibernateTestAutomationServerDao implements
		TestAutomationServerDao {
	
	@Inject
	private SessionFactory sessionFactory;

	
	@Override
	public void persist(TestAutomationServer server) {
		if (findByExample(server)==null){
			sessionFactory.getCurrentSession().persist(server);
		}
		else{
			throw new NonUniqueEntityException(); 
		}
	}



	@Override
	public TestAutomationServer uniquePersist(TestAutomationServer server) {
	
		//id exists ?
		if ((server.getId() != null) && (findById(server.getId())!=null)){
			return server;
		}
		
		//content exists ?
		TestAutomationServer baseServer = findByExample(server);
		if (baseServer != null){
			return baseServer;
		}
		
		//or else, persist
		else{
			sessionFactory.getCurrentSession().persist(server);
			return server;
		}
		
	}
	
	
	@Override
	public TestAutomationServer findById(Long id) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.getNamedQuery("testAutomationServer.findById");
		query.setParameter("serverId", id);
		return (TestAutomationServer)query.uniqueResult();
	}
	
	
	@Override
	public TestAutomationServer findByExample(TestAutomationServer example) {
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(TestAutomationServer.class);
		criteria.add(Example.create(example));
		List<?> res = criteria.list();
		
		if (res.isEmpty()){
			return null;
		}
		else if (res.size()==1){
			return (TestAutomationServer)res.get(0);
		}
		else{
			throw new NonUniqueEntityException();
		}
	}

	
	@Override
	public List<TestAutomationProject> findAllHostedProjects(long serverId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.getNamedQuery("testAutomationServer.findAllHostedProjects");
		query.setParameter("serverId", serverId);
		return (List<TestAutomationProject>)query.list();		
	}


}
