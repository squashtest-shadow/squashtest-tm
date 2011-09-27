/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.domain.report.common.dto;

import java.util.ArrayList;
import java.util.List;

public class ReqCoverageByTestProjectDto {

	/***
	 * Name of the project
	 */
	private String projectName;

	/***
	 * List of all requirement
	 */
	private List<ReqCoverageByTestRequirementSingleDto> singleRequirementList = new ArrayList<ReqCoverageByTestRequirementSingleDto>();

	/****************************/
	/** STATISTICS **/
	/****************************/

	/* TOTALS */
	private Long totalRequirementNumber = 0L;
	private Long totalVerifiedRequirementNumber = 0L;

	// by criticality
	private Long criticalRequirementNumber = 0L;
	private Long criticalVerifiedRequirementNumber = 0L;

	private Long majorRequirementNumber = 0L;
	private Long majorVerifiedRequirementNumber = 0L;

	private Long minorRequirementNumber = 0L;
	private Long minorVerifiedRequirementNumber = 0L;

	private Long undefinedRequirementNumber = 0L;
	private Long undefinedVerifiedRequirementNumber = 0L;

	/* RATES */
	private Byte globalRequirementCoverage;

	// by criticality
	private Byte criticalRequirementCoverage;

	private Byte majorRequirementCoverage;

	private Byte minorRequirementCoverage;

	private Byte undefinedRequirementCoverage;

	/* ACCESSORS */

	public List<ReqCoverageByTestRequirementSingleDto> getSingleRequirementList() {
		return singleRequirementList;
	}

	public void addRequirement(ReqCoverageByTestRequirementSingleDto requirementSingleDto) {
		this.singleRequirementList.add(requirementSingleDto);
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getProjectName() {
		return projectName;
	}

	public Long getTotalRequirementNumber() {
		return totalRequirementNumber;
	}

	public Long getTotalVerifiedRequirementNumber() {
		return totalVerifiedRequirementNumber;
	}

	public Long getCriticalRequirementNumber() {
		return criticalRequirementNumber;
	}

	public Long getCriticalVerifiedRequirementNumber() {
		return criticalVerifiedRequirementNumber;
	}

	public Long getMajorRequirementNumber() {
		return majorRequirementNumber;
	}

	public Long getMajorVerifiedRequirementNumber() {
		return majorVerifiedRequirementNumber;
	}

	public Long getMinorRequirementNumber() {
		return minorRequirementNumber;
	}

	public Long getMinorVerifiedRequirementNumber() {
		return minorVerifiedRequirementNumber;
	}

	public Long getUndefinedRequirementNumber() {
		return undefinedRequirementNumber;
	}

	public Long getUndefinedVerifiedRequirementNumber() {
		return undefinedVerifiedRequirementNumber;
	}

	public Byte getGlobalRequirementCoverage() {
		return globalRequirementCoverage;
	}

	public Byte getCriticalRequirementCoverage() {
		return criticalRequirementCoverage;
	}

	public Byte getMajorRequirementCoverage() {
		return majorRequirementCoverage;
	}

	public Byte getMinorRequirementCoverage() {
		return minorRequirementCoverage;
	}

	public Byte getUndefinedRequirementCoverage() {
		return undefinedRequirementCoverage;
	}

	public void setGlobalRequirementCoverage(Byte globalRequirementCoverage) {
		this.globalRequirementCoverage = globalRequirementCoverage;
	}

	public void setCriticalRequirementCoverage(Byte criticalRequirementCoverage) {
		this.criticalRequirementCoverage = criticalRequirementCoverage;
	}

	public void setMajorRequirementCoverage(Byte majorRequirementCoverage) {
		this.majorRequirementCoverage = majorRequirementCoverage;
	}

	public void setMinorRequirementCoverage(Byte minorRequirementCoverage) {
		this.minorRequirementCoverage = minorRequirementCoverage;
	}

	public void setUndefinedRequirementCoverage(Byte undefinedRequirementCoverage) {
		this.undefinedRequirementCoverage = undefinedRequirementCoverage;
	}

	/**
	 * Increments the number identified by the ReqCoverageByTestStatType
	 *
	 * @param type
	 *            identify the number to increment
	 */
	public void incrementNumber(ReqCoverageByTestStatType type) {
		// One method instead of ten which does the exact same thing
		switch (type) {
		case TOTAL:
			this.totalRequirementNumber++;
			break;
		case TOTAL_VERIFIED:
			this.totalVerifiedRequirementNumber++;
			break;
		case CRITICAL:
			this.criticalRequirementNumber++;
			break;
		case CRITICAL_VERIFIED:
			this.criticalVerifiedRequirementNumber++;
			break;
		case MAJOR:
			this.majorRequirementNumber++;
			break;
		case MAJOR_VERIFIED:
			this.majorVerifiedRequirementNumber++;
			break;
		case MINOR:
			this.minorRequirementNumber++;
			break;
		case MINOR_VERIFIED:
			this.minorVerifiedRequirementNumber++;
			break;
		case UNDEFINED:
			this.undefinedRequirementNumber++;
			break;
		case UNDEFINED_VERIFIED:
			this.undefinedVerifiedRequirementNumber++;
			break;
		default:
			// useless as we choose the type
			break;
		}
	}

	/***
	 * Method which add the given values to the totals
	 *
	 * @param totalRequirementNumber
	 *            total number of requirements
	 * @param totalVerifiedRequirementNumber
	 *            total number of verified requirements
	 * @param criticalRequirementNumber
	 *            total number of critical requirements
	 * @param criticalVerifiedRequirementNumber
	 *            total number of critical and verified requirements
	 * @param majorRequirementNumber
	 *            total number of major requirements
	 * @param majorVerifiedRequirementNumber
	 *            total number of major and verified requirements
	 * @param minorRequirementNumber
	 *            total number of minor requirements
	 * @param minorVerifiedRequirementNumber
	 *            total number of minor and verified requirements
	 * @param undefinedRequirementNumber
	 *            total number of undefined requirements
	 * @param undefinedVerifiedRequirementNumber
	 *            total number of undefined and verified requirements
	 */
	public void increaseTotals(Long totalRequirementNumber, Long totalVerifiedRequirementNumber,
			Long criticalRequirementNumber, Long criticalVerifiedRequirementNumber, Long majorRequirementNumber,
			Long majorVerifiedRequirementNumber, Long minorRequirementNumber, Long minorVerifiedRequirementNumber,
			Long undefinedRequirementNumber, Long undefinedVerifiedRequirementNumber) {
		this.totalRequirementNumber += totalRequirementNumber;
		this.totalVerifiedRequirementNumber += totalVerifiedRequirementNumber;
		this.criticalRequirementNumber += criticalRequirementNumber;
		this.criticalVerifiedRequirementNumber += criticalVerifiedRequirementNumber;
		this.majorRequirementNumber += majorRequirementNumber;
		this.majorVerifiedRequirementNumber += majorVerifiedRequirementNumber;
		this.minorRequirementNumber += minorRequirementNumber;
		this.minorVerifiedRequirementNumber += minorVerifiedRequirementNumber;
		this.undefinedRequirementNumber += undefinedRequirementNumber;
		this.undefinedVerifiedRequirementNumber += undefinedVerifiedRequirementNumber;

	}

}
