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
package org.squashtest.tm.domain.planning;

import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.LocalDate;

public class StandardWorkloadCalendar implements WorkloadCalendar {

	private static final float BUSINESS_DAY_WORKLOAD = 1.0f;
	private static final float WEEKEND_DAY_WORKLOAD = 0.0f;
	
	@Override
	public float getWorkload(Date date) {
		Calendar c = Calendar.getInstance();	// XXX thread safety ?
		c.setTime(date);
		int day = c.get(Calendar.DAY_OF_WEEK) ;
		return (day == Calendar.SATURDAY || day == Calendar.SUNDAY) ? WEEKEND_DAY_WORKLOAD : BUSINESS_DAY_WORKLOAD;
	}

	
	/*
	 * 
	 * This works by "normalizing" the scheduled period, computing the number of days in this period, then and substracting 
	 * 2 days per slices of 7 days. 
	 * 
	 * Normalizing means :
	 * offsetting the start date to next monday,
	 * offsetting the end date by the same number of days and set it back to friday if it corresponds to a weekend day
	 * 
	 */
	@Override	
	public float getWorkload(Date start, Date end) {
		
		LocalDate lstart = new LocalDate(start);
		LocalDate lend = new LocalDate(end);

		// normalization
		LocalDate normalizedStart = toNextMonday(lstart);
		int offsetDays = Days.daysBetween(lstart, normalizedStart).getDays();
		LocalDate normalizedEnd = shaveDown(lend.plusDays(offsetDays));
		
		// actual computation
		int normalizedDays = Days.daysBetween(normalizedStart, normalizedEnd).getDays();
		int numberWeeks = normalizedDays / 7;
		int remainingDays = normalizedDays % 7;
		
		return numberWeeks * ( 5*BUSINESS_DAY_WORKLOAD /*+ 2*WEEKEND_DAY_WORKLOAD = 0*/ ) + remainingDays*BUSINESS_DAY_WORKLOAD;
		
	}
	
	private boolean isWeekend(LocalDate date){
		return (date.getDayOfWeek() == DateTimeConstants.SATURDAY || date.getDayOfWeek() == DateTimeConstants.SUNDAY);
	}
	
	// push date to next monday, if not already monday
	private LocalDate toNextMonday(LocalDate date){
		if (date.getDayOfWeek() == DateTimeConstants.MONDAY){
			return date;
		}
		else{
			return date.plusWeeks(1).withDayOfWeek(DateTimeConstants.MONDAY);
		}
	}

	
	// if the date is saturday or sunday, will set the date back to the friday of the same week
	private LocalDate shaveDown(LocalDate date){
		if (isWeekend(date)){
			return date.withDayOfWeek(DateTimeConstants.FRIDAY);
		}
		else{
			return date;
		}
	}

}
