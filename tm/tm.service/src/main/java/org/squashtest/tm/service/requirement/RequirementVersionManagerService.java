/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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

import javax.validation.constraints.NotNull;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.dynamicmanager.annotation.DynamicManager;
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.domain.requirement.RequirementVersion;

/**
 * Requirement Version mangement related services.
 * 
 * @author Gregory Fouquet
 * 
 */
@Transactional
@DynamicManager(name = "squashtest.tm.service.RequirementVersionManagerService", entity = RequirementVersion.class)
public interface RequirementVersionManagerService extends CustomRequirementVersionManagerService {
	@Transactional(readOnly = true)
	@PostAuthorize("hasPermission(returnObject,'READ') or hasRole('ROLE_ADMIN')")
	RequirementVersion findById(long requirementVersionId);

	@PreAuthorize("hasPermission(#arg0, 'org.squashtest.tm.domain.requirement.RequirementVersion','WRITE') or hasRole('ROLE_ADMIN')")
	void changeDescription(long requirementId, @NotNull String newDescription);

	@PreAuthorize("hasPermission(#arg0, 'org.squashtest.tm.domain.requirement.RequirementVersion', 'WRITE') or hasRole('ROLE_ADMIN')")
	void changeReference(long requirementVersionId, @NotNull String reference);

	@PreAuthorize("hasPermission(#arg0, 'org.squashtest.tm.domain.requirement.RequirementVersion', 'WRITE') or hasRole('ROLE_ADMIN')")
	void changeStatus(long requirementVersionId, @NotNull RequirementStatus status);

	@PreAuthorize("hasPermission(#arg0, 'org.squashtest.tm.domain.requirement.RequirementVersion', 'WRITE') or hasRole('ROLE_ADMIN')")
	void changeName(long requirementVersionId, String newName);


}
