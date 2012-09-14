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
package org.squashtest.csp.tm.internal.repository.testautomation;

import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.springframework.stereotype.Repository;
import org.squashtest.csp.tm.domain.testautomation.AutomatedExecutionExtender;

@Repository("CustomAutomatedExecutionExtenderDao")
public class HibernateAutomatedExecutionExtenderDao implements CustomAutomatedExecutionExtenderDao {

	
	@Inject
	private SessionFactory sessionFactory;
	
	
	
	@Override
	public Collection<AutomatedExecutionExtender> getInitializedAndDetachedExtenders(
			Collection<AutomatedExecutionExtender> toInit) {
		
		if (toInit.isEmpty()){
			return Collections.emptyList();
		}
		
				
		StatelessSession s = sessionFactory.openStatelessSession();
		Transaction tx = s.beginTransaction();
		
		try{
			Query query = s.getNamedQuery("AutomatedExecutionExtender.completeInitialization");
			query.setParameterList("extenders", toInit);
			Collection<AutomatedExecutionExtender> result = (Collection<AutomatedExecutionExtender>) query.list();
			tx.commit();
			return result;
		}
		catch(HibernateException ex){
			tx.rollback();
			throw ex;
		}
		finally{
			tx.commit();
			s.close();
		}
		
	}

}
