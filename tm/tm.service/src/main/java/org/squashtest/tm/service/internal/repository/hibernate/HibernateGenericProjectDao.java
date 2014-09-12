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
package org.squashtest.tm.service.internal.repository.hibernate;

import javax.inject.Inject;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.project.ProjectTemplate;
import org.squashtest.tm.service.internal.repository.CustomGenericProjectDao;

/**
 * @author Gregory Fouquet
 * 
 */
@Repository("CustomGenericProjectDao")
public class HibernateGenericProjectDao implements CustomGenericProjectDao {
	@Inject
	private SessionFactory sessionFactory;

	/**
	 * @return the coerced project
	 * @see org.squashtest.tm.service.internal.repository.org.squashtest.tm.service.internal.repository.CustomGenericProjectDao#coerceTemplateIntoProject(long)
	 */
	@Override
	public Project coerceTemplateIntoProject(long templateId) {
		Session session = sessionFactory.getCurrentSession();

		ProjectTemplate template = (ProjectTemplate) session.load(ProjectTemplate.class, templateId);
		session.flush();
		session.evict(template);

		// TODO replace PROJECt_ID by PROJECT_ID in 1.10.0 Dont seem to break (even on case sensitive mysql) yet its not correct
		SQLQuery query = session.createSQLQuery("update PROJECT set PROJECT_TYPE = 'P' where PROJECt_ID = :id");
		query.setParameter("id", templateId);
		final int changedRows = query.executeUpdate();
		if (changedRows != 1) {
			throw new HibernateException("Expected 1 changed row but got " + changedRows + " instead");
		}
		session.flush();

		return (Project) session.load(Project.class, templateId);
	}

	
	@Override
	public boolean isProjectTemplate(long projectId) {
		Query query = sessionFactory.getCurrentSession().getNamedQuery("GenericProject.findProjectTypeOf");
		query.setParameter("projectId", projectId);
		
		String type = (String)query.uniqueResult();
		
		return type.equals("T");
	}

	
}
