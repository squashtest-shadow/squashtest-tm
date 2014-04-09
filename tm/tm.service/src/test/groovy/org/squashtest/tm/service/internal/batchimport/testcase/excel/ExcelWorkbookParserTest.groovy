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
import org.squashtest.tm.core.foundation.lang.IsoDateUtils;
import org.squashtest.tm.exception.SheetCorruptedException;

import spock.lang.Specification;
import spock.lang.Unroll;

import static org.squashtest.tm.service.importer.ImportMode.*

/**
 * @author Gregory Fouquet
 *
 */
class ExcelWorkbookParserTest extends Specification {
	@Unroll
	def "should create a parser for correct excel file #file"() {
		given:
		Resource xls = new ClassPathResource("batchimport/testcase/" + file)

		expect:
		ExcelWorkbookParser.createParser(xls.file)

		where:
		file << [
			"import-2269.xlsx",
			"ignored-headers.xlsx"
		]
	}

	@Unroll
	def "should raise exception #exception for corrupted sheet #file "() {
		given:
		Resource xls = new ClassPathResource(file)

		when:
		ExcelWorkbookParser.createParser(xls.file)

		then:
		thrown(exception);

		where:
		file										| exception
		"batchimport/testcase/garbage-file.xlsx"	| SheetCorruptedException
		"batchimport/testcase/no-header.xlsx"		| TemplateMismatchException // should be refined
		"batchimport/testcase/missing-headers.xlsx"	| TemplateMismatchException // should be refined
		//		"batchimport/testcase/duplicate-ws.xlsx" | DuplicateWorksheetException
	}

	def "should parse file and create test case target object"() {
		given:
		Resource xls = new ClassPathResource("batchimport/testcase/import-2269.xlsx")

		and:
		ExcelWorkbookParser parser = ExcelWorkbookParser.createParser(xls.file)

		and:
		def expectedPaths = (1..8).collect { "path/row$it" }
		def expectedNums = (1..8).collect { it + 10 }
		def expectedRefs = (1..8).collect { "ref$it" }
		def expectedNames = (1..8).collect { "name$it" }
		def expectedPres = (1..8).collect { "pre$it" }
		def expectedDescs = (1..8).collect { "desc$it" }
		def expectedCreators = (1..8).collect { "creator$it" }
		def expectedCreateds = (1..8).collect { "2003-02-0$it" }
		def expectedActions = [CREATE, CREATE, UPDATE, UPDATE, DELETE, DELETE, UPDATE, null]

		when:
		parser.parse().releaseResources()

		then:
		parser.instructions*.target.path == expectedPaths
		parser.instructions*.testCase.reference == expectedRefs
		parser.instructions*.testCase.name == expectedNames
		parser.instructions*.testCase.prerequisite == expectedPres
		parser.instructions*.testCase.description == expectedDescs
		parser.instructions.collect { IsoDateUtils.formatIso8601Date(it.testCase.createdOn) }  == expectedCreateds
		parser.instructions*.testCase.createdBy == expectedCreators
		parser.instructions*.mode == expectedActions

	}
}
