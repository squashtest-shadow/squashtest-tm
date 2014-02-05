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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public final class IsoDateUtils {
	
	private static final String ISO_DATE = "yyyy-MM-dd";
	private static final String ISO_DATETIME = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	private static final TimeZone TZ = TimeZone.getTimeZone("UTC");
	
	
	
	private IsoDateUtils(){
		
	}
	
	/**
	 * @param date
	 * @return returns that date formatted according to the ISO 8601 Date (no time info)
	 */
	public static String formatIso8601Date(Date date){
		if (date == null){
			return null;
		} else{
			return formatDate(date, ISO_DATE);
		}
	}
	
	private static String formatDate(Date date, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		sdf.setTimeZone(TZ);
		return sdf.format(date);
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
			return formatDate(date, ISO_DATETIME);
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
			return parseDate(strDate, ISO_DATE);
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
			return parseDate(strDatetime, ISO_DATETIME);
		}
	}
	

	private static Date parseDate(String strDatetime, String format) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.parse(strDatetime);
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
