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
package org.squashtest.tm.service.internal.security;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.PermissionFactory;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.security.acls.CustomPermission;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.UserContextService;

@Service("squashtest.core.security.PermissionEvaluationService")
@Transactional(readOnly = true)
public class AclPermissionEvaluationService implements PermissionEvaluationService {
	@Inject
	private UserContextService userContextService;

	@Inject
	private AffirmativeBasedCompositePermissionEvaluator permissionEvaluator;

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

		return hasPermissionOnObject(permissionName, entityId, entityClassName);
	}



	@Override
	public boolean canRead(Object object) {
		return hasRoleOrPermissionOnObject("ROLE_ADMIN", "READ", object);
	}

	@Override
	public boolean hasRole(String role) {
		return userContextService.hasRole(role);
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


	@Override
	public boolean hasPermissionOnObject(String permissionName, Long entityId, String entityClassName) {
		Authentication authentication = userContextService.getPrincipal();
		Permission permission = permissionFactory.buildFromName(permissionName);

		return permissionEvaluator.hasPermission(authentication,entityId, entityClassName, permission);
	}


	@Override
	public Map<Permission, Boolean> listPermissionsOnObject(Object object) {
		String admin = "ROLE_ADMIN";

		Map<Permission, Boolean> permissionMap = new HashMap<Permission, Boolean>(13);

		permissionMap.put(BasePermission.READ, hasRoleOrPermissionOnObject(admin, BasePermission.READ.toString(), object));
		permissionMap.put(BasePermission.WRITE, hasRoleOrPermissionOnObject(admin, BasePermission.WRITE.toString(), object));
		permissionMap.put(BasePermission.CREATE, hasRoleOrPermissionOnObject(admin, BasePermission.CREATE.toString(), object));
		permissionMap.put(BasePermission.DELETE, hasRoleOrPermissionOnObject(admin, BasePermission.DELETE.toString(), object));
		permissionMap.put(BasePermission.ADMINISTRATION, hasRoleOrPermissionOnObject(admin, BasePermission.ADMINISTRATION.toString(), object));
		permissionMap.put(CustomPermission.MANAGEMENT, hasRoleOrPermissionOnObject(admin, CustomPermission.MANAGEMENT.toString(), object));
		permissionMap.put(CustomPermission.EXPORT, hasRoleOrPermissionOnObject(admin, CustomPermission.EXPORT.toString(), object));
		permissionMap.put(CustomPermission.EXECUTE, hasRoleOrPermissionOnObject(admin, CustomPermission.EXECUTE.toString(), object));
		permissionMap.put(CustomPermission.LINK, hasRoleOrPermissionOnObject(admin, CustomPermission.LINK.toString(), object));
		permissionMap.put(CustomPermission.IMPORT, hasRoleOrPermissionOnObject(admin, CustomPermission.IMPORT.toString(), object));
		permissionMap.put(CustomPermission.ATTACH, hasRoleOrPermissionOnObject(admin, CustomPermission.ATTACH.toString(), object));
		permissionMap.put(CustomPermission.EXTENDED_DELETE, hasRoleOrPermissionOnObject(admin, CustomPermission.EXTENDED_DELETE.toString(), object));
		permissionMap.put(CustomPermission.READ_UNASSIGNED, hasRoleOrPermissionOnObject(admin, CustomPermission.READ_UNASSIGNED.toString(), object));

		return permissionMap;
	}

}
