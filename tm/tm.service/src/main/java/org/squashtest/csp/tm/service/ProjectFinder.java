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
package org.squashtest.csp.tm.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;

/**
 * @author mpagnon
 * 
 */
@Transactional(readOnly = true)
public interface ProjectFinder {
	
	Project findById(long projectId);

	List<Project> findAllOrderedByName();
	/**
	 * Will find all Projects to which the user has management access to and return them ordered according to the given params.
	 * 
	 * @param filter the {@link CollectionSorting} that holds order and paging params
	 * @return a {@link FilteredCollectionHolder} containing all projects the user has management access to, ordered according to the given params.
	 */
	FilteredCollectionHolder<List<Project>> findSortedProjects(CollectionSorting filter);

	List<Project> findAllReadable();
}