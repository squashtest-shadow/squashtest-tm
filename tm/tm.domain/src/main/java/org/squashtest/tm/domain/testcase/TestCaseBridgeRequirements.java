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
import org.hibernate.SessionFactory;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

public class TestCaseBridgeRequirements implements FieldBridge{

	
	@Inject 
	private SessionFactory sessionFactory;
	
	private Long id;
	
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
	
		TestCase testcase = (TestCase) value;
		this.id = testcase.getId();

		Field field = new Field(name, this.getNumberOfRequirements(), luceneOptions.getStore(),
	    luceneOptions.getIndex(), luceneOptions.getTermVector() );
	    field.setBoost( luceneOptions.getBoost() );
	    document.add(field);
	}
	
	public String getNumberOfRequirements(){

		/*
        Session session = sessionFactory.getCurrentSession();
        Transaction tx = session.beginTransaction();
        String HQL_QUERY = "select c from Customer c";
        Query query = session.createQuery(HQL_QUERY);
        Integer size = (Integer) query.uniqueResult();
        session.close();
        return size.toString();
		 */
		
		return Integer.valueOf(10).toString();
	}

}