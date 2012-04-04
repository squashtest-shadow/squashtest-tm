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
package org.squashtest.csp.tm.internal.service;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.CannotDeleteProjectException;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.internal.repository.ProjectDao;
import org.squashtest.csp.tm.service.CustomProjectModificationService;

/**
 * 
 * @author mpagnon
 * 
 */
@Service("CustomProjectModificationService")
@Transactional
public class CustomProjectModificationServiceImpl implements CustomProjectModificationService {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomProjectModificationServiceImpl.class);
	@Inject
	private ProjectDao projectDao;
	@Inject
	private ProjectDeletionHandler projectDeletionHandler;

	@Override
	public Project findById(long projectId) {
		return projectDao.findById(projectId);
	}

	@Override
	public void deleteProject(long projectId) {
		projectDeletionHandler.deleteProject(projectId);
	}

}
