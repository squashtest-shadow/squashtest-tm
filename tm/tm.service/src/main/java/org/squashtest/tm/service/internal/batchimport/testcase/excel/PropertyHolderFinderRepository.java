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

import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.internal.batchimport.TestCaseInstruction;
import org.squashtest.tm.service.internal.batchimport.TestCaseTarget;
import org.squashtest.tm.service.internal.batchimport.excel.TargetFinder;

import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TestCaseSheetColumn.*;

/**
 * 
 * @author Gregory Fouquet
 * 
 */
public class PropertyHolderFinderRepository {
	public static final PropertyHolderFinderRepository INSTANCE = new PropertyHolderFinderRepository();

	private final Map<TestCaseSheetColumn, TargetFinder<TestCaseInstruction, ?>> finderByColumn = new HashMap<TestCaseSheetColumn, TargetFinder<TestCaseInstruction, ?>>();

	private final TargetFinder<TestCaseInstruction, TestCase> testCaseFinder = new TargetFinder<TestCaseInstruction, TestCase>() {
		@Override
		public TestCase find(TestCaseInstruction instruction) {
			return instruction.getTestCase();
		}
	};

	private PropertyHolderFinderRepository() {
		TargetFinder<TestCaseInstruction, TestCaseTarget> targetFinder = new TargetFinder<TestCaseInstruction, TestCaseTarget>() {
			@Override
			public TestCaseTarget find(TestCaseInstruction instruction) {
				return instruction.getTarget();
			}
		};

		finderByColumn.put(TC_NUM, targetFinder);
		finderByColumn.put(TC_PATH, targetFinder);
		
		TargetFinder<TestCaseInstruction, TestCaseInstruction> instructionFinder = new TargetFinder<TestCaseInstruction, TestCaseInstruction>() {
			@Override
			public TestCaseInstruction find(TestCaseInstruction instruction) {
				return instruction;
			}
		};
		finderByColumn.put(ACTION, instructionFinder);
	}

	@SuppressWarnings("unchecked")
	public <T> TargetFinder<TestCaseInstruction, T> findTargetFinder(TestCaseSheetColumn col) {
		TargetFinder<TestCaseInstruction, ?> finder = finderByColumn.get(col);
		return (TargetFinder<TestCaseInstruction, T>) (finder == null ? testCaseFinder : finder);
	}
}
