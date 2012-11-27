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

package org.squashtest.csp.tm.service.project;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.project.GenericProject;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;

/**
 * @author Gregory Fouquet
 *
 */
@Transactional
public interface CustomGenericProjectManager {
	/**
	 * Will find all Projects and Templates to which the user has management access to and return them ordered according to the given params.
	 * 
	 * @param filter the {@link CollectionSorting} that holds order and paging params
	 * @return a {@link FilteredCollectionHolder} containing all projects the user has management access to, ordered according to the given params.
	 */
	@Transactional(readOnly=true)
	PagedCollectionHolder<List<GenericProject>> findSortedProjects(PagingAndSorting pagingAndSorting);

	/**
	 * @param project
	 */
	void persist(GenericProject project);
}
