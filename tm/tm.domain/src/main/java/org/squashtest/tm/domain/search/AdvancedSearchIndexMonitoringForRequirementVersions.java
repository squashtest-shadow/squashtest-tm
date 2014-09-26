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
package org.squashtest.tm.domain.search;

public final class AdvancedSearchIndexMonitoringForRequirementVersions {

	private static boolean isIndexingOver = false;
	private static long documentsAdded = 0L;
	private static long addToTotalCount = 0L;
	private static int documentsBuilt = 0;
	private static int entitiesLoaded = 0;
	private static double progressPercentage = 0;

	private AdvancedSearchIndexMonitoringForRequirementVersions() {
		super();
	}

	public static void reset() {
		AdvancedSearchIndexMonitoringForRequirementVersions.isIndexingOver = false;
		AdvancedSearchIndexMonitoringForRequirementVersions.documentsAdded = 0L;
		AdvancedSearchIndexMonitoringForRequirementVersions.addToTotalCount = 0L;
		AdvancedSearchIndexMonitoringForRequirementVersions.documentsBuilt = 0;
		AdvancedSearchIndexMonitoringForRequirementVersions.entitiesLoaded = 0;
		AdvancedSearchIndexMonitoringForRequirementVersions.setProgressPercentage(0);
	}

	public static boolean isIndexingOver() {
		return isIndexingOver;
	}

	public static void setIndexingOver(boolean isIndexingOver) {
		AdvancedSearchIndexMonitoringForRequirementVersions.isIndexingOver = isIndexingOver;
	}

	public static long getDocumentsAdded() {
		if(isIndexingOver || documentsAdded > addToTotalCount){
			return addToTotalCount;
		} else {
			return documentsAdded;
		}
	}

	public static void setDocumentsAdded(long documentsAdded) {
		AdvancedSearchIndexMonitoringForRequirementVersions.documentsAdded += documentsAdded;
	}

	public static long getAddToTotalCount() {
		return addToTotalCount;
	}

	public static void setAddToTotalCount(long addToTotalCount) {
		AdvancedSearchIndexMonitoringForRequirementVersions.addToTotalCount += addToTotalCount;
	}

	public static int getDocumentsBuilt() {
		if(isIndexingOver || documentsBuilt > entitiesLoaded){
			return entitiesLoaded;
		} else {
			return documentsBuilt;
		}
	}

	public static void setDocumentsBuilt(int documentsBuilt) {
		AdvancedSearchIndexMonitoringForRequirementVersions.documentsBuilt += documentsBuilt;
	}

	public static int getEntitiesLoaded() {
		return entitiesLoaded;
	}

	public static void setEntitiesLoaded(int entitiesLoaded) {
		AdvancedSearchIndexMonitoringForRequirementVersions.entitiesLoaded += entitiesLoaded;
	}

	public static double getProgressPercentage() {
		AdvancedSearchIndexMonitoringForRequirementVersions.progressPercentage = Math.round(((double) AdvancedSearchIndexMonitoringForRequirementVersions.documentsBuilt
				*100.0 / (double) AdvancedSearchIndexMonitoringForRequirementVersions.addToTotalCount))/100.0;
		if(isIndexingOver){
			return 1;
		} else {
			return AdvancedSearchIndexMonitoringForRequirementVersions.progressPercentage;
		}
	}

	public static void setProgressPercentage(float progressPercentage) {
		AdvancedSearchIndexMonitoringForRequirementVersions.progressPercentage = progressPercentage;
	}
}
