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
package org.squashtest.tm.service.internal.batchimport;

final class Messages {

	public static final String ERROR_MALFORMED_PATH = "message.import.log.error.field.malformedPath";
	public static final String ERROR_FIELD_MANDATORY = "message.import.log.error.mandatoryColumn";
	public static final String ERROR_PROJECT_NOT_EXIST = "message.import.log.error.tc.tcPath.projectNotFound";
	public static final String ERROR_MAX_SIZE = "message.import.log.error.field.maxSize";
	
	
	public static final String IMPACT_MAX_SIZE = "message.import.log.impact.truncatedValue";
	
	
	private Messages(){
		super();
	}
}
