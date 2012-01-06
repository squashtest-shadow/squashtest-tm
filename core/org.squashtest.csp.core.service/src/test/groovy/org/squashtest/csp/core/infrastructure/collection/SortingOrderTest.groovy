package org.squashtest.csp.core.infrastructure.collection

import spock.lang.Specification
import spock.lang.Unroll;

class SortingOrderTest extends Specification {
	@Unroll("Should coerce code #code into SortOrder #expectedOrder")
	def "Should coerce code into SortOrder"() {
		when: 
		def order = SortOrder.coerceFromCode(code) 
		
		then: 
		order == expectedOrder
		
		where:
		code   | expectedOrder
		"asc"  | SortOrder.ASCENDING
		"desc" | SortOrder.DESCENDING
		
	}
	
	def "Should break when coercing unknown code"() {
		when: 
		SortOrder.coerceFromCode("bs code")
		
		then:
		thrown IllegalArgumentException
	}
}
