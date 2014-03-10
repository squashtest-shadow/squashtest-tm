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
package org.squashtest.tm.service.internal.batchimport

import spock.lang.Specification;
import spock.lang.Unroll;

class UtilsTest extends Specification{

	@Unroll("should say that path '#path' is #textres valid because #why")
	def "should say that the following pathes are valid"(){
		
		expect :
			Utils.isPathWellFormed(path) == predicate
		
		where :
			path								|	predicate	| textres 	| why
			"/toto/titi"						|	true		|	""		| "it's well formed"
			"/toto/tata/tutu/rere"				|	true		|	""		| "it's well formed"
			"/to \\/ to/tutu \\/ slash/tata"	|	true		|	""		| "it's well formed and escaped slashes are harmless"
			"/toto/tata \\/"					|	true		|	""		| "it's well formed and escaped slashes are harmless"
			"toto/tata"							|	false		|	"not"	| "it doesn't begin with a '/'"
			"/toto/tuytu\\/ends with slash/"	|	false		|	"not"	| "it ends with a slash"
			"/toto"								|	false		|	"not"	| "it's too short (only project name)"
		
	}
	

	@Unroll("the project name in '#path' is '#name'")
	def "should extract project names"(){
		
		expect :
			def res = Utils.extractProjectName(path)
			name.equals(res);
		
		where :
			path										|	name
			"/toto/tata"								|	"toto"
			"/toto \\/ with escape \\/ slash \\//tutu"	|	"toto \\/ with escape \\/ slash \\/"
		
	}
	
	@Unroll("the test case name in '#path' is '#name'")
	def "should extract test case names"(){
		expect :
			def res = Utils.extractTestCaseName(path)
			name.equals(res);
		
		where :
			path										|	name
			"/toto/tutu"								|	"tutu"
			"/toto/tata/tete/titi/tutu"					|	"tutu"
			"/\\/yeah\\//yo\\/yo/tu\\/tu\\/"			|	"tu\\/tu\\/"
	}

	
	
	@Unroll("path '#path' splits into #num elements")
	def "should split a path"(){
		
		expect :
			def pathes = Utils.splitPath(path) as List
			pathes == names
		
		where :
			path										|	num	|	names
			"/toto/tutu"								|	2	|	["toto", "tutu"] 
			"/toto/tata/tete/titi/tutu"					|	5	|	["toto", "tata", "tete", "titi", "tutu"]
			"/\\/yeah\\//yo\\/yo/tu\\/tu\\/"			|	3	|	["\\/yeah\\/", "yo\\/yo", "tu\\/tu\\/"]
		
	}
	
	
	def "should return uniques project names"(){
		
		given :
			def paths = [
					"/project 1/toto/titi",
					"/project \\/2/toto/titi",
					"/project 1/tata",
					"/project \\/2//bob, mike"
				]
		
		when :
			def res = Utils.extractProjectNames(paths)
		
		then :
			res == ["project 1", "project \\/2"]
	}
	
	
	@Unroll("should rename #path to #newpath")
	def "should 'rename' a path with a new name"(){
		expect:
			newpath == Utils.rename(path, newname)
		
		where :
			path					| 	newname			| newpath
			"/bob/robert/toto"		|	"mike"			| "/bob/robert/mike"
			"/home/couch"			|	"bed"			| "/home/bed"
	}
}
