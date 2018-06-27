/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.globaldataset;

import org.apache.commons.collections.MultiMap;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.squashtest.tm.service.internal.customreport.CustomReportWorkspaceDisplayService;
import org.squashtest.tm.service.internal.dto.PermissionWithMask;
import org.squashtest.tm.service.internal.dto.UserDto;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;
import org.squashtest.tm.service.user.UserAccountService;
import org.squashtest.tm.web.internal.helper.JsTreeHelper;

import javax.inject.Inject;
import java.util.*;

/**
 * This controller is dedicated to the initial page of Global Datasets.
 * @author cholc
 */
@Controller
@RequestMapping("/dataset-workspace")
public class DatasetWorkspaceController {

	@Inject
	private UserAccountService userAccountService;

	@Inject
	private CustomReportWorkspaceDisplayService customReportWorkspaceDisplayService;

	@RequestMapping(method = RequestMethod.GET)
	public String showWorkspace(Model model, Locale locale,
	                            @CookieValue(value = "jstree_open", required = false, defaultValue = "") String[] openedNodes,
	                            @CookieValue(value = "jstree_select", required = false, defaultValue = "") String elementId) {


		List<JsTreeNode> rootNodes = new ArrayList<>();
		EnumSet<PermissionWithMask> permissions = EnumSet.allOf(PermissionWithMask.class);
		UserDto currentUser = userAccountService.findCurrentUserDto();

		JsTreeNode datasetLibrary = new JsTreeNode();
		datasetLibrary.setTitle("Test Project-1");
		datasetLibrary.addAttr("name", "Test Project-1");
		datasetLibrary.setState(JsTreeNode.State.open);
		datasetLibrary.addAttr("rel", "drive");
		datasetLibrary.addAttr("id", "DatasetLibrary-1");
		datasetLibrary.addAttr("resId", 1L);
		datasetLibrary.addAttr("restype", "global-dataset-libraries");

		JsTreeNode datasetFolder = new JsTreeNode();
		datasetFolder.setTitle("Dataset Folder");
		datasetFolder.addAttr("name","Dataset Folder");
		datasetFolder.setState(JsTreeNode.State.open);
		datasetFolder.addAttr("rel", "folder");
		datasetFolder.addAttr("id", "DatasetFolder-2");
		datasetFolder.addAttr("resId", 2L);

		JsTreeNode datasetFolder2 = new JsTreeNode();
		datasetFolder2.setTitle("Dataset Folder2");
		datasetFolder2.addAttr("name","Dataset Folder2");
		datasetFolder2.setState(JsTreeNode.State.open);
		datasetFolder2.addAttr("rel", "folder");
		datasetFolder2.addAttr("id", "DatasetFolder-6");
		datasetFolder2.addAttr("resId", 6L);

		JsTreeNode dataset = new JsTreeNode();
		dataset.setTitle("Dataset");
		dataset.addAttr("name","Dataset");
		dataset.setState(JsTreeNode.State.leaf);
		dataset.addAttr("rel", "global-dataset");
		dataset.addAttr("id", "Dataset-3");
		dataset.addAttr("resId", 3L);

		JsTreeNode datasetTemplate = new JsTreeNode();
		datasetTemplate.setTitle("Dataset Template");
		datasetTemplate.addAttr("name","Dataset Template");
		datasetTemplate.setState(JsTreeNode.State.leaf);
		datasetTemplate.addAttr("rel", "dataset-template");
		datasetTemplate.addAttr("id", "DatasetTemplate-4");
		datasetTemplate.addAttr("resId", 4L);

		JsTreeNode compositeDataset = new JsTreeNode();
		compositeDataset.setTitle("Dataset Composite");
		compositeDataset.addAttr("name","Dataset Composite");
		compositeDataset.setState(JsTreeNode.State.leaf);
		compositeDataset.addAttr("rel", "composite-dataset");
		compositeDataset.addAttr("id", "CompositeDataset-5");
		compositeDataset.addAttr("resId", 5L);

		for (PermissionWithMask permission : permissions) {
			datasetLibrary.addAttr(permission.getQuality(), String.valueOf(currentUser.isAdmin()));
			datasetFolder2.addAttr(permission.getQuality(), String.valueOf(currentUser.isAdmin()));
			dataset.addAttr(permission.getQuality(), String.valueOf(currentUser.isAdmin()));
			datasetTemplate.addAttr(permission.getQuality(), String.valueOf(currentUser.isAdmin()));
			compositeDataset.addAttr(permission.getQuality(), String.valueOf(currentUser.isAdmin()));
		}

		datasetFolder.setChildren(new ArrayList<>(Arrays.asList(dataset, compositeDataset, datasetTemplate)));
		datasetLibrary.setChildren(new ArrayList<>(Arrays.asList(datasetFolder, datasetFolder2)));

		rootNodes.add(datasetLibrary);

//		Set<String> nodeToOpen = new HashSet<>(Arrays.asList(openedNodes));
//
//		List<JsTreeNode> rootNodes = new ArrayList<>(
//			customReportWorkspaceDisplayService.findAllLibraries(Collections.singletonList(14L), currentUser,
//				mapIdsByType(nodeToOpen.toArray(new String[0]))));

		model.addAttribute("rootModel", rootNodes);
		return getWorkspaceViewName();
	}

	private String getWorkspaceViewName() {
		return "dataset-workspace.html";
	}

	protected MultiMap mapIdsByType(String[] openedNodes) {
		return JsTreeHelper.mapIdsByType(openedNodes);
	}
}
