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
package org.squashtest.tm.domain.appcredentials;

import javax.persistence.Entity;


/**
 * <p>
 * That entity represents credentials used at the application-level by Squash to connect to other third party servers,
 * and are persistent in the database.
 *</p>
 *
 * <p>
 * More exactly it is a wrapper around the actual credentials : the proper attributes
 * of a PersistentCredentials are solely metadata, the credentials themselves are stored in the encrypted part.
 *</p>
 *
 * <p>
 *     The actual credentials are one of the several implementations
 * 	of {@link ApplicationCredentials}
 * </p>
 *
 */
//@Entity
public class PersistentCredentials {


}
