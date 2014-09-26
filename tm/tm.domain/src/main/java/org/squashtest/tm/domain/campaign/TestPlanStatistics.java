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
package org.squashtest.tm.domain.campaign;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import org.squashtest.tm.domain.execution.ExecutionStatus;

/**
 * 
 * Dto for Test-plan statistics <br>
 * Properties :
 * <ul>
 * <li>int nbTestCases</li>
 * <li>int progression (number between 0 and 100)</li>
 * <li>int nbSuccess  =  SUCCESS + WARNING</li>
 * <li>int nbFailure</li>
 * <li>int nbUntestable = UNTESTABLE + NOT_FOUND</li>
 * <li>int nbBlocked = BLOCKED + ERROR + NOT_RUN</li>
 * <li>int nbReady</li>
 * <li>int nbRunning</li>
 * <li>{@linkplain TestPlanStatus} status</li>
 * <li>int nbDone</li>
 * </ul>
 * 
 */
// made "final" because SONAR complained about constructors and overridable methods used in there
public final class TestPlanStatistics {
	public static final String TOTAL_NUMBER_OF_TEST_CASE_KEY = "total";
	private int progression;
	private TestPlanStatus status;
	private int nbDone ;
	private Map<String, Integer> statisticValues;


	public int getNbTestCases() {
		return findIntValue(TOTAL_NUMBER_OF_TEST_CASE_KEY);
	}

	public int getProgression() {
		return progression;
	}

	public int getNbSuccess() {
		return findIntValue(ExecutionStatus.SUCCESS.name()) + findIntValue(ExecutionStatus.WARNING.name());
	}

	public int getNbFailure() {
		return findIntValue(ExecutionStatus.FAILURE.name());
	}

	public int getNbUntestable() {
		return findIntValue(ExecutionStatus.UNTESTABLE.name());
	}

	public int getNbSettled() {
		return findIntValue(ExecutionStatus.SETTLED.name());
	}

	public int getNbBlocked() {
		return findIntValue(ExecutionStatus.BLOCKED.name())+ findIntValue(ExecutionStatus.ERROR.name());
	}

	public int getNbReady() {
		return findIntValue(ExecutionStatus.READY.name());
	}

	public int getNbRunning() {
		return findIntValue(ExecutionStatus.RUNNING.name());
	}

	public TestPlanStatus getStatus() {
		return status;
	}

	/**
	 * 
	 * @return summ of Test-plan-items with status of "untestable", "blocked", "failure" or "success".<br>
	 * Nb : (success = success + warning) and (blocked = blocked + error)
	 */
	public int getNbDone() {
		return nbDone;
	}

	public TestPlanStatistics() {

	}

	public TestPlanStatistics(Map<String, Integer> statisticValues) {
		super();
		this.statisticValues = statisticValues;
		computeDone();
		computeProgression();
		this.status = TestPlanStatus.getStatus(this);
	}

	private int findIntValue( String key){
		Integer integer = statisticValues.get(key);
		if(integer == null){
			return 0;
		}else{
			return integer;
		}
	}

	private void computeProgression() {
		if (getNbTestCases() != 0 ) {
			BigDecimal progressionBD = new BigDecimal(nbDone).divide(new BigDecimal(getNbTestCases()), 2,	RoundingMode.HALF_UP).multiply(new BigDecimal(100));
			progression = progressionBD.intValue();
		} else {
			progression = 0;
		}
	}

	private void computeDone() {
		nbDone = getNbSettled() + getNbUntestable()  + getNbBlocked() +  getNbFailure() + getNbSuccess() ;
	}
}
