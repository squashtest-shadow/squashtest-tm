/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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

import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.infolist.DenormalizedInfoList;
import org.squashtest.tm.domain.infolist.DenormalizedListItemReference;
import org.squashtest.tm.service.internal.repository.DenormalizedInfoListDao;

@Repository
public class HibernateDenormalizedInfoListDao extends HibernateEntityDao<DenormalizedInfoList>
implements DenormalizedInfoListDao {

	@Override
	public DenormalizedInfoList findByItemReference(DenormalizedListItemReference reference) {
		return findByOriginalIdAndVersion(reference.getOriginalListId(), reference.getOriginalListVersion());
	}

	@Override
	public DenormalizedInfoList findByOriginalIdAndVersion(long originalId, int originalVersion) {
		Query q = currentSession().getNamedQuery("denormalizedInfoList.findByOriginalIdAndVersion");
		q.setParameter("id", originalId);
		q.setParameter("version", originalVersion);
		return (DenormalizedInfoList)q.uniqueResult();
	}

}
