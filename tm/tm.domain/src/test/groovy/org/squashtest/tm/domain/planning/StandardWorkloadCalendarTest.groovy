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
package org.squashtest.tm.domain.planning

import java.text.SimpleDateFormat;

import org.joda.time.LocalDate;

import spock.lang.Shared;
import spock.lang.Specification
import spock.lang.Unroll;

class StandardWorkloadCalendarTest extends Specification {

	@Shared
	SimpleDateFormat dateformatter = new SimpleDateFormat("dd/MM/yyyy");
	
	@Unroll("should say that workload for #strday is #res")
	def "should say that workload is 1 or 0"(){
		
		expect : 
			res == new StandardWorkloadCalendar().getWorkload(date)
			
		where :
		strday		|	res		|	date
		"monday"	|	1.0f	|	dateformatter.parse("28/10/2013")
		"tuesday"	|	1.0f	|	dateformatter.parse("29/10/2013")
		"wednesday"	|	1.0f	|	dateformatter.parse("30/10/2013")
		"thursday"	|	1.0f	|	dateformatter.parse("31/10/2013")
		"friday"	|	1.0f	|	dateformatter.parse("01/11/2013")
		"saturday"	|	0.0f	|	dateformatter.parse("02/11/2013")
		"sunday"	|	0.0f	|	dateformatter.parse("03/11/2013")
	}
	

	
	def "should return a workload of 10.f, because of approximately two weeks"(){
		
		given :
			Date start = dateformatter.parse("28/10/2013");	//wednesday
			Date end = dateformatter.parse("09/11/2013");	//saturday two weeks later
			
		when :
			def res = new StandardWorkloadCalendar().getWorkload(start, end)
			
		then :
			res == 10.0f;
	}
	
	def "should return a workload of 0 because nobody (should) work the weekend"(){
		
		given :
			Date start = dateformatter.parse("02/11/2013");	//saturday
			Date end = start.plus(1);	//sunday
			
		when :
			def res = new StandardWorkloadCalendar().getWorkload(start, end)
			
		then :
			res == 0.0f;
		
	}
	
	def "should return a workload of 1 because we're working only the monday"(){
		given :
			Date start = dateformatter.parse("02/11/2013");	//saturday
			Date end = start.plus(2);	//monday
			
		when :
			def res = new StandardWorkloadCalendar().getWorkload(start, end)
			
		then :
			res == 1.0f;
	}
	
	def "should return a workload of 1 because the period lasts for 1 day only"(){
		given :
			Date start = dateformatter.parse("28/10/2013");	//monday
			Date end = start;	//same monday
		
		when :
			def res = new StandardWorkloadCalendar().getWorkload(start, end)
			
		then :
			res == 1.0f;
	}
	
	def "should return a workload of 10 because this is the workload of a sprint"(){
		given :
			Date start = dateformatter.parse("28/10/2013");	//monday
			Date end = dateformatter.parse("08/11/2013");	//friday the week after
		
		when :
			def res = new StandardWorkloadCalendar().getWorkload(start, end)
			
		then :
			res == 10.0f;
	}
	
	def "should rant because the end date predate the start date"(){
		given :
			Date end =dateformatter.parse("27/10/2013") //sunday
			Date start = end.plus(1);	//monday
		
		when :
			new StandardWorkloadCalendar().getWorkload(start, end)
			
		then :
			thrown IllegalArgumentException
		
	}
}
