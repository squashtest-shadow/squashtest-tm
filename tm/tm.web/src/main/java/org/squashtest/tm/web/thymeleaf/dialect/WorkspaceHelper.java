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
package org.squashtest.tm.web.thymeleaf.dialect;

import java.util.Collection;
import java.util.List;

import javax.servlet.ServletContext;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.domain.IdentifiedUtil;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.tm.service.bugtracker.BugTrackerFinderService;
import org.squashtest.tm.service.project.ProjectFilterModificationService;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.web.internal.model.jquery.FilterModel;



/**
 * Workspace helper, as an expression extension for thymeleaf 
 * 
 * @author bsiri
 *
 */
public class WorkspaceHelper {

	private final ServletContext servletContext;
	
	public WorkspaceHelper(final ServletContext servletContext){
		super();
		this.servletContext = servletContext;
	}
	
	public Collection<BugTracker> visibleBugtrackers(){
		
		WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		
		ProjectFinder projectFinder = wac.getBean(ProjectFinder.class);
		BugTrackerFinderService bugtrackerService = wac.getBean(BugTrackerFinderService.class);
		
		List<Project> projects = projectFinder.findAllReadable();
		List<Long> projectsIds = IdentifiedUtil.extractIds(projects);
		
		return bugtrackerService.findDistinctBugTrackersForProjects(projectsIds);
		
	}
	
	
	public FilterModel projectFilter(){		
		WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(servletContext);		
		ProjectFilterModificationService service = wac.getBean(ProjectFilterModificationService.class);
		
		ProjectFilter filter = service.findProjectFilterByUserLogin();
		List<Project> allProjects = service.getAllProjects();
		
		return new FilterModel(filter, allProjects);
		
	}

}
