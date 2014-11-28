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
import org.squashtest.tm.domain.infolist.DenormalizedInfoListItem;
import org.squashtest.tm.domain.infolist.DenormalizedListItemReference;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.service.internal.repository.DenormalizedInfoListItemDao;

@Repository("DenormalizedInfoListItemDao")
public class HibernateDenormalizedInfoListItemDao extends HibernateEntityDao<DenormalizedInfoListItem>
implements DenormalizedInfoListItemDao{

	@Override
	public DenormalizedInfoListItem findByReference(InfoListItem item) {
		Query q =  currentSession().getNamedQuery("denormalizedInfoListItem.findByReference");
		q.setParameter("code", item.getCode());
		q.setParameter("listId", item.getInfoList().getId());
		q.setParameter("listVersion", item.getInfoList().getVersion());

		return (DenormalizedInfoListItem)q.uniqueResult();
	}

	@Override
	public DenormalizedInfoListItem findByReference(DenormalizedListItemReference item) {
		Query q =  currentSession().getNamedQuery("denormalizedInfoListItem.findByReference");
		q.setParameter("code", item.getCode());
		q.setParameter("listId", item.getInfoList().getOriginalId());
		q.setParameter("listVersion", item.getInfoList().getOriginalVersion());

		return (DenormalizedInfoListItem)q.uniqueResult();
	}



}
