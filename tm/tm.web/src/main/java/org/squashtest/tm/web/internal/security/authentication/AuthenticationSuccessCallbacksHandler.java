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
package org.squashtest.tm.web.internal.security.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.squashtest.tm.annotation.SecurityComponent;
import org.squashtest.tm.web.security.authentication.AuthenticationSuccessCallback;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * <p>
 * 	  That bean is plugged as a AuthenticationSuccessHandler to the web authentication filter and performs additional
 *    operations once the user successfully logged in.
 * </p>
 *
 */
@SecurityComponent
public class AuthenticationSuccessCallbacksHandler extends
	SavedRequestAwareAuthenticationSuccessHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationSuccessCallbacksHandler.class);

	private String requestParamsPasswordKey = UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_PASSWORD_KEY;
	private String requestParamsUsernameKey = UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY;

	@Inject
	private List<AuthenticationSuccessCallback> callbacks = new ArrayList<AuthenticationSuccessCallback>();

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
	                                    Authentication authentication) throws ServletException, IOException {


		String user = findLogin(request);
		String password = findPassword(request);
		HttpSession session = request.getSession();

		for (AuthenticationSuccessCallback action : callbacks) {
			try {
				action.onSuccess(user, password, session);
			} catch (Exception ex) {
				LOGGER.info("Authentication success callbacks : callback class '"
						+ action.getClass().getName() + "' raised an exception : ", ex
				);
			}
		}

		super.onAuthenticationSuccess(request, response, authentication);
	}

	@PostConstruct
	protected void onInitialize() {
		LOGGER.debug("AuthenticationSuccessCallbacksHandler was initialized with {} callbacks : {}", callbacks.size(), callbacks);
	}

	public void setRequestParamsPasswordKey(String requestParamsPasswordKey) {
		this.requestParamsPasswordKey = requestParamsPasswordKey;
	}

	public void setRequestParamsUsernameKey(String requestParamsUsernameKey) {
		this.requestParamsUsernameKey = requestParamsUsernameKey;
	}


	private String findLogin(HttpServletRequest request) {
		return request.getParameter(requestParamsUsernameKey);
	}

	private String findPassword(HttpServletRequest request) {
		return request.getParameter(requestParamsPasswordKey);
	}


}
