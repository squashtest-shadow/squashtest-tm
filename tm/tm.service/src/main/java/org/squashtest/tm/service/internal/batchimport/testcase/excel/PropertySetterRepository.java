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

import org.squashtest.tm.service.internal.batchimport.excel.NullPropertySetter;
import org.squashtest.tm.service.internal.batchimport.excel.PropertySetter;
import org.squashtest.tm.service.internal.batchimport.excel.ReflectionFieldSetter;
import org.squashtest.tm.service.internal.batchimport.excel.ReflectionMutatorSetter;

/**
 * Repository of {@link PropertySetter}s in the context of a specific {@link TemplateWorksheet}
 * 
 * @author Gregory Fouquet
 * 
 */
final class PropertySetterRepository<COL extends Enum<COL> & TemplateColumn> {
	private static final Map<TemplateWorksheet, PropertySetterRepository<?>> FINDER_REPO_BY_WORKSHEET = new HashMap<TemplateWorksheet, PropertySetterRepository<?>>(
			TemplateWorksheet.values().length);

	static {
		FINDER_REPO_BY_WORKSHEET.put(TemplateWorksheet.TEST_CASES_SHEET, createTestCasesWorksheetRepo());
		FINDER_REPO_BY_WORKSHEET.put(TemplateWorksheet.STEPS_SHEET, createStepsWorksheetRepo());
		FINDER_REPO_BY_WORKSHEET.put(TemplateWorksheet.PARAMETERS_SHEET, createParamsWorksheetRepo());
		FINDER_REPO_BY_WORKSHEET.put(TemplateWorksheet.DATASETS_SHEET, createDatasetsWorksheetRepo());
		FINDER_REPO_BY_WORKSHEET.put(TemplateWorksheet.DATASET_PARAM_VALUES_SHEET, createDatasetParamValuesWorksheetRepo());
	}

	/**
	 * 
	 * @param worksheet
	 * @return the {@link PropertySetterRepository} suitable fot the given worksheet
	 */
	@SuppressWarnings("unchecked")
	public static <C extends Enum<C> & TemplateColumn> PropertySetterRepository<C> forWorksheet(
			@NotNull TemplateWorksheet worksheet) {
		return (PropertySetterRepository<C>) FINDER_REPO_BY_WORKSHEET.get(worksheet);
	}

	/**
	 * @return
	 */
	private static PropertySetterRepository<?> createDatasetsWorksheetRepo() {
		PropertySetterRepository<DatasetSheetColumn> r = new PropertySetterRepository<DatasetSheetColumn>();

		// target
		r.propSetterByColumn.put(DatasetSheetColumn.TC_OWNER_PATH,
				ReflectionMutatorSetter.forProperty("path", String.class));
		r.propSetterByColumn.put(DatasetSheetColumn.TC_DATASET_NAME, ReflectionFieldSetter.forField("name"));

		// instruction
		r.propSetterByColumn.put(DatasetSheetColumn.ACTION, ReflectionFieldSetter.forOptionalField("mode"));

		// datasetvalue
		// None of the following columns actually need processing (because they will be treated
		// in DatasetParamValuesWorksheetRepo).
		r.propSetterByColumn.put(DatasetSheetColumn.TC_PARAM_OWNER_PATH, NullPropertySetter.INSTANCE );
		r.propSetterByColumn.put(DatasetSheetColumn.TC_DATASET_PARAM_NAME,NullPropertySetter.INSTANCE);
		r.propSetterByColumn.put(DatasetSheetColumn.TC_DATASET_PARAM_VALUE,NullPropertySetter.INSTANCE);


		return r;
	}


	/**
	 * @return
	 */
	private static PropertySetterRepository<?> createDatasetParamValuesWorksheetRepo() {
		PropertySetterRepository<DatasetParamValuesSheetColumn> r = new PropertySetterRepository<DatasetParamValuesSheetColumn>();

		// target
		r.propSetterByColumn.put(DatasetParamValuesSheetColumn.TC_OWNER_PATH,
				ReflectionMutatorSetter.forProperty("path", String.class));
		r.propSetterByColumn.put(DatasetParamValuesSheetColumn.TC_DATASET_NAME, ReflectionFieldSetter.forField("name"));

		// instruction
		r.propSetterByColumn.put(DatasetParamValuesSheetColumn.ACTION, ReflectionFieldSetter.forOptionalField("mode"));

		// datasetvalue
		r.propSetterByColumn.put(DatasetParamValuesSheetColumn.TC_PARAM_OWNER_PATH,
				ReflectionFieldSetter.forOptionalField("parameterOwnerPath"));
		r.propSetterByColumn.put(DatasetParamValuesSheetColumn.TC_DATASET_PARAM_NAME,
				ReflectionFieldSetter.forField("parameterName"));
		r.propSetterByColumn.put(DatasetParamValuesSheetColumn.TC_DATASET_PARAM_VALUE,
				ReflectionFieldSetter.forOptionalField("value"));

		return r;
	}


