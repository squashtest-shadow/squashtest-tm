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


import spock.lang.Specification

class UppercaseUnderscoreNamingStrategyTest extends Specification{
	UppercaseUnderscoreNamingStrategy strategy = new UppercaseUnderscoreNamingStrategy()
	
	def "table name should be UC-US class  name"() {
		when:
		def name = strategy.classToTableName("foo.bar.EntityName")
		then:
		name == "ENTITY_NAME"
	}
	
	def "column name should be UC-US prop name"() {
		when:
		def name = strategy.propertyToColumnName("propertyNameOfAKind")
		then:
		name == "PROPERTY_NAME_OF_A_KIND"
	}
	
	def "contuiguous capitals should be considered as one word"() {
		when:
		def name = strategy.classToTableName("MyURLParser")
		then:
		name == "MY_URL_PARSER"
	}
	
	public "should not modify table name"() {
		given:
		def nameFromConfig = "Table-name" 
		
		when:
		def name = strategy.tableName(nameFromConfig);
		then:
		name == nameFromConfig
	}
	
	public "should not modify column name"() {
		given:
		def nameFromConfig = "COlumn-name" 
		
		when:
		def name = strategy.columnName(nameFromConfig);
		then:
		name == nameFromConfig
	}
	
	public "should not modify logical column name"() {
		given:
		def nameFromConfig = "COlumn-name" 
		
		when:
		def name = strategy.logicalColumnName(nameFromConfig, "propName")
		then:
		name == nameFromConfig
	}
	
	public "should UC-US property name into logical column name"() {
		when:
		def name = strategy.logicalColumnName(null, "propName")
		then:
		name == "PROP_NAME"
	}
	
	public "should not modify logical collection table name"() {
		given:
		def nameFromConfig = "TAblE-name"
		
		when:
		def name = strategy.logicalCollectionTableName(nameFromConfig, "foo", "bar", "baz")
		
		then:
		name == nameFromConfig
	}
	
	public "should append tables names into logical collection table name"() {
		when:
		def name = strategy.logicalCollectionTableName(null, "OwnerTable", "AssociatedTable", "propertyName")
		
		then:
		name == "OwnerTable_AssociatedTable"
	}
	
	public "should append prop name into logical collection table name when no association table"() {
		when:
		def name = strategy.logicalCollectionTableName(null, "OwnerTable", null, "foo.bar.Baz.propertyName")
		
		then:
		name == "OwnerTable_PROPERTY_NAME"
	}
	
	public "should not modify logical collection column name"() {
		given:
		def nameFromConfig = "cOlum-name"
		
		when:
		def name = strategy.logicalCollectionColumnName(nameFromConfig, "foo", "bar")
		
		then:
		name == nameFromConfig
	}
	
	public "should append referenced col name to UC-US prop name into logical collection columnname"() {
		when:
		def name = strategy.logicalCollectionColumnName("", "org.fooBar", "Baz")
		
		then:
		name == "FOO_BAR_Baz"
	}
	
	public "collection table name should be US-UC owner entity table appended to prop name"() {
		when:
		def name = strategy.collectionTableName("OwnerEntity", "ownerEntityTable", "associatedEntity", "associatedEntityTable", "propertyName")
		
		then:
		name == "ownerEntityTable_PROPERTY_NAME"
	}

	public "join key column name should be column name"() {
		when:
		def name = strategy.joinKeyColumnName("Column-name", "Table-name")
		
		then:
		name == "Column-name"
	}
	
	public "fk column name should be US_UC property name"() {
		when:
		def name = strategy.foreignKeyColumnName("propertyName", "propertyEntityName", "propertyTableName", "referencedColumnName")
		
		then:
		name == "PROPERTY_NAME"
	}

	public "fk column name should be property table name when no property name"() {
		when:
		def name = strategy.foreignKeyColumnName(null, "propertyEntityName", "propertyTableName", "referencedColumnName")
		
		then:
		name == "propertyTableName"
	}
}
