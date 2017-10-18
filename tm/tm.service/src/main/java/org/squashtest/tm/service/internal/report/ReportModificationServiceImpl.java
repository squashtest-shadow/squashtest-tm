/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.service.internal.report;

import org.hibernate.Session;
import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.report.ReportDefinition;
import org.squashtest.tm.service.report.ReportModificationService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Service("squashtest.tm.service.ReportModificationService")
public class ReportModificationServiceImpl implements ReportModificationService{

	@PersistenceContext
	private EntityManager em;

	@Override
	public void persist(ReportDefinition newReport) {

	}

	@Override
	public ReportDefinition findById(long id) {
		return null;
	}

	@Override
	public void update(ReportDefinition report) {

	}

	@Override
	public void save(ReportDefinition report) {
		session().save(report);
	}

	private Session session(){
		return em.unwrap(Session.class);
	}
}
