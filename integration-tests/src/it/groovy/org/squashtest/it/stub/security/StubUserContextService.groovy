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
package org.squashtest.it.stub.security

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.squashtest.tm.service.security.UserContextService

import java.security.Principal

class StubUserContextService implements UserContextService {

    @Override
    public String getUsername() {
        return "StubUserContextService";
    }

    @Override
    public boolean hasRole(String role) {
        return true;
    }

    @Override
    public Authentication getPrincipal() {
        return Principal
    }
}


public class StubAuthentication implements Authentication {

    @Override
    public String getName() {
        return "StubAuthentication";
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return [];
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return this;
    }

    @Override
    public boolean isAuthenticated() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        // TODO Auto-generated method stub

    }
}
