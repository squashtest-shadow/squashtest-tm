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

import java.util.Date;

import org.squashtest.tm.domain.planning.StandardWorkloadCalendar;
import org.squashtest.tm.domain.planning.WorkloadCalendar;

public class TestSuiteTestInventoryStatistics {

    private String testsuiteName = "";
    private Date scheduledStart;
    private Date scheduledEnd;
    
    private int nbReady = 0;
    private  int nbRunning = 0;
    private  int nbSuccess = 0;
    private int nbFailure = 0;
    private int nbBlocked = 0;
    private int nbUntestable = 0;

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
		return Math.round(((float) getNbExecuted() / (float) getNbTotal())*10000)/100;
	}
	public float getPcSuccess() {
		return  Math.round(((float) getNbSuccess() / (float) getNbTotal())*10000)/100;
	}
	public float getPcFailure() {
		return  Math.round(((float) getNbFailure() / (float) getNbTotal())*10000)/100;
	}
	public float getPcPrevProgress() {
		if(nbOfTestsToExecuteToDate(scheduledStart, scheduledEnd, new Date(), getNbTotal()) != 0.0f){
			return Math.round(((float) getNbExecuted() / nbOfTestsToExecuteToDate(scheduledStart, scheduledEnd, new Date(), getNbTotal()))*10000)/100;
		} else {
			return getPcProgress();
		}
		
	}
	public int getNbPrevToExecute() {
		return ((int) nbOfTestsToExecuteToDate(scheduledStart, scheduledEnd,  new Date(), getNbTotal()) - getNbExecuted());
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
	
	private float nbOfTestsToExecuteToDate(Date scheduledStart, Date scheduledEnd, Date currentDate, int nbTests){
		
		float result = 0.0f;
		
		//if current date is before the start of the previsional schedule
		if(scheduledStart == null || scheduledEnd==null || currentDate.before(scheduledStart)){
			result = 0.0f;
		//if current date is after the end of the execution schedule
		} else if(currentDate.after(scheduledEnd)){
			result = nbTests;
		} else {
			
			//Get total number of business days
			WorkloadCalendar workloadCalendar = new StandardWorkloadCalendar();
			float totalNumberOfBusinessDays = workloadCalendar.getWorkload(scheduledStart, scheduledEnd);

			//Get number of open days before current date
			float numberOfSpentBusinessDays = workloadCalendar.getWorkload(scheduledStart, currentDate);
			
			//Compute percentage of already spent time 
			float spentTime = numberOfSpentBusinessDays / totalNumberOfBusinessDays;
			
			result = nbTests * spentTime;
		}
		return result;
	}
	public Date getScheduledStart() {
		return scheduledStart;
	}
	public void setScheduledStart(Date scheduledStart) {
		this.scheduledStart = scheduledStart;
	}
	public Date getScheduledEnd() {
		return scheduledEnd;
	}
	public void setScheduledEnd(Date scheduledEnd) {
		this.scheduledEnd = scheduledEnd;
	}
}
