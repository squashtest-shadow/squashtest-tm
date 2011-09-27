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

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Service;
import org.squashtest.csp.core.service.security.PermissionEvaluationService;
import org.squashtest.csp.tm.domain.project.ProjectResource;
import org.squashtest.csp.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.csp.tm.service.DataFilteringService;
import org.squashtest.csp.tm.service.ProjectFilterModificationService;


@Service("squashtest.tm.service.DataFilteringService")
public class DataFilteringServiceImpl implements DataFilteringService {

	private PermissionEvaluationService permissionService;
	
	@Inject
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

}
