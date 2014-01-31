/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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

package org.squashtest.tm.web.thymeleaf.dialect;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;

import org.thymeleaf.context.IContext;
import org.thymeleaf.context.IProcessingContext;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.dialect.IExpressionEnhancingDialect;

/**
 * Squash dialect for Thmymeleaf
 *
 * @author Gregory Fouquet
 *
 */
public class SquashTMDialect extends org.squashtest.tm.core.web.thymeleaf.dialect.SquashDialect implements
		IExpressionEnhancingDialect {


	/* partly ripped from SpringSecutiryDialect */
	@Override
	public Map<String, Object> getAdditionalExpressionObjects(IProcessingContext processingContext) {
		final IContext context = processingContext.getContext();
		final IWebContext webContext = (context instanceof IWebContext ? (IWebContext) context : null);

		final Map<String, Object> extensions = new HashMap<String, Object>(1);

		if (webContext != null) {
			final ServletContext servletContext = webContext.getServletContext();
			final WorkspaceHelper wHelper = new WorkspaceHelper(servletContext);

			extensions.put("workspace", wHelper);

		}

		return extensions;

	}



}
