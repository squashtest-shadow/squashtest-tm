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
package org.squashtest.tm.web.internal.controller.authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

public class LoginFormDiscardingHttpSessionRequestCache extends
		HttpSessionRequestCache {
	
	private String loginFormUrl = "/login";
	
	public void setLoginFormUrl(String path){
		this.loginFormUrl=path;
	}
	
	public String getLoginFormUrl(){
		return loginFormUrl;
	}
	
	@Override
	public void saveRequest(HttpServletRequest request,
			HttpServletResponse response) {
		
		String path = request.getServletPath();
		if (path.equals(loginFormUrl)) {return;}
		
		super.saveRequest(request, response);
	}
}
