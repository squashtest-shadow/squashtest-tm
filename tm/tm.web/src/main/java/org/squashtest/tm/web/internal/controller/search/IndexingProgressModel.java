/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.search;

public class IndexingProgressModel {

	private int progressPercentage;
	private long writtenEntities;
	private long totalEntities;

	private int progressPercentageForTestcases;
	private long writtenEntitiesForTestcases;
	private long totalEntitiesForTestcases;
	
	private int progressPercentageForRequirementVersions;
	private long writtenEntitiesForRequirementVersions;
	private long totalEntitiesForRequirementVersions;

	private double progressPercentageForCampaigns;
	private long writtenEntitiesForCampaigns;
	private long totalEntitiesForCampaigns;
	
	public int getProgressPercentage() {
		return progressPercentage;
	}
	public void setProgressPercentage(int progressPercentage) {
		this.progressPercentage = progressPercentage;
	}
	public long getWrittenEntities() {
		return writtenEntities;
	}
	public void setWrittenEntities(long writtenEntities) {
		this.writtenEntities = writtenEntities;
	}
	public long getTotalEntities() {
		return totalEntities;
	}
	public void setTotalEntities(long totalEntities) {
		this.totalEntities = totalEntities;
	}
	public int getProgressPercentageForTestcases() {
		return progressPercentageForTestcases;
	}
	public void setProgressPercentageForTestcases(
			int progressPercentageForTestcases) {
		this.progressPercentageForTestcases = progressPercentageForTestcases;
	}
	public long getWrittenEntitiesForTestcases() {
		return writtenEntitiesForTestcases;
	}
	public void setWrittenEntitiesForTestcases(long writtenEntitiesForTestcases) {
		this.writtenEntitiesForTestcases = writtenEntitiesForTestcases;
	}
	public long getTotalEntitiesForTestcases() {
		return totalEntitiesForTestcases;
	}
	public void setTotalEntitiesForTestcases(long totalEntitiesForTestcases) {
		this.totalEntitiesForTestcases = totalEntitiesForTestcases;
	}
	public int getProgressPercentageForRequirementVersions() {
		return progressPercentageForRequirementVersions;
	}
	public void setProgressPercentageForRequirementVersions(
			int progressPercentageForRequirementVersions) {
		this.progressPercentageForRequirementVersions = progressPercentageForRequirementVersions;
	}
	public long getWrittenEntitiesForRequirementVersions() {
		return writtenEntitiesForRequirementVersions;
	}
	public void setWrittenEntitiesForRequirementVersions(
			long writtenEntitiesForRequirementVersions) {
		this.writtenEntitiesForRequirementVersions = writtenEntitiesForRequirementVersions;
	}
	public long getTotalEntitiesForRequirementVersions() {
		return totalEntitiesForRequirementVersions;
	}
	public void setTotalEntitiesForRequirementVersions(
			long totalEntitiesForRequirementVersions) {
		this.totalEntitiesForRequirementVersions = totalEntitiesForRequirementVersions;
	}

	public double getProgressPercentageForCampaigns() {
		return progressPercentageForCampaigns;
	}

	public void setProgressPercentageForCampaigns(double progressPercentageForCampaigns) {
		this.progressPercentageForCampaigns = progressPercentageForCampaigns;
	}

	public long getWrittenEntitiesForCampaigns() {
		return writtenEntitiesForCampaigns;
	}

	public void setWrittenEntitiesForCampaigns(long writtenEntitiesForCampaigns) {
		this.writtenEntitiesForCampaigns = writtenEntitiesForCampaigns;
	}

	public long getTotalEntitiesForCampaigns() {
		return totalEntitiesForCampaigns;
	}

	public void setTotalEntitiesForCampaigns(long totalEntitiesForCampaigns) {
		this.totalEntitiesForCampaigns = totalEntitiesForCampaigns;
	}

}
