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

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface ProjectModificationService extends CustomProjectModificationService {
	@PreAuthorize("hasPermission(#arg0, 'org.squashtest.csp.tm.domain.project.Project', 'MANAGEMENT') or hasRole('ROLE_ADMIN')")
	void changeDescription(long projectId, String newDescription);

	@PreAuthorize("hasPermission(#arg0, 'org.squashtest.csp.tm.domain.project.Project', 'MANAGEMENT') or hasRole('ROLE_ADMIN')")
	void changeLabel(long projectId, String newLabel);

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	void changeName(long projectId, String newName);

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	void changeActive(long projectId, boolean isActive);
	
	@PreAuthorize("hasPermission(#arg0, 'org.squashtest.csp.tm.domain.project.Project', 'MANAGEMENT') or hasRole('ROLE_ADMIN')")
	void changeTestAutomationEnabled(long projectId, boolean isEnabled);

}
