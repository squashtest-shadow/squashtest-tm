package org.squashtest.tm.core.foundation.lang

import spock.lang.Specification

class NullFilterListCollectorTest extends Specification{

	def "sould collect with null filtering" (){
		given:
		def strings = ["joe","bob",null,"","john",null,null,"laura"]

		when:
		def collectedStrings = strings.stream().collect(NullFilterListCollector.toNullFilteredList())

		then:
		collectedStrings.sort() == ["joe","bob","","john","laura"].sort()


	}

}
