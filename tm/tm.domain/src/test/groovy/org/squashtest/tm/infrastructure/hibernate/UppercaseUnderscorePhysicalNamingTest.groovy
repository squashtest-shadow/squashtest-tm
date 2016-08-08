/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.infrastructure.hibernate;

import static org.junit.Assert.*
import org.hibernate.boot.model.naming.PhysicalNamingStrategy
import org.hibernate.boot.model.naming.Identifier


import spock.lang.Specification

class UppercaseUnderscorePhysicalNamingTestTest extends Specification{
	PhysicalNamingStrategy strategy = new UppercaseUnderscorePhysicalNaming()
	
	def "table name should be UC-US class  name"() {
		when:
		def name = strategy.toPhysicalTableName(id("foo.bar.EntityName"), null)
		then:
		name == "ENTITY_NAME"
	}
	
	def "column name should be UC-US prop name"() {
		when:
		def name = strategy.toPhysicalColumnName(id("propertyNameOfAKind"))
		then:
		name == "PROPERTY_NAME_OF_A_KIND"
	}
	
	def "contuiguous capitals should be considered as one word"() {
		when:
		def name = strategy.toPhysicalTableName(id("MyURLParser"))
		then:
		name == "MY_URL_PARSER"
	}
	
        
    def id(name){
        return Identifier.toIdentifier(name)
    }
}
