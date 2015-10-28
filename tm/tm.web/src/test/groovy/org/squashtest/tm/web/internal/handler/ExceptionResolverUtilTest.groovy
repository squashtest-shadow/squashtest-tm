/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.web.internal.handler;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.EnumerationUtils;
import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;
import org.squashtest.tm.web.internal.exceptionresolver.ExceptionResolverUtils;
import org.squashtest.tm.web.internal.exceptionresolver.HandlerBindExceptionResolver;
import org.squashtest.tm.web.internal.exceptionresolver.MimeType;

import spock.lang.Specification;

class ExceptionResolverUtilTest extends Specification {
	

	
	def "should say that the client accepts json"() {
		given:
		HttpServletRequest req = Mock()
		req.getHeaders("Accept") >> new IteratorEnumeration(['foo/bar, application/json, text/javascript'].iterator())

				when:
		def res = ExceptionResolverUtils.clientAcceptsMIME(req, MimeType.APPLICATION_JSON)
		
		then: 
		res == true
	}
	
	def "should say that the client accepts json 2"() {
		given:
		HttpServletRequest req = Mock()
		req.getHeaders("Accept") >> new IteratorEnumeration(['*/*'].iterator())

				when:
		def res = ExceptionResolverUtils.clientAcceptsMIMEOrAnything(req, MimeType.APPLICATION_JSON)
		
		then:
		res == true
	}

	
}
