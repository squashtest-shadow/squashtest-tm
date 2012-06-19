/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.csp.tm.web.internal.controller.users;

import javax.validation.Valid;

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;
import org.squashtest.csp.tm.domain.users.User;
import org.squashtest.csp.tm.service.UserAccountService;


@Controller
@RequestMapping("/user-account")
public class UserAccountController {

	private UserAccountService userService;
	
	@ServiceReference
	public void setUserAccountService(UserAccountService service){
		this.userService=service;
	}
	
	@RequestMapping(method=RequestMethod.GET)
	public ModelAndView getUserAccountDetails(){
		User user = userService.findCurrentUser();
		
		ModelAndView mav = new ModelAndView("page/users/user-account");
		mav.addObject("user", user);
		return mav;

	}

	@RequestMapping(value="/update", method=RequestMethod.POST, params={"oldPassword", "newPassword"})
	@ResponseBody
	public void changePassword(@ModelAttribute @Valid PasswordChangeForm form){
			userService.setCurrentUserPassword(form.getOldPassword(), form.getNewPassword());
	}
	
	@RequestMapping(value="/update", method=RequestMethod.POST, params={"id=user-account-email", "value"})
	@ResponseBody
	public String updateUserEmail(@RequestParam("value") String email){
		userService.setCurrentUserEmail(email);
		return HtmlUtils.htmlEscape(email);
	}
	
}
