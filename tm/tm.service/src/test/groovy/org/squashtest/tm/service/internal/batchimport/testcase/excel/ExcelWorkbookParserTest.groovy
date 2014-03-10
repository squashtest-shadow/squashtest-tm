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

package org.squashtest.tm.service.internal.batchimport.testcase.excel;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.squashtest.tm.exception.SheetCorruptedException;

import spock.lang.Specification;

/**
 * @author Gregory Fouquet
 *
 */
class ExcelWorkbookParserTest extends Specification {
	def "should create a parser for correct excel file"() {
		given:
		Resource xls = new ClassPathResource("batchimport/testcase/import-2269.xlsx")
		
		expect:
		ExcelWorkbookParser.createParser(xls.file)
	}
	
	def "should fail to create parser for corrupted sheet"() {
		given:
		Resource xls = new ClassPathResource("batchimport/testcase/garbage-file.xlsx")
		
		when:
		ExcelWorkbookParser.createParser(xls.file)
		
		then:
		thrown(SheetCorruptedException);
	}
}
