/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.web.internal.oauth;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;

public class AAAUserAuthenticationProvider implements AuthenticationProvider {
 
    @Inject
    AAAProxy aaaProxy;
 
    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
 
        boolean result = aaaProxy.isValidUser(authentication.getPrincipal()
                .toString(), authentication.getCredentials().toString());
 
        if (result) {
            List<GrantedAuthority> grantedAuthorities =
 
new ArrayList<GrantedAuthority>();
            AAAUserAuthenticationToken auth =
 
new AAAUserAuthenticationToken(authentication.getPrincipal(),
            authentication.getCredentials(), grantedAuthorities);
 
            return auth;
        } else {
            throw new BadCredentialsException("Bad User Credentials.");
        }
    }
 
    public boolean supports(Class<?> arg0) {
        return true;
    }
}