	/**
	 * @return
	 */
	private static PropertySetterRepository<?> createParamsWorksheetRepo() {
		PropertySetterRepository<ParameterSheetColumn> r = new PropertySetterRepository<ParameterSheetColumn>();

		// target
		r.propSetterByColumn.put(ParameterSheetColumn.TC_OWNER_PATH,
				ReflectionMutatorSetter.forProperty("path", String.class));

		// instruction
		r.propSetterByColumn.put(ParameterSheetColumn.ACTION, ReflectionFieldSetter.forOptionalField("mode"));

		// paraameter
		// param.setName(..) has logic we'd rather short-circuit
		r.propSetterByColumn.put(ParameterSheetColumn.TC_PARAM_NAME, ReflectionFieldSetter.forField("name"));
		r.propSetterByColumn.put(ParameterSheetColumn.TC_PARAM_DESCRIPTION,
				ReflectionFieldSetter.forOptionalField("description"));

		return r;
	}

	/**
	 * @return
	 */
	private static PropertySetterRepository<?> createStepsWorksheetRepo() {
		PropertySetterRepository<StepSheetColumn> stepWorksheetRepo = new PropertySetterRepository<StepSheetColumn>();

		// target
		stepWorksheetRepo.propSetterByColumn.put(StepSheetColumn.TC_OWNER_PATH,
				ReflectionMutatorSetter.forProperty("path", String.class));
		stepWorksheetRepo.propSetterByColumn.put(StepSheetColumn.TC_STEP_NUM, ReflectionFieldSetter.forOptionalField("index"));

		// instruction
		stepWorksheetRepo.propSetterByColumn.put(StepSheetColumn.ACTION, ReflectionMutatorSetter.forOptionalProperty("mode"));

		// step props
		stepWorksheetRepo.propSetterByColumn.put(StepSheetColumn.TC_STEP_IS_CALL_STEP, NullPropertySetter.INSTANCE);

		stepWorksheetRepo.propSetterByColumn.put(StepSheetColumn.TC_STEP_ACTION, StepActionPropSetter.INSTANCE);
		stepWorksheetRepo.propSetterByColumn.put(StepSheetColumn.TC_STEP_EXPECTED_RESULT, StepResultPropSetter.INSTANCE);

		// call step prop only (will rant if the other step shows up)
		stepWorksheetRepo.propSetterByColumn.put(StepSheetColumn.TC_STEP_CALL_DATASET, ParamAssignationModeSetter.INSTANCE);

		return stepWorksheetRepo;
	}

	/**
	 * @return
	 */
	private static PropertySetterRepository<?> createTestCasesWorksheetRepo() {
		PropertySetterRepository<TestCaseSheetColumn> r = new PropertySetterRepository<TestCaseSheetColumn>();

		// target
		r.propSetterByColumn.put(TestCaseSheetColumn.TC_PATH, ReflectionFieldSetter.forField("path"));
		r.propSetterByColumn.put(TestCaseSheetColumn.TC_NUM, ReflectionFieldSetter.forOptionalField("order"));

		// test case
		r.propSetterByColumn.put(TestCaseSheetColumn.TC_REFERENCE, ReflectionFieldSetter.forOptionalField("reference"));
		r.propSetterByColumn.put(TestCaseSheetColumn.TC_NAME, ReflectionFieldSetter.forOptionalField("name"));
		r.propSetterByColumn.put(TestCaseSheetColumn.TC_WEIGHT_AUTO,
				ReflectionFieldSetter.forOptionalField("importanceAuto"));
		r.propSetterByColumn.put(TestCaseSheetColumn.TC_WEIGHT, ReflectionFieldSetter.forOptionalField("importance"));
		r.propSetterByColumn.put(TestCaseSheetColumn.TC_NATURE, ReflectionFieldSetter.forOptionalField("nature"));
		r.propSetterByColumn.put(TestCaseSheetColumn.TC_TYPE, ReflectionFieldSetter.forOptionalField("type"));
		r.propSetterByColumn.put(TestCaseSheetColumn.TC_STATUS, ReflectionFieldSetter.forOptionalField("status"));
		r.propSetterByColumn.put(TestCaseSheetColumn.TC_DESCRIPTION,
				ReflectionFieldSetter.forOptionalField("description"));
		r.propSetterByColumn.put(TestCaseSheetColumn.TC_PRE_REQUISITE,
				ReflectionFieldSetter.forOptionalField("prerequisite"));
		// createdOn and createdBy field name is not known, we use mutators to set'em
		r.propSetterByColumn.put(TestCaseSheetColumn.TC_CREATED_ON,
				ReflectionMutatorSetter.forOptionalProperty("createdOn"));
		r.propSetterByColumn.put(TestCaseSheetColumn.TC_CREATED_BY,
				ReflectionMutatorSetter.forOptionalProperty("createdBy"));

		// instruction
		r.propSetterByColumn.put(TestCaseSheetColumn.ACTION, ReflectionMutatorSetter.forOptionalProperty("mode"));

		return r;
	}

	private Map<COL, PropertySetter<?, ?>> propSetterByColumn = new HashMap<COL, PropertySetter<?, ?>>();

	private PropertySetterRepository() {
		super();
	}

	/**
	 * Finds the {@link PropertySetter} for the given column.
	 * 
	 * @param col
	 * @return the {@link PropertySetter} or <code>null</code> when nothing found.
	 */
	@SuppressWarnings("unchecked")
	public <V, T> PropertySetter<V, T> findPropSetter(COL col) {
		return (PropertySetter<V, T>) propSetterByColumn.get(col);
	}
}
