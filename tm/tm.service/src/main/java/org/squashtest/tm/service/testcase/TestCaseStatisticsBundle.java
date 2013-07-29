/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package org.squashtest.tm.service.testcase;

public class TestCaseStatisticsBundle {

	private TestCaseBoundRequirementsStatistics boundRequirementsStatistics;
	private TestCaseImportanceStatistics importanceStatistics;
	private TestCaseStatusesStatistics statusesStatistics;
	private TestCaseSizeStatistics sizeStatistics;
	
	
	public TestCaseBoundRequirementsStatistics getBoundRequirementsStatistics() {
		return boundRequirementsStatistics;
	}

	public void setBoundRequirementsStatistics(
			TestCaseBoundRequirementsStatistics boundRequirementsStatistics) {
		this.boundRequirementsStatistics = boundRequirementsStatistics;
	}

	public TestCaseImportanceStatistics getImportanceStatistics() {
		return importanceStatistics;
	}

	public void setImportanceStatistics(
			TestCaseImportanceStatistics importanceStatistics) {
		this.importanceStatistics = importanceStatistics;
	}

	public TestCaseStatusesStatistics getStatusesStatistics() {
		return statusesStatistics;
	}

	public void setStatusesStatistics(TestCaseStatusesStatistics statusesStatistics) {
		this.statusesStatistics = statusesStatistics;
	}

	public TestCaseSizeStatistics getSizeStatistics() {
		return sizeStatistics;
	}

	public void setSizeStatistics(TestCaseSizeStatistics sizeStatistics) {
		this.sizeStatistics = sizeStatistics;
	}

	public TestCaseStatisticsBundle(
			TestCaseBoundRequirementsStatistics boundRequirementsStatistics,
			TestCaseImportanceStatistics importanceStatistics,
			TestCaseStatusesStatistics statusesStatistics,
			TestCaseSizeStatistics sizeStatistics) {
		super();
		this.boundRequirementsStatistics = boundRequirementsStatistics;
		this.importanceStatistics = importanceStatistics;
		this.statusesStatistics = statusesStatistics;
		this.sizeStatistics = sizeStatistics;
	}
	
	public TestCaseStatisticsBundle() {
		super();
	}
	
	
	
	
	
	
	// ************************* inner yet public classes *****************************
	
	
	public static final class TestCaseBoundRequirementsStatistics{
		
		private int zeroRequirements = 0;
		private int havingRequirements = 0;
		
		
		public int getZeroRequirements() {
			return zeroRequirements;
		}
		
		public int getHavingRequirements() {
			return havingRequirements;
		}
		
		public void setZeroRequirements(int zeroRequirements) {
			this.zeroRequirements = zeroRequirements;
		}

		public void setHavingRequirements(int havingRequirements) {
			this.havingRequirements = havingRequirements;
		}

		public TestCaseBoundRequirementsStatistics(int zeroRequirements,
				int havingRequirements) {
			super();
			this.zeroRequirements = zeroRequirements;
			this.havingRequirements = havingRequirements;
		}
		
		public TestCaseBoundRequirementsStatistics(){
			super();
		}
		
	}
	
	
	public static final class TestCaseImportanceStatistics {
		private int veryHigh = 0;
		private int high = 0;
		private int medium = 0;
		private int low = 0;
		
		public int getVeryHigh() {
			return veryHigh;
		}
		
		public void setVeryHigh(int veryHigh) {
			this.veryHigh = veryHigh;
		}
		
		public int getHigh() {
			return high;
		}
		
		public void setHigh(int high) {
			this.high = high;
		}
		
		public int getMedium() {
			return medium;
		}
		
		public void setMedium(int medium) {
			this.medium = medium;
		}
		
		public int getLow() {
			return low;
		}
		
		public void setLow(int low) {
			this.low = low;
		}

		public TestCaseImportanceStatistics(int veryHigh, int high, int medium,
				int low) {
			super();
			this.veryHigh = veryHigh;
			this.high = high;
			this.medium = medium;
			this.low = low;
		}
		
		public TestCaseImportanceStatistics() {
			super();
		}
		
	}
	
	
	public static class TestCaseSizeStatistics {

		private int zeroSteps = 0;
		private int between0And10Steps = 0;
		private int between11And20Steps = 0;
		private int above20Steps = 0;
		
		public int getZeroSteps() {
			return zeroSteps;
		}
		
		public void setZeroSteps(int zeroSteps) {
			this.zeroSteps = zeroSteps;
		}
		
		public int getBetween0And10Steps() {
			return between0And10Steps;
		}
		
		public void setBetween0And10Steps(int between0And10Steps) {
			this.between0And10Steps = between0And10Steps;
		}
		
		public int getBetween11And20Steps() {
			return between11And20Steps;
		}
		
		public void setBetween11And20Steps(int between11And20Steps) {
			this.between11And20Steps = between11And20Steps;
		}
		
		public int getAbove20Steps() {
			return above20Steps;
		}
		
		public void setAbove20Steps(int above20Steps) {
			this.above20Steps = above20Steps;
		}
		
	}
	
	
	public static final class TestCaseStatusesStatistics{
		
		private int workInProgress;
		private int underReview;
		private int approved;
		private int obsolete;
		private int toBeUpdated;
		
		
		public int getWorkInProgress() {
			return workInProgress;
		}
		
		public void setWorkInProgress(int workInProgress) {
			this.workInProgress = workInProgress;
		}
		
		public int getUnderReview() {
			return underReview;
		}
		
		public void setUnderReview(int underReview) {
			this.underReview = underReview;
		}
		
		public int getApproved() {
			return approved;
		}
		
		public void setApproved(int approved) {
			this.approved = approved;
		}
		
		public int getObsolete() {
			return obsolete;
		}
		
		public void setObsolete(int obsolete) {
			this.obsolete = obsolete;
		}
		
		public int getToBeUpdated() {
			return toBeUpdated;
		}
		
		public void setToBeUpdated(int toBeUpdated) {
			this.toBeUpdated = toBeUpdated;
		}
		
			
		public TestCaseStatusesStatistics() {
			super();
		}

		public TestCaseStatusesStatistics(int workInProgress, int underReview,
				int approved, int obsolete, int toBeUpdated) {
			super();
			this.workInProgress = workInProgress;
			this.underReview = underReview;
			this.approved = approved;
			this.obsolete = obsolete;
			this.toBeUpdated = toBeUpdated;
		}

	}
	
}
