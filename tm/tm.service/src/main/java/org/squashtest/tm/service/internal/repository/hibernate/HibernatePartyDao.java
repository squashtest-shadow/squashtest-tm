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
package org.squashtest.tm.service.internal.repository.hibernate;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.users.Party;
import org.squashtest.tm.service.internal.repository.PartyDao;

@Repository
public class HibernatePartyDao extends HibernateEntityDao<Party> implements PartyDao {

	@Override
	public List<Party> findAllActiveParties() {
		return executeListNamedQuery("party.findAllActive");
	}

	@Override
	public List<Party> findAllActiveByIds(final Collection<Long> ids) {
		if (ids.isEmpty()) {
			return Collections.emptyList();
		} else {
			return executeListNamedQuery("party.findAllActiveByIds", new SetQueryParametersCallback() {
				@Override
				public void setQueryParameters(Query query) {
					query.setParameterList("partyIds", ids, LongType.INSTANCE);
				}
			});
		}
	}

}
