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

import javax.validation.constraints.NotNull;

import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.importer.Target;
import org.squashtest.tm.service.internal.batchimport.ActionStepInstruction;
import org.squashtest.tm.service.internal.batchimport.CallStepInstruction;
import org.squashtest.tm.service.internal.batchimport.Instruction;
import org.squashtest.tm.service.internal.batchimport.StepInstruction;
import org.squashtest.tm.service.internal.batchimport.TestCaseInstruction;
import org.squashtest.tm.service.internal.batchimport.TestCaseTarget;
import org.squashtest.tm.service.internal.batchimport.TestStepTarget;
import org.squashtest.tm.service.internal.batchimport.excel.TargetFinder;

/**
 * 
 * @author Gregory Fouquet
 * 
 */
public class PropertyHolderFinderRepository<COL extends Enum<COL> & TemplateColumn> {
	private static final Map<TemplateWorksheet, PropertyHolderFinderRepository<?>> finderRepoByWorksheet = new HashMap<TemplateWorksheet, PropertyHolderFinderRepository<?>>(
			TemplateWorksheet.values().length);

	static {
		finderRepoByWorksheet.put(TemplateWorksheet.TEST_CASES_SHEET, createTestCasesWorksheetRepo());
		finderRepoByWorksheet.put(TemplateWorksheet.STEPS_SHEET, createStepsWorksheetRepo());
	}

	private static PropertyHolderFinderRepository<TestCaseSheetColumn> createTestCasesWorksheetRepo() {
		PropertyHolderFinderRepository<TestCaseSheetColumn> r = new PropertyHolderFinderRepository<TestCaseSheetColumn>();

		TargetFinder<TestCaseInstruction, TestCaseTarget> targetFinder = new TargetFinder<TestCaseInstruction, TestCaseTarget>() {
			@Override
			public TestCaseTarget find(TestCaseInstruction instruction) {
				return instruction.getTarget();
			}
		};

		r.finderByColumn.put(TestCaseSheetColumn.TC_NUM, targetFinder);
		r.finderByColumn.put(TestCaseSheetColumn.TC_PATH, targetFinder);

		TargetFinder<TestCaseInstruction, TestCaseInstruction> instructionFinder = new TargetFinder<TestCaseInstruction, TestCaseInstruction>() {
			@Override
			public TestCaseInstruction find(TestCaseInstruction instruction) {
				return instruction;
			}
		};
		r.finderByColumn.put(TestCaseSheetColumn.ACTION, instructionFinder);

		TargetFinder<TestCaseInstruction, TestCase> testCaseFinder = new TargetFinder<TestCaseInstruction, TestCase>() {
			@Override
			public TestCase find(TestCaseInstruction instruction) {
				return instruction.getTestCase();
			}
		};

		r.defaultFinder = testCaseFinder;

		return r;
	}

	/**
	 * @return
	 */
	private static PropertyHolderFinderRepository<?> createStepsWorksheetRepo() {
		PropertyHolderFinderRepository<StepSheetColumn> r = new PropertyHolderFinderRepository<StepSheetColumn>();

		TargetFinder<StepInstruction, TestStepTarget> targetFinder = new TargetFinder<StepInstruction, TestStepTarget>() {
			@Override
			public TestStepTarget find(StepInstruction instruction) {
				return instruction.getTarget();
			}
		};

		r.finderByColumn.put(StepSheetColumn.TC_OWNER_PATH, targetFinder);
		r.finderByColumn.put(StepSheetColumn.TC_STEP_NUM, targetFinder);

		TargetFinder<StepInstruction, StepInstruction> instructionFinder = new TargetFinder<StepInstruction, StepInstruction>() {
			@Override
			public StepInstruction find(StepInstruction instruction) {
				return instruction;
			}
		};

		r.finderByColumn.put(StepSheetColumn.ACTION, instructionFinder);

		TargetFinder<StepInstruction, Object> stepFinder = new TargetFinder<StepInstruction, Object>() {
			@Override
			public Object find(StepInstruction instruction) {
				if (instruction instanceof ActionStepInstruction) {
					return ((ActionStepInstruction) instruction).getTestStep();
				}
				if (instruction instanceof CallStepInstruction) {
					return ((CallStepInstruction) instruction).getCalledTC();
				}

				throw new IllegalArgumentException("Cannot process this type of instruction : " + instruction);
			}
		};

		r.defaultFinder = stepFinder;

		return r;
	}

	@SuppressWarnings("unchecked")
	public static <C extends Enum<C> & TemplateColumn> PropertyHolderFinderRepository<C> forWorksheet(
			@NotNull TemplateWorksheet worksheet) {
		return (PropertyHolderFinderRepository<C>) finderRepoByWorksheet.get(worksheet);
	}

	private final Map<COL, TargetFinder<?, ?>> finderByColumn = new HashMap<COL, TargetFinder<?, ?>>();
	/**
	 * the default finder is to be used when no finder could be found from {@link #finderByColumn}
	 */
	private TargetFinder<?, ?> defaultFinder;

	private PropertyHolderFinderRepository() {
		super();
	}

	@SuppressWarnings("unchecked")
	public <I extends Instruction, T extends Target> TargetFinder<I, T> findTargetFinder(COL col) {
		TargetFinder<?, ?> finder = finderByColumn.get(col);
		return (TargetFinder<I, T>) (finder == null ? defaultFinder : finder);
	}
}
