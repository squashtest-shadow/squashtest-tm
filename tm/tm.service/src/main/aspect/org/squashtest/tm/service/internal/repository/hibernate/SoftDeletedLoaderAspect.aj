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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.squashtest.tm.domain.softdelete.SoftDeletableMixin;
import org.squashtest.tm.domain.softdelete.SoftDeletable;

/**
 * This aspect overrides Hibernate's default get / load behaviour when loading an @SoftDeletable entity from a DAO.
 * Hibernate does not apply filter on load / get operation so we either need to query instead of get / load (which will not hit any cache) or drop by hand deleted entities.
 * @author Gregory Fouquet
 *
 */
public aspect SoftDeletedLoaderAspect {
	private static final Logger LOGGER = LoggerFactory.getLogger(SoftDeletedLoaderAspect.class);

	/**
	 * Advises the {@link HibernateDao#getEntity(long)} method called from within a filtered finder and drops any soft-deleted entity.
	 * @return
	 */
	Object around() : cflowbelow(SoftDeletableDaoPointcut.filteredFinder()) && execution(protected * org.squashtest.tm.service.internal.repository.hibernate.HibernateDao.getEntity(long)) {
		Object entity = proceed();

		// we must perform a runtime type check because type erasure won't allow to discriminate through a pointcut enough.
		if (entity != null && isSoftDeletable(entity) && wasDeleted(entity)) {
			LOGGER.trace("Gotten soft-deleted entity. Will substitute it with null");
			entity = null;
		}

		return entity;
	}

	private boolean isSoftDeletable(Object entity) {
		return (AnnotationUtils.findAnnotation(entity.getClass(), SoftDeletable.class) != null);
	}

	/**
	 * Tells if a @SoftDeletable entity was deleted.
	 * 
	 * @param softDeletableEntity
	 *            entity to check. Should be @SoftDeletable.
	 * @return
	 */
	private boolean wasDeleted(Object softDeletableEntity) {
		return ((SoftDeletableMixin) softDeletableEntity).isDeleted();
	}

}
