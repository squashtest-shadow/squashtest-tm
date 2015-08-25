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
package org.squashtest.tm.web.internal.filter;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * This patches empty responses by setting them an "app/json" type + "null" content, otherwise js clients interpreting
 * the response as JSON won't be able to parse it (null is valid JSON while nothing is not).
 * 
 * Yet there are bugs because this filter accesses the response's Writer. If response.getOutpouStream() alreadyt has
 * been called somewhere in the responce processing stack, this will throw exception.
 * 
 * To work around this, one can set a "notJson" request attribute which will override the filter's behaviour.
 * 
 */
public class AjaxEmptyResponseFilter implements Filter {

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
	ServletException {
		chain.doFilter(request, response);

		if (response.getContentType() == null) {
			response.setCharacterEncoding("UTF-8");
			response.setContentType("application/json");
			Writer writer = response.getWriter();
			writer.write("null");
			writer.close();
			response.flushBuffer();
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}