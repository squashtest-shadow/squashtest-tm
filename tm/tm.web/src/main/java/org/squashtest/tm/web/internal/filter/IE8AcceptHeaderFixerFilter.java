/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.web.internal.filter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang.StringUtils;
import org.squashtest.tm.web.internal.http.ContentTypes;
import org.squashtest.tm.web.internal.http.RequestHeaders;

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
 * IE8 accept header may look different when some things are installed on the computer or not (e.g. MS Office) so we
 * also have a look at the user agent.
 * 
 * @author Gregory Fouquet
 * 
 */
public class IE8AcceptHeaderFixerFilter implements Filter {
	private static final String ACCEPT = RequestHeaders.ACCEPT;
	private static final String TEXT_HTML = ContentTypes.TEXT_HTML;
	private static final Pattern IE8_AGENT_PATTERN = Pattern.compile("\\; ?MSIE ?8\\.0 ?\\;");

	private static class FixedIE8AcceptWrapper extends HttpServletRequestWrapper {
		/**
		 * 
		 */
		@SuppressWarnings("rawtypes")
		private Vector acceptHeaders; // NOSONAR We *do* need a vector, which is Enumerable
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

		@SuppressWarnings({ "rawtypes" })
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
		private Vector readAcceptHeadersFromCache() { // NOSONAR We *do* need a vector, which is Enumerable
			if (acceptHeaders == null) {
				Enumeration source = super.getHeaders(ACCEPT);
				acceptHeaders = new Vector(); // NOSONAR We *do* need a vector, which is Enumerable

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

		String accept = acceptHeader(httpRequest);

		if (accept.contains(TEXT_HTML) || acceptsJson(accept) || notFromIE8(httpRequest)) {
			chainedRequest = (HttpServletRequest) request;
		} else {
			chainedRequest = new FixedIE8AcceptWrapper(httpRequest);
		}

		chain.doFilter(chainedRequest, response);
	}

	/**
	 * Nullsafe reads the accept header of http request.
	 * 
	 * @param httpRequest
	 * @return the accept header from the request or an empty string when no header.
	 */
	private String acceptHeader(HttpServletRequest httpRequest) {
		return StringUtils.defaultString(httpRequest.getHeader(ACCEPT));
	}

	private boolean notFromIE8(HttpServletRequest request) {
		// notIE8Accept is not 100% reliable, yet we try not to use the regexp-based notIE8UserAgent
		return notIE8Accept(acceptHeader(request)) && notIE8UserAgent(request.getHeader(RequestHeaders.USER_AGENT));
	}

	/**
	 * @param userAgent
	 * @return <code>true</code> when there are reasonable clues that the user agent string was issued by IE8.
	 */
	private boolean notIE8UserAgent(String userAgent) {
		return !IE8_AGENT_PATTERN.matcher(userAgent).find();
	}

	/**
	 * @param accept
	 * @return <code>true</code> when there are reasonable clues that the accept string was issued by IE8.
	 */
	private boolean notIE8Accept(String accept) {
		return !accept.contains("application/x-ms-application");
	}

	private boolean acceptsJson(String accept) {
		return accept.contains(ContentTypes.APPLICATION_JSON) || accept.contains(ContentTypes.TEXT_JAVASCRIPT);
	}

	/**
	 * @see javax.servlet.Filter#destroy()
	 */
	@Override
	public void destroy() {
		// NOOP

	}

}
