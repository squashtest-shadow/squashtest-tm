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
package org.squashtest.csp.tm.web.internal.utils;

import java.util.Date;

public final class DateUtils {
	private DateUtils() {
		super();
	}

	/**
	 * 
	 * @param milliseconds
	 * @return <code>null</code> if the string is empty, or a date otherwise. No check regarding the actual content of strDate.
	 */
	public static Date millisecondsToDate(String milliseconds) {
		Date newDate = null;

		if (milliseconds.length() > 0) {
			Long millisecs = Long.valueOf(milliseconds);
			newDate = new Date(millisecs);
		}

		return newDate;
	}
	
	public static String dateToMillisecondsAsString(Date date) {
		if (date != null) {
			return Long.valueOf(date.getTime()).toString();
		} else {
			return "";
		}
	}
}
