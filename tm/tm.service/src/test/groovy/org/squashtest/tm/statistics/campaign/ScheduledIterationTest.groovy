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
package org.squashtest.tm.statistics.campaign

import java.text.SimpleDateFormat;

import org.squashtest.tm.service.statistics.campaign.ScheduledIteration;
import org.squashtest.tm.service.statistics.campaign.ScheduledIteration.ScheduledDatesIterator;

import spock.lang.Specification
import spock.lang.Unroll;

class ScheduledIterationTest extends Specification {
	
	SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy")

	// *********** ScheduledDatesIterator test *****************
	
	def "should say that empty collection has no next"(){
		
		given :
			def datesIter = new ScheduledDatesIterator([])
			
		when :
			def res = datesIter.hasNext()
			
		then : 
			res == false
		
	}
	
	def "should say that when it has been asked for a start date, an end date will follow"(){
		
		given :
			def datesIter = new ScheduledDatesIterator([new ScheduledIteration()])
			datesIter.next()
			
		when :
			def res = datesIter.hasNext()
		
		then :
			res == true
	}
	
	def "should say that there is another iteration to process "(){
		
		given :
			def datesIter = new ScheduledDatesIterator([new ScheduledIteration(), new ScheduledIteration()])
			datesIter.next()
			datesIter.next()
		
		when :
			def res = datesIter.hasNext()
		
		
		then :
			res == true
	}
	
	def "should say that there is no more dates to process"(){
		
		given :
			def datesIter = new ScheduledDatesIterator([new ScheduledIteration(), new ScheduledIteration()])
			datesIter.next()
			datesIter.next()
			datesIter.next()
			datesIter.next()
			
		
		when :
			def res = datesIter.hasNext()
		
		then :
			res == false
	}
	
	def "should return the dates in correct order"(){
		
		given :
			def refDate = new Date()
			def date1 = refDate.plus(1)
			def date2 = refDate.plus(2)
			def date3 = refDate.plus(3)
			def date4 = refDate.plus(4)
			def date5 = refDate.plus(5)
			def date6 = refDate.plus(6)
			def date7 = refDate.plus(7)
			def date8 = refDate.plus(8)
			
		and :
			def collection = [
				new ScheduledIteration(1l, "1", 0, date1, date2),
				new ScheduledIteration(2l, "2", 0, date3, date4),
				new ScheduledIteration(3l, "3", 0, date5, date6),
				new ScheduledIteration(4l, "4", 0, date7, date8),				
				]
		
		and :
			def iter = new ScheduledDatesIterator(collection)
		
		when :
			def res = []
			while (iter.hasNext()) res << iter.next() 	
		
		then :
			res == [date1, date2, date3, date4, date5, date6, date7, date8]
		
		
	} 
	
	
	// ******************** ScheduledIteration test *******************
	
	def "should say that all dates are ok"(){
		
		given :
			def refDate = new Date()
			def date1 = refDate.plus(1)
			def date2 = refDate.plus(2)
			def date3 = refDate.plus(3)
			def date4 = refDate.plus(4)
			def date5 = refDate.plus(5)
			def date6 = refDate.plus(6)
			def date7 = refDate.plus(7)
			def date8 = refDate.plus(8)
			
		and :
			def collection = [
				new ScheduledIteration(1l, "1", 0, date1, date2),
				new ScheduledIteration(2l, "2", 0, date3, date4),
				new ScheduledIteration(3l, "3", 0, date5, date6),
				new ScheduledIteration(4l, "4", 0, date7, date8),
			]
		
		when :
			ScheduledIteration.checkIterationsDatesIntegrity(collection)
		
		then :
			notThrown IllegalArgumentException
		
	}
	
	def "should say that some dates are missing"(){
		
		given :
				
				def refDate = new Date()
				def date1 = refDate.plus(1)
				def date2 = refDate.plus(2)
				def date3 = refDate.plus(3)
				def date4 = refDate.plus(4)
				def date5 = refDate.plus(5)
				def date6 = refDate.plus(6)
				def date7 = refDate.plus(7)
				def date8 = refDate.plus(8)
				
		and :
			def collection = [
				new ScheduledIteration(1l, "1", 0, date1, date2),
				new ScheduledIteration(2l, "2", 0, date3, null),
				new ScheduledIteration(3l, "3", 0, date5, date6),
				new ScheduledIteration(4l, "4", 0, date7, date8),
			]
		
		
		when :
			ScheduledIteration.checkIterationsDatesIntegrity(collection)
		
		then :
			IllegalArgumentException ex = thrown(IllegalArgumentException)
			ex.message == ScheduledIteration.SCHED_ITER_MISSING_DATES_I18N
	}
	
	
	def "should say that some dates are overlapping"(){
		
		given :
				
				def refDate = new Date()
				def date1 = refDate.plus(1)
				def date2 = refDate.plus(2)
				def date3 = refDate.plus(3)
				def date4 = refDate.plus(4)
				def date5 = refDate.plus(5)
				def date6 = refDate.plus(6)
				def date7 = refDate.plus(7)
				def date8 = refDate.plus(8)
				
		and :
			def collection = [
				new ScheduledIteration(1l, "1", 0, date1, date2),
				new ScheduledIteration(2l, "2", 0, date3, date5),
				new ScheduledIteration(3l, "3", 0, date4, date6),
				new ScheduledIteration(4l, "4", 0, date7, date8),
			]
		
		
		when :
			ScheduledIteration.checkIterationsDatesIntegrity(collection)
		
		then :
			IllegalArgumentException ex = thrown(IllegalArgumentException)
			ex.message == ScheduledIteration.SCHED_ITER_OVERLAP_DATES_I18N
	}
	
	def "should say that empty lists are ok"(){
		given :
			def collection = []
			
		when :
			ScheduledIteration.checkIterationsDatesIntegrity(collection);
			
		then :
			notThrown IllegalArgumentException
	}
	
	def "should also correctly handle the first item"(){
		
		given :
			def collection = [ new ScheduledIteration()]
		
		when :
			ScheduledIteration.checkIterationsDatesIntegrity(collection)
		
		then :
			IllegalArgumentException e = thrown(IllegalArgumentException)
			e.message == ScheduledIteration.SCHED_ITER_MISSING_DATES_I18N
	}
	
	def "should compute the cumulative number of tests per day wrt workload"(){
		
		given :
			ScheduledIteration iter = new ScheduledIteration(1l, "iter", 20, date("28/10/2013"), date("08/11/2013"))
		
		when :
			iter.computeCumulativeTestByDate(0.0f)
		
		then :
			iter.cumulativeTestsByDate == [
				[date("28/10/2013"), 2.0f] as Object[],
				[date("29/10/2013"), 4.0f] as Object[],
				[date("30/10/2013"), 6.0f] as Object[],
				[date("31/10/2013"), 8.0f] as Object[],
				[date("01/11/2013"), 10.0f] as Object[],
				[date("02/11/2013"), 10.0f] as Object[],
				[date("03/11/2013"), 10.0f] as Object[],
				[date("04/11/2013"), 12.0f] as Object[],
				[date("05/11/2013"), 14.0f] as Object[],
				[date("06/11/2013"), 16.0f] as Object[],
				[date("07/11/2013"), 18.0f] as Object[],
				[date("08/11/2013"), 20.0f] as Object[]
			]
			
	}
	
	def date(String str){
		dateFormat.parse(str)
	}
}
