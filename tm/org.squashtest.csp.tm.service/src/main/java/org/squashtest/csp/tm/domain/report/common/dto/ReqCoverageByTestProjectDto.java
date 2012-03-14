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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.squashtest.csp.tm.domain.requirement.RequirementStatus;

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

	private Map<ReqCoverageByTestStatType, Long> requirementNumbers = new HashMap<ReqCoverageByTestStatType, Long>();
	private Map<String, Long> requirementStatusNumbers = new HashMap<String, Long>();

	public ReqCoverageByTestProjectDto() {
		for (ReqCoverageByTestStatType reqStatType : ReqCoverageByTestStatType.values()) {
			requirementNumbers.put(reqStatType, 0L);
		}
		for (RequirementStatus status : RequirementStatus.values()) {
			for (ReqCoverageByTestStatType reqStatType : ReqCoverageByTestStatType.values()) {
				String key = status.toString() + reqStatType.toString();
				requirementStatusNumbers.put(key, 0L);
			}
		}
	}

	/* RATES */
	private Byte globalRequirementCoverage;

	// by criticality
	private Byte criticalRequirementCoverage;

	private Byte majorRequirementCoverage;

	private Byte minorRequirementCoverage;

	private Byte undefinedRequirementCoverage;

	/**
	 * Increments the number identified by the ReqCoverageByTestStatType
	 * 
	 * @param type
	 *            identify the reqNumber type to increment
	 */
	public void incrementReqNumber(ReqCoverageByTestStatType type) {
		Long number = requirementNumbers.get(type);
		number++;
		requirementNumbers.put(type, number);
	}

	/**
	 * Increments the number identified by the String status+reqCoverageByTestStartType
	 * 
	 * @param key
	 *            the String status+reqCoverageByTestStartType
	 */
	public void incrementReqStatusNumber(String key) {
		Long number = requirementStatusNumbers.get(key);
		number++;
		requirementStatusNumbers.put(key, number);
	}

	/**
	 * 
	 * Method which add the given values to the totals
	 * 
	 * @param requirementNumbers
	 *            of the project that will increase the projectTotals numbers
	 */
	public void increaseTotals(Map<ReqCoverageByTestStatType, Long> requirementNumbers2) {
		for (Entry<ReqCoverageByTestStatType, Long> parameterEntry : requirementNumbers2.entrySet()) {
			ReqCoverageByTestStatType concernedType = parameterEntry.getKey();
			Long reqNumber = this.requirementNumbers.get(concernedType);
			reqNumber += parameterEntry.getValue();
			this.requirementNumbers.put(concernedType, reqNumber);
		}

	}

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
		return requirementNumbers.get(ReqCoverageByTestStatType.TOTAL);
	}

	public Long getTotalVerifiedRequirementNumber() {
		return requirementNumbers.get(ReqCoverageByTestStatType.TOTAL_VERIFIED);
	}

	public Long getCriticalRequirementNumber() {
		return requirementNumbers.get(ReqCoverageByTestStatType.CRITICAL);
	}

	public Long getCriticalVerifiedRequirementNumber() {
		return requirementNumbers.get(ReqCoverageByTestStatType.CRITICAL_VERIFIED);
	}

	public Long getMajorRequirementNumber() {
		return requirementNumbers.get(ReqCoverageByTestStatType.MAJOR);
	}

	public Long getMajorVerifiedRequirementNumber() {
		return requirementNumbers.get(ReqCoverageByTestStatType.MAJOR_VERIFIED);
	}

	public Long getMinorRequirementNumber() {
		return requirementNumbers.get(ReqCoverageByTestStatType.MINOR);
	}

	public Long getMinorVerifiedRequirementNumber() {
		return requirementNumbers.get(ReqCoverageByTestStatType.MINOR_VERIFIED);
	}

	public Long getUndefinedRequirementNumber() {
		return requirementNumbers.get(ReqCoverageByTestStatType.UNDEFINED);
	}

	public Long getUndefinedVerifiedRequirementNumber() {
		return requirementNumbers.get(ReqCoverageByTestStatType.UNDEFINED_VERIFIED);
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

	public Map<ReqCoverageByTestStatType, Long> getRequirementNumbers() {
		return requirementNumbers;
	}

	public void setRequirementNumbers(Map<ReqCoverageByTestStatType, Long> requirementNumbers) {
		this.requirementNumbers = requirementNumbers;
	}

	public Long getWorkInProgressTotalRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.WORK_IN_PROGRESS.toString()
				+ ReqCoverageByTestStatType.TOTAL.toString());
	}

	public Long getWorkInProgressTotalVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.WORK_IN_PROGRESS.toString()
				+ ReqCoverageByTestStatType.TOTAL_VERIFIED.toString());
	}

	public Long getWorkInProgressCriticalRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.WORK_IN_PROGRESS.toString()
				+ ReqCoverageByTestStatType.CRITICAL.toString());
	}

	public Long getWorkInProgressCriticalVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.WORK_IN_PROGRESS.toString()
				+ ReqCoverageByTestStatType.CRITICAL_VERIFIED.toString());
	}

	public Long getWorkInProgressMajorRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.WORK_IN_PROGRESS.toString()
				+ ReqCoverageByTestStatType.MAJOR.toString());
	}

	public Long getWorkInProgressMajorVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.WORK_IN_PROGRESS.toString()
				+ ReqCoverageByTestStatType.MAJOR_VERIFIED.toString());
	}

	public Long getWorkInProgressMinorRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.WORK_IN_PROGRESS.toString()
				+ ReqCoverageByTestStatType.MINOR.toString());
	}

	public Long getWorkInProgressMinorVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.WORK_IN_PROGRESS.toString()
				+ ReqCoverageByTestStatType.MINOR_VERIFIED.toString());
	}

	public Long getWorkInProgressUndefinedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.WORK_IN_PROGRESS.toString()
				+ ReqCoverageByTestStatType.UNDEFINED.toString());
	}

	public Long getWorkInProgressUndefinedVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.WORK_IN_PROGRESS.toString()
				+ ReqCoverageByTestStatType.UNDEFINED_VERIFIED.toString());
	}

	public Long getUnderReviewTotalRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.UNDER_REVIEW.toString()
				+ ReqCoverageByTestStatType.TOTAL.toString());
	}

	public Long getUnderReviewTotalVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.UNDER_REVIEW.toString()
				+ ReqCoverageByTestStatType.TOTAL_VERIFIED.toString());
	}

	public Long getUnderReviewCriticalRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.UNDER_REVIEW.toString()
				+ ReqCoverageByTestStatType.CRITICAL.toString());
	}

	public Long getUnderReviewCriticalVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.UNDER_REVIEW.toString()
				+ ReqCoverageByTestStatType.CRITICAL_VERIFIED.toString());
	}

	public Long getUnderReviewMajorRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.UNDER_REVIEW.toString()
				+ ReqCoverageByTestStatType.MAJOR.toString());
	}

	public Long getUnderReviewMajorVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.UNDER_REVIEW.toString()
				+ ReqCoverageByTestStatType.MAJOR_VERIFIED.toString());
	}

	public Long getUnderReviewMinorRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.UNDER_REVIEW.toString()
				+ ReqCoverageByTestStatType.MINOR.toString());
	}

	public Long getUnderReviewMinorVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.UNDER_REVIEW.toString()
				+ ReqCoverageByTestStatType.MINOR_VERIFIED.toString());
	}

	public Long getUnderReviewUndefinedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.UNDER_REVIEW.toString()
				+ ReqCoverageByTestStatType.UNDEFINED.toString());
	}

	public Long getUnderReviewUndefinedVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.UNDER_REVIEW.toString()
				+ ReqCoverageByTestStatType.UNDEFINED_VERIFIED.toString());
	}

	public Long getApprovedTotalRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.APPROVED.toString()
				+ ReqCoverageByTestStatType.TOTAL.toString());
	}

	public Long getApprovedTotalVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.APPROVED.toString()
				+ ReqCoverageByTestStatType.TOTAL_VERIFIED.toString());
	}

	public Long getApprovedCriticalRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.APPROVED.toString()
				+ ReqCoverageByTestStatType.CRITICAL.toString());
	}

	public Long getApprovedCriticalVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.APPROVED.toString()
				+ ReqCoverageByTestStatType.CRITICAL_VERIFIED.toString());
	}

	public Long getApprovedMajorRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.APPROVED.toString()
				+ ReqCoverageByTestStatType.MAJOR.toString());
	}

	public Long getApprovedMajorVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.APPROVED.toString()
				+ ReqCoverageByTestStatType.MAJOR_VERIFIED.toString());
	}

	public Long getApprovedMinorRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.APPROVED.toString()
				+ ReqCoverageByTestStatType.MINOR.toString());
	}

	public Long getApprovedMinorVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.APPROVED.toString()
				+ ReqCoverageByTestStatType.MINOR_VERIFIED.toString());
	}

	public Long getApprovedUndefinedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.APPROVED.toString()
				+ ReqCoverageByTestStatType.UNDEFINED.toString());
	}

	public Long getApprovedUndefinedVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.APPROVED.toString()
				+ ReqCoverageByTestStatType.UNDEFINED_VERIFIED.toString());
	}

	public Long getObsoleteTotalRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.OBSOLETE.toString()
				+ ReqCoverageByTestStatType.TOTAL.toString());
	}

	public Long getObsoleteTotalVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.OBSOLETE.toString()
				+ ReqCoverageByTestStatType.TOTAL_VERIFIED.toString());
	}

	public Long getObsoleteCriticalRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.OBSOLETE.toString()
				+ ReqCoverageByTestStatType.CRITICAL.toString());
	}

	public Long getObsoleteCriticalVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.OBSOLETE.toString()
				+ ReqCoverageByTestStatType.CRITICAL_VERIFIED.toString());
	}

	public Long getObsoleteMajorRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.OBSOLETE.toString()
				+ ReqCoverageByTestStatType.MAJOR.toString());
	}

	public Long getObsoleteMajorVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.OBSOLETE.toString()
				+ ReqCoverageByTestStatType.MAJOR_VERIFIED.toString());
	}

	public Long getObsoleteMinorRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.OBSOLETE.toString()
				+ ReqCoverageByTestStatType.MINOR.toString());
	}

	public Long getObsoleteMinorVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.OBSOLETE.toString()
				+ ReqCoverageByTestStatType.MINOR_VERIFIED.toString());
	}

	public Long getObsoleteUndefinedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.OBSOLETE.toString()
				+ ReqCoverageByTestStatType.UNDEFINED.toString());
	}

	public Long getObsoleteUndefinedVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.OBSOLETE.toString()
				+ ReqCoverageByTestStatType.UNDEFINED_VERIFIED.toString());
	}

}
