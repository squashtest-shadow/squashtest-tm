/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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

import java.util.Collections;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.squashtest.csp.tm.internal.repository.EntityDao;

public class HibernateEntityDao<ENTITY_TYPE> extends HibernateDao<ENTITY_TYPE> implements EntityDao<ENTITY_TYPE> {

	@Override
	public final ENTITY_TYPE findById(long id) {
		return getEntity(id);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<ENTITY_TYPE> findAllById(List<Long> ids){
		if (ids.isEmpty()) {
			return Collections.emptyList();
		} else {
			Criteria criteria = currentSession()
				.createCriteria(entityType)
				.add(Restrictions.in(getIdPropertyName(), ids.toArray()));

			return criteria.list();
		}
	}
	
	public String getIdPropertyName(){
		return "id";
	}

 
	@Override
	public final void persist(ENTITY_TYPE transientEntity) {
		persistEntity(transientEntity);
	}

	@Override
	public final void remove(ENTITY_TYPE entity) {
		removeEntity(entity);
	}
	
	@Override
	public final void flush(){
		currentSession().flush();
	}


}
