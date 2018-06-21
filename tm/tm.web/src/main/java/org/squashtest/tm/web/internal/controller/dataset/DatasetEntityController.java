package org.squashtest.tm.web.internal.controller.dataset;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.domain.dataset.DatasetFolder;
import org.squashtest.tm.domain.dataset.DatasetLibrary;
import org.squashtest.tm.service.dataset.DatasetLibraryNodeService;

import javax.inject.Inject;

@Controller
public class DatasetEntityController {

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
}
