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

import spock.lang.Specification
import spock.lang.Unroll;

class StandardWorkloadCalendarTest extends Specification {

	@Unroll("should say that workload for #strday is #res")
	def "should say that workload is 1 or 0"(){
		
		expect : 
			res == new StandardWorkloadCalendar().getWorkload(date)
			
		where :
		strday		|	res		|	date
		"monday"	|	1.0f	|	new SimpleDateFormat("dd/MM/yyyy").parse("28/10/2013")
		"tuesday"	|	1.0f	|	new SimpleDateFormat("dd/MM/yyyy").parse("29/10/2013")
		"wednesday"	|	1.0f	|	new SimpleDateFormat("dd/MM/yyyy").parse("30/10/2013")
		"thursday"	|	1.0f	|	new SimpleDateFormat("dd/MM/yyyy").parse("31/10/2013")
		"friday"	|	1.0f	|	new SimpleDateFormat("dd/MM/yyyy").parse("01/11/2013")
		"saturday"	|	0.0f	|	new SimpleDateFormat("dd/MM/yyyy").parse("02/11/2013")
		"sunday"	|	0.0f	|	new SimpleDateFormat("dd/MM/yyyy").parse("03/11/2013")
	}
	/*
	def "should return a workload of 10.f, because of exactly two weeks"(){
		
		given :
			Date start = new SimpleDateFormat()
	}*/
	
}
