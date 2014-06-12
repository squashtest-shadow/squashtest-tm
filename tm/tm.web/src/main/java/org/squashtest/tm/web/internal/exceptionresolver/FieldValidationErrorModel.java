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
package org.squashtest.tm.web.internal.exceptionresolver;

import java.io.Serializable;

public final class FieldValidationErrorModel implements Serializable {
	private static final long serialVersionUID = -2424352395793715437L;

	public final String objectName; // NOSONAR Field is immutable
	public final String fieldName; // NOSONAR Field is immutable
	public final String fieldValue; //NOSONAR Field is immutable
	public final String errorMessage; // NOSONAR Field is immutable

	public FieldValidationErrorModel(String objectName, String fieldName, String errorMessage) {
		this(objectName, fieldName, errorMessage, null);
	}

	public FieldValidationErrorModel(String objectName, String fieldName, String errorMessage, String fieldValue) {
		super();
		this.objectName = objectName;
		this.fieldName = fieldName;
		this.errorMessage = errorMessage;
		this.fieldValue = fieldValue;
	}
}
