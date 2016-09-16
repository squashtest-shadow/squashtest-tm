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
package org.squashtest.tm.web.internal.controller.testautomation;


import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.testautomation.TestAutomationServerManagerService;

import java.util.ArrayList;
import java.util.List;


@Controller
@RequestMapping("/administration/test-automation-servers/{serverId}")
public class TestAutomationServerModificationAdminController {


	@Inject
	private TestAutomationServerManagerService service;


	@RequestMapping(method=RequestMethod.GET)
	public String showTAServer(@PathVariable("serverId") long serverId, Model model){

		TestAutomationServer server = service.findById(serverId);

		List<User> usersList = new ArrayList<User>();
		User user = new User();
		user.setFirstName("toto");
		user.setLastName("titi");
		user.setLogin("tototiti");

		usersList.add(user);

		model.addAttribute("server", server);
		model.addAttribute("users", usersList);


		return "test-automation/server-modification.html";

	}


}
