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
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.squashtest.tm.api.workspace.WorkspaceType;
import org.squashtest.tm.domain.Level;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.infolist.InfoList;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.infolist.SystemInfoListCode;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.service.customreport.CustomReportLibraryNodeService;
import org.squashtest.tm.service.customreport.CustomReportWorkspaceService;
import org.squashtest.tm.service.infolist.InfoListFinderService;
import org.squashtest.tm.web.internal.argumentresolver.MilestoneConfigResolver.CurrentMilestone;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.i18n.MessageObject;
import org.squashtest.tm.web.internal.model.builder.CustomReportTreeNodeBuilder;
import org.squashtest.tm.web.internal.model.json.JsonMilestone;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;

@Controller
@RequestMapping("/custom-report-workspace")
public class CustomReportWorkspaceController {

	public static final Logger LOGGER = LoggerFactory.getLogger(CustomReportWorkspaceController.class);
	
	private final String cookieDelimiter = "#";
	
	@Inject
	@Named("org.squashtest.tm.service.customreport.CustomReportWorkspaceService")
	private CustomReportWorkspaceService workspaceService;
	
	@Inject
	private CustomReportLibraryNodeService customReportLibraryNodeService;
	
	@Inject
	@Named("customReport.nodeBuilder")
	private Provider<CustomReportTreeNodeBuilder> builderProvider;
	
	@Inject
	private InfoListFinderService infoListFinder;
	
	@Inject
	protected InternationalizationHelper i18nHelper;
	
	@RequestMapping(method = RequestMethod.GET)
	public String showWorkspace(Model model, Locale locale,
			@CurrentMilestone Milestone activeMilestone,
			@CookieValue(value = "jstree_open", required = false, defaultValue = "") String[] openedNodes,
			@CookieValue(value = "jstree_select", required = false, defaultValue = "") String elementId) {
		
		List<CustomReportLibraryNode> libraries = workspaceService.findRootNodes();
		
		LOGGER.debug("JTH - selected Node " + elementId);
		LOGGER.debug("JTH - openedNodes" + openedNodes.toString());
		for (int i = 0; i < openedNodes.length; i++) {
			LOGGER.debug("JTH - " + openedNodes[i]);
		}
		
		Set<Long> nodeIdToOpen = new HashSet<Long>();
		nodeIdToOpen.addAll(convertCookieIds(openedNodes));
		//Every node above selected node should be opened and it should be not necessary to get ancestors.
		//But we have corner cases liken when we create a new chart in different screen...
		if (StringUtils.isNotBlank(elementId)) {
			nodeIdToOpen.addAll(findAncestorsOfselectedNode(elementId));
		}
		
		//Placeholder with just library for the beginning
		List<JsTreeNode> rootNodes = new ArrayList<JsTreeNode>();
		
		for (CustomReportLibraryNode crl : libraries) {
			JsTreeNode treeNode = builderProvider.get().buildWithOpenedNodes(crl, nodeIdToOpen);
			rootNodes.add(treeNode);
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
		
		//defaults lists and enums levels
		model.addAttribute("defaultInfoLists", getInternationalizedDefaultList(locale));
		model.addAttribute("testCaseImportance", getI18nLevelEnum(TestCaseImportance.class,locale));
		model.addAttribute("testCaseStatus", getI18nLevelEnum(TestCaseStatus.class,locale));
		model.addAttribute("requirementStatus", getI18nLevelEnum(RequirementStatus.class,locale));
		model.addAttribute("requirementCriticality", getI18nLevelEnum(RequirementCriticality.class,locale));
		
		return getWorkspaceViewName();
	}

	private Collection<Long> findAncestorsOfselectedNode(String elementId) {
		Long nodeId = convertCookieId(elementId);
		List<Long> ancestorIds = customReportLibraryNodeService.findAncestorIds(nodeId);
		//The selected node isn't opened by default (it can be a leaf node !).
		//So it will be open ONLY if he's also in open node cookies.
		ancestorIds.remove(nodeId);
		return ancestorIds;
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
	
	private Long convertCookieId(String cookieValue){
		cookieValue = cookieValue.replace(cookieDelimiter, "");
		return Long.parseLong(cookieValue);
	}
	
	private Set<Long> convertCookieIds(String[] cookieValues){
		Set<Long> nodeIdToOpen = new HashSet<Long>();
		for (String value : cookieValues) {
			nodeIdToOpen.add(convertCookieId(value));
		}
		return nodeIdToOpen;
	}
	
	private MessageObject getInternationalizedDefaultList(Locale locale){
		//default infolist values
		Map<String, InfoList> listMap = new HashMap<String, InfoList>();
		listMap.put("REQUIREMENT_VERSION_CATEGORY",infoListFinder.findByCode(SystemInfoListCode.REQUIREMENT_CATEGORY.getCode()));
		listMap.put("TEST_CASE_NATURE", infoListFinder.findByCode(SystemInfoListCode.TEST_CASE_NATURE.getCode()));
		listMap.put("TEST_CASE_TYPE", infoListFinder.findByCode(SystemInfoListCode.TEST_CASE_TYPE.getCode()));
		
		MessageObject mapItems = new MessageObject();
		for (InfoList infoList : listMap.values()) {
			List<InfoListItem> infoListItems = infoList.getItems();
			for (InfoListItem infoListItem : infoListItems) {
				mapItems.put(infoListItem.getLabel(), infoListItem.getLabel());
			}
		}
		
		i18nHelper.resolve(mapItems, locale);
		return mapItems;
	}
	
	private <E extends Enum<E> & Level> MessageObject getI18nLevelEnum(Class<E> clazz, Locale locale) {
		MessageObject i18nEnums = new MessageObject();
		EnumSet<E> levels = EnumSet.allOf(clazz);
		for (E level : levels) {
			i18nEnums.put(level.name(), level.getI18nKey());
		}
		i18nHelper.resolve(i18nEnums, locale);
		return i18nEnums;
	}
}
