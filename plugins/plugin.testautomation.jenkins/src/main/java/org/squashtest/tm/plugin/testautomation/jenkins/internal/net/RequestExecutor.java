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
package org.squashtest.tm.plugin.testautomation.jenkins.internal.net;

import static org.apache.commons.httpclient.HttpStatus.SC_FORBIDDEN;
import static org.apache.commons.httpclient.HttpStatus.SC_OK;
import static org.apache.commons.httpclient.HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED;
import static org.apache.commons.httpclient.HttpStatus.SC_UNAUTHORIZED;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.service.testautomation.spi.AccessDenied;
import org.squashtest.tm.service.testautomation.spi.ServerConnectionFailed;

public class RequestExecutor {

	public static final Logger LOGGER = LoggerFactory.getLogger(RequestExecutor.class);

	public static final RequestExecutor INSTANCE = new RequestExecutor();

	private RequestExecutor() {
		super();
	}

	public static RequestExecutor getInstance() {
		return INSTANCE;
	}

	public String execute(HttpClient client, HttpMethod method) {
		try {
			int responseCode = client.executeMethod(method);

			checkResponseCode(responseCode);

			String response = method.getResponseBodyAsString();

			return response;
		} catch (AccessDenied ex) {
			throw new AccessDenied(
					"Test automation - jenkins : operation rejected the operation because of wrong credentials");
		} catch (IOException ex) {
			throw new ServerConnectionFailed(
					"Test automation - jenkins : could not connect to server due to technical error : ", ex);
		} finally {
			method.releaseConnection();
		}
	}

	private void checkResponseCode(int responseCode) {

		if (responseCode == SC_OK) {
			return;
		}

		switch (responseCode) {
		case SC_FORBIDDEN:
		case SC_UNAUTHORIZED:
		case SC_PROXY_AUTHENTICATION_REQUIRED:
			throw new AccessDenied();
		}
	}

}
