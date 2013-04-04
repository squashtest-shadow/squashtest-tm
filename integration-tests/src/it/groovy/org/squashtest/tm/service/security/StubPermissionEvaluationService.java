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
package org.squashtest.tm.service.security;

import org.squashtest.it.infrastructure.Stub;

@Stub
public class StubPermissionEvaluationService implements PermissionEvaluationService {

	public StubPermissionEvaluationService() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean hasRoleOrPermissionOnObject(String role, String permission, Object object) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean canRead(Object object) {
		return true;

	}

	@Override
	public boolean hasMoreThanRead(Object object) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean hasRoleOrPermissionOnObject(String role, String permission, Long entityId, String entityClassName) {
		return true;
	}

	@Override
	public boolean hasRole(String role) {
		return true;
	}

}
