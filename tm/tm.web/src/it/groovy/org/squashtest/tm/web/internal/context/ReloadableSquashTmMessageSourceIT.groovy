/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.web.internal.context


import javax.inject.Inject;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.test.context.ContextConfiguration;

import spock.lang.Specification;
import spock.lang.Unroll;
import spock.unitils.UnitilsSupport;
/**
 * @author Gregory Fouquet
 *
 */
@ContextConfiguration(["classpath:ReloadableSquashTmMessageSourceIT.xml"])
@UnitilsSupport
class ReloadableSquashTmMessageSourceIT extends Specification {
	@Inject
	MessageSource messageSource
	
	@Unroll
	def "message with key #key should resolve to #message"() {
		expect:
		message == messageSource.getMessage(key, null, Locale.default)
		
		where:
		key 			| message
		"whizz.label"	| "whizz"
		"bang.label" 	| "bang"
		"whatever.label"| "whatever"
	}

}
