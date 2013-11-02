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
		return (isWeekend(date)) ? WEEKEND_DAY_WORKLOAD : BUSINESS_DAY_WORKLOAD;
	}


	@Override	
	public float getWorkload(Date start, Date end) {
		
		if (end.before(start)){
			throw new IllegalArgumentException("the end date should not predate the start date");
		}
		
		Date restart = (isWeekend(start)) ? toNextMonday(start) : start;
		
		LocalDate lstart = new LocalDate(restart);
		LocalDate lend = new LocalDate(end).plusDays(1);	//we add 1 day because end dates are inclusive

		int daysbetween = Days.daysBetween(lstart, lend).getDays();
		
		//because of the next monday trick, the start could now happen after the end
		//in that case, we return workload of WEEKEND_DAY_WORKLOAD (because this happens
		//only when both dates are weekend days)
		if (daysbetween <0){
			return WEEKEND_DAY_WORKLOAD;
		}else{
		
			int nbweeks = daysbetween / 7;
			int remainder = daysbetween % 7;
			
			return nbweeks * (5* BUSINESS_DAY_WORKLOAD /*+ 2 * WEEKEND_DAY_WORKLOAD*/) + Math.min(remainder, 5) * BUSINESS_DAY_WORKLOAD;
		}
	}
	
	private boolean isWeekend(Date date){
		Calendar c = Calendar.getInstance();	// XXX thread safety ?
		c.setTime(date);
		int day = c.get(Calendar.DAY_OF_WEEK) ;
		return (day == Calendar.SATURDAY || day == Calendar.SUNDAY);
	}	
	
	private Date toNextMonday(Date date){
		Calendar c = Calendar.getInstance(); //XXX thread safety ?
		c.setTime(date);
		c.add(Calendar.WEEK_OF_YEAR, 1);
		c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		return c.getTime();
	}

	/*
	private boolean isWeekend(LocalDate date){
		return (date.getDayOfWeek() == DateTimeConstants.SATURDAY || date.getDayOfWeek() == DateTimeConstants.SUNDAY);
	}
	
	private LocalDate toNextMonday(LocalDate date){
		return date.plusWeeks(1).withDayOfWeek(DateTimeConstants.MONDAY);
	}*/


	
}
