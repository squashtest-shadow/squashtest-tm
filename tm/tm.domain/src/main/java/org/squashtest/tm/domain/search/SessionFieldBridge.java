/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.domain.search;

import javax.inject.Inject;

import org.apache.lucene.document.Document;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.squashtest.tm.domain.Identified;

@Configurable
public abstract class SessionFieldBridge implements FieldBridge{
	private static final Logger LOGGER = LoggerFactory.getLogger(SessionFieldBridge.class);
	
	@Inject
	private BeanFactory beanFactory;

	private SessionFactory getSessionFactory() {
	// We cannot inject the SessionFactory because it creates a cyclic dependency injection problem :
	// SessionFactory -> Hibernate Search -> this bridge -> SessionFactory
		return beanFactory.getBean(SessionFactory.class);
	}

	protected abstract void writeFieldToDocument(String name, Session session, Object value, Document document, LuceneOptions luceneOptions);
	
	
	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
		long start = 0;
		if (LOGGER.isDebugEnabled()) {
			start = System.nanoTime();
		}
		
		Session currentSession = null;
		Session session = null;
		Transaction tx = null;
		
		try{
			currentSession = getSessionFactory().getCurrentSession();
			session = currentSession;
		}catch(HibernateException ex){
			currentSession = null;
		}
		
		if(currentSession == null){
			session = getSessionFactory().openSession();
			tx = session.beginTransaction();
			try{
				writeFieldToDocument(name, session, value, document, luceneOptions);
			} catch(HibernateException ex){
				//ugly hack in order to be able to run integration tests
			}
			tx.commit();
			session.close();
		} else {
			writeFieldToDocument(name, session, value, document, luceneOptions);
		}
		
		if (LOGGER.isDebugEnabled()) {
			long end = System.nanoTime();
			int timeInMilliSec = Math.round((end - start) / 1000000f);
			LOGGER.trace(this.getClass().getSimpleName() + ".set(..) took {} ms for entity {}", timeInMilliSec, ((Identified)value).getId());
			final int threshold = 10;
			if (timeInMilliSec > threshold) {
				LOGGER.trace("BEWARE : " + this.getClass().getSimpleName() + ".set(..) took more than {} ms", threshold);
			}
		}
	}
}