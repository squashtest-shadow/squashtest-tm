/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.domain.library.structures

import org.squashtest.tm.domain.library.NodeReference
import org.squashtest.tm.domain.library.structures.StringPathMap

import spock.lang.Specification


class StringPathMapTest extends Specification {
	
	
	def "should ... meeh"(){
		

		
		when :
			File file = new File("/maurice");
			def parent = file.getParentFile()
		
		then :
			def fName = file.getName()
			def pName = parent.getName()
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
