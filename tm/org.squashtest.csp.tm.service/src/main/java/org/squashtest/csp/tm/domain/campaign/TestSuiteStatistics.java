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
package org.squashtest.csp.tm.domain.campaign;

import org.squashtest.csp.tm.domain.execution.ExecutionStatus;

/* a good old bean used as a dto */
public class TestSuiteStatistics {

	private long nbTestCases;
	private double progression;
	private int nbSuccess;
	private int nbFailure;
	private int nbBloqued;
	private int nbReady;
	private int nbRunning;
	private ExecutionStatus status;

	public long getNbTestCases() {
		return nbTestCases;
	}

	public void setNbTestCases(int nbTestCases) {
		this.nbTestCases = nbTestCases;
	}

	public double getProgression() {
		return progression;
	}

	public void setProgression(double progression) {
		this.progression = progression;
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

	public int getNbBloqued() {
		return nbBloqued;
	}

	public void setNbBloqued(int nbBloqued) {
		this.nbBloqued = nbBloqued;
	}
	
	public int getNbRunning() {
		return nbRunning;
	}

	public void setNbRunning(int nbRunning) {
		this.nbRunning = nbRunning;
	}

	public int getNbReady() {
		return nbReady;
	}

	public void setNbReady(int nbReady) {
		this.nbReady = nbReady;
	}

	public ExecutionStatus getStatus() {
		return status;
	}

	public void setStatus(ExecutionStatus status) {
		this.status = status;
	}

	public TestSuiteStatistics() {

	}
	
	public TestSuiteStatistics(long nbTestCases, int nbBloqued, int nbFailure, int nbSuccess, int nbRunning, int nbReady) {
		super();
		this.nbTestCases = nbTestCases;
		this.nbBloqued = nbBloqued;
		this.nbFailure = nbFailure;
		this.nbSuccess = nbSuccess;
		this.nbRunning = nbRunning;
		this.nbReady = nbReady;
		
		findStatus();
		findProgression();
		long nbReadyRecalc = findReady();
		if (nbReadyRecalc != this.nbReady){
			this.nbReady = (int)nbReadyRecalc;
		}
	}
	
	private void findStatus(){
		if ((nbBloqued + nbFailure + nbSuccess + nbRunning) == 0 ){
			status = ExecutionStatus.READY;
		} else if ( (nbBloqued + nbFailure + nbSuccess) == nbTestCases ){
			status = ExecutionStatus.SUCCESS;
		} else {
			status = ExecutionStatus.RUNNING;
		}
	}
	
	private void findProgression(){
		progression = (nbBloqued + nbFailure + nbSuccess + nbRunning) / nbTestCases;
	}
	
	private long findReady(){
		return nbTestCases - (nbBloqued + nbFailure + nbSuccess);
	}

}
