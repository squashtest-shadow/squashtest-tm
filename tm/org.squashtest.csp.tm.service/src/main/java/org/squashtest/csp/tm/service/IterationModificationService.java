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

import java.util.Date;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface IterationModificationService extends CustomIterationModificationService {
	@PreAuthorize("hasPermission(#arg0, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'SMALL_EDIT') "
			+ "or hasRole('ROLE_ADMIN')")
	void changeDescription(long iterationId, String newDescription);

	@PreAuthorize("hasPermission(#arg0, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'SMALL_EDIT') "
			+ "or hasRole('ROLE_ADMIN')")
	void changeScheduledStartDate(long iterationId, Date scheduledStart);

	@PreAuthorize("hasPermission(#arg0, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'SMALL_EDIT') "
			+ "or hasRole('ROLE_ADMIN')")
	void changeScheduledEndDate(long iterationId, Date scheduledEnd);

	@PreAuthorize("hasPermission(#arg0, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'SMALL_EDIT') "
			+ "or hasRole('ROLE_ADMIN')")
	void changeActualStartDate(long iterationId, Date actualStart);

	@PreAuthorize("hasPermission(#arg0, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'SMALL_EDIT') "
			+ "or hasRole('ROLE_ADMIN')")
	void changeActualEndDate(long iterationId, Date actualEnd);

	@PreAuthorize("hasPermission(#arg0, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'SMALL_EDIT') "
			+ "or hasRole('ROLE_ADMIN')")
	void changeActualStartAuto(long iterationId, boolean isAuto);

	@PreAuthorize("hasPermission(#arg0, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'SMALL_EDIT') "
			+ "or hasRole('ROLE_ADMIN')")
	void changeActualEndAuto(long iterationId, boolean isAuto);

}
