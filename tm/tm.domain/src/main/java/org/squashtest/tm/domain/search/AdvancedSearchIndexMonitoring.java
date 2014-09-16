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
package org.squashtest.tm.domain.search;

public final class AdvancedSearchIndexMonitoring {

	private static boolean isIndexingOver = false;
	private static long documentsAdded = 0L;
	private static long addToTotalCount = 0L;
	private static int documentsBuilt = 0;
	private static int entitiesLoaded = 0;
	private static double progressPercentage = 0;

	private AdvancedSearchIndexMonitoring() {
		super();
	}

	public static void reset() {
		AdvancedSearchIndexMonitoring.isIndexingOver = false;
		AdvancedSearchIndexMonitoring.documentsAdded = 0L;
		AdvancedSearchIndexMonitoring.addToTotalCount = 0L;
		AdvancedSearchIndexMonitoring.documentsBuilt = 0;
		AdvancedSearchIndexMonitoring.entitiesLoaded = 0;
		AdvancedSearchIndexMonitoring.setProgressPercentage(0);
	}

	public static boolean isIndexingOver() {
		return isIndexingOver;
	}

	public static void setIndexingOver(boolean isIndexingOver) {
		AdvancedSearchIndexMonitoring.isIndexingOver = isIndexingOver;
	}

	public static long getDocumentsAdded() {
		if(isIndexingOver || documentsAdded > addToTotalCount){
			return addToTotalCount;
		} else {
			return documentsAdded;
		}
	}

	public static void setDocumentsAdded(long documentsAdded) {
		AdvancedSearchIndexMonitoring.documentsAdded += documentsAdded;
	}

	public static long getAddToTotalCount() {
		return addToTotalCount;
	}

	public static void setAddToTotalCount(long addToTotalCount) {
		AdvancedSearchIndexMonitoring.addToTotalCount += addToTotalCount;
	}

	public static int getDocumentsBuilt() {
		if(isIndexingOver || documentsBuilt > entitiesLoaded){
			return entitiesLoaded;
		} else {
			return documentsBuilt;
		}
	}

	public static void setDocumentsBuilt(int documentsBuilt) {
		AdvancedSearchIndexMonitoring.documentsBuilt += documentsBuilt;
	}

	public static int getEntitiesLoaded() {
		return entitiesLoaded;
	}

	public static void setEntitiesLoaded(int entitiesLoaded) {
		AdvancedSearchIndexMonitoring.entitiesLoaded += entitiesLoaded;
	}

	public static double getProgressPercentage() {
		AdvancedSearchIndexMonitoring.progressPercentage = Math.round(((double) AdvancedSearchIndexMonitoring.documentsBuilt
				*100.0 / (double) AdvancedSearchIndexMonitoring.addToTotalCount))/100.0;	
		if(isIndexingOver){
			return 1;
		} else {
			return AdvancedSearchIndexMonitoring.progressPercentage;
		}
	}

	public static void setProgressPercentage(float progressPercentage) {
		AdvancedSearchIndexMonitoring.progressPercentage = progressPercentage;
	}
}
