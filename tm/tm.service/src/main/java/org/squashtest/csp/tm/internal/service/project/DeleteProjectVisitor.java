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
package org.squashtest.csp.tm.internal.service.project;

import javax.inject.Inject;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.csp.tm.domain.project.GenericProject;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.project.ProjectTemplate;
import org.squashtest.csp.tm.domain.project.ProjectVisitor;
import org.squashtest.csp.tm.internal.repository.ProjectTemplateDao;
import org.squashtest.csp.tm.internal.service.ProjectDeletionHandler;
@Component
@Scope("prototype")
public class DeleteProjectVisitor implements ProjectVisitor {
	
	@Inject
	private ProjectDeletionHandler projectDeletionHandler;
	
	@Inject
	private ProjectTemplateDao projectTemplateDao;
	
		public void deleteProject(GenericProject project){
		project.accept(this);
	}

	@Override
	public void visit(Project project) {
		projectDeletionHandler.deleteProject(project.getId());
	}
	
	@Override
	public void visit(ProjectTemplate projectTemplate) {
		//TODO
	}

}
