/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.service.internal.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.requirement.RequirementSyncExtender;
import org.squashtest.tm.service.annotation.EmptyCollectionGuard;

import java.util.Collection;

public interface RequirementSyncExtenderDao extends Repository<RequirementSyncExtender, Long> {

	@NativeMethodFromJpaRepository
	void save(RequirementSyncExtender extender);

	@UsesANamedQueryInPackageInfoOrElsewhere
	RequirementSyncExtender retrieveByRemoteKey(@Param("id") String remoteId, @Param("pId") Long projectId);

	@UsesANamedQueryInPackageInfoOrElsewhere
	@EmptyCollectionGuard
	Collection<RequirementSyncExtender> retrieveAllByRemoteKey(@Param("ids") Collection<String> remoteId, @Param("pId") Long projectId);

	@UsesANamedQueryInPackageInfoOrElsewhere
	Collection<RequirementSyncExtender> retrieveAllByRemoteProjectsAndFilter(@Param("remotePId") String remoteProjectId, @Param("filter") String filterName, @Param("pId") Long projectId);

	@UsesANamedQueryInPackageInfoOrElsewhere
	Collection<RequirementSyncExtender> retrieveAllByServer(@Param("serverId") Long serverId);

	@NativeMethodFromJpaRepository
	void delete(RequirementSyncExtender extender);

	@UsesANamedQueryInPackageInfoOrElsewhere
	@Modifying
	@Transactional
	void deleteAllByServer(@Param("serverId") Long serverId);

}
