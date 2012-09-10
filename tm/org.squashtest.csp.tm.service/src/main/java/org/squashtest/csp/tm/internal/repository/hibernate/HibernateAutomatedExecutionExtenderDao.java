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
package org.squashtest.csp.tm.internal.repository.hibernate;

import javax.inject.Inject;

import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.springframework.stereotype.Repository;
import org.squashtest.csp.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.csp.tm.internal.repository.AutomatedExecutionExtenderDao;

@Repository
public class HibernateAutomatedExecutionExtenderDao implements AutomatedExecutionExtenderDao{

	
	@Inject
	private SessionFactory factory;
	
	protected Session currentSession(){
		return factory.getCurrentSession();
	}
	
	@Override
	public AutomatedExecutionExtender findById(long extenderId) {
		return (AutomatedExecutionExtender)currentSession().get(AutomatedExecutionExtender.class, extenderId);
	}

	@Override
	public void persist(AutomatedExecutionExtender extender) {
		currentSession().persist(extender);
	}

	
	
}
