/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.service.requirement;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.dynamicmanager.annotation.DynamicManager;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementStatus;

@Transactional
@DynamicManager(name = "squashtest.tm.service.RequirementModificationService", entity = Requirement.class)
public interface RequirementModificationService extends CustomRequirementModificationService {
	@PreAuthorize("hasPermission(#arg0, 'org.squashtest.tm.domain.requirement.Requirement','WRITE') or hasRole('ROLE_ADMIN')")
	void changeDescription(long requirementId, String newDescription);

	@PreAuthorize("hasPermission(#arg0, 'org.squashtest.tm.domain.requirement.Requirement', 'WRITE') or hasRole('ROLE_ADMIN')")
	void changeReference(long requirementId, String reference);

	@PreAuthorize("hasPermission(#arg0, 'org.squashtest.tm.domain.requirement.Requirement', 'WRITE') or hasRole('ROLE_ADMIN')")
	void changeStatus(long requirementId, RequirementStatus status);

	@Transactional(readOnly = true)
	@PostAuthorize("hasPermission(returnObject,'READ') or hasRole('ROLE_ADMIN')")
	Requirement findById(long reqId);


}
