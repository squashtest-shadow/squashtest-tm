package org.squashtest.csp.tm.domain.requirement

import static org.squashtest.csp.tm.domain.requirement.RequirementStatus.*
import spock.lang.Specification
import spock.lang.Unroll

class RequirementStatusTest extends Specification {

	
	@Unroll("specifications for next available status for #status")
	def "correctedness of next available status"(){
		when :
			def nextSet = status.getAvailableNextStatus();
		
		then :
			nextSet == expectedSet
		
		where : 
			status				|	expectedSet
			WORK_IN_PROGRESS  	|	[ OBSOLETE, WORK_IN_PROGRESS, UNDER_REVIEW ] as Set
			UNDER_REVIEW		|	[ OBSOLETE, UNDER_REVIEW, APPROVED, WORK_IN_PROGRESS ] as Set
			APPROVED			|	[ OBSOLETE, APPROVED, UNDER_REVIEW, WORK_IN_PROGRESS ] as Set
			OBSOLETE			|	[ OBSOLETE ] as Set
			
	}
	
	@Unroll("specifications for the i18n key for #status")
	def "correctedness of the i18n"(){
		when :
			def key = status.getI18nKey();
		then :
			key == expectedKey
		
		where :		
			status 				|	expectedKey
			WORK_IN_PROGRESS  	| 	"requirement.status.WORK_IN_PROGRESS"
			UNDER_REVIEW		|	"requirement.status.UNDER_REVIEW"
			APPROVED			|	"requirement.status.APPROVED"
			OBSOLETE			|	"requirement.status.OBSOLETE"
	}
	
	@Unroll("specifications for update allowance for #status ")
	def "correctedness of update allowance"(){
		
		when :
			def allowed = status.getAllowsUpdate()
			
		then :
			allowed == expected
			
		where :
			status				|	expected
			WORK_IN_PROGRESS	|	true
			UNDER_REVIEW		|	true
			APPROVED			|	false
			OBSOLETE			|	false
	}
	
	
	@Unroll("specifications for status update allowance for #status")
	def "correctedness for status update allowance"(){
		
		when :
			def allowed = status.getAllowsStatusUpdate();
		
		then :
			allowed == expected
		
		where :
			status				|	expected
			WORK_IN_PROGRESS	|	true
			UNDER_REVIEW		|	true
			APPROVED			|	true
			OBSOLETE			|	false
	}
	
}
