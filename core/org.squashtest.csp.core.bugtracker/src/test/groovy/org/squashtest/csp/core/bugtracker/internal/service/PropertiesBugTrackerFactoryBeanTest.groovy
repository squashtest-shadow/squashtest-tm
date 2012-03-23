/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.core.bugtracker.internal.service

import org.spockframework.runtime.UnrolledFeatureNameGenerator;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.service.PropertiesBugTrackerFactoryBean;

import spock.lang.Specification;
import spock.lang.Unroll;

class PropertiesBugTrackerFactoryBeanTest extends Specification {
	PropertiesBugTrackerFactoryBean factoryBean = new PropertiesBugTrackerFactoryBean()

	def "product should be singleton"() {
		when:
		factoryBean.afterPropertiesSet()

		then:
		factoryBean.singleton
	}

	@Unroll("should return NOT_DEFINED BT when kind is #kind and url is #url") 
	def "should return NOT_DEFINED BT when properties are not properly set"() {
		given:
		factoryBean.url = url
		factoryBean.kind = kind
		
		when:
		factoryBean.afterPropertiesSet()

		then:
		factoryBean.object == BugTracker.NOT_DEFINED
		
		where:
		url               | kind 
		null              | null 
		null              | "whatever"
		""                | "whatever"
		"http://wherever" | null 
		"http://wherever" | ""
		""                | "" 
	}

	def "should create a BT from properties"() {
		given:
		factoryBean.url = "http://peterparker.com"
		factoryBean.kind = "spider-man"


		when:
		factoryBean.afterPropertiesSet()
		def res = factoryBean.object

		then:
		res.url == "http://peterparker.com"
		res.kind == "spider-man"
	}
	
	def "should create undefined BT from properties"() {
		given:
		factoryBean.url = "none"
		factoryBean.kind = "none"


		when:
		factoryBean.afterPropertiesSet()

		then:
		factoryBean.object == BugTracker.NOT_DEFINED
	}
}
