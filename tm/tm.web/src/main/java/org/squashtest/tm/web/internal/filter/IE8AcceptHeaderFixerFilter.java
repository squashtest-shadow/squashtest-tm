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

package org.squashtest.tm.web.internal.filter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * When sending a request, IE8 sends a shitload of accept headers but no "text/html". This filter adds an
 * "Accept=text/html" on non-json, IE8 looking requests.
 * 
 * Typical IE8 accept header : <code>Accept: 
 * 		image/jpeg, application/x-ms-application, 
 * 		image/gif, application/xaml+xml, 
 * 		image/pjpeg, application/x-ms-xbap, 
 * 		application/x-shockwave-flash, application/msword, * / *
 * </code>
 * 
 * @author Gregory Fouquet
 * 
 */
public class IE8AcceptHeaderFixerFilter implements Filter {
	private static final String ACCEPT = "Accept";
	private static final String TEXT_HTML = "text/html";
	
	private static class FixedIE8AcceptWrapper extends HttpServletRequestWrapper {
		/**
		 * 
		 */
		@SuppressWarnings("rawtypes")
		private Vector acceptHeaders;
		private String acceptHeader;

		/**
		 * @param request
		 */
		public FixedIE8AcceptWrapper(HttpServletRequest request) {
			super(request);
		}

		@Override
		public String getHeader(String name) {
			if (ACCEPT.equals(name)) {
				return readAcceptHeaderFromCache();
			}

			return super.getHeader(name);
		}

		/**
		 * @return
		 */
		private String readAcceptHeaderFromCache() {
			if (acceptHeader == null) {
				acceptHeader = super.getHeader(ACCEPT) + ", " + TEXT_HTML; 
			}
			
			return acceptHeader;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public Enumeration getHeaders(String name) {
			if (ACCEPT.equals(name)) {
				return readAcceptHeadersFromCache().elements();
			}

			return super.getHeaders(name);
		}

		/**
		 * @return
		 */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		private Vector readAcceptHeadersFromCache() {
			if (acceptHeaders == null) {
				Enumeration source = super.getHeaders(ACCEPT);
				acceptHeaders = new Vector();

				while (source.hasMoreElements()) {
					acceptHeaders.add(source.nextElement());
				}

				acceptHeaders.add(TEXT_HTML);
			}

			return acceptHeaders;
		}

	}

	/**
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// NOOP

	}

	/**
	 * If the request comes from IE8
	 * 
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
	 *      javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletRequest chainedRequest;
		
		String accept = httpRequest.getHeader(ACCEPT);
		
		if (accept.contains(TEXT_HTML) || acceptsJson(accept) || notFromIE8(accept)) {
			chainedRequest = (HttpServletRequest) request;
		} else {
			chainedRequest = new FixedIE8AcceptWrapper(httpRequest);
		}
		
		chain.doFilter(chainedRequest, response);
	}

	/**
	 * @param accept
	 * @return
	 */
	private boolean notFromIE8(String accept) {
		return !accept.contains("application/x-ms-application");
	}

	private boolean acceptsJson(String accept) {
		return accept.contains("application/json") || accept.contains("text/javascript");
	}

	/**
	 * @see javax.servlet.Filter#destroy()
	 */
	@Override
	public void destroy() {
		// NOOP

	}

}
