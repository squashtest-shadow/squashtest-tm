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
package org.squashtest.tm.service.internal.user;

import java.util.List;

import javax.inject.Inject;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.users.Team;
import org.squashtest.tm.exception.customfield.NameAlreadyInUseException;
import org.squashtest.tm.service.internal.repository.TeamDao;
import org.squashtest.tm.service.security.acls.model.ObjectAclService;
import org.squashtest.tm.service.user.CustomTeamModificationService;
@Service("CustomTeamModificationService")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class CustomTeamModificationServiceImpl implements CustomTeamModificationService {

	@Inject
	private TeamDao teamDao;
	
	@Inject
	private ObjectAclService aclService;

	/**
	 * @see CustomTeamModificationService#persist(Team)
	 */
	@Override
	public void persist(Team team) {
		if(teamDao.findAllByName(team.getName()).isEmpty()){
		teamDao.persist(team);
		}else{
			throw new NameAlreadyInUseException("Team", team.getName());
		}
	}
	
	/**
	 * @see CustomTeamModificationService#deleteTeam(long)
	 */
	@Override
	public void deleteTeam(long teamId) {
		Team team = teamDao.findById(teamId);
		aclService.removeAllResponsibilitiesForParty(teamId);
		teamDao.delete(team);
		
	}
	
	@Override
	public PagedCollectionHolder<List<Team>> findAllFiltered(PagingAndSorting filter) {
		List<Team> teams = teamDao.findSortedTeams(filter);
		long count = teamDao.count();
		return new PagingBackedPagedCollectionHolder<List<Team>>(filter, count, teams);
	}

}
