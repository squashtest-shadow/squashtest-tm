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
 package org.squashtest.tm.web.internal.controller.welcome;
 
 import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

import java.util.List;

import javax.inject.Inject;

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.domain.IdentifiedUtil;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.service.bugtracker.BugTrackerFinderService;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.service.user.AdministrationService;

@Controller
public class HomeController {
	
	private AdministrationService administrationService;
	
	@Inject
	protected ProjectFinder projectFinder;
	
	@Inject
	protected BugTrackerFinderService  bugtrackerService;

	

	@ServiceReference
	public void setAdministrationService(AdministrationService administrationService) {
		this.administrationService = administrationService;
	}

	@RequestMapping("/home-workspace")
	public ModelAndView home() {
		String welcomeMessage = administrationService.findWelcomeMessage();
		
		ModelAndView mav = new ModelAndView("page/home-workspace");

		mav.addObject("welcomeMessage", welcomeMessage);
		
		// put the available bugtrackers too
		List<Project> projects = projectFinder.findAllReadable();
		List<Long> projectsIds = IdentifiedUtil.extractIds(projects);
		List<BugTracker> visibleBugtrackers = bugtrackerService.findDistinctBugTrackersForProjects(projectsIds);

		mav.addObject("visibleBugtrackers", visibleBugtrackers);
		
		
		return mav;
	}
	
	@RequestMapping(value = "/configuration/modify-welcome-message", method=RequestMethod.POST)
	public @ResponseBody
	String modifyWelcomeMessage(@RequestParam(VALUE) String welcomeMessage){
		administrationService.modifyWelcomeMessage(welcomeMessage);
		return welcomeMessage;
	}
	@RequestMapping(value = "/configuration/modify-login-message", method=RequestMethod.POST)
	public @ResponseBody
	String modifyLoginMessage(@RequestParam(VALUE) String loginMessage){
		administrationService.modifyLoginMessage(loginMessage);
		return loginMessage;
	}
	
	@RequestMapping("/configuration/welcome-message")
	public ModelAndView welcomeMessagePage(){
		String welcomeMessage = administrationService.findWelcomeMessage();
		ModelAndView mav = new ModelAndView("page/configurations/welcome-message-workspace");
		mav.addObject("welcomeMessage", welcomeMessage);
		return mav;
	}
	
	@RequestMapping("/configuration/login-message")
	public ModelAndView loginMessagePage(){
		String loginMessage = administrationService.findLoginMessage();
		ModelAndView mav = new ModelAndView("page/configurations/login-message-workspace");
		mav.addObject("loginMessage", loginMessage);
		return mav;
	}
	
}
