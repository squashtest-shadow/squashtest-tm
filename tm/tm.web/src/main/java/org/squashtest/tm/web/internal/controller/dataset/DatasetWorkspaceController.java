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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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

		String[] emptyList = new String[0];
		UserDto currentUser = userAccountService.findCurrentUserDto();
		List<JsTreeNode> rootNodes = new ArrayList<>(customReportWorkspaceDisplayService.findAllLibraries(Collections.singletonList(14L), currentUser, mapIdsByType(emptyList)));

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
