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

public class CampaignTestCaseStatusStatistics {

	private int nbReady = 0;
	private int nbRunning = 0;
	private int nbSuccess = 0;
	private int nbFailure = 0;
	private int nbBlocked = 0;
	private int nbUntestable = 0;
	
	public int getNbReady() {
		return nbReady;
	}
	public void addNbReady(int nbReady) {
		this.nbReady += nbReady;
	}
	public int getNbRunning() {
		return nbRunning;
	}
	public void addNbRunning(int nbRunning) {
		this.nbRunning += nbRunning;
	}
	public int getNbSuccess() {
		return nbSuccess;
	}
	public void addNbSuccess(int nbSuccess) {
		this.nbSuccess += nbSuccess;
	}
	public int getNbFailure() {
		return nbFailure;
	}
	public void addNbFailure(int nbFailure) {
		this.nbFailure += nbFailure;
	}
	public int getNbBlocked() {
		return nbBlocked;
	}
	public void addNbBlocked(int nbBlocked) {
		this.nbBlocked += nbBlocked;
	}
	public int getNbUntestable() {
		return nbUntestable;
	}
	public void addNbUntestable(int nbUntestable) {
		this.nbUntestable += nbUntestable;
	}
	

}
