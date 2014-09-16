/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.internal.service;


import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.project.ProjectResource;
import org.squashtest.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.tm.plugin.report.std.service.DataFilteringService;
import org.squashtest.tm.service.project.ProjectFilterModificationService;
import org.squashtest.tm.service.security.PermissionEvaluationService;


@Service("squashtest.tm.service.DataFilteringService")
public class DataFilteringServiceImpl implements DataFilteringService {
	private PermissionEvaluationService permissionService;
	
	private ProjectFilterModificationService userFilterService;
	
	@ServiceReference
	public void setPermissionEvaluationService(PermissionEvaluationService service){
		this.permissionService = service;
	}
	
	@Override
	public boolean isFullyAllowed(Object object) {
		return hasReadPermissions(object) 
		&& (object instanceof ProjectResource ? 
				isAllowedByUser((ProjectResource)object) 
				: true //will prolly change that later.
			);
	}
	
	
	@Override
	public boolean hasReadPermissions(Object object) {
		return permissionService.canRead(object);
	}

	@Override
	public boolean isAllowedByUser(ProjectResource object) {
		ProjectFilter filter = userFilterService.findProjectFilterByUserLogin();
		
		return filter.getActivated() ? 
				filter.isProjectSelected(object.getProject())
				: true;
		
	}

	/**
	 * @param userFilterService the userFilterService to set
	 */
	@ServiceReference
	public void setUserFilterService(ProjectFilterModificationService userFilterService) {
		this.userFilterService = userFilterService;
	}

	/**
	 * @param permissionService the permissionService to set
	 */
	@ServiceReference
	public void setPermissionService(PermissionEvaluationService permissionService) {
		this.permissionService = permissionService;
	}

}
