/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.csp.core.bugtracker.core;

import org.squashtest.csp.core.bugtracker.net.AuthenticationCredentials;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerConnector;
import org.squashtest.tm.domain.thirdpartyservers.AuthenticationMode;
import org.squashtest.tm.domain.thirdpartyservers.Credentials;
import org.squashtest.tm.domain.thirdpartyservers.UsernamePasswordCredentials;

public final class ConnectorUtils {
	private ConnectorUtils(){

	}

	public static boolean supports(AuthenticationMode[] supported, AuthenticationMode mode){
		for (AuthenticationMode mm : supported){
			if (mode == mm){
				return true;
			}
		}
		return false;
	}

	/**
	 * Convert a {@link UsernamePasswordCredentials} to a {@link AuthenticationCredentials} for
	 * retrocompatibility purposes
	 */
	public static AuthenticationCredentials backportCredentials(Credentials credentials, AuthenticationMode[] supported){
		if (!supports(supported, AuthenticationMode.USERNAME_PASSWORD)){
			throw new UnsupportedAuthenticationModeException(AuthenticationMode.USERNAME_PASSWORD.toString());
		}

		if (!UsernamePasswordCredentials.class.isAssignableFrom(credentials.getClass())){
			throw new UnsupportedAuthenticationModeException(credentials.getClass().getSimpleName());
		}

		UsernamePasswordCredentials creds = (UsernamePasswordCredentials) credentials;
		return new AuthenticationCredentials(creds.getUsername(), new String(creds.getPassword()));
	}

}
