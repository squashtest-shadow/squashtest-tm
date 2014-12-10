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
package org.squashtest.tm.service.internal.batchexport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.squashtest.tm.core.foundation.lang.DateUtils;
import org.squashtest.tm.service.internal.batchexport.ExportModel.CustomField;
import org.squashtest.tm.service.internal.batchexport.ExportModel.DatasetModel;
import org.squashtest.tm.service.internal.batchexport.ExportModel.ParameterModel;
import org.squashtest.tm.service.internal.batchexport.ExportModel.TestCaseModel;
import org.squashtest.tm.service.internal.batchexport.ExportModel.TestStepModel;
import org.squashtest.tm.service.internal.batchimport.testcase.excel.DatasetSheetColumn;
import org.squashtest.tm.service.internal.batchimport.testcase.excel.ParameterSheetColumn;
import org.squashtest.tm.service.internal.batchimport.testcase.excel.StepSheetColumn;
import org.squashtest.tm.service.internal.batchimport.testcase.excel.TemplateWorksheet;
import org.squashtest.tm.service.internal.batchimport.testcase.excel.TestCaseSheetColumn;


/**
 * @author bsiri
 * 
 */
class ExcelExporter {

	private static final String DS_SHEET = TemplateWorksheet.DATASETS_SHEET.sheetName;
	private static final String PRM_SHEET = TemplateWorksheet.PARAMETERS_SHEET.sheetName;
	private static final String ST_SHEET = TemplateWorksheet.STEPS_SHEET.sheetName;
	private static final String TC_SHEET = TemplateWorksheet.TEST_CASES_SHEET.sheetName;
	// that map will remember which column index is
	private Map<String, Integer> cufColumnsByCode = new HashMap<String, Integer>();

	private Workbook workbook;

	public ExcelExporter() {
		super();
		createWorkbook();
		createHeaders();
	}

	public void appendToWorkbook(ExportModel model, boolean keepRteFormat) {

		if (!keepRteFormat) {
			removeRteFormat(model);
		}
		appendTestCases(model);
		appendTestSteps(model);
		appendParameters(model);
		appendDatasets(model);
	}

	private void removeRteFormat(ExportModel model) {
		removeRteFormatFromParameters(model.getParameters());
		removeRteFormatFromTestCases(model.getTestCases());
		removeRteFormatFromTestSteps(model.getTestSteps());
	}

	private void removeRteFormatFromTestSteps(List<TestStepModel> testSteps) {
		for (TestStepModel ts : testSteps) {
			ts.setAction(removeHtml(ts.getAction()));
			ts.setResult(removeHtml(ts.getResult()));
		}
	}

	private void removeRteFormatFromTestCases(List<TestCaseModel> testCases) {
		for (TestCaseModel tc : testCases) {
			tc.setDescription(removeHtml(tc.getDescription()));
		}
	}

	private void removeRteFormatFromParameters(List<ParameterModel> parameters) {
		for (ParameterModel param : parameters) {
			param.setDescription(removeHtml(param.getDescription()));
		}
	}

	private String removeHtml(String html) {
		return html.replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", "");
	}

