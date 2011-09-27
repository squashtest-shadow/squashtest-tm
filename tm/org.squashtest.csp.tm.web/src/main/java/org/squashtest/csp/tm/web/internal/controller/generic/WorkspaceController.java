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
package org.squashtest.csp.tm.web.internal.controller.generic;

import java.util.List;

import javax.inject.Inject;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.tm.domain.library.Library;
import org.squashtest.csp.tm.service.WorkspaceService;
import org.squashtest.csp.tm.web.internal.model.builder.JsTreeNodeListBuilder;
import org.squashtest.csp.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.csp.tm.web.internal.model.jstree.JsTreeNode;

public abstract class WorkspaceController<LIBRARY extends Library<?>> {

	@Inject
	private DriveNodeBuilder nodeBuilder;

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView showWorkspace() {

		List<LIBRARY> libraries = getWorkspaceService().findAllLibraries();

		ModelAndView mav = new ModelAndView(getWorkspaceViewName());

		List<JsTreeNode> rootNodes = new JsTreeNodeListBuilder<LIBRARY>(nodeBuilder).setModel(libraries).build();
		mav.addObject("rootModel", rootNodes);
		return mav;
	}

	/**
	 * Should return a workspace service.
	 *
	 * @return
	 */
	protected abstract WorkspaceService<LIBRARY> getWorkspaceService();

	/**
	 * Returns the logical name of the page which shows the workspace.
	 *
	 * @return
	 */
	protected abstract String getWorkspaceViewName();

}
