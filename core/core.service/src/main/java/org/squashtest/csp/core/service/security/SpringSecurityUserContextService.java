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

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * A {@link UserContextService} backed by Spring Security.
 *
 * TODO should migrate to core services
 *
 * @author Gregory Fouquet
 */
@Service("squashtest.core.user.UserContextService")
public class SpringSecurityUserContextService implements UserContextService {

	@Override
	public String getUsername() {
		Authentication principal = getPrincipal();
		return principal == null ? "" : principal.getName();
	}

	private SecurityContext getContext() {
		return SecurityContextHolder.getContext();
	}

	@Override
	public boolean hasRole(String role) {
		Collection<GrantedAuthority> grantedAuths = getGrantedAuthorities();

		for (GrantedAuthority grantedAuth : grantedAuths) {
			if (grantedAuth.getAuthority().equals(role)) {
				return true;
			}
		}

		return false;
	}

	private Collection<GrantedAuthority> getGrantedAuthorities() {
		Authentication principal = getPrincipal();

		Collection<GrantedAuthority> grantedAuths;

		if (principal == null) {
			grantedAuths = Collections.emptyList();
		} else {
			grantedAuths = getContext().getAuthentication().getAuthorities();

		}
		return grantedAuths;
	}

	@Override
	public Authentication getPrincipal() {
		SecurityContext context = getContext();
		return context.getAuthentication();
	}
}
