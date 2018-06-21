package org.squashtest.tm.web.internal.controller.dataset;

import org.apache.commons.collections.MultiMap;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.squashtest.tm.service.internal.customreport.CustomReportWorkspaceDisplayService;
import org.squashtest.tm.service.internal.dto.UserDto;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;
import org.squashtest.tm.service.user.UserAccountService;
import org.squashtest.tm.web.internal.helper.JsTreeHelper;

import javax.inject.Inject;
import java.util.*;

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

		JsTreeNode datasetLibrary = new JsTreeNode();
		datasetLibrary.setTitle("Test Project-1");
		datasetLibrary.setState(JsTreeNode.State.open);
		datasetLibrary.addAttr("rel", "drive");
		datasetLibrary.addAttr("id", "DatasetLibrary-1");
		datasetLibrary.addAttr("resId", 1L);

		JsTreeNode datasetFolder = new JsTreeNode();
		datasetFolder.setTitle("Dataset Folder");
		datasetFolder.setState(JsTreeNode.State.open);
		datasetFolder.addAttr("rel", "folder");
		datasetFolder.addAttr("id", "DatasetFolder-1");
		datasetFolder.addAttr("resId", 1L);


		JsTreeNode dataset = new JsTreeNode();
		dataset.setTitle("Dataset");
		dataset.setState(JsTreeNode.State.closed);
		dataset.addAttr("rel", "dataset");
		dataset.addAttr("id", "Dataset-1");
		dataset.addAttr("resId", 1L);

		JsTreeNode datasetTemplate = new JsTreeNode();
		datasetTemplate.setTitle("Dataset Template");
		datasetTemplate.setState(JsTreeNode.State.closed);
		datasetTemplate.addAttr("rel", "dataset-template");
		datasetTemplate.addAttr("id", "DatasetTemplate-1");
		datasetTemplate.addAttr("resId", 1L);

		JsTreeNode datasetComposite = new JsTreeNode();
		datasetComposite.setTitle("Dataset Composite");
		datasetComposite.setState(JsTreeNode.State.closed);
		datasetComposite.addAttr("rel", "dataset-composite");
		datasetComposite.addAttr("id", "DatasetComposite-1");
		datasetComposite.addAttr("resId", 1L);

		datasetFolder.setChildren(new ArrayList<>(Arrays.asList(dataset,datasetComposite,datasetTemplate)));
		datasetLibrary.setChildren(Collections.singletonList(datasetFolder));

		rootNodes.add(datasetLibrary);

//		Set<String> nodeToOpen = new HashSet<>(Arrays.asList(openedNodes));
//
//		UserDto currentUser = userAccountService.findCurrentUserDto();
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