	public File print() {
		try {
			File temp = File.createTempFile("tc_export_", "xls");
			temp.deleteOnExit();

			FileOutputStream fos = new FileOutputStream(temp);
			workbook.write(fos);
			fos.close();

			return temp;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void appendTestCases(ExportModel model) {

		List<TestCaseModel> models = model.getTestCases();
		Sheet tcSheet = workbook.getSheet(TC_SHEET);
		Row r;
		int rIdx = tcSheet.getLastRowNum() + 1;
		int cIdx = 0;

		for (TestCaseModel tcm : models) {

			r = tcSheet.createRow(rIdx);

			r.createCell(cIdx++).setCellValue(tcm.getProjectId());
			r.createCell(cIdx++).setCellValue(tcm.getProjectName());
			r.createCell(cIdx++).setCellValue(tcm.getPath());
			r.createCell(cIdx++).setCellValue(tcm.getOrder());
			r.createCell(cIdx++).setCellValue(tcm.getId());
			r.createCell(cIdx++).setCellValue(tcm.getReference());
			r.createCell(cIdx++).setCellValue(tcm.getName());
			r.createCell(cIdx++).setCellValue(tcm.getWeightAuto());
			r.createCell(cIdx++).setCellValue(tcm.getWeight().toString());
			r.createCell(cIdx++).setCellValue(tcm.getNature().getCode());
			r.createCell(cIdx++).setCellValue(tcm.getType().getCode());
			r.createCell(cIdx++).setCellValue(tcm.getStatus().toString());
			r.createCell(cIdx++).setCellValue(tcm.getDescription());
			r.createCell(cIdx++).setCellValue(tcm.getPrerequisite());
			r.createCell(cIdx++).setCellValue(tcm.getNbReq());
			r.createCell(cIdx++).setCellValue(tcm.getNbCaller());
			r.createCell(cIdx++).setCellValue(tcm.getNbAttachments());
			r.createCell(cIdx++).setCellValue(format(tcm.getCreatedOn()));
			r.createCell(cIdx++).setCellValue(tcm.getCreatedBy());
			r.createCell(cIdx++).setCellValue(format(tcm.getLastModifiedOn()));
			r.createCell(cIdx++).setCellValue(tcm.getLastModifiedBy());

			appendCustomFields(r, "TC_CUF_", tcm.getCufs());

			rIdx++;
			cIdx = 0;
		}
	}

	private void appendTestSteps(ExportModel model) {

		List<TestStepModel> models = model.getTestSteps();
		Sheet stSheet = workbook.getSheet(ST_SHEET);

		Row r;
		int rIdx = stSheet.getLastRowNum() + 1;
		int cIdx = 0;

		for (TestStepModel tsm : models) {

			r = stSheet.createRow(rIdx);

			r.createCell(cIdx++).setCellValue(tsm.getTcOwnerPath());
			r.createCell(cIdx++).setCellValue(tsm.getTcOwnerId());
			r.createCell(cIdx++).setCellValue(tsm.getId());
			r.createCell(cIdx++).setCellValue(tsm.getOrder());
			r.createCell(cIdx++).setCellValue(tsm.getIsCallStep());
			r.createCell(cIdx++).setCellValue(tsm.getDsName());
			r.createCell(cIdx++).setCellValue(tsm.getAction());
			r.createCell(cIdx++).setCellValue(tsm.getResult());
			r.createCell(cIdx++).setCellValue(tsm.getNbReq());
			r.createCell(cIdx++).setCellValue(tsm.getNbAttach());

			appendCustomFields(r, "TC_STEP_CUF_", tsm.getCufs());

			rIdx++;
			cIdx = 0;
		}
	}

	private void appendParameters(ExportModel model) {

		List<ParameterModel> models = model.getParameters();
		Sheet pSheet = workbook.getSheet(PRM_SHEET);

		Row r;
		int rIdx = pSheet.getLastRowNum() + 1;
		int cIdx = 0;

		for (ParameterModel pm : models) {
			r = pSheet.createRow(rIdx);

			r.createCell(cIdx++).setCellValue(pm.getTcOwnerPath());
			r.createCell(cIdx++).setCellValue(pm.getTcOwnerId());
			r.createCell(cIdx++).setCellValue(pm.getId());
			r.createCell(cIdx++).setCellValue(pm.getName());
			r.createCell(cIdx++).setCellValue(pm.getDescription());

			rIdx++;
			cIdx = 0;
		}
	}

	private void appendDatasets(ExportModel model) {

		List<DatasetModel> models = model.getDatasets();
		Sheet dsSheet = workbook.getSheet(DS_SHEET);

		Row r;
		int rIdx = dsSheet.getLastRowNum() + 1;
		int cIdx = 0;

		for (DatasetModel dm : models) {
			r = dsSheet.createRow(rIdx);

			r.createCell(cIdx++).setCellValue(dm.getTcOwnerPath());
			r.createCell(cIdx++).setCellValue(dm.getOwnerId());
			r.createCell(cIdx++).setCellValue(dm.getId());
			r.createCell(cIdx++).setCellValue(dm.getName());
			r.createCell(cIdx++).setCellValue(dm.getParamOwnerPath());
			r.createCell(cIdx++).setCellValue(dm.getParamOwnerId());
			r.createCell(cIdx++).setCellValue(dm.getParamName());
			r.createCell(cIdx++).setCellValue(dm.getParamValue());

			rIdx++;
			cIdx = 0;
		}

	}

	private void appendCustomFields(Row r, String codePrefix, List<CustomField> cufs) {

		for (CustomField cuf : cufs) {

			String code = codePrefix + cuf.getCode();
			Integer idx = cufColumnsByCode.get(code);

			// if unknown : register it
			if (idx == null) {
				idx = registerCuf(r.getSheet(), code);
			}

			Cell c = r.createCell(idx);
			String value = nullSafeValue(cuf);
			c.setCellValue(value);
		}
	}

	private String nullSafeValue(CustomField customField) {
		String value = customField.getValue();
		return value == null ? "" : value;
	}

	private int registerCuf(Sheet sheet, String code) {

		Row headers = sheet.getRow(0);
		int nextIdx = headers.getLastCellNum();
		headers.createCell(nextIdx).setCellValue(code);

		cufColumnsByCode.put(code, nextIdx);

		return nextIdx;
	}

	private String format(Date date) {
		if (date == null) {
			return "";
		} else {
			return DateUtils.formatIso8601Date(date);
		}
	}

	// for now we care only of Excel 2003
	private void createWorkbook() {
		Workbook wb = new HSSFWorkbook();
		wb.createSheet(TC_SHEET);
		wb.createSheet(ST_SHEET);
		wb.createSheet(PRM_SHEET);
		wb.createSheet(DS_SHEET);

		this.workbook = wb;
	}

	private void createHeaders() {

		createTestCaseSheetHeaders();
		createStepSheetHeaders();
		createParameterSheetHeaders();
		createDatasetSheetHeaders();

	}

	private void createDatasetSheetHeaders() {
		Sheet dsSheet = workbook.getSheet(DS_SHEET);
		Row h = dsSheet.createRow(0);
		int cIdx = 0;

		h.createCell(cIdx++).setCellValue(DatasetSheetColumn.TC_OWNER_PATH.header);
		h.createCell(cIdx++).setCellValue(DatasetSheetColumn.TC_OWNER_ID.header);
		h.createCell(cIdx++).setCellValue(DatasetSheetColumn.TC_DATASET_ID.header);
		h.createCell(cIdx++).setCellValue(DatasetSheetColumn.TC_DATASET_NAME.header);
		h.createCell(cIdx++).setCellValue(DatasetSheetColumn.TC_PARAM_OWNER_PATH.header);
		h.createCell(cIdx++).setCellValue(DatasetSheetColumn.TC_PARAM_OWNER_ID.header);
		h.createCell(cIdx++).setCellValue(DatasetSheetColumn.TC_DATASET_PARAM_NAME.header);
		h.createCell(cIdx++).setCellValue(DatasetSheetColumn.TC_DATASET_PARAM_VALUE.header);
	}

	private void createParameterSheetHeaders() {
		Sheet pSheet = workbook.getSheet(PRM_SHEET);
		Row h = pSheet.createRow(0);
		int cIdx = 0;

		h.createCell(cIdx++).setCellValue(ParameterSheetColumn.TC_OWNER_PATH.header);
		h.createCell(cIdx++).setCellValue(ParameterSheetColumn.TC_OWNER_ID.header);
		h.createCell(cIdx++).setCellValue(ParameterSheetColumn.TC_PARAM_ID.header);
		h.createCell(cIdx++).setCellValue(ParameterSheetColumn.TC_PARAM_NAME.header);
		h.createCell(cIdx++).setCellValue(ParameterSheetColumn.TC_PARAM_DESCRIPTION.header);
	}

	private void createStepSheetHeaders() {
		Sheet stSheet = workbook.getSheet(ST_SHEET);
		Row h = stSheet.createRow(0);
		int cIdx = 0;

		h.createCell(cIdx++).setCellValue(StepSheetColumn.TC_OWNER_PATH.header);
		h.createCell(cIdx++).setCellValue(StepSheetColumn.TC_OWNER_ID.header);
		h.createCell(cIdx++).setCellValue(StepSheetColumn.TC_STEP_ID.header);
		h.createCell(cIdx++).setCellValue(StepSheetColumn.TC_STEP_NUM.header);
		h.createCell(cIdx++).setCellValue(StepSheetColumn.TC_STEP_IS_CALL_STEP.header);
		h.createCell(cIdx++).setCellValue(StepSheetColumn.TC_STEP_CALL_DATASET.header);
		h.createCell(cIdx++).setCellValue(StepSheetColumn.TC_STEP_ACTION.header);
		h.createCell(cIdx++).setCellValue(StepSheetColumn.TC_STEP_EXPECTED_RESULT.header);
		h.createCell(cIdx++).setCellValue(StepSheetColumn.TC_STEP_NB_REQ.header);
		h.createCell(cIdx++).setCellValue(StepSheetColumn.TC_STEP_NB_ATTACHMENT.header);
	}

	private void createTestCaseSheetHeaders() {
		Sheet tcSheet = workbook.getSheet(TC_SHEET);
		Row h = tcSheet.createRow(0);
		int cIdx = 0;

		h.createCell(cIdx++).setCellValue(TestCaseSheetColumn.PROJECT_ID.header);
		h.createCell(cIdx++).setCellValue(TestCaseSheetColumn.PROJECT_NAME.header);
		h.createCell(cIdx++).setCellValue(TestCaseSheetColumn.TC_PATH.header);
		h.createCell(cIdx++).setCellValue(TestCaseSheetColumn.TC_NUM.header);
		h.createCell(cIdx++).setCellValue(TestCaseSheetColumn.TC_ID.header);
		h.createCell(cIdx++).setCellValue(TestCaseSheetColumn.TC_REFERENCE.header);
		h.createCell(cIdx++).setCellValue(TestCaseSheetColumn.TC_NAME.header);
		h.createCell(cIdx++).setCellValue(TestCaseSheetColumn.TC_WEIGHT_AUTO.header);
		h.createCell(cIdx++).setCellValue(TestCaseSheetColumn.TC_WEIGHT.header);
		h.createCell(cIdx++).setCellValue(TestCaseSheetColumn.TC_NATURE.header);
		h.createCell(cIdx++).setCellValue(TestCaseSheetColumn.TC_TYPE.header);
		h.createCell(cIdx++).setCellValue(TestCaseSheetColumn.TC_STATUS.header);
		h.createCell(cIdx++).setCellValue(TestCaseSheetColumn.TC_DESCRIPTION.header);
		h.createCell(cIdx++).setCellValue(TestCaseSheetColumn.TC_PRE_REQUISITE.header);
		h.createCell(cIdx++).setCellValue(TestCaseSheetColumn.TC_NB_REQ.header);
		h.createCell(cIdx++).setCellValue(TestCaseSheetColumn.TC_NB_CALLED_BY.header);
		h.createCell(cIdx++).setCellValue(TestCaseSheetColumn.TC_NB_ATTACHMENT.header);
		h.createCell(cIdx++).setCellValue(TestCaseSheetColumn.TC_CREATED_ON.header);
		h.createCell(cIdx++).setCellValue(TestCaseSheetColumn.TC_CREATED_BY.header);
		h.createCell(cIdx++).setCellValue(TestCaseSheetColumn.TC_LAST_MODIFIED_ON.header);
		h.createCell(cIdx++).setCellValue(TestCaseSheetColumn.TC_LAST_MODIFIED_BY.header);
	}

}
