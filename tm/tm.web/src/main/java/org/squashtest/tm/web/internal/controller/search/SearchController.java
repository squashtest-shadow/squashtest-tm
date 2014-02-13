/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.search;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.domain.requirement.RequirementSearchCriteria;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.service.library.SearchService;
import org.squashtest.tm.web.internal.model.search.ExpandedTestCase;

@Controller
@RequestMapping("/search")
public class SearchController {

	private static final String RESULT_LIST = "resultList";

	private static final String WORKSPACE = "workspace";
	
//	private static final String PROJECT_LIST = "projectList";
	
	private static final String NODE_NAME = "nodeName";

	private static final String ICON = "icon";

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchController.class);
	private static final String NODE_NAME_REJEX = "-";

	private SearchService searchService;

	@ServiceReference
	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}
	
	@RequestMapping(value = "test-cases/breadcrumb", method = RequestMethod.POST, params = {NODE_NAME})
	@ResponseBody
	public List<String> findBreadCrumbTestCase(@RequestParam(NODE_NAME) String nodeName){
		LOGGER.trace("search breadcrumb");

		String[] splitedNodeName = nodeName.split(NODE_NAME_REJEX);
		String className = splitedNodeName[0];
		Long nodeId = Long.parseLong(splitedNodeName[1]);
		return searchService.findBreadCrumbForTestCase(className, nodeId, NODE_NAME_REJEX);
	}
	@RequestMapping(value = "requirements/breadcrumb", method = RequestMethod.POST, params = {NODE_NAME})
	@ResponseBody
	public List<String> findBreadCrumbRequirement(@RequestParam(NODE_NAME) String nodeName){
		LOGGER.trace("search breadcrumb");

		String[] splitedNodeName = nodeName.split(NODE_NAME_REJEX);
		String className = splitedNodeName[0];
		Long nodeId = Long.parseLong(splitedNodeName[1]);
		return searchService.findBreadCrumbForRequirement(className, nodeId, NODE_NAME_REJEX);
	}
	@RequestMapping(value = "campaigns/breadcrumb", method = RequestMethod.POST, params = {NODE_NAME})
	@ResponseBody
	public List<String> findBreadCrumbCampaign(@RequestParam(NODE_NAME) String nodeName){
		LOGGER.trace("search breadcrumb");

		String[] splitedNodeName = nodeName.split(NODE_NAME_REJEX);
		String className = splitedNodeName[0];
		Long nodeId = Long.parseLong(splitedNodeName[1]);
		return searchService.findBreadCrumbForCampaign(className, nodeId, NODE_NAME_REJEX);
	}
	/*
	 * note that according to the mapping below importance[] may be legally null.
	 * 
	 */
	@RequestMapping(value = "/test-cases", method = RequestMethod.GET)
	public ModelAndView searchOrderedTestCases(@RequestParam("name") String name, @RequestParam("order") Boolean order, 
			@RequestParam(value="importance[]", required=false) String[] importance,
			@RequestParam(value="nature[]", required=false) String[] nature,
			@RequestParam(value="type[]", required=false) String[] type,
			@RequestParam(value="status[]", required=false) String[] status) {
		LOGGER.info("SQUASH INFO: TRY TestCase search with name : " + name);

		TestCaseSearchCriteriaAdapter criteria = new TestCaseSearchCriteriaAdapter(name, order, importance, nature, type, status);
		
		List<TestCaseLibraryNode> resultList = searchService.findTestCase(criteria);

		LOGGER.info("SQUASH INFO: DONE TestCase search with name : " + name);
		ModelAndView mav;
		if (order) {
			mav = new ModelAndView("fragment/generics/search-result-display-ordered");
		} else {
			mav = new ModelAndView("fragment/generics/search-result-display");
		}
		mav.addObject(RESULT_LIST, resultList);
		mav.addObject(ICON, "TestCase");
		mav.addObject(WORKSPACE, "test-case");

		return mav;
	}

	/**
	 * jQuery submits selected criticalities with "criticalities[]" name. Spring cannot bind this name to a command
	 * object property out-of-the-box.
	 *
	 * @param params
	 * @param criticalitiesSelection
	 * @return
	 */
	@RequestMapping(value = "/requirements", method = RequestMethod.GET)
	public ModelAndView searchRequirements(@ModelAttribute final RequirementSearchParams params,
			@RequestParam("criticalities[]") final boolean[] criticalitiesSelection, @RequestParam("categories[]") final boolean[] categoriesSelection) {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Searching requirements using criteria " + params);
		}

		RequirementSearchCriteria criteria = new RequirementSearchCriteriaAdapter(params, criticalitiesSelection, categoriesSelection);
		List<RequirementLibraryNode> resultList = searchService.findAllBySearchCriteria(criteria);

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SQUASH INFO: DONE requirement search with name : " + criteria.getName() + " found "
					+ resultList.size() + " items");
		}

		ModelAndView mav = new ModelAndView("fragment/generics/search-result-display");
		mav.addObject(RESULT_LIST, resultList);
		mav.addObject(ICON, "Requirement");
		mav.addObject(WORKSPACE, "requirement");

		return mav;
	}

	@RequestMapping(value = "/requirements", method = RequestMethod.GET, params = { "order=true" })
	public ModelAndView searchOrderedRequirements(@ModelAttribute final RequirementSearchParams params,
			@RequestParam("criticalities[]") final boolean[] criticalitiesSelection, @RequestParam("categories[]") final boolean[] categoriesSelection) {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Searching requirements using criteria " + params);
		}
		RequirementSearchCriteria criteria = new RequirementSearchCriteriaAdapter(params, criticalitiesSelection, categoriesSelection);
		
		List<RequirementLibraryNode> resultList = searchService.findAllBySearchCriteriaOrderByProject(criteria);

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SQUASH INFO: DONE requirement search with name : " + criteria.getName() + " found "
					+ resultList.size() + " items");
		}

		ModelAndView mav;
		LOGGER.info("SQUASH INFO: DONE requirement ordered search with name : " + params.getName());
		mav = new ModelAndView("fragment/generics/search-result-display-ordered");

		mav.addObject(RESULT_LIST, resultList);
		mav.addObject(ICON, "Requirement");
		mav.addObject(WORKSPACE, "requirement");

		return mav;
	}

	@RequestMapping(value = "/campaigns", method = RequestMethod.GET, params = { "order" })
	public ModelAndView searchOrderedCampaigns(@RequestParam("name") String name, @RequestParam String order) {
		LOGGER.info("SQUASH INFO: TRY Campaign search with name : " + name);

		boolean isOrdered = Boolean.parseBoolean(order);

		List<CampaignLibraryNode> resultList = searchService.findCampaignByName(name, isOrdered);

		LOGGER.info("SQUASH INFO: DONE Campaign search with name : " + name);

		ModelAndView mav;

		if (isOrdered) {
			mav = new ModelAndView("fragment/generics/search-result-display-ordered");
		} else {
			mav = new ModelAndView("fragment/generics/search-result-display");
		}

		mav.addObject(RESULT_LIST, resultList);
		mav.addObject(ICON, "Campaign");
		mav.addObject(WORKSPACE, "campaign");

		return mav;
	}

	@RequestMapping(value = "/tc-by-requirement", method = RequestMethod.GET, params = { "order" })
	public ModelAndView searchTestCasesByRequirement(@ModelAttribute final RequirementSearchParams params,
			@RequestParam("criticalities[]") final boolean[] criticalitiesSelection, @RequestParam("categories[]") final boolean[] categoriesSelection,@RequestParam String order) {
		boolean isProjectOrdered = Boolean.parseBoolean(order);

		RequirementSearchCriteria criteria = new RequirementSearchCriteriaAdapter(params, criticalitiesSelection, categoriesSelection);
		List<TestCase> resultList = searchService.findTestCaseByRequirement(criteria, isProjectOrdered);
		ModelAndView mav;
		if (isProjectOrdered) {
			mav = new ModelAndView("fragment/generics/search-result-display-ordered-by-requirement");
		} else {
			mav = new ModelAndView("fragment/generics/search-result-display-by-requirement");
		}

		mav.addObject(RESULT_LIST, getExpandedTestCases(resultList, criteria));
		mav.addObject(ICON, "ExpandedTestCase");
		mav.addObject(WORKSPACE, "test-case");

		return mav;
	}

	/***
	 * This methods mixes useful informations from test case and requirement saved in an ExpandedTestCase List. The
	 * strongest criticality for a test case is also defined here, based on the list of selected criticalities
	 *
	 * @param testCaseList
	 *            the testCases (which contains requirement collections)
	 * @param criteria
	 *            the criticalities selected for the research
	 * @return an ExpandedTestCase list
	 */
	private List<ExpandedTestCase> getExpandedTestCases(List<TestCase> testCaseList, RequirementSearchCriteria criteria) {
		List<ExpandedTestCase> toReturn = new ArrayList<ExpandedTestCase>();
		for (TestCase testCase : testCaseList) {
			ExpandedTestCase expandedTestCase = new ExpandedTestCase();
			expandedTestCase.setSelectedCriticalities(criteria.getCriticalities());
			expandedTestCase.setTestCase(testCase);
			toReturn.add(expandedTestCase);
		}
		return toReturn;
	}

}
