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
package org.squashtest.tm.service.internal.repository.hibernate;

import org.hibernate.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.domain.softdelete.SoftDeletableMixin;
import org.squashtest.tm.service.internal.repository.hibernate.DoNotFilterSoftDeleted;
import org.squashtest.tm.service.internal.repository.hibernate.HibernateDao;
import org.squashtest.tm.domain.softdelete.SoftDeletable;

/**
 * Enables the soft delete filter on the current session.
 * 
 * @author Gregory Fouquet
 * 
 */
public aspect SoftDeleteFilterEnablerAspect {
	private static final Logger LOGGER = LoggerFactory.getLogger(SoftDeleteFilterEnablerAspect.class);

	/**
	 * This advice applies soft deletion filters to filtered finder methods.
	 * 
	 * @param dao
	 */
	@SuppressWarnings("rawtypes")
	before(HibernateDao dao) : SoftDeletableDaoPointcut.filteredFinder() && target(dao) {
		LOGGER.trace("Soft delete filter will be enabled on current Hibernate session");
		dao.currentSession().enableFilter("filter.entity.deleted");
	}

	/**
	 * This advice deactivates soft deletion filters while executing an unfiltered finder method.
	 * 
	 * @param dao
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	Object around(HibernateDao dao) : SoftDeletableDaoPointcut.unfilteredFinder() && target(dao) {
		LOGGER.trace("Soft delete filter will be temporarily deactivated on current Hibernate session");
		Filter entityFilter = dao.currentSession().getEnabledFilter("filter.entity.deleted");

		if (entityFilter != null) {
			dao.currentSession().disableFilter(entityFilter.getName());
		}

		Object res = proceed(dao);

		if (entityFilter != null) {
			dao.currentSession().enableFilter(entityFilter.getName());
		}
		LOGGER.trace("Soft delete filter was reactivated");

		return res;
	}

}
