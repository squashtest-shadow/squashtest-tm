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

import org.squashtest.tm.domain.execution.ExecutionStatus;

public class CampaignTestCaseSuccessRateStatistics {

	private int nbVeryHighSuccess = 0;
	private int nbHighSuccess = 0;
	private int nbMediumSuccess = 0;
	private int nbLowSuccess = 0;
	
	private int nbVeryHighFailure = 0;
	private int nbHighFailure = 0;
	private int nbMediumFailure = 0;
	private int nbLowFailure = 0;

	private int nbVeryHighOther = 0;
	private int nbHighOther = 0;
	private int nbMediumOther = 0;
	private int nbLowOther = 0;
	
	public void addNbLow(ExecutionStatus status, int number){
		
		switch(status){
		case BLOCKED: nbLowOther++;
			break;
		case ERROR: nbLowFailure++;
			break;
		case FAILURE: nbLowFailure++;
			break;
		case READY: nbLowOther++;
			break;
		case RUNNING: nbLowOther++;
			break;
		case SUCCESS: nbLowSuccess++;
			break;
		case UNTESTABLE: nbLowOther++;
			break;
		case WARNING: nbLowSuccess++;
			break;
		}
	}

	public void addNbMedium(ExecutionStatus status, int number){
		switch(status){
		case BLOCKED: nbMediumOther++;
			break;
		case ERROR: nbMediumFailure++;
			break;
		case FAILURE: nbMediumFailure++;
			break;
		case READY: nbMediumOther++;
			break;
		case RUNNING: nbMediumOther++;
			break;
		case SUCCESS: nbMediumSuccess++;
			break;
		case UNTESTABLE: nbMediumOther++;
			break;
		case WARNING: nbMediumSuccess++;
			break;
		}
	}
	
	public void addNbHigh(ExecutionStatus status, int number){
		switch(status){
		case BLOCKED: nbHighOther++;
			break;
		case ERROR: nbHighFailure++;
			break;
		case FAILURE: nbHighFailure++;
			break;
		case READY: nbHighOther++;
			break;
		case RUNNING: nbHighOther++;
			break;
		case SUCCESS: nbHighSuccess++;
			break;
		case UNTESTABLE: nbHighOther++;
			break;
		case WARNING: nbHighSuccess++;
			break;
		}
	}
	
	public void addNbVeryHigh(ExecutionStatus status, int number){
		switch(status){
		case BLOCKED: nbVeryHighOther++;
			break;
		case ERROR: nbVeryHighFailure++;
			break;
		case FAILURE: nbVeryHighFailure++;
			break;
		case READY: nbVeryHighOther++;
			break;
		case RUNNING: nbVeryHighOther++;
			break;
		case SUCCESS: nbVeryHighSuccess++;
			break;
		case UNTESTABLE: nbVeryHighOther++;
			break;
		case WARNING: nbVeryHighSuccess++;
			break;
		}
	}
	
	public int getNbVeryHighSuccess() {
		return nbVeryHighSuccess;
	}
	public void setNbVeryHighSuccess(int nbVeryHighSuccess) {
		this.nbVeryHighSuccess = nbVeryHighSuccess;
	}
	public int getNbHighSuccess() {
		return nbHighSuccess;
	}
	public void setNbHighSuccess(int nbHighSuccess) {
		this.nbHighSuccess = nbHighSuccess;
	}
	public int getNbMediumSuccess() {
		return nbMediumSuccess;
	}
	public void setNbMediumSuccess(int nbMediumSuccess) {
		this.nbMediumSuccess = nbMediumSuccess;
	}
	public int getNbLowSuccess() {
		return nbLowSuccess;
	}
	public void setNbLowSuccess(int nbLowSuccess) {
		this.nbLowSuccess = nbLowSuccess;
	}
	public int getNbVeryHighFailure() {
		return nbVeryHighFailure;
	}
	public void setNbVeryHighFailure(int nbVeryHighFailure) {
		this.nbVeryHighFailure = nbVeryHighFailure;
	}
	public int getNbHighFailure() {
		return nbHighFailure;
	}
	public void setNbHighFailure(int nbHighFailure) {
		this.nbHighFailure = nbHighFailure;
	}
	public int getNbMediumFailure() {
		return nbMediumFailure;
	}
	public void setNbMediumFailure(int nbMediumFailure) {
		this.nbMediumFailure = nbMediumFailure;
	}
	public int getNbLowFailure() {
		return nbLowFailure;
	}
	public void setNbLowFailure(int nbLowFailure) {
		this.nbLowFailure = nbLowFailure;
	}
	public int getNbVeryHighOther() {
		return nbVeryHighOther;
	}
	public void setNbVeryHighOther(int nbVeryHighOther) {
		this.nbVeryHighOther = nbVeryHighOther;
	}
	public int getNbHighOther() {
		return nbHighOther;
	}
	public void setNbHighOther(int nbHighOther) {
		this.nbHighOther = nbHighOther;
	}
	public int getNbMediumOther() {
		return nbMediumOther;
	}
	public void setNbMediumOther(int nbMediumOther) {
		this.nbMediumOther = nbMediumOther;
	}
	public int getNbLowOther() {
		return nbLowOther;
	}
	public void setNbLowOther(int nbLowOther) {
		this.nbLowOther = nbLowOther;
	}
}
