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
package org.squashtest.tm.service.statistics.campaign;

public final class IterationTestInventoryStatistics{
	
	private String iterationName;
	private int nbReady;
	private int nbRunning;
	private int nbSuccess;
	private int nbFailure;
	private int nbBlocked;
	private int nbUntestable;
	private int nbWarning;
	private int nbError;
	
	public String getIterationName() {
		return iterationName;
	}
	
	public void setIterationName(String iterationName) {
		this.iterationName = iterationName;
	}
	
	public int getNbReady() {
		return nbReady;
	}
	
	public void setNbReady(int nbReady) {
		this.nbReady = nbReady;
	}
	
	public int getNbRunning() {
		return nbRunning;
	}
	
	public void setNbRunning(int nbRunning) {
		this.nbRunning = nbRunning;
	}
	
	public int getNbSuccess() {
		return nbSuccess;
	}
	
	public void setNbSuccess(int nbSuccess) {
		this.nbSuccess = nbSuccess;
	}
	public int getNbFailure() {
		return nbFailure;
	}
	
	public void setNbFailure(int nbFailure) {
		this.nbFailure = nbFailure;
	}
	
	public int getNbBlocked() {
		return nbBlocked;
	}
	public void setNbBlocked(int nbBlocked) {
		this.nbBlocked = nbBlocked;
	}
	
	public int getNbUntestable() {
		return nbUntestable;
	}
	
	public void setNbUntestable(int nbUntestable) {
		this.nbUntestable = nbUntestable;
	}

	public int getNbWarning() {
		return nbWarning;
	}

	public void setNbWarning(int nbWarning) {
		this.nbWarning = nbWarning;
	}

	public int getNbError() {
		return nbError;
	}

	public void setNbError(int nbError) {
		this.nbError = nbError;
	}
	
	
	
}