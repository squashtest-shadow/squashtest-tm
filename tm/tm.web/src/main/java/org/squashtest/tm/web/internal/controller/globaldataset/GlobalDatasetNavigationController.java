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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.squashtest.tm.domain.dataset.DatasetFolder;
import org.squashtest.tm.domain.dataset.DatasetLibraryNode;
import org.squashtest.tm.domain.tree.TreeEntity;
import org.squashtest.tm.service.dataset.DatasetLibraryNodeService;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;
import org.squashtest.tm.web.internal.model.builder.GlobalDatasetTreeNodeBuilder;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.validation.Valid;

/**
 * This controller is dedicated to the operations in the tree of Global Datasets.
 * Based on {@link org.squashtest.tm.web.internal.controller.customreport.CustomReportNavigationController}.
 * @author aguilhem
 */
@Controller
@RequestMapping("/global-dataset-browser")
public class GlobalDatasetNavigationController {
	public static final Logger LOGGER = LoggerFactory.getLogger(GlobalDatasetNavigationController.class);

	@Inject
	DatasetLibraryNodeService datasetLibraryNodeService;

	@Inject
	@Named("globalDataset.nodeBuilder")
	private Provider<GlobalDatasetTreeNodeBuilder> builderProvider;

	//----- CREATE NODE METHODS -----

	@ResponseBody
	@RequestMapping(value = "/drives/{libraryId}/content/new-folder", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	public JsTreeNode createNewFolderInLibrary(@PathVariable Long libraryId, @Valid @RequestBody DatasetFolder datasetFolder) {
		return createNewDatasetLibraryNode(libraryId, datasetFolder);
	}

	@ResponseBody
	@RequestMapping(value = "/folders/{folderId}/content/new-folder", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	public JsTreeNode createNewFolderInFolder(@PathVariable Long folderId, @Valid @RequestBody DatasetFolder datasetFolder) {
		return createNewDatasetLibraryNode(folderId, datasetFolder);
	}

	//-------------- PRIVATE STUFF ---------------------------
	private JsTreeNode createNewDatasetLibraryNode(Long libraryId, TreeEntity entity) {
		DatasetLibraryNode newNode = datasetLibraryNodeService.createNewNode(libraryId, entity);
		return builderProvider.get().build(newNode);
	}
}
