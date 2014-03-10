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

import static org.junit.Assert.*;

import org.junit.Test;

import spock.lang.Specification;

/**
 * @author Gregory Fouquet
 *
 */
class WorkbookMetaDataTest extends Specification {
	def "should validate"() {
		given:
		WorkbookMetaData wmd = new WorkbookMetaData();

		wmd.addWorksheetDef(new WorksheetDef(TemplateWorksheet.TEST_CASES_SHEET) {
					void validate() {
						// validating sheet
					}
				})

		when:
		wmd.validate()

		then:
		notThrown(TemplateMismatchException)
	}
	def "should NOT validate"() {
		given:
		WorkbookMetaData wmd = new WorkbookMetaData();

		wmd.addWorksheetDef(new WorksheetDef(TemplateWorksheet.TEST_CASES_SHEET) {
					void validate() {
						// non validation sheet
						throw new TemplateMismatchException();
					}
				})

		wmd.addWorksheetDef(new WorksheetDef(TemplateWorksheet.STEPS_SHEET) {
					void validate() {
						// non validation sheet
						throw new TemplateMismatchException();
					}
				})

		when:
		wmd.validate()

		then:
		def e = thrown(TemplateMismatchException)
		e.mismatches.size() == 2
	}
}
