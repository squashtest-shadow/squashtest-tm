
package org.squashtest.csp.tm.domain.library.structures

import org.squashtest.csp.tm.domain.library.NodeReference
import spock.lang.Specification
import java.io.File;


class StringPathMapTest extends Specification {
	
	
	def "should ... meeh"(){
		

		
		when :
			File file = new File("/maurice");
			def parent = file.getParentFile()
		
		then :
			def fName = file.getName()
			def pName = parent.getName()
			println("toto")
		
	}

	def "should get the path from the node reference"(){
		
		given :
			def ref1 = new NodeReference(1l, "ref1", true)
			def ref2 = new NodeReference(2l, "ref2", true)
			def ref3 = new NodeReference(3l, "ref3", true)
		
		and :
			def map = new StringPathMap<NodeReference>()
			map.put("/ref1", ref1)
			map.put("/ref1/ref2", ref2)
			map.put("/ref1/ref2/ref3", ref3)
		
		when :
		
			def ref= new NodeReference(3l, "ref3", true)
			def res = map.getPath(ref)
				
		then :
			res == "/ref1/ref2/ref3"
		
	}
	
	
	
	def "should get children nodes path of a given path"(){
		
		given :
			def ref0 = new NodeReference(0l, "ref0", true)
			def ref1 = new NodeReference(1l, "ref1", true)
			def ref2 = new NodeReference(2l, "ref2", true)
			def ref3 = new NodeReference(3l, "ref3", true)
			def ref4 = new NodeReference(4l, "ref4", true)
		
		and :
			def map = new StringPathMap<NodeReference>()
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
			def map = new StringPathMap<NodeReference>()
		
		when :
			def res = map.tokenizePath("/toto/titi/tutu")
		then :
			res == ["/", "toto", "titi", "tutu"]
			
		
		
	}
	
}
