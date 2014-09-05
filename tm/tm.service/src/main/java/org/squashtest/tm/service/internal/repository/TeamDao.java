/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.service.internal.repository;

import java.util.List;

import org.squashtest.tm.core.dynamicmanager.annotation.DynamicDao;
import org.squashtest.tm.core.dynamicmanager.factory.DynamicDaoFactoryBean;
import org.squashtest.tm.domain.users.Team;

/**
 * Data access methods for {@link Team}s. Methods are all dynamically generated: see {@link DynamicDaoFactoryBean}.
 * 
 * @author mpagnon
 * 
 */
@DynamicDao(entity = Team.class)
public interface TeamDao extends CustomTeamDao{
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
	 * 
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

	/**
	 * Simply count all Teams
	 * 
	 * @return amount of {@link Team} in database
	 */
	long count();
	
	/**
	 * Find all teams matching the given ids
	 * 
	 * @param teamIds : ids of {@link Team}s to return
	 * @return List of matching {@link Team}s.
	 */
	List<Team> findAllByIds(List<Long> teamIds);
	
	/**
	 * Will count the number of Teams where the concerned user is member.
	 * @param userId : id of the concerned user
	 * @return the total number of teams associated to the user
	 */
	long countAssociatedTeams(long userId);
	
	/**
	 * Will return all {@link Team}s that don't have the concerned user as a member.
	 * @param userId : the id of the concerned user
	 * @return
	 */
	List<Team> findAllNonAssociatedTeams(long userId);

}
