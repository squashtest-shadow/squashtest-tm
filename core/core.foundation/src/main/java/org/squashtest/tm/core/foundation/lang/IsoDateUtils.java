/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package org.squashtest.tm.core.foundation.lang;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class IsoDateUtils {
	
	private static final DateFormat ISO_DATE;
	private static final DateFormat ISO_DATETIME;
	
	
	static {
		TimeZone tz = TimeZone.getTimeZone("UTC");
		
		ISO_DATE = new SimpleDateFormat("yyyy-MM-dd");
		ISO_DATE.setTimeZone(tz);
		
		ISO_DATETIME = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		ISO_DATETIME.setTimeZone(tz);
	}

	private IsoDateUtils(){}
	
	/**
	 * @param date
	 * @return returns that date formatted according to the ISO 8601 Date (no time info)
	 */
	public static String formatIso8601Date(Date date){
		if (date == null){
			return null;
		} else{
			return ISO_DATE.format(date);
		}
	}
	
	/**
	 * @param date
	 * @return returns that date formatted according to the ISO 8601 DateTime (with time and timezone info)
	 */
	public static String formatIso8601DateTime(Date date){
		if (date == null){
			return null;
		}
		else{
			return ISO_DATETIME.format(date);
		}
	}
	
	/**
	 * @param strDate
	 * @return the Date obtained when parsing the argument against pattern yyyy-MM-dd
	 */
	public static Date parseIso8601Date(String strDate) throws ParseException{
		if (strDate == null){
			return null;
		}
		else{
			return ISO_DATE.parse(strDate);
		}
	}
	
	/**
	 * @param strDate
	 * @return the Date obtained when parsing the argument against pattern yyyy-MM-dd'T'HH:mm:ssZ
	 */
	public static Date parseIso8601DateTime(String strDatetime) throws ParseException{
		if (strDatetime == null){
			return null;
		}
		else{
			return ISO_DATETIME.parse(strDatetime);
		}
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
