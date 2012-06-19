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
package org.squashtest.csp.core.service.security;

import org.squashtest.csp.core.domain.Identified;

/**
 * This service evaluates permissions of the current user.
 *
 * @author Gregory Fouquet
 *
 */
public interface PermissionEvaluationService {
	/**
	 * @param role
	 * @param permission
	 *            String representation of the permission.
	 * @param object
	 * @returntrue if the current user either has the given role or has the required permission on the given object.
	 */
	boolean hasRoleOrPermissionOnObject(String role, String permission, Object object);
	
	/**
	 * short hand for hasRoleOrPermissionOnObject('ROLE_ADMIN', 'READ', object);
	 * 
	 * @param object
	 * @return
	 */
	boolean canRead(Object object);
	
	/**
	 * return true if the user has more than readonly on the object
	 * @param object
	 * @return
	 */
	boolean hasMoreThanRead(Object object);
	
}
