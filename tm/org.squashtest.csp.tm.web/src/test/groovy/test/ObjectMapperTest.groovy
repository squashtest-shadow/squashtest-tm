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

package test

import org.codehaus.jackson.map.ObjectMapper;

import spock.lang.Specification;

/**
 * @author Gregory
 *
 */
class ObjectMapperTest extends Specification {
	ObjectMapper mapper = new ObjectMapper()
	def "should marshall linked map in entries order"() {
		given:
		LinkedHashMap map = new LinkedHashMap();
		map.put 2, "foo"
		map.put 1, "bar"

		when:
		def res = mapper.writeValueAsString(map);

		then:
		res == '{"2":"foo","1":"bar"}'
	}
	def "should marshall linked map in entries order, take 2"() {
		given:
		LinkedHashMap map = new LinkedHashMap();
		map.put 1, "foo"
		map.put 2, "bar"

		when:
		def res = mapper.writeValueAsString(map);

		then:
		res == '{"1":"foo","2":"bar"}'
	}
}
