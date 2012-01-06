package org.squashtest.csp.tm.domain.library.structures

import org.squashtest.csp.tm.domain.library.NodeReference
import spock.lang.Specification

class StringPathMapTest extends Specification {

	def "should get the path from the node reference"(){
		
		given :
			def ref1 = new NodeReference(1l, "ref1")
			def ref2 = new NodeReference(2l, "ref2")
			def ref3 = new NodeReference(3l, "ref3")
		
		and :
			def map = new StringPathMap()
			map.put("/ref1", ref1)
			map.put("/ref1/ref2", ref2)
			map.put("/ref1/ref2/ref3", ref3)
		
		when :
		
			def ref= new NodeReference(3l, "ref3")
			def res = map.getPath(ref)
				
		then :
			res == "/ref1/ref2/ref3"
		
	}
	
	
	
	def "should get children nodes path of a given path"(){
		
		given :
			def ref0 = new NodeReference(0l, "ref0")
			def ref1 = new NodeReference(1l, "ref1")
			def ref2 = new NodeReference(2l, "ref2")
			def ref3 = new NodeReference(3l, "ref3")
			def ref4 = new NodeReference(4l, "ref4")
		
		and :
			def map = new StringPathMap()
			map.put("/", ref0)
			map.put("/ref1", ref1)
			map.put("/ref2", ref2)
			map.put("/ref1/ref3", ref3)
			map.put("/ref4", ref4)
			
			def expected = ["/ref1", "/ref2", "/ref4"]
		
		when :
			def result = map.getKnownChildrenPath("/")
		
		then :
			result == expected
		
	}
	
	
	def "should split a path in ordered tokens"(){
		
		given :
			def map = new StringPathMap()
		
		when :
			def res = map.tokenizePath("/toto/titi/tutu")
		then :
			res == ["/", "toto", "titi", "tutu"]
			
		
		
	}
	
}
