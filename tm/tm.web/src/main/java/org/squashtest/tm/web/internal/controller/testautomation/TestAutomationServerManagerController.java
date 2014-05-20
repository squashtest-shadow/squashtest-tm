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
package org.squashtest.tm.web.internal.controller.testautomation;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import javax.inject.Inject;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.service.testautomation.TestAutomationServerManagerService;


@Controller
@RequestMapping("/test-automation-servers/{serverId}")
public class TestAutomationServerManagerController {

	@Inject
	private MessageSource messageSource;


	@Inject
	private TestAutomationServerManagerService service;


	@RequestMapping(method=RequestMethod.GET)
	public String showTAServer(@PathVariable("serverId") long serverId, Model model){

		TestAutomationServer server = service.findById(serverId);

		model.addAttribute("server", server);

		return "templates/test-automation/server-manager.html";

	}



	@RequestMapping(value="/name", method=RequestMethod.POST, params="newName")
	@ResponseBody
	public String changeName(@PathVariable("serverId") long serverId, @RequestParam("newName") String newName){
		service.changeName(serverId, newName);
		return newName;
	}


	@RequestMapping(value="/baseURL", method=RequestMethod.POST, params="newURL")
	@ResponseBody
	public String changeURL(@PathVariable("serverId") long serverId, @RequestParam("newURL") String newURL, Locale locale)
			throws BindException{
		try{
			URL url = new URL(newURL);
			service.changeURL(serverId, url);
			return newURL;
		}
		catch(MalformedURLException ex){
			//quick and dirty validation
			BindException be = new BindException(new TestAutomationServer(), "ta-project");
			be.rejectValue("baseURL", null, findMessage(locale, "error.url.malformed"));
			throw be;
		}
	}



	private String findMessage(Locale locale, String key){
		return messageSource.getMessage(key, null, locale);
	}

}
