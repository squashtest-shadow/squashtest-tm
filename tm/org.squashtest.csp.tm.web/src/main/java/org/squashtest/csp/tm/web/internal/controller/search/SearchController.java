/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.web.internal.controller.search;

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
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.csp.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.csp.tm.domain.requirement.RequirementSearchCriteria;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.csp.tm.service.SearchService;
import org.squashtest.csp.tm.web.internal.search.ExpandedTestCase;

@Controller
@RequestMapping("/search")
public class SearchController {

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchController.class);

	private SearchService searchService;

	@ServiceReference
	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	@RequestMapping(value = "/test-cases", method = RequestMethod.GET, params = { "order", "importance[]" })
	public ModelAndView searchOrderedTestCases(@RequestParam("name") String name, @RequestParam("order") Boolean order, 
			@RequestParam("importance[]") String[] importance) {
		LOGGER.info("SQUASH INFO: TRY TestCase search with name : " + name);

		TestCaseSearchCriteriaAdapter criteria = new TestCaseSearchCriteriaAdapter(name, order, importance);
		
		List<TestCaseLibraryNode> resultList = searchService.findTestCase(criteria);

		LOGGER.info("SQUASH INFO: DONE TestCase search with name : " + name);
		ModelAndView mav;
		if (order == true) {
			mav = new ModelAndView("fragment/generics/search-result-display-ordered");
		} else {
			mav = new ModelAndView("fragment/generics/search-result-display");
		}
		mav.addObject("resultList", resultList);
		mav.addObject("icon", "TestCase");
		mav.addObject("workspace", "test-case");

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
			@RequestParam("criticalities[]") final boolean[] criticalitiesSelection) {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Searching requirements using criteria " + params);
		}

		RequirementSearchCriteria criteria = new RequirementSearchCriteriaAdapter(params, criticalitiesSelection);
		List<RequirementLibraryNode> resultList = searchService.findAllBySearchCriteria(criteria);

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SQUASH INFO: DONE requirement search with name : " + criteria.getName() + " found "
					+ resultList.size() + " items");
		}

		ModelAndView mav = new ModelAndView("fragment/generics/search-result-display");
		mav.addObject("resultList", resultList);
		mav.addObject("icon", "Requirement");
		mav.addObject("workspace", "requirement");

		return mav;
	}

	@RequestMapping(value = "/requirements", method = RequestMethod.GET, params = { "order=true" })
	public ModelAndView searchOrderedRequirements(@ModelAttribute final RequirementSearchParams params,
			@RequestParam("criticalities[]") final boolean[] criticalitiesSelection) {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Searching requirements using criteria " + params);
		}
		RequirementSearchCriteria criteria = new RequirementSearchCriteriaAdapter(params, criticalitiesSelection);
		List<RequirementLibraryNode> resultList = searchService.findAllBySearchCriteriaOrderByProject(criteria);

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SQUASH INFO: DONE requirement search with name : " + criteria.getName() + " found "
					+ resultList.size() + " items");
		}

		ModelAndView mav;
		LOGGER.info("SQUASH INFO: DONE requirement ordered search with name : " + params.getName());
		mav = new ModelAndView("fragment/generics/search-result-display-ordered");

		mav.addObject("resultList", resultList);
		mav.addObject("icon", "Requirement");
		mav.addObject("workspace", "requirement");

		return mav;
	}

	@RequestMapping(value = "/campaigns", method = RequestMethod.GET, params = { "order" })
	public ModelAndView searchOrderedCampaigns(@RequestParam("name") String name, @RequestParam String order) {
		LOGGER.info("SQUASH INFO: TRY Campaign search with name : " + name);

		boolean isOrdered = Boolean.parseBoolean(order);

		List<CampaignLibraryNode> resultList = searchService.findCampaignByName(name, isOrdered);

		LOGGER.info("SQUASH INFO: DONE Campaign search with name : " + name);

		ModelAndView mav;

		if (isOrdered == true) {
			mav = new ModelAndView("fragment/generics/search-result-display-ordered");
		} else {
			mav = new ModelAndView("fragment/generics/search-result-display");
		}

		mav.addObject("resultList", resultList);
		mav.addObject("icon", "Campaign");
		mav.addObject("workspace", "campaign");

		return mav;
	}

	@RequestMapping(value = "/tc-by-requirement", method = RequestMethod.GET, params = { "order" })
	public ModelAndView searchTestCasesByRequirement(@ModelAttribute final RequirementSearchParams params,
			@RequestParam("criticalities[]") final boolean[] criticalitiesSelection, @RequestParam String order) {
		boolean isProjectOrdered = Boolean.parseBoolean(order);

		RequirementSearchCriteria criteria = new RequirementSearchCriteriaAdapter(params, criticalitiesSelection);
		List<TestCase> resultList = searchService.findTestCaseByRequirement(criteria, isProjectOrdered);

		ModelAndView mav;
		if (isProjectOrdered) {
			mav = new ModelAndView("fragment/generics/search-result-display-ordered-by-requirement");
		} else {
			mav = new ModelAndView("fragment/generics/search-result-display-by-requirement");
		}

		mav.addObject("resultList", getExpandedTestCases(resultList, criteria));
		mav.addObject("icon", "ExpandedTestCase");
		mav.addObject("workspace", "test-case");

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
