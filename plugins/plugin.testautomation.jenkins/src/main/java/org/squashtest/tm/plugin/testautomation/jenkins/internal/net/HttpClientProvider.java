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

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.CommonsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;

/*
 * TODO : have the client shutdown and disposed of when it is not needed after a certain amount
 * of time passed. See http://hc.apache.org/httpclient-3.x/performance.html#Reuse_of_HttpClient_instance
 * 
 */
@Component
@SuppressWarnings("deprecation") // spring support of httpclient 3.1 is deprecated yet we heavily rely on httpclient 3.1
public class HttpClientProvider {

	private Set<ServerKey> knownServers = new HashSet<ServerKey>();

	private final HttpClient client;

	private final ClientHttpRequestFactory requestFactory;

	public HttpClientProvider() {
		MultiThreadedHttpConnectionManager manager = new MultiThreadedHttpConnectionManager();
		manager.getParams().setMaxTotalConnections(25);

		client = new HttpClient(manager);
		client.getParams().setAuthenticationPreemptive(true);

		requestFactory = new CommonsClientHttpRequestFactory(client);
	}

	/**
	 * Returns the instance of HttpClient, registering the required informations from the TestAutomationServer instance
	 * first if needed
	 * 
	 * @param server
	 * @return
	 */
	public HttpClient getClientFor(TestAutomationServer server) {

		ServerKey key = new ServerKey(server);

		if (!knownServers.contains(key)) {
			registerServer(server);
		}

		return client;

	}

	public ClientHttpRequestFactory getRequestFactoryFor(TestAutomationServer server) {
		getClientFor(server);
		return requestFactory;
	}

	protected void registerServer(TestAutomationServer server) {

		URL baseURL = server.getBaseURL();

		AuthScope authscope = new AuthScope(baseURL.getHost(), baseURL.getPort(), AuthScope.ANY_REALM);
		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(server.getLogin(), server.getPassword());

		client.getState().setCredentials(authscope, creds);

	}

	/* ************************ private stuff *********************** */

	// one rely on that class because I don't want TestAutomationServer to implement equals and hashcode itself
	private static class ServerKey {

		private String serverURL;
		private String serverLogin;
		private String serverPass;
		private String serverKind;

		public ServerKey(TestAutomationServer server) {
			serverURL = server.getBaseURL().toExternalForm();
			serverLogin = server.getLogin();
			serverPass = server.getPassword();
			serverKind = server.getKind();
		}
		// GENERATED:START
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((serverKind == null) ? 0 : serverKind.hashCode());
			result = prime * result + ((serverLogin == null) ? 0 : serverLogin.hashCode());
			result = prime * result + ((serverPass == null) ? 0 : serverPass.hashCode());
			result = prime * result + ((serverURL == null) ? 0 : serverURL.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {



			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			ServerKey other = (ServerKey) obj;
			if (serverKind == null) {
				if (other.serverKind != null) {
					return false;
				}
			} else if (!serverKind.equals(other.serverKind)) {
				return false;
			}
			if (serverLogin == null) {
				if (other.serverLogin != null) {
					return false;
				}
			} else if (!serverLogin.equals(other.serverLogin)) {
				return false;
			}
			if (serverPass == null) {
				if (other.serverPass != null) {
					return false;
				}
			} else if (!serverPass.equals(other.serverPass)) {
				return false;
			}
			if (serverURL == null) {
				if (other.serverURL != null) {
					return false;
				}
			} else if (!serverURL.equals(other.serverURL)) {
				return false;
			}
			return true;


		}
		// GENERATED:END
	}

}
