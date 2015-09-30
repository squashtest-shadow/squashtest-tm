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
package org.squashtest.tm.web.internal.controller.customreport;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.squashtest.tm.api.workspace.WorkspaceType;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.tree.TreeLibraryNode;
import org.squashtest.tm.service.customreport.CustomReportWorkspaceService;
import org.squashtest.tm.web.internal.argumentresolver.MilestoneConfigResolver.CurrentMilestone;
import org.squashtest.tm.web.internal.helper.HyphenedStringHelper;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.json.JsonMilestone;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;

@Controller
@RequestMapping("/custom-report-workspace")
public class CustomReportWorkspaceController {

	@Inject
	protected InternationalizationHelper i18nHelper;

	@Inject
	@Named("org.squashtest.tm.service.customreport.CustomReportWorkspaceService")
	private CustomReportWorkspaceService workspaceService;
	
	@RequestMapping(method = RequestMethod.GET)
	public String showWorkspace(Model model, Locale locale,
			@CurrentMilestone Milestone activeMilestone,
			@CookieValue(value = "jstree_open", required = false, defaultValue = "") String[] openedNodes,
			@CookieValue(value = "workspace-prefs", required = false, defaultValue = "") String elementId) {
		
		List<TreeLibraryNode> libraries = workspaceService.findRootNodes();
		String[] nodesToOpen = null;

		if(elementId == null || "".equals(elementId)){
			nodesToOpen = openedNodes;
			model.addAttribute("selectedNode", "");
		} else {
			Long id = Long.valueOf(elementId);
//			nodesToOpen = getNodeParentsInWorkspace(id);
//			model.addAttribute("selectedNode", getTreeElementIdInWorkspace(id));
		}
		
//		List<JsTreeNode> rootNodes = new JsTreeNodeListBuilder<Library<LN>>(nodeBuilder).expand(expansionCandidates)
//				.setModel(libraries).build();
//		
		
		//Placeholder with just library for the beginning
		List<JsTreeNode> rootNodes = new ArrayList<JsTreeNode>();
		
		for (TreeLibraryNode crl : libraries) {
			JsTreeNode treeNode = new JsTreeNode();
			treeNode.setTitle(crl.getEntityName());
			rootNodes.add(treeNode);
			treeNode.addAttr("rel", "drive");
		}
		
		model.addAttribute("rootModel", rootNodes);
		
		//Active Milestone
		if (activeMilestone != null){
			JsonMilestone jsMilestone =
					new JsonMilestone(
							activeMilestone.getId(),
							activeMilestone.getLabel(),
							activeMilestone.getStatus(),
							activeMilestone.getRange(),
							activeMilestone.getEndDate(),
							activeMilestone.getOwner().getLogin()
							);
			model.addAttribute("activeMilestone", jsMilestone);
		}
		
		return getWorkspaceViewName();
	}

	protected String getWorkspaceViewName() {
		return "custom-report-workspace.html";
	}

	/**
	 * @see org.squashtest.tm.web.internal.controller.generic.WorkspaceController#getWorkspaceType()
	 */
	protected WorkspaceType getWorkspaceType() {
		return WorkspaceType.CUSTOM_REPORT_WORKSPACE;
	}
	
	private void getNodeParentsInWorkspace(){
		
	}
	
	/**
	 * Build a {@link JsTreeNode} for a given {@link TreeLibraryNode}
	 * @param model
	 * @return
	 */
	private JsTreeNode buildNode(TreeLibraryNode model){
		JsTreeNode treeNode = new JsTreeNode();
		treeNode.setTitle(model.getEntityName());
		return treeNode;
	}
	
	private String buildResourceType(String classSimpleName) {
		String singleResourceType = HyphenedStringHelper.camelCaseToHyphened(classSimpleName);
		return singleResourceType.replaceAll("y$", "ies");
	}
}
