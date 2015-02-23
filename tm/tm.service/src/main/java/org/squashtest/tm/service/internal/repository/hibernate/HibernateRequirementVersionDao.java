/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.service.internal.foundation.collection.PagingUtils;
import org.squashtest.tm.service.internal.foundation.collection.SortingUtils;
import org.squashtest.tm.service.internal.repository.CustomRequirementVersionDao;

/**
 * 
 * @author Gregory Fouquet
 * 
 */
@Repository("CustomRequirementVersionDao")
public class HibernateRequirementVersionDao implements CustomRequirementVersionDao {
	@Inject
	private SessionFactory sessionFactory;

	private Session currentSession() {
		return sessionFactory.getCurrentSession();
	}

	/**
	 * @see org.squashtest.tm.service.internal.repository.CustomRequirementVersionDao#findAllByRequirement(long,
	 *      org.squashtest.tm.core.foundation.collection.PagingAndSorting)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<RequirementVersion> findAllByRequirement(long requirementId, PagingAndSorting pas) {
		Criteria crit = currentSession().createCriteria(RequirementVersion.class, "RequirementVersion");
		crit.createAlias("requirement", "Requirement");
		crit.add(Restrictions.eq("Requirement.id", Long.valueOf(requirementId)));
		if (!pas.shouldDisplayAll()) {
			PagingUtils.addPaging(crit, pas);
		}
		SortingUtils.addOrder(crit, pas);

		return crit.list();
	}

	@Override
	public Requirement findRequirementById(long requirementId) {
		return (Requirement)currentSession().load(Requirement.class, requirementId);
	}

	@Override
	public RequirementVersion findByRequirementIdAndMilestone(long requirementId, Long milestoneId) {
		if (milestoneId== null){
			Query q = currentSession().getNamedQuery("requirementVersion.findLatestRequirementVersion");
			q.setParameter("requirementId", requirementId);
			return (RequirementVersion)q.uniqueResult();
		}
		else{
			Query q = currentSession().getNamedQuery("requirementVersion.findVersionByRequirementAndMilestone");
			q.setParameter("requirementId", requirementId);
			q.setParameter("milestoneId", milestoneId);
			return (RequirementVersion)q.uniqueResult();
		}
	}

}
