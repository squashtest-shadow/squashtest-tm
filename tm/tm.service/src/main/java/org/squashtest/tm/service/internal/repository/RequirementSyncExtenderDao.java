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
package org.squashtest.tm.service.internal.repository;

import java.util.Collection;

import org.squashtest.tm.core.dynamicmanager.annotation.DynamicDao;
import org.squashtest.tm.core.dynamicmanager.annotation.QueryParam;
import org.squashtest.tm.domain.requirement.RequirementSyncExtender;

@DynamicDao(entity = RequirementSyncExtender.class, hasCustomImplementation = false)
public interface RequirementSyncExtenderDao {


	void persist(RequirementSyncExtender extender);
	
	RequirementSyncExtender retrieveByRemoteKey(@QueryParam("id") String remoteId);
	Collection<RequirementSyncExtender> retrieveAllByRemoteKey(@QueryParam("ids") Collection<String> remoteId);
	Collection<RequirementSyncExtender> retrieveAllByRemoteProjectsAndFilter(@QueryParam("pId") String remoteProjectId, @QueryParam("filter") String filterName);
	Collection<RequirementSyncExtender> retrieveAllByServer(@QueryParam("serverId") Long serverId);
	

	void delete(RequirementSyncExtender extender);
	void deleteAllByServer(@QueryParam("serverId") Long serverId);
	
}
