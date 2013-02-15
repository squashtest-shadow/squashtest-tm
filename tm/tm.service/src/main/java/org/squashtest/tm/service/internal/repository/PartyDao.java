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
package org.squashtest.tm.service.internal.repository;

import java.util.List;

import org.squashtest.tm.core.dynamicmanager.annotation.DynamicDao;
import org.squashtest.tm.domain.users.Party;


@DynamicDao(entity = Party.class)
public interface PartyDao {
	/**
	 * Will persist a new party.
	 * 
	 * @param party
	 *            : the new party to persist
	 */
	void persist(Party party);

	/**
	 * Simply retrieve the {@link Party} of the given id
	 * 
	 * @param partyId
	 * @return
	 */
	Party findById(long partyId);

	
	List<Party> findAllByIds(List<Long> idList);
	
	/**
	 * Simply delete the given {@link Party}
	 * 
	 * @param party
	 */
	void delete(Party party);

	/**
	 * Simply count all Parties
	 * 
	 * @return amount of {@link Party} in database
	 */
	long count();
}
