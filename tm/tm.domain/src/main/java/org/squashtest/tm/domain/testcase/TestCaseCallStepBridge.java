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

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.squashtest.tm.domain.campaign.Iteration;

@Configurable
public class TestCaseCallStepBridge implements FieldBridge{


	@Autowired
	private SessionFactory sessionFactory;
    
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {

		TestCase testcase = (TestCase) value;
		
		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		
		Integer numberOfCalledTestCases = findNumberOfCalledTestCases(testcase.getId());
		
		Field field = new Field(name, String.valueOf(numberOfCalledTestCases), luceneOptions.getStore(),
	    luceneOptions.getIndex(), luceneOptions.getTermVector() );
	    field.setBoost( luceneOptions.getBoost());
	    document.add(field);

	    tx.commit();
	    session.close();
	}

	private Integer findNumberOfCalledTestCases(Long id) {

		
		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		
		Integer numberOfCalledTestCases = (Integer) session.createCriteria(TestCase.class)
			.add(Restrictions.eq("id", id))
			.createCriteria("steps")
			.createCriteria("calledTestCase")
			.setProjection(Projections.rowCount())
			.uniqueResult();

	    tx.commit();
	    session.close();
	    
	    return numberOfCalledTestCases;
	}

}
