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

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.squashtest.csp.tm.domain.execution.ExecutionStatus;

/* a good old bean used as a dto */
public class TestSuiteStatistics {

	private BigDecimal nbTestCases;
	private BigDecimal progression;
	private BigDecimal nbSuccess;
	private BigDecimal nbFailure;
	private BigDecimal nbBloqued;
	private BigDecimal nbReady;
	private BigDecimal nbRunning;
	private ExecutionStatus status;
	private BigDecimal nbDone;

	public long getNbTestCases() {
		return nbTestCases.longValue();
	}

	public void setNbTestCases(int nbTestCases) {
		this.nbTestCases = new BigDecimal(nbTestCases);
	}

	public int getProgression() {
		return progression.intValue();
	}

	public void setProgression(double progression) {
		this.progression = new BigDecimal(progression);
	}

	public int getNbSuccess() {
		return nbSuccess.intValue();
	}

	public void setNbSuccess(int nbSuccess) {
		this.nbSuccess = new BigDecimal(nbSuccess);
	}

	public int getNbFailure() {
		return nbFailure.intValue();
	}

	public void setNbFailure(int nbFailure) {
		this.nbFailure = new BigDecimal(nbFailure);
	}

	public int getNbBloqued() {
		return nbBloqued.intValue();
	}

	public void setNbBloqued(int nbBloqued) {
		this.nbBloqued = new BigDecimal(nbBloqued);
	}
	
	public int getNbRunning() {
		return nbRunning.intValue();
	}

	public void setNbRunning(int nbRunning) {
		this.nbRunning = new BigDecimal(nbRunning);
	}

	public int getNbReady() {
		return nbReady.intValue();
	}

	public void setNbReady(int nbReady) {
		this.nbReady = new BigDecimal(nbReady);
	}

	public ExecutionStatus getStatus() {
		return status;
	}

	public void setStatus(ExecutionStatus status) {
		this.status = status;
	}

	public int getNbDone() {
		return nbDone.intValue();
	}

	public void setNbDone(int nbDone) {
		this.nbDone = new BigDecimal(nbDone);
	}

	public TestSuiteStatistics() {

	}
	
	public TestSuiteStatistics(long nbTestCases, int nbBloqued, int nbFailure, int nbSuccess, int nbRunning, int nbReady) {
		super();
		this.nbTestCases = new BigDecimal(nbTestCases);
		this.nbBloqued = new BigDecimal(nbBloqued);
		this.nbFailure = new BigDecimal(nbFailure);
		this.nbSuccess = new BigDecimal(nbSuccess);
		this.nbRunning = new BigDecimal(nbRunning);
		this.nbReady = new BigDecimal(nbReady);
		
		findStatus();
		findProgression();
//		long nbReadyRecalc = findReady();
//		if (nbReadyRecalc != this.nbReady.longValue()){
//			this.nbReady = new BigDecimal(nbReadyRecalc);
//		}
		findDone();
	}
	
	private void findStatus(){
		if ((nbBloqued.add(nbFailure).add(nbSuccess).add(nbRunning)).intValue() == 0 ){
			status = ExecutionStatus.READY;
		} else if ((nbBloqued.add(nbFailure).add(nbSuccess)).equals(nbTestCases) ){
			status = ExecutionStatus.SUCCESS;
		} else {
			status = ExecutionStatus.RUNNING;
		}
	}
	
	private void findProgression(){
		progression = (nbBloqued.add(nbFailure).add(nbSuccess)).divide(nbTestCases, 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
	}
	
//	private long findReady(){
//		return (nbTestCases.subtract(nbBloqued.add(nbFailure).add(nbSuccess))).longValue();
//	}
	
	private void findDone(){
		nbDone = nbBloqued.add(nbFailure).add(nbSuccess);
	}

}
