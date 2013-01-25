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
package org.squashtest.csp.tm.domain.execution;

import static org.squashtest.tm.domain.execution.ExecutionStatus.*

import org.squashtest.tm.domain.execution.ExecutionStatus
import org.squashtest.tm.domain.execution.ExecutionStatusReport

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Gregory Fouquet
 *
 */
class ExecutionStatusTest extends Specification {
	
	
	def "checks the constant sets"(){
		
		when :
			def canonical = ExecutionStatus.getCanonicalStatusSet();
			def terminal = ExecutionStatus.getTerminatedStatusSet();
			def nonTerminal = ExecutionStatus.getNonTerminatedStatusSet();
			
			
		then :
			
			canonical == [UNTESTABLE, SUCCESS, RUNNING, BLOCKED, FAILURE, READY] as Set
			terminal == [UNTESTABLE, SUCCESS, WARNING, BLOCKED, ERROR, FAILURE] as Set
			nonTerminal == [RUNNING, READY] as Set
		
	}
	
	
	def "should turn a list of ExecutionStatus to canonical status list"(){
		
		given :
			def nonCanon = [RUNNING, ERROR, BLOCKED, WARNING ]
			
		when :
			def canon = ExecutionStatus.toCanonicalStatusList(nonCanon)
			
		then :
			canon == [RUNNING, BLOCKED, BLOCKED, SUCCESS] as List
		
		
	}
	
	
	@Unroll("i18n key of #status should be '#key'")
	def "should return i18n key"() {
		when:
		def actualKey = status.i18nKey
		
		then:
		actualKey == key
		
		where: 
		status  	| key
		UNTESTABLE  | "execution.execution-status.UNTESTABLE"
		BLOCKED 	| "execution.execution-status.BLOCKED"
		FAILURE 	| "execution.execution-status.FAILURE"
		SUCCESS 	| "execution.execution-status.SUCCESS"
		RUNNING 	| "execution.execution-status.RUNNING"
		READY   	| "execution.execution-status.READY"
		ERROR	    | "execution.execution-status.ERROR"
		WARNING	    | "execution.execution-status.WARNING"
	}
	
	def "should compute a new status (1)"(){
		
		given :
			ExecutionStatusReport report = new ExecutionStatusReport()
			report.untestable = 0
			report.success = 3
			report.bloqued = 1
			report.failure = 2
			report.warning = 1
			report.error = 1
			report.running = 3
			report.ready = 1
			
		when :
			def res = ExecutionStatus.computeNewStatus(report)
		
		then :
			res == BLOCKED
	}
	
	def "should compute a new status (2)"(){
		
		given :
			ExecutionStatusReport report = new ExecutionStatusReport()
			report.untestable = 0
			report.success = 3
			report.bloqued = 0
			report.failure = 2
			report.warning = 1
			report.error = 1
			report.running = 3
			report.ready = 1
			
		when :
			def res = ExecutionStatus.computeNewStatus(report)
		
		then :
			res == ERROR
	}
	
	def "should compute a new status (3)"(){
		
		given :
			ExecutionStatusReport report = new ExecutionStatusReport()
			report.untestable = 0
			report.success = 3
			report.bloqued = 0
			report.failure = 2
			report.warning = 1
			report.error = 0
			report.running = 3
			report.ready = 1
			
		when :
			def res = ExecutionStatus.computeNewStatus(report)
		
		then :
			res == FAILURE
	}
	
	def "should compute a new status (4)"(){
		
		given :
			ExecutionStatusReport report = new ExecutionStatusReport()
			report.untestable = 0
			report.success = 3
			report.bloqued = 0
			report.failure = 0
			report.warning = 1
			report.error = 0
			report.running = 3
			report.ready = 1
			
		when :
			def res = ExecutionStatus.computeNewStatus(report)
		
		then :
			res == RUNNING
	}
	
	def "should compute a new status (5)"(){
		
		given :
			ExecutionStatusReport report = new ExecutionStatusReport()
			report.untestable = 0
			report.success = 3
			report.bloqued = 0
			report.failure = 0
			report.warning = 1
			report.error = 0
			report.running = 0
			report.ready = 0
			
		when :
			def res = ExecutionStatus.computeNewStatus(report)
		
		then :
			res == SUCCESS
	}
	
	def "should compute a new status (6)"(){
		
		given :
			ExecutionStatusReport report = new ExecutionStatusReport()
			report.untestable = 0
			report.success = 0
			report.bloqued = 0
			report.failure = 0
			report.warning = 1
			report.error = 0
			report.running = 0
			report.ready = 0
			
		when :
			def res = ExecutionStatus.computeNewStatus(report)
		
		then :
			res == SUCCESS
	}

	
	def "should never invoke resolveStatus on non canon status when using plublic methods"(){
		expect :
			FAILURE == WARNING.deduceNewStatus(FAILURE, RUNNING)
	}
	
	def "should crash when trying to resolveStatus on non canon status through class-protected methods"(){
		when :
			WARNING.resolveStatus(FAILURE, RUNNING)
		then :
			thrown(UnsupportedOperationException)
	}
	
}
