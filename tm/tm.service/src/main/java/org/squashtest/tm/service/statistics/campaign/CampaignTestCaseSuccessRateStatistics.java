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
package org.squashtest.tm.service.statistics.campaign;

public class CampaignTestCaseSuccessRateStatistics {

	private int successPercentageVeryHigh;
	private int successPercentageHigh;
	private int successPercentageMedium;
	private int successPercentageLow;
	private int globalSuccessPercentage;
	
	public int getSuccessPercentageVeryHigh() {
		return successPercentageVeryHigh;
	}
	public void setSuccessPercentageVeryHigh(int successPercentageVeryHigh) {
		this.successPercentageVeryHigh = successPercentageVeryHigh;
	}
	public int getSuccessPercentageHigh() {
		return successPercentageHigh;
	}
	public void setSuccessPercentageHigh(int successPercentageHigh) {
		this.successPercentageHigh = successPercentageHigh;
	}
	public int getSuccessPercentageMedium() {
		return successPercentageMedium;
	}
	public void setSuccessPercentageMedium(int successPercentageMedium) {
		this.successPercentageMedium = successPercentageMedium;
	}
	public int getSuccessPercentageLow() {
		return successPercentageLow;
	}
	public void setSuccessPercentageLow(int successPercentageLow) {
		this.successPercentageLow = successPercentageLow;
	}
	public int getGlobalSuccessPercentage() {
		return globalSuccessPercentage;
	}
	public void setGlobalSuccessPercentage(int globalSuccessPercentage) {
		this.globalSuccessPercentage = globalSuccessPercentage;
	}
}
