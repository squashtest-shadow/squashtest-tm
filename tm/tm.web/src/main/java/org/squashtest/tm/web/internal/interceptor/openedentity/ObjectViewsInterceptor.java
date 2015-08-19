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
package org.squashtest.tm.web.internal.interceptor.openedentity;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.service.security.PermissionEvaluationService;
/**
 * Groups mutual code to store the information of an access to the view of an entity in the OpenedEntities stored in the ServeletContext. 
 * see {@linkplain OpenedEntities}
 * @author mpagnon
 *
 */
public abstract class ObjectViewsInterceptor implements WebRequestInterceptor {
	private static final Logger LOGGER = LoggerFactory.getLogger(ObjectViewsInterceptor.class);

	@Autowired
	protected ServletContext context;

	@ServiceReference
	public void setPermissionService(PermissionEvaluationService permissionService) {
		this.permissionService = permissionService;
	}

	private PermissionEvaluationService permissionService;

	public boolean addViewerToEntity(String contextAttributeName, Identified object, String userLogin) {
		LOGGER.debug("New view added for {} = {}  Viewer = {}", new Object[] {contextAttributeName, object.getId(), userLogin});
		boolean otherViewers = false;
		if (permissionService.hasMoreThanRead(object)) {
			LOGGER.debug("User has more than readonly in object = true");

			OpenedEntities openedEntities = (OpenedEntities) context.getAttribute(contextAttributeName);
			if (openedEntities != null) {
				otherViewers = openedEntities.addViewerToEntity(object, userLogin);

			}
		} else {
			LOGGER.debug("User has more than readonly in object = false");
		}

		return otherViewers;
	}

	

}
