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

package org.squashtest.csp.tm.internal.service;

import java.util.List;

import javax.inject.Inject;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.project.GenericProject;
import org.squashtest.csp.tm.internal.repository.GenericProjectDao;
import org.squashtest.csp.tm.service.project.CustomGenericProjectManager;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;

/**
 * @author Gregory Fouquet
 * 
 */
@Service("CustomGenericProjectManager")
public class CustomGenericProjectManagerImpl implements CustomGenericProjectManager {
	@Inject private GenericProjectDao genericProjectDao;
	/**
	 * @see org.squashtest.csp.tm.service.project.CustomGenericProjectManager#findSortedProjects(org.squashtest.tm.core.foundation.collection.PagingAndSorting)
	 */
	@Override
	@Transactional(readOnly = true)
	@PreAuthorize("hasRole('ROLE_TM_PROJECT_MANAGER') or hasRole('ROLE_ADMIN')")
	public PagedCollectionHolder<List<GenericProject>> findSortedProjects(PagingAndSorting pagingAndSorting) {
		List<GenericProject> projects = genericProjectDao.findAll(pagingAndSorting);
		long count = genericProjectDao.countGenericProjects();
		return new PagingBackedPagedCollectionHolder<List<GenericProject>>(pagingAndSorting, count, projects);
	}

}
