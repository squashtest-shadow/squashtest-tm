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
package org.squashtest.tm.web.internal.filter;

import static org.junit.Assert.*;

import org.junit.Test;

import spock.lang.Specification;
import spock.lang.Unroll;

/**
 * @author Gregory
 *
 */
class IE8AcceptHeaderFixerFilterTest extends Specification {

	@Unroll
	def "'#agent' should not be an ie8 user agent : #notIe8"() {
		expect:
		new IE8AcceptHeaderFixerFilter().notIE8UserAgent(agent) == notIe8
		
		where:
		agent | notIe8
		"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0)"          | false
		"Mozilla/4.0 (compatible;MSIE8.0;Windows NT 5.1; Trident/4.0)"             | false
		"Mozilla/4.0 (compatible; MSIE 9.0; Windows NT 5.1; Trident/4.0)"          | true
		"Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko"     | true
		"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:24.0) Gecko/20100101 Firefox/24.0" | true
	}
	
}
