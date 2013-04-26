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
package org.squashtest.tm.web.internal.controller.generic;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.api.wizard.WorkspaceWizard;
import org.squashtest.tm.api.workspace.WorkspaceType;
import org.squashtest.tm.domain.library.Library;
import org.squashtest.tm.service.library.WorkspaceService;
import org.squashtest.tm.web.internal.controller.campaign.MenuItem;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.JsTreeNodeListBuilder;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;
import org.squashtest.tm.web.internal.wizard.WorkspaceWizardManager;

public abstract class WorkspaceController<LIBRARY extends Library<?>> {

	@Inject
	private Provider<DriveNodeBuilder> nodeBuilderProvider;
	@Inject
	private WorkspaceWizardManager workspaceWizardManager;

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView showWorkspace() {

		List<LIBRARY> libraries = getWorkspaceService().findAllLibraries();

		ModelAndView mav = new ModelAndView(getWorkspaceViewName());

		DriveNodeBuilder nodeBuilder = nodeBuilderProvider.get();
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

	/**
	 * Returns the workspace type managed by the concrete controller.
	 * 
	 * @return
	 */
	protected abstract WorkspaceType getWorkspaceType();

	@ModelAttribute("wizards")
	public MenuItem[] getWorkspaceWizards() {
		Collection<WorkspaceWizard> wizards = workspaceWizardManager.findAllByWorkspace(getWorkspaceType());
	
		return menuItems(wizards);
	}

	/**
	 * @param wizards
	 * @return
	 */
	private MenuItem[] menuItems(Collection<WorkspaceWizard> wizards) {
		MenuItem[] res = new MenuItem[wizards.size()];
		int i = 0;
	
		for (WorkspaceWizard wizard : wizards) {
			res[i] = createMenuItem(wizard);
			i++;
		}
	
		return res;
	}

	/**
	 * @param wizard
	 * @return
	 */
	private MenuItem createMenuItem(WorkspaceWizard wizard) {
		MenuItem item = new MenuItem();
		item.setId(wizard.getId());
		item.setLabel(wizard.getWizardMenu().getLabel());
		item.setTooltip(wizard.getWizardMenu().getTooltip());
		item.setUrl(wizard.getWizardMenu().getUrl());
		item.setAccessRule(wizard.getWizardMenu().getAccessRule());
		return item;
	}

}
