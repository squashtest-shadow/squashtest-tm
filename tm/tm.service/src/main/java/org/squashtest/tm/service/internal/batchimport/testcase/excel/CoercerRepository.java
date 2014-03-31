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

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseNature;
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.domain.testcase.TestCaseType;
import org.squashtest.tm.service.internal.batchimport.excel.CellValueCoercer;
import org.squashtest.tm.service.internal.batchimport.excel.OptionalBooleanCellCoercer;
import org.squashtest.tm.service.internal.batchimport.excel.OptionalDateCellCoercer;
import org.squashtest.tm.service.internal.batchimport.excel.OptionalEnumCellCoercer;
import org.squashtest.tm.service.internal.batchimport.excel.OptionalIntegerCellCoercer;
import org.squashtest.tm.service.internal.batchimport.excel.StringCellCoercer;

/**
 * Repository of {@link CellValueCoercer} for {@link TestCaseSheetColumn}s
 * @author Gregory Fouquet
 * 
 */
@Component
public final class CoercerRepository {
	public static final CoercerRepository INSTANCE = new CoercerRepository();
	/**
	 * The default coercer that shall be given when no other is defined.
	 */
	private static final CellValueCoercer<?> DEFAULT_COERCER = StringCellCoercer.INSTANCE;

	private Map<TestCaseSheetColumn, CellValueCoercer<?>> coercerByColumn = new HashMap<TestCaseSheetColumn, CellValueCoercer<?>>();

	private CoercerRepository() {
		coercerByColumn.put(TestCaseSheetColumn.TC_NUM, OptionalIntegerCellCoercer.INSTANCE);
		coercerByColumn.put(TestCaseSheetColumn.TC_WEIGHT_AUTO, OptionalBooleanCellCoercer.INSTANCE);
		coercerByColumn.put(TestCaseSheetColumn.TC_WEIGHT, OptionalEnumCellCoercer.forEnum(TestCaseImportance.class));
		coercerByColumn.put(TestCaseSheetColumn.TC_NATURE, OptionalEnumCellCoercer.forEnum(TestCaseNature.class));
		coercerByColumn.put(TestCaseSheetColumn.TC_TYPE, OptionalEnumCellCoercer.forEnum(TestCaseType.class));
		coercerByColumn.put(TestCaseSheetColumn.TC_STATUS, OptionalEnumCellCoercer.forEnum(TestCaseStatus.class));
		coercerByColumn.put(TestCaseSheetColumn.TC_CREATED_ON, OptionalDateCellCoercer.INSTANCE);
	}

	/**
	 * Finds a coercer for the given column. When no coercer is available, returns the default coercer
	 * 
	 * @param col
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <VAL> CellValueCoercer<VAL> findCoercer(TestCaseSheetColumn col) {
		CellValueCoercer<?> coercer = (CellValueCoercer<VAL>) coercerByColumn.get(col);
		return (CellValueCoercer<VAL>) (coercer == null ? DEFAULT_COERCER : coercer);
	}
}
