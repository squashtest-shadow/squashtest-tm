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
package org.squashtest.csp.tm.internal.repository;

import java.util.List;

import org.squashtest.csp.tm.domain.bugtracker.Bugged;
import org.squashtest.csp.tm.domain.bugtracker.Issue;
import org.squashtest.csp.tm.domain.bugtracker.IssueOwnership;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;

public interface IssueDao extends EntityDao<Issue>{
	
	<X extends Bugged> X findBuggedEntity(Long entityId, Class<X> entityClass);
	

	/**
	 * Will recursively find a sorted/filtered list of Issues paired with their owning Bugged entity, 
	 * from the given bugged entity and its related Bugged entities.
	 *  
	 * The data will be sorted/filtered regardless of their owner.
	 * 
	 * @param entity the entity at the base of the bugged hierarchy
	 * @param sorter a CollectionSoring object that will tell who we must sort the data
	 * @return a non null but possibly empty list of IssueOwnership<Issue>, that will pair each Issue with their Owner.
	 */
	List<IssueOwnership<Issue>> findIssuesWithOwner(Bugged entity, CollectionSorting sorter);

	
	/**
	 * Will count the total number of issues related to the given IssueList.  
	 * 
	 * 
	 * @param issueListIds the id of the issue lists.
	 * @return how many issues they hold.
	 */
	Integer countIssuesfromIssueList(List<Long> issueListIds);

}