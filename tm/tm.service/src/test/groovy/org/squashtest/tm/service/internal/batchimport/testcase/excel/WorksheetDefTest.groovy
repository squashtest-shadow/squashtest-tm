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

import org.junit.Test;
import org.squashtest.tm.service.internal.repository.hibernate.HibernateCustomCustomFieldBindingDao.NewBindingPosition;

import spock.lang.Specification;
import spock.lang.Unroll;

/**
 * @author Gregory Fouquet
 *
 */
class WorksheetDefTest extends Specification {
	@Unroll
	def "colset #cols should produce a valid worksheet"() {
		given:
		WorksheetDef wd = new WorksheetDef(TemplateWorksheet.TEST_CASES_SHEET)
		cols.each { wd.addColumnDef(new ColumnDef(it, 1)) }
		
		when:
		wd.validate()
		
		then:
		notThrown(TemplateMismatchException)
		
		where:
		cols << [
			TestCaseSheetColumn.values(), 
			[TestCaseSheetColumn.TC_NAME, TestCaseSheetColumn.TC_PATH]
		] 
		
	}
	
	@Unroll
	def "colset #cols should produce an invalid worksheet"() {
		given:
		WorksheetDef wd = new WorksheetDef(TemplateWorksheet.TEST_CASES_SHEET)
		cols.each { wd.addColumnDef(new ColumnDef(it, 1)) }
		
		when:
		wd.validate()
		
		then:
		thrown(TemplateMismatchException)
		
		where:
		cols << [
			[],
			[TestCaseSheetColumn.TC_PATH],
			[TestCaseSheetColumn.TC_NAME],
			[TestCaseSheetColumn.TC_ID] 
		]
		
	}

}
