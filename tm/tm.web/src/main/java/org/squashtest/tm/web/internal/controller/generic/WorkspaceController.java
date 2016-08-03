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
package org.squashtest.tm.web.internal.controller.generic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.api.wizard.WorkspaceWizard;
import org.squashtest.tm.api.workspace.WorkspaceType;
import org.squashtest.tm.domain.library.Library;
import org.squashtest.tm.domain.library.LibraryNode;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.service.library.WorkspaceService;
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder;
import org.squashtest.tm.service.milestone.MilestoneFinderService;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.web.internal.controller.campaign.MenuItem;
import org.squashtest.tm.web.internal.helper.JsTreeHelper;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.JsTreeNodeListBuilder;
import org.squashtest.tm.web.internal.model.builder.JsonProjectBuilder;
import org.squashtest.tm.web.internal.model.json.JsonMilestone;
import org.squashtest.tm.web.internal.model.json.JsonProject;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;
import org.squashtest.tm.web.internal.wizard.WorkspaceWizardManager;

import com.google.common.base.Optional;

public abstract class WorkspaceController<LN extends LibraryNode> {
	private static final Logger LOGGER = LoggerFactory.getLogger(WorkspaceController.class);

	@Inject
	private WorkspaceWizardManager workspaceWizardManager;

	@Inject
	protected InternationalizationHelper i18nHelper;

	@Inject
	protected ProjectFinder projectFinder;

	@Inject
	protected JsonProjectBuilder jsonProjectBuilder;

	@Inject
	protected MilestoneFinderService milestoneFinder;

	@Inject
	protected ActiveMilestoneHolder activeMilestoneHolder;

	/**
	 * Shows a workspace.
	 *
	 * @param model
	 * @param locale
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String showWorkspace(Model model, Locale locale,
	                            @CookieValue(value = "jstree_open", required = false, defaultValue = "") String[] openedNodes,
	                            @CookieValue(value = "workspace-prefs", required = false, defaultValue = "") String elementId) {

		List<Library<LN>> libraries = getWorkspaceService().findAllLibraries();
		String[] nodesToOpen;

		// #5585 : the case where elementId explicitly equals string litteral "null" can and will happen
		// thus the test here
		if (StringUtils.isBlank(elementId) || "null".equals(elementId)) {
			nodesToOpen = openedNodes;
			model.addAttribute("selectedNode", "");
		} else {
			Long id = Long.valueOf(elementId);
			nodesToOpen = getNodeParentsInWorkspace(id);
			model.addAttribute("selectedNode", getTreeElementIdInWorkspace(id));
		}

		MultiMap expansionCandidates = mapIdsByType(nodesToOpen);

		DriveNodeBuilder<LN> nodeBuilder = driveNodeBuilderProvider().get();

		Optional<Milestone> activeMilestone = activeMilestoneHolder.getActiveMilestone();
		if (activeMilestone.isPresent()) {
			nodeBuilder.filterByMilestone(activeMilestone.get());
		}

		List<JsTreeNode> rootNodes = new JsTreeNodeListBuilder<>(nodeBuilder).expand(expansionCandidates)
			.setModel(libraries).build();

		model.addAttribute("rootModel", rootNodes);

		populateModel(model, locale);

		// also add meta data about projects
		Collection<JsonProject> jsProjects = jsonProjectBuilder.getExtendedReadableProjects();

		model.addAttribute("projects", jsProjects);

		// also, milestones
		if (activeMilestone.isPresent()) {
			JsonMilestone jsMilestone =
				new JsonMilestone(
activeMilestone.get().getId(),
					activeMilestone.get().getLabel(), activeMilestone.get().getStatus(),
					activeMilestone.get().getRange(), activeMilestone.get().getEndDate(),
					activeMilestone.get().getOwner().getLogin()
				);
			model.addAttribute("activeMilestone", jsMilestone);
		}

		return getWorkspaceViewName();
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/tree/{openedNodes}")
	public
	List<JsTreeNode> getRootModel(@PathVariable String[] openedNodes) {
		List<Library<LN>> libraries = getWorkspaceService().findAllLibraries();


		MultiMap expansionCandidates = mapIdsByType(openedNodes);

		DriveNodeBuilder<LN> nodeBuilder = driveNodeBuilderProvider().get();
		List<JsTreeNode> rootNodes = new JsTreeNodeListBuilder<>(nodeBuilder).expand(expansionCandidates)
				.setModel(libraries).build();


		return rootNodes;
	}

	/**
	 * @param openedNodes
	 * @return
	 */
	protected MultiMap mapIdsByType(String[] openedNodes) {
		return JsTreeHelper.mapIdsByType(openedNodes);
	}

	/**
	 * Should return a workspace service.
	 *
	 * @return
	 */
	protected abstract <T extends Library<LN>> WorkspaceService<T> getWorkspaceService();

	/**
	 * Returns the logical name of the page which shows the workspace.
	 *
	 * @return
	 */
	protected abstract String getWorkspaceViewName();


	/**
	 * Returns the list of parents of a node given the id of an element
	 *
	 * @param elementId
	 * @return
	 */
	protected abstract String[] getNodeParentsInWorkspace(Long elementId);

	/**
	 * Returns the id of a node in the tree given the id of an element
	 *
	 * @param elementId
	 * @return
	 */
	protected abstract String getTreeElementIdInWorkspace(Long elementId);

	/**
	 * Called when {@link #getWorkspaceViewName()} is invoked. This allows you to add anything you need to
	 * thisworkspace's model. No need to supply the treenodes : they will be provided.
	 */
	protected abstract void populateModel(Model model, Locale locale);

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
	@SuppressWarnings("all")
	private MenuItem[] menuItems(Collection<WorkspaceWizard> wizards) {
		Collection<WorkspaceWizard> effective = CollectionUtils.select(wizards, new Predicate() {
			@Override
			public boolean evaluate(Object object) {
				return (((WorkspaceWizard)object).getWizardMenu() != null);
			}
		});

		MenuItem[] res = new MenuItem[effective.size()];
		int i = 0;

		for (WorkspaceWizard wizard : wizards) {
			if (wizard.getWizardMenu()!=null){
				res[i] = createMenuItem(wizard);
				i++;
			}
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

	protected InternationalizationHelper getI18nHelper() {
		return i18nHelper;
	}

	/**
	 * Returns the appropriate drive node builder. Should never return null.
	 *
	 * @return
	 */
	protected abstract Provider<DriveNodeBuilder<LN>> driveNodeBuilderProvider();

}
