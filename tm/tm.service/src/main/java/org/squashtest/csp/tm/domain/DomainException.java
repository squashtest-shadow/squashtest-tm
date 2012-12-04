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
package org.squashtest.csp.tm.domain;

import org.squashtest.tm.core.foundation.i18n.Internationalizable;

public abstract class DomainException extends RuntimeException implements Internationalizable {

	private static final long serialVersionUID = 5203532234097674488L;

	private static final Object[] NO_PARAMS = {};
	
	private String objectName;
	private final String field;

	protected DomainException(String message, String field) {
		super(message);
		this.field = field;
	}

	protected DomainException(String field) {
		super();
		this.field = field;
	}

	public String getObjectName() {
		return objectName;
	}

	public String getField() {
		return field;
	}

	public String getDefaultMessage() {
		return getMessage();
	}

	/**
	 * Can be overridden by subclasses to return the params to use when fetching the i18n'd message. 
	 * @return should never return <code>null</code>
	 */
	public Object[] getI18nParams() {
		return NO_PARAMS;
	}
}
