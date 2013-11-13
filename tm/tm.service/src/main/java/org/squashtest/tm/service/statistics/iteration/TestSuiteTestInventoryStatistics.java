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
package org.squashtest.tm.service.statistics.iteration;

public class TestSuiteTestInventoryStatistics {

    private String testsuiteName = "";

    private int nbReady = 0;
    private  int nbRunning = 0;
    private  int nbSuccess = 0;
    private int nbFailure = 0;
    private int nbBlocked = 0;
    private int nbUntestable = 0;

    private float pcPrevProgress = 0;
    private int nbPrevToExecute = 0;
   
    private int nbVeryHigh = 0;
    private int nbHigh = 0;
    private int nbMedium = 0;
    private int nbLow = 0;
	
    public String getTestsuiteName() {
		return testsuiteName;
	}
	public void setTestsuiteName(String testsuiteName) {
		this.testsuiteName = testsuiteName;
	}
	public int getNbTotal() {
		return nbReady+nbRunning+nbSuccess+nbFailure+nbBlocked+nbUntestable;
	}
	public int getNbToExecute() {
		return nbReady+nbRunning;
	}
	public int getNbExecuted() {
		return nbSuccess+nbFailure+nbBlocked+nbUntestable;
	}
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
	public float getPcProgress() {
		return ((float) getNbExecuted() / (float) getNbTotal())*100;
	}
	public float getPcSuccess() {
		return  ((float) getNbSuccess() / (float) getNbTotal())*100;
	}
	public float getPcFailure() {
		return  ((float) getNbFailure() / (float) getNbTotal())*100;
	}
	public float getPcPrevProgress() {
		return pcPrevProgress;
	}
	public void setPcPrevProgress(float pcPrevProgress) {
		this.pcPrevProgress = pcPrevProgress;
	}
	public int getNbPrevToExecute() {
		return nbPrevToExecute;
	}
	public void setNbPrevToExecute(int nbPrevToExecute) {
		this.nbPrevToExecute = nbPrevToExecute;
	}
	public int getNbVeryHigh() {
		return nbVeryHigh;
	}
	public void addNbVeryHigh(int nbVeryHigh) {
		this.nbVeryHigh += nbVeryHigh;
	}
	public int getNbHigh() {
		return nbHigh;
	}
	public void addNbHigh(int nbHigh) {
		this.nbHigh += nbHigh;
	}
	public int getNbMedium() {
		return nbMedium;
	}
	public void addNbMedium(int nbMedium) {
		this.nbMedium += nbMedium;
	}
	public int getNbLow() {
		return nbLow;
	}
	public void addNbLow(int nbLow) {
		this.nbLow += nbLow;
	}
}
