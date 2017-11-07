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
package org.squashtest.tm.domain.thirdpartyservers;

/**
 * This enum says whether Squash should let the users authenticate themselves, or use one of its stored credentials, when time is come to authenticate.
 * In that it is different from {@link AuthenticationProtocol}, that says which protocol should be used
 * for the said authentication.
 *
 *
 */
public enum AuthenticationPolicy {
	/**
	 * Indicates that the users need to authenticate themselves.
	 *
	 */
	USER,
	/**
	 * Indicates that the application will use one of its stored credentials.
	 *
	 */
	APPL_LEVEL
}
