/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package org.squashtest.tm.web.internal.helper;

import java.util.Collection;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.domain.IdentifiedUtil;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.service.bugtracker.BugTrackerFinderService;
import org.squashtest.tm.service.project.ProjectFinder;

/**
 * Warning : strongly tied to Spring
 * 
 * @author bsiri
 *
 */
public class WorkspaceHelper extends SimpleTagSupport{

	
	public static Collection<BugTracker> getVisibleBugtrackers(ServletContext context){
		
		WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(context);
		
		ProjectFinder projectFinder = wac.getBean(ProjectFinder.class);
		BugTrackerFinderService bugtrackerService = wac.getBean(BugTrackerFinderService.class);
		
		List<Project> projects = projectFinder.findAllReadable();
		List<Long> projectsIds = IdentifiedUtil.extractIds(projects);
		return bugtrackerService.findDistinctBugTrackersForProjects(projectsIds);
	}
	
}
