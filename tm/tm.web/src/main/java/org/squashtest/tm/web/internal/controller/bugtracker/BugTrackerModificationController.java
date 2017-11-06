/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.bugtracker;

import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.domain.thirdpartyservers.AuthenticationMode;
import org.squashtest.tm.domain.thirdpartyservers.AuthenticationPolicy;
import org.squashtest.tm.domain.thirdpartyservers.Credentials;
import org.squashtest.tm.service.bugtracker.BugTrackerFinderService;
import org.squashtest.tm.service.bugtracker.BugTrackerModificationService;
import org.squashtest.tm.web.internal.helper.JsonHelper;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.jquery.RenameModel;

@Controller
@RequestMapping("/bugtracker/{bugtrackerId}")
public class BugTrackerModificationController {
	private static final Logger LOGGER = LoggerFactory.getLogger(BugTrackerModificationController.class);

	@Inject
	private BugTrackerModificationService bugtrackerModificationService;
	
	@Inject
	private InternationalizationHelper i18nHelper;

	@Inject
	private BugTrackerFinderService bugtrackerFinder;

	@RequestMapping(method = RequestMethod.POST, params = { "newName" })
	@ResponseBody
	public Object changeName(@PathVariable long bugtrackerId, @RequestParam String newName) {
		bugtrackerModificationService.changeName(bugtrackerId, newName);
		LOGGER.debug("BugTracker modification : change bugtracker {} name = {}", bugtrackerId, newName);
		return new RenameModel(newName);
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=bugtracker-url", VALUE })
	@ResponseBody
	public String changeUrl(@PathVariable long bugtrackerId, @RequestParam(VALUE) String newUrl) {
		bugtrackerModificationService.changeUrl(bugtrackerId, newUrl);
		LOGGER.debug("BugTracker modification : change bugtracker {} url = {}", bugtrackerId, newUrl);
		return newUrl;
	}

	@RequestMapping(method = RequestMethod.POST, params = { "isIframeFriendly" })
	@ResponseBody
	public Object changeIframeFriendly(@PathVariable long bugtrackerId,
									   @RequestParam boolean isIframeFriendly) {
		bugtrackerModificationService.changeIframeFriendly(bugtrackerId, isIframeFriendly);
		LOGGER.debug("BugTracker modification : change bugtracker {} is iframe-friendly = {}", bugtrackerId,
				isIframeFriendly);
		return new IframeFriendly(isIframeFriendly);
	}

	private static final class IframeFriendly {
		private Boolean iframeFriendly;

		private IframeFriendly(boolean iframeFriendly) {
			this.iframeFriendly = iframeFriendly;
		}

		@SuppressWarnings("unused")
		public Boolean isIframeFriendly() {
			return iframeFriendly;
		}
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=bugtracker-kind", VALUE })
	@ResponseBody
	public String changeKind(@RequestParam(VALUE) String kind, @PathVariable long bugtrackerId) {
		LOGGER.debug("BugTracker modification : change bugtracker {} kind = {}", bugtrackerId, kind);
		bugtrackerModificationService.changeKind(bugtrackerId, kind);

		return kind;
	}

	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public ModelAndView getProjectInfos(@PathVariable long bugtrackerId) {

		BugTracker bugTracker = bugtrackerFinder.findById(bugtrackerId);
		String jsonBugtrackerKinds = findJsonBugTrackerKinds();
		ModelAndView mav = new ModelAndView("page/bugtrackers/bugtracker-info");
		mav.addObject("bugtracker", bugTracker);
		mav.addObject("bugtrackerKinds", jsonBugtrackerKinds);
		return mav;
	}

	
	// ********************** more private stuffs ******************
	
	
	private String findJsonBugTrackerKinds() {
		Set<String> bugtrackerKinds = bugtrackerFinder.findBugTrackerKinds();
		Map<String, String> mapKinds = new HashMap<>(bugtrackerKinds.size());
		for (String kind : bugtrackerKinds) {
			mapKinds.put(kind, kind);
		}
		return JsonHelper.serialize(mapKinds);
	}
	
	
	private BugtrackerCredentialsManagementBean makeCredentialsBean(BugTracker bugTracker){
		BugtrackerCredentialsManagementBean bean = new BugtrackerCredentialsManagementBean();
		
		try{
			
		}
		catch(Exception ex){
			
		}
		
	}
	
	
	
	public static final class BugtrackerCredentialsManagementBean{
		
		// if this String remains to null it is a good thing
		private String whyServiceUnavailable = null;
		
		// the rest is used if the above is null
		private AuthenticationPolicy authPolicy;
		private AuthenticationMode authMode;
		private Credentials credentials;
		
		
		public String getWhyServiceUnavailable() {
			return whyServiceUnavailable;
		}

		public void setWhyServiceUnavailable(String whyServiceUnavailable) {
			this.whyServiceUnavailable = whyServiceUnavailable;
		}

		public AuthenticationPolicy getAuthPolicy() {
			return authPolicy;
		}
		
		public void setAuthPolicy(AuthenticationPolicy authPolicy) {
			this.authPolicy = authPolicy;
		}
		
		public AuthenticationMode getAuthMode() {
			return authMode;
		}
		
		public void setAuthMode(AuthenticationMode authMode) {
			this.authMode = authMode;
		}
		
		public Credentials getCredentials() {
			return credentials;
		}
		
		public void setCredentials(Credentials credentials) {
			this.credentials = credentials;
		}
	}
	

}
