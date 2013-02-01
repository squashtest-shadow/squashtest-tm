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
package org.squashtest.csp.tm.internal.repository;

import java.util.List;

import org.squashtest.csp.tm.domain.users.Team;
import org.squashtest.tm.core.dynamicmanager.factory.DynamicDaoFactoryBean;

/**
 * Data access methods for {@link Team}s. Methods are all dynamically generated: see {@link DynamicDaoFactoryBean}.
 * 
 * @author mpagnon
 * 
 */
public interface TeamDao {
	/**
	 * Will persist a new team.
	 * 
	 * @param team
	 *            : the new team to persist
	 */
	void persist(Team team);
	
	/**
	 * Simply retrieve the {@link Team} of the given id
	 * 
	 * @param teamId
	 * @return
	 */
	Team findById(long teamId);
	
	/**
	 * Simply delete the given {@link Team}
	 * @param team
	 */
	void delete(Team team);
	
	/**
	 * Find all teams with name equals to the given name param.
	 * 
	 * @param name
	 * @return list of team with same name as param
	 */
	List<Team> findAllByName(String name);
}
