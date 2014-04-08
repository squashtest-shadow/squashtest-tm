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

import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.importer.Target;
import org.squashtest.tm.service.internal.batchimport.ActionStepInstruction;
import org.squashtest.tm.service.internal.batchimport.CallStepInstruction;
import org.squashtest.tm.service.internal.batchimport.Instruction;
import org.squashtest.tm.service.internal.batchimport.ParameterInstruction;
import org.squashtest.tm.service.internal.batchimport.ParameterTarget;
import org.squashtest.tm.service.internal.batchimport.StepInstruction;
import org.squashtest.tm.service.internal.batchimport.TestCaseInstruction;
import org.squashtest.tm.service.internal.batchimport.TestCaseTarget;
import org.squashtest.tm.service.internal.batchimport.TestStepTarget;
import org.squashtest.tm.service.internal.batchimport.excel.PropertyHolderFinder;

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
		finderRepoByWorksheet.put(TemplateWorksheet.PARAMETERS_SHEET, createParamsWorksheetRepo());
	}

	private static PropertyHolderFinderRepository<TestCaseSheetColumn> createTestCasesWorksheetRepo() {
		PropertyHolderFinderRepository<TestCaseSheetColumn> r = new PropertyHolderFinderRepository<TestCaseSheetColumn>();

		PropertyHolderFinder<TestCaseInstruction, TestCaseTarget> targetFinder = new PropertyHolderFinder<TestCaseInstruction, TestCaseTarget>() {
			@Override
			public TestCaseTarget find(TestCaseInstruction instruction) {
				return instruction.getTarget();
			}
		};

		r.finderByColumn.put(TestCaseSheetColumn.TC_NUM, targetFinder);
		r.finderByColumn.put(TestCaseSheetColumn.TC_PATH, targetFinder);

		PropertyHolderFinder<TestCaseInstruction, TestCaseInstruction> instructionFinder = new PropertyHolderFinder<TestCaseInstruction, TestCaseInstruction>() {
			@Override
			public TestCaseInstruction find(TestCaseInstruction instruction) {
				return instruction;
			}
		};
		r.finderByColumn.put(TestCaseSheetColumn.ACTION, instructionFinder);

		PropertyHolderFinder<TestCaseInstruction, TestCase> testCaseFinder = new PropertyHolderFinder<TestCaseInstruction, TestCase>() {
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
	private static PropertyHolderFinderRepository<?> createParamsWorksheetRepo() {
		PropertyHolderFinderRepository<ParameterSheetColumn> r = new PropertyHolderFinderRepository<ParameterSheetColumn>();

		PropertyHolderFinder<ParameterInstruction, ParameterTarget> targetFinder = new PropertyHolderFinder<ParameterInstruction, ParameterTarget>() {
			@Override
			public ParameterTarget find(ParameterInstruction instruction) {
				return instruction.getTarget();
			}
		};

		r.finderByColumn.put(ParameterSheetColumn.TC_OWNER_PATH, targetFinder);

		PropertyHolderFinder<ParameterInstruction, ParameterInstruction> instructionFinder = new PropertyHolderFinder<ParameterInstruction, ParameterInstruction>() {
			@Override
			public ParameterInstruction find(ParameterInstruction instruction) {
				return instruction;
			}
		};

		r.finderByColumn.put(ParameterSheetColumn.ACTION, instructionFinder);

		PropertyHolderFinder<ParameterInstruction, Parameter> paramFinder = new PropertyHolderFinder<ParameterInstruction, Parameter>() {
			@Override
			public Parameter find(ParameterInstruction instruction) {
				return instruction.getParameter();
			}
		};

		r.defaultFinder = paramFinder;

		return r;
	}

	/**
	 * @return
	 */
	private static PropertyHolderFinderRepository<?> createStepsWorksheetRepo() {
		PropertyHolderFinderRepository<StepSheetColumn> r = new PropertyHolderFinderRepository<StepSheetColumn>();

		PropertyHolderFinder<StepInstruction, TestStepTarget> targetFinder = new PropertyHolderFinder<StepInstruction, TestStepTarget>() {
			@Override
			public TestStepTarget find(StepInstruction instruction) {
				return instruction.getTarget();
			}
		};

		r.finderByColumn.put(StepSheetColumn.TC_OWNER_PATH, targetFinder);
		r.finderByColumn.put(StepSheetColumn.TC_STEP_NUM, targetFinder);

		PropertyHolderFinder<StepInstruction, StepInstruction> instructionFinder = new PropertyHolderFinder<StepInstruction, StepInstruction>() {
			@Override
			public StepInstruction find(StepInstruction instruction) {
				return instruction;
			}
		};

		r.finderByColumn.put(StepSheetColumn.ACTION, instructionFinder);

		PropertyHolderFinder<StepInstruction, Object> stepFinder = new PropertyHolderFinder<StepInstruction, Object>() {
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

	private final Map<COL, PropertyHolderFinder<?, ?>> finderByColumn = new HashMap<COL, PropertyHolderFinder<?, ?>>();
	/**
	 * the default finder is to be used when no finder could be found from {@link #finderByColumn}
	 */
	private PropertyHolderFinder<?, ?> defaultFinder;

	private PropertyHolderFinderRepository() {
		super();
	}

	@SuppressWarnings("unchecked")
	public <I extends Instruction, T extends Target> PropertyHolderFinder<I, T> findTargetFinder(COL col) {
		PropertyHolderFinder<?, ?> finder = finderByColumn.get(col);
		return (PropertyHolderFinder<I, T>) (finder == null ? defaultFinder : finder);
	}
}
