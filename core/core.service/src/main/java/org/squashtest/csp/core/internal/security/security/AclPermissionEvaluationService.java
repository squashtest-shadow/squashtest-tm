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
package org.squashtest.csp.core.internal.security.security;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.domain.PermissionFactory;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.security.acls.CustomPermission;
import org.squashtest.csp.core.service.security.PermissionEvaluationService;
import org.squashtest.csp.core.service.security.UserContextService;

@Service("squashtest.core.security.PermissionEvaluationService")
@Transactional(readOnly = true)
public class AclPermissionEvaluationService implements PermissionEvaluationService {
	@Inject
	private UserContextService userContextService;

	@Inject
	private PermissionEvaluator permissionEvaluator;

	@Inject
	private PermissionFactory permissionFactory;

	/*
	 * Not exposed so that interface remains spring-sec agnostic.
	 */
	private boolean hasRoleOrPermissionOnObject(String role, Permission permission, Object object) {
		if (userContextService.hasRole(role)) {
			return true;
		}

		return permissionEvaluator.hasPermission(userContextService.getPrincipal(), object, permission);
	}


	@Override
	public boolean hasRoleOrPermissionOnObject(String role, String permissionName, Object object) {
		return hasRoleOrPermissionOnObject(role, permissionFactory.buildFromName(permissionName), object);
	}
	
	@Override
	public boolean hasRoleOrPermissionOnObject(String role, String permissionName,
			Long entityId, String entityClassName) {
		
		if (userContextService.hasRole(role)) {
			return true;
		}
		
		Authentication authentication = userContextService.getPrincipal();
		Permission permission = permissionFactory.buildFromName(permissionName);

		return permissionEvaluator.hasPermission(authentication,entityId, entityClassName, permission);
	}

	@Override
	public boolean canRead(Object object) {
		return hasRoleOrPermissionOnObject("ROLE_ADMIN", "READ", object);
	}

	@Override
	public boolean hasMoreThanRead(Object object) {
		boolean hasMore = false;
		if (userContextService.hasRole("ROLE_ADMIN")) {
			hasMore = true;
		} else {
			Authentication authentication = userContextService.getPrincipal();
			Field[] fields = CustomPermission.class.getFields();
			for (int i = 0; i < fields.length; i++) {
				try {
					if ((!fields[i].getName().equals("READ"))
							&& permissionEvaluator.hasPermission(authentication, object, fields[i].getName())) {
						return true;
					}
				} catch (IllegalArgumentException iaexecption) {
					List<String> knownMessages = Arrays.asList("Unknown permission 'RESERVED_ON'",
							"Unknown permission 'RESERVED_OFF'", "Unknown permission 'THIRTY_TWO_RESERVED_OFF'");
					if (!(knownMessages.contains(iaexecption.getMessage()))) {
						throw iaexecption;
					}
				}
			}

		}
		return hasMore;
	}

}
