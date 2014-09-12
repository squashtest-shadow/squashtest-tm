/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.service.internal.importer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;

import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.exception.ColumnHeaderNotFoundException;
import org.squashtest.tm.exception.requirement.RequirementAlreadyVerifiedException;
import org.squashtest.tm.service.internal.repository.RequirementDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.security.PermissionEvaluationService;

@Component
public class RequirementTestCaseLinkParserImpl implements RequirementTestCaseLinkParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(RequirementTestCaseLinkParserImpl.class);
	@Inject
	private RequirementDao requirementDao;
	@Inject
	private TestCaseDao testCaseDao;

	@Inject
	private PermissionEvaluationService permissionService;

	public void setPermissionService(PermissionEvaluationService permissionService) {
		this.permissionService = permissionService;
	}

	@Override
	public void parseRow(Row row, ImportRequirementTestCaseLinksSummaryImpl summary,
			Map<String, Integer> columnsMapping,
			Map<TestCase, List<RequirementVersion>> requirementVersionsByTestCaseList) {
		if (validateRow(row, columnsMapping)) {
			summary.incrTotal();
			LOGGER.debug("Parse Row #" + getRowLineNumber(row));
			doParseRow(row, summary, columnsMapping, requirementVersionsByTestCaseList);
		}
	}

	private void doParseRow(Row row, ImportRequirementTestCaseLinksSummaryImpl summary,
			Map<String, Integer> columnsMapping,
			Map<TestCase, List<RequirementVersion>> requirementVersionsByTestCaseList) {

		// find requirementVersion
		RequirementVersion requirementVersion = findRequirementVersion(row, summary, columnsMapping);

		// find testCase
		TestCase testCase = findTestCase(row, summary, columnsMapping);

		// store information if ok
		if (requirementVersion != null && testCase != null) {
			checkLinkAlreadyExistAndStore(row, summary, requirementVersionsByTestCaseList, requirementVersion, testCase);
		} else {
			summary.incrFailures();
		}

	}

	private void checkLinkAlreadyExistAndStore(Row row, ImportRequirementTestCaseLinksSummaryImpl summary,
			Map<TestCase, List<RequirementVersion>> requirementVersionsByTestCaseList,
			RequirementVersion requirementVersion, TestCase testCase) {

		if (!testCaseAlreadyBoundToRequirement(testCase, requirementVersion)) {
			checkIfASisterAndStore(row, summary, requirementVersionsByTestCaseList, requirementVersion, testCase);
		} else {
			summary.incrFailures();
			summary.addLinkAlreadyExist(getRowLineNumber(row));
		}
	}

	private void checkIfASisterAndStore(Row row, ImportRequirementTestCaseLinksSummaryImpl summary,
			Map<TestCase, List<RequirementVersion>> requirementVersionsByTestCaseList,
			RequirementVersion requirementVersion, TestCase testCase) {
		
		List<RequirementVersion> requirementVersions = requirementVersionsByTestCaseList.get(testCase);
		if (requirementVersions == null) {
			requirementVersions = new ArrayList<RequirementVersion>();
			storeRequirementVersion(requirementVersionsByTestCaseList, requirementVersion, testCase,
					requirementVersions);
		} else {
			checkIfSisterInListAndStore(row, summary, requirementVersionsByTestCaseList, requirementVersion, testCase,
					requirementVersions);
		}
	}

	private void checkIfSisterInListAndStore(Row row, ImportRequirementTestCaseLinksSummaryImpl summary,
			Map<TestCase, List<RequirementVersion>> requirementVersionsByTestCaseList,
			RequirementVersion requirementVersion, TestCase testCase, List<RequirementVersion> requirementVersions) {
		if (containsASisterVersion(requirementVersion, requirementVersions)) {
			summary.incrFailures();
			summary.addLinkAlreadyExist(getRowLineNumber(row));
		} else {
			storeRequirementVersion(requirementVersionsByTestCaseList, requirementVersion, testCase,
					requirementVersions);
		}
	}

	private void storeRequirementVersion(Map<TestCase, List<RequirementVersion>> requirementVersionsByTestCaseList,
			RequirementVersion requirementVersion, TestCase testCase, List<RequirementVersion> requirementVersions) {
		requirementVersions.add(requirementVersion);
		requirementVersionsByTestCaseList.put(testCase, requirementVersions);
	}

	private boolean testCaseAlreadyBoundToRequirement(TestCase testCase, RequirementVersion requirementVersion) {
		try {
			testCase.checkRequirementNotVerified(requirementVersion);
		} catch (RequirementAlreadyVerifiedException e) {
			return true;
		}
		return false;
	}

	private boolean containsASisterVersion(RequirementVersion requirementVersion,
			List<RequirementVersion> potentialSisters) {
		Requirement requirement = requirementVersion.getRequirement();
		List<RequirementVersion> sisters = requirement.getRequirementVersions();
		for (RequirementVersion potentialSister : potentialSisters) {
			if (sisters.contains(potentialSister)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * will try to find the testCase. If testCase is not found or the user has no "link" access to it, will return
	 * <code>null</code>;
	 * 
	 * @param row
	 * @param summary
	 * @param columnsMapping
	 * @return the testCase or <code>null</code> row parameters are not suitable (not found, no good access rights )
	 */
	private TestCase findTestCase(Row row, ImportRequirementTestCaseLinksSummaryImpl summary,
			Map<String, Integer> columnsMapping) {
		Double testCaseDoubleId = ExcelRowReaderUtils.readNumericField(row, columnsMapping, ID_TEST_CASE_TAG);
		long testCaseId = testCaseDoubleId.longValue();
		TestCase testCase = testCaseDao.findById(testCaseId);
		if (testCase != null) {
			if (permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "LINK", testCase)) {
				return testCase;
			} else {
				summary.addTestCaseAccessRejected(getRowLineNumber(row));
			}
		} else {
			summary.addTestCaseNotFound(getRowLineNumber(row));
		}

		return null;
	}

	/**
	 * will try to find the requirement (see
	 * {@linkplain RequirementTestCaseLinkParserImpl#findRequirement(Row, ImportRequirementTestCaseLinksSummaryImpl, Map)}
	 * . <br>
	 * If the requirement is found, will find the requirementVersion and update the summary.<br>
	 * If the requirement is found but is obsolete <code>null </code> is returned.
	 * 
	 * @param row
	 * @param summary
	 * @param columnsMapping
	 * @return the requirementVersion or <code>null</code> row parameters are not suitable (not found, obsolete, no good
	 *         access rights)
	 */
	private RequirementVersion findRequirementVersion(Row row, ImportRequirementTestCaseLinksSummaryImpl summary,
			Map<String, Integer> columnsMapping) {
		RequirementVersion requirementVersion = null;
		// find Requirement
		Requirement requirement = findRequirement(row, summary, columnsMapping);
		if (requirement != null) {

			Double versionDoubleNumber = ExcelRowReaderUtils.readNumericField(row, columnsMapping, VERSION_TAG);
			if (versionDoubleNumber == null) {
				requirementVersion = requirement.getCurrentVersion();
			} else {
				int versionNumber = versionDoubleNumber.intValue();
				try {
					requirementVersion = requirement.findRequirementVersion(versionNumber);
				} catch (EntityNotFoundException e) {
					LOGGER.debug(e.getMessage());
					summary.addVersionNotFound(getRowLineNumber(row));
				}
			}
			if (requirementVersion != null) {
				if (!requirementVersion.isNotObsolete()) {
					requirementVersion = null;
					summary.addObsolete(getRowLineNumber(row));
				}
			}
		}
		return requirementVersion;
	}

	private int getRowLineNumber(Row row) {
		return row.getRowNum() + 1;
	}

	/**
	 * will try to find the requirement. If requirement is not found or the user has no "link" access to it, will return
	 * <code>null</code>;
	 * 
	 * @param row
	 * @param summary
	 * @param columnsMapping
	 * @return the requirement or <code>null</code> row parameters are not suitable (not found, no good access rights )
	 */
	private Requirement findRequirement(Row row, ImportRequirementTestCaseLinksSummaryImpl summary,
			Map<String, Integer> columnsMapping) {
		Double requirementDoubleId = ExcelRowReaderUtils.readNumericField(row, columnsMapping, ID_REQUIREMENT_TAG);
		long requirementId = requirementDoubleId.longValue();
		Requirement requirement = requirementDao.findById(requirementId);
		if (requirement != null) {
			if (permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "LINK", requirement)) {
				return requirement;
			} else {
				summary.addRequirementAccessRejected(getRowLineNumber(row));
			}
		} else {
			summary.addRequirementNotFound(getRowLineNumber(row));
		}

		return null;
	}

	private boolean validateRow(Row row, Map<String, Integer> columnsMapping) {
		boolean isValid = false;
		if (row != null) {
			Double requirementId = ExcelRowReaderUtils.readNumericField(row, columnsMapping, ID_REQUIREMENT_TAG);
			Double testCaseId = ExcelRowReaderUtils.readNumericField(row, columnsMapping, ID_TEST_CASE_TAG);
			if (requirementId != null && testCaseId != null) {
				isValid = true;
			}
		}
		return isValid;
	}

	@Override
	public void checkColumnsMapping(Map<String, Integer> columnsMapping, ImportRequirementTestCaseLinksSummaryImpl summary) {
		
		boolean badFormat = false;
		
		if (columnsMapping.get(ID_REQUIREMENT_TAG) == null) {
			badFormat = true;
			summary.addMissingColumnHeader(ID_REQUIREMENT_TAG);
		}
		if (columnsMapping.get(ID_TEST_CASE_TAG) == null) {
			badFormat = true;
			summary.addMissingColumnHeader(ID_TEST_CASE_TAG);
		}
		
		if (badFormat){
			throw new ColumnHeaderNotFoundException("The mandatory columns '" + ID_REQUIREMENT_TAG + "' or '"+ID_TEST_CASE_TAG+"' were not found");
		}

	}

}
