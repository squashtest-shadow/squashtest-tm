/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package org.squashtest.tm.domain.testcase;

import javax.inject.Inject;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.squashtest.tm.domain.execution.Execution;

@Configurable
public class TestCaseIssueBridge implements FieldBridge{

	@Inject
	private BeanFactory beanFactory;

	private SessionFactory getSessionFactory() {
	// We cannot inject the SessionFactory because it creates a cyclic dependency injection problem :
	// SessionFactory -> Hibernate Search -> this bridge -> SessionFactory
		return beanFactory.getBean(SessionFactory.class);
	}

	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {

		TestCase testcase = (TestCase) value;
		
		Long numberOfIssues = findNumberOfIssues(testcase.getId());
		
		Field field = new Field(name, String.valueOf(numberOfIssues), luceneOptions.getStore(),
	    luceneOptions.getIndex(), luceneOptions.getTermVector() );
	    field.setBoost( luceneOptions.getBoost());
	    document.add(field);


	}

	private Long findNumberOfIssues(Long id) {

		Session currentSession = null;
		Session session = null;
		Transaction tx = null;
		Long numberOfIssues = 0L;
		
		try{
			currentSession = getSessionFactory().getCurrentSession();
			session = currentSession;
		}catch(HibernateException ex){
			session = getSessionFactory().openSession();
			tx = session.beginTransaction();
		}finally{
		
		 numberOfIssues = (Long) session.createCriteria(Execution.class)
			.add(Restrictions.eq("referencedTestCase.id", id))
			.createCriteria("issueList")
			.createCriteria("issues")
			.setProjection(Projections.rowCount())
			.uniqueResult();

	    if(currentSession == null){
		    tx.commit();
		    session.close();
	    }
		}
	    return numberOfIssues;
	}

}
