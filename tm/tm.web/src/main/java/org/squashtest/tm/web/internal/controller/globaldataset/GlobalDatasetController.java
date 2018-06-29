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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.squashtest.tm.domain.dataset.*;
import org.squashtest.tm.service.dataset.DatasetLibraryNodeService;
import org.squashtest.tm.web.internal.model.jquery.RenameModel;

import javax.inject.Inject;

/**
 * This controller is dedicated to the contextual content of global dataset (ie the right part of the screen)
 * @author aguilhem
 * @author cholc
 */
@Controller
public class GlobalDatasetController {

	public static final Logger LOGGER = LoggerFactory.getLogger(GlobalDatasetController.class);

	private static final String NEW_NAME = "newName";
	@Inject
	private DatasetLibraryNodeService datasetLibraryNodeService;

	@ResponseBody
	@RequestMapping(value = "dataset-library/{id}", method = RequestMethod.GET)
	public DatasetLibrary getLibraryDetails(@PathVariable Long id) {
		return datasetLibraryNodeService.findLibraryByTreeNodeId(id);
	}

	@ResponseBody
	@RequestMapping(value = "dataset-folder/{id}", method = RequestMethod.GET)
	public DatasetFolder getFolderDetails(@PathVariable Long id) {
		return datasetLibraryNodeService.findFolderByTreeNodeId(id);
	}

	@ResponseBody
	@RequestMapping(value = "global-dataset/{id}", method = RequestMethod.GET)
	public GlobalDataset getGlobalDatasetDetails(@PathVariable Long id) {
		return datasetLibraryNodeService.findGlobalDatasetByTreeNodeId(id);
	}

	@ResponseBody
	@RequestMapping(value = "composite-dataset/{id}", method = RequestMethod.GET)
	public CompositeDataset getCompositeDatasetDetails(@PathVariable Long id) {
		return datasetLibraryNodeService.findCompositeDatasetByTreeNodeId(id);
	}

	@ResponseBody
	@RequestMapping(value = "dataset-template/{id}", method = RequestMethod.GET)
	public DatasetTemplate getDatasetTemplateDetails(@PathVariable Long id) {
		return datasetLibraryNodeService.findDatasetTemplateByTreeNodeId(id);
	}

	//---- RENAME ----

	@RequestMapping(method = RequestMethod.POST, value="dataset-folders/{nodeId}",params = {NEW_NAME})
	@ResponseBody
	public RenameModel renameDatasetFolder(@PathVariable long nodeId, @RequestParam String newName) {
		return renameNode(nodeId, newName);
	}

	@RequestMapping(method = RequestMethod.POST, value="global-datasets/{nodeId}",params = {NEW_NAME})
	@ResponseBody
	public RenameModel renameGlobalDataset(@PathVariable long nodeId, @RequestParam String newName) {
		return renameNode(nodeId, newName);
	}

	@RequestMapping(method = RequestMethod.POST, value="composite-datasets/{nodeId}",params = {NEW_NAME})
	@ResponseBody
	public RenameModel renameCompositeDataset(@PathVariable long nodeId, @RequestParam String newName) {
		return renameNode(nodeId, newName);
	}

	@RequestMapping(method = RequestMethod.POST, value="dataset-templates/{nodeId}",params = {NEW_NAME})
	@ResponseBody
	public RenameModel renameDatasetTemplate(@PathVariable long nodeId, @RequestParam String newName) {
		return renameNode(nodeId, newName);
	}

	private RenameModel renameNode (long nodeId, String newName){
		datasetLibraryNodeService.renameNode(nodeId, newName);
		return new RenameModel(newName);
	}
}
