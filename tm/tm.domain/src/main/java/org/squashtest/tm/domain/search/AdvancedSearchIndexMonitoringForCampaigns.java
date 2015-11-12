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
package org.squashtest.tm.domain.search;

public final class AdvancedSearchIndexMonitoringForCampaigns {

	private static boolean isIndexingOver = false;
	private static long documentsAdded = 0L;
	private static long addToTotalCount = 0L;
	private static int documentsBuilt = 0;
	private static int entitiesLoaded = 0;
	private static double progressPercentage = 0;

	private AdvancedSearchIndexMonitoringForCampaigns() {
		super();
	}

	public static void reset() {
		AdvancedSearchIndexMonitoringForCampaigns.isIndexingOver = false;
		AdvancedSearchIndexMonitoringForCampaigns.documentsAdded = 0L;
		AdvancedSearchIndexMonitoringForCampaigns.addToTotalCount = 0L;
		AdvancedSearchIndexMonitoringForCampaigns.documentsBuilt = 0;
		AdvancedSearchIndexMonitoringForCampaigns.entitiesLoaded = 0;
		AdvancedSearchIndexMonitoring.setProgressPercentage(0);
	}

	public static boolean isIndexingOver() {
		return isIndexingOver;
	}

	public static void setIndexingOver(boolean isIndexingOver) {
		AdvancedSearchIndexMonitoringForCampaigns.isIndexingOver = isIndexingOver;
	}

	public static long getDocumentsAdded() {
		if(isIndexingOver || documentsAdded > addToTotalCount){
			return addToTotalCount;
		} else {
			return documentsAdded;
		}
	}

	public static void setDocumentsAdded(long documentsAdded) {
		AdvancedSearchIndexMonitoringForCampaigns.documentsAdded += documentsAdded;
	}

	public static long getAddToTotalCount() {
		return addToTotalCount;
	}

	public static void setAddToTotalCount(long addToTotalCount) {
		AdvancedSearchIndexMonitoringForCampaigns.addToTotalCount += addToTotalCount;
	}

	public static int getDocumentsBuilt() {
		if(isIndexingOver || documentsBuilt > entitiesLoaded){
			return entitiesLoaded;
		} else {
			return documentsBuilt;
		}
	}

	public static void setDocumentsBuilt(int documentsBuilt) {
		AdvancedSearchIndexMonitoringForCampaigns.documentsBuilt += documentsBuilt;
	}

	public static int getEntitiesLoaded() {
		return entitiesLoaded;
	}

	public static void setEntitiesLoaded(int entitiesLoaded) {
		AdvancedSearchIndexMonitoringForCampaigns.entitiesLoaded += entitiesLoaded;
	}

	public static double getProgressPercentage() {
		AdvancedSearchIndexMonitoringForCampaigns.progressPercentage = Math.round(((double) AdvancedSearchIndexMonitoringForCampaigns.documentsBuilt
				*100.0 / (double) AdvancedSearchIndexMonitoringForCampaigns.addToTotalCount))/100.0;
		if(isIndexingOver){
			return 1;
		} else {
			return AdvancedSearchIndexMonitoringForCampaigns.progressPercentage;
		}
	}

	public static void setProgressPercentage(float progressPercentage) {
		AdvancedSearchIndexMonitoringForCampaigns.progressPercentage = progressPercentage;
	}
}
