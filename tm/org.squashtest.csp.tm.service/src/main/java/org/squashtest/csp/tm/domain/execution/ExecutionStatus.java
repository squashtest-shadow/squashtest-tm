/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.csp.tm.domain.execution;

import org.squashtest.tm.core.i18n.Internationalizable;

/**
 *
 * <p>This class declare the 5 executions status.
 *
 * Also, it declares and additional methods to update the new execution status of an execution, based on the former
 * states of the execution, of the step, and the new status of the step. See their documentation for details.</p>
 *
 * <b>Definitions</b> : 
 * <ul>
 * <li><u>former execution status</u> : former ExecutionStatus of the considered Execution</li>
 * <li><u>former step status</u> : former ExecutionStatus of the ExecutionStep (that belongs to the considered Execution) that just had been updated</li>
 * <li><u>new step status</u> : new ExecutionStatus of the ExecutionStep that just had been updated. From the object point of view, it's "this" : the public methods here will consider 
 * "this" as the new step status.</li>
 * <li><u>new execution status</u> : the new ExecutionStatus of the considered Execution. It is what is being computed there.</li>
 * </ul>
 * 
 * 
 * @author bsiri
 *
 */

/*
 * Tech documentation :
 *
 * When asking for the new status of an Execution given its former state, and those of the modified step, the procedure goes through multiple stages.
 * It's a rather awkward procedure because the implementation first concern was optimization rather than code clarity. Indeed we need to reduce calls to
 * the database to the minimum because this code may be called numerous times in a short period of time, especially since a recomputation requires to load the 
 * complete collection of steps for the considered execution.
 * 
 * To achieve the lowest number of db calls, we apply successive tests, from the less information-consuming to the most information-consuming. 
 * 
 * Computation here is based on the table of truth of : former execution status, former step status, new step status. From all the possible combinations obtained in that table
 * we can deduce the tests mentioned above, and which informations are required to deduce them. When no test can be deduced it usually means that a db call is needed.  
 * 
 * The tests are grouped in the following stages (in that order) : 
 * 
 * 1/ trivial deductions : we filter the table of truth with trivial computations wrt the former states of the execution only. If the new status could be deducted it is 
 * returned immediately, otherwise we proceed to the next stage.
 * 
 * 2/ check if computation is mandatory : another row of quick tests that will tell if a db call is unavoidable. If that's the case the method returns null immediately, 
 * otherwise we proceed to the next stage. The calling method is now in charge to call the db.
 *
 * 3/ Status-specific computations : we perform specific tests, like in the first one (trivial computation), this time adding all the informations we have at disposal. If 
 * a result was found the method returns it immediately, otherwise we proceed.
 * 
 * 4/ Failure : none of the step above succeeded. The result is null, and the calling method must call the DB.
 *
 * See below for more comments about the various tests .
 *
 * Note 1 : impossible states like formerStepStatus = BLOCKED and formerExecStatus = SUCCESS are filtered out (they aren't supposed to happen).
 *
 * Note 2 : see the method computeNewStatus for the simplest statement about what does this thing compute.
 */

public enum ExecutionStatus implements Internationalizable {
	BLOCKED() {
		@Override
		protected ExecutionStatus resolveStatus(ExecutionStatus formerExecutionStatus, ExecutionStatus formerStepStatus) {
			return ExecutionStatus.BLOCKED;
		}
	},

	FAILURE() {
		@Override
		// the case 'former exec status blocked' is already ruled out in the trivialDeductions
		protected ExecutionStatus resolveStatus(ExecutionStatus formerExecutionStatus, ExecutionStatus formerStepStatus) {
			return ExecutionStatus.FAILURE;
		}
	},

	SUCCESS() {
		@Override
		protected ExecutionStatus resolveStatus(ExecutionStatus formerExecutionStatus, ExecutionStatus formerStepStatus) {
			ExecutionStatus newStatus;

			if (formerExecutionStatus == ExecutionStatus.FAILURE) {
				newStatus = ExecutionStatus.FAILURE;
			} else if (formerStepStatus == ExecutionStatus.RUNNING && formerExecutionStatus == ExecutionStatus.READY) {
				newStatus = ExecutionStatus.RUNNING;
			} else {
				newStatus = needsComputation;
			}

			return newStatus;
		}
	},

	RUNNING() {
		
		@Override
		protected ExecutionStatus resolveStatus(ExecutionStatus formerExecutionStatus, ExecutionStatus formerStepStatus) {
			ExecutionStatus newStatus;

			if (formerExecutionStatus == ExecutionStatus.FAILURE) {
				newStatus = ExecutionStatus.FAILURE;
			} else if (formerExecutionStatus == ExecutionStatus.READY) {
				newStatus = ExecutionStatus.READY;
			} else if (formerExecutionStatus == ExecutionStatus.RUNNING) {
				newStatus = needsComputation;
			} else {
				newStatus = ExecutionStatus.RUNNING;
			}
			return newStatus;
		}
	},

	READY() {
		
		@Override
		protected ExecutionStatus resolveStatus(ExecutionStatus formerExecutionStatus, ExecutionStatus formerStepStatus) {
			ExecutionStatus newStatus;

			if (formerExecutionStatus == ExecutionStatus.FAILURE) {
				newStatus = ExecutionStatus.FAILURE;
			} else {
				newStatus = needsComputation;
			}

			return newStatus;
		}
	};

	private static final String I18N_KEY_ROOT = "execution.execution-status.";

	/*
	 * those fields exists only for code semantics since this class is a complete mess. It's for the same purpose than a
	 * #define in C
	 */
	protected static ExecutionStatus isAmbiguous = null;
	protected static ExecutionStatus needsComputation = null;

	/**
	 * will deduce the new status of an execution based on the former execution status and former step status. "this" is
	 * here the new step status. In some case the deduction is impossible and a further computation will be necessary.
	 *
	 * @param formerExecutionStatus : the former execution status
	 * @param formerStepStatus : the former step status
	 * @return : the new execution status when possible, or null if it wasn't. The later usually means that a call to the database is needed.
	 */
	public ExecutionStatus deduceNewStatus(ExecutionStatus formerExecutionStatus, ExecutionStatus formerStepStatus) {

		ExecutionStatus newStatus;

		// first pass : trivial deductions
		ExecutionStatus deductedStatus = trivialDeductions(formerExecutionStatus, formerStepStatus);

		if (deductedStatus != isAmbiguous) {
			newStatus = deductedStatus;
		}

		else {
			// second pass : trivial computations
			boolean needComputation = trivialNeedComputation(formerExecutionStatus, formerStepStatus);
			if (needComputation) {
				newStatus = null;
			}

			// third pass : we need further resolution
			else {
				newStatus = resolveStatus(formerExecutionStatus, formerStepStatus);
			}

		}

		return newStatus;
	}

	/**
	 * will compute from scratch a status using a complete report.
	 *
	 * @param report
	 *            : ExecutionStatusReport.
	 * @return : ExecutionStatus.
	 */
	public static ExecutionStatus computeNewStatus(ExecutionStatusReport report) {

		ExecutionStatus newStatus = ExecutionStatus.READY;

		if (report.getBloqued() > 0) {
			newStatus = ExecutionStatus.BLOCKED;
		} else if (report.getFailure() > 0) {
			newStatus = ExecutionStatus.FAILURE;
		} else if (report.areAllSuccess()) {
			newStatus = ExecutionStatus.SUCCESS;
		} else if (report.getSuccess() > 0) {
			newStatus = ExecutionStatus.RUNNING;
		} else {
			newStatus = ExecutionStatus.READY;
		}

		return newStatus;
	}

	/* ************************************ tests ******************************************* */

	// trivial tests who do not depend on the actual value of "this" (ie, new Step status)
	protected ExecutionStatus trivialDeductions(ExecutionStatus formerExecutionStatus, ExecutionStatus formerStepStatus) {
		ExecutionStatus newStatus;

		// step status unchanged : no change
		if (!hasChanged(formerStepStatus)) {
			newStatus = formerExecutionStatus;
		}
		// new step status = former step status : no change. Running is an exception : the Execution might be set to ready.
		else if (isSetToExecutionStatus(formerExecutionStatus)) {
			newStatus = formerExecutionStatus;
		}

		// if the former execution status was blocked and the former step status wasn't blocked, the execution will stay
		// blocked
		else if (wontUnlockBloquedExecution(formerExecutionStatus, formerStepStatus)) {
			newStatus = ExecutionStatus.BLOCKED;
		} else {
			newStatus = isAmbiguous;
		}

		return newStatus;

	}

	// in certain cases the computation is mandatory no matter what. This method will check that.
	protected boolean trivialNeedComputation(ExecutionStatus formerExecutionStatus, ExecutionStatus formerStepStatus) {
		boolean isMandatory = false;

		// if the former Step status was blocked and is now changing then computation is mandatory
		if (mayUnlockBloquedExecution(formerStepStatus)) {
			isMandatory = true;
		}

		// here we test if the former step status is the former execution status : this step was maybe the only one
		// responsible for the former exec status (eg, the only one blocked).
		// we then now the new exec status must then be recomputed.
		else if (couldHaveSetExecStatusAlone(formerExecutionStatus, formerStepStatus)) {
			isMandatory = true;
		}

		return isMandatory;
	}

	protected abstract ExecutionStatus resolveStatus(ExecutionStatus formerExecutionStatus,
			ExecutionStatus formerStepStatus);

	/* *************************** Micro tests ************************************** */

	protected boolean hasChanged(ExecutionStatus formerStepStatus) {
		return (this != formerStepStatus);
	}

	// new step status = former step status : no change. new Step Status Running is an exception : the Execution might
	// be set to ready later.
	protected boolean isSetToExecutionStatus(ExecutionStatus formerExecutionStatus) {
		if (this == ExecutionStatus.RUNNING) {
			return false;
		}
		return (this == formerExecutionStatus);
	}

	protected boolean wontUnlockBloquedExecution(ExecutionStatus formerExecutionStatus, ExecutionStatus formerStepStatus) {
		return (formerExecutionStatus == ExecutionStatus.BLOCKED && formerStepStatus != ExecutionStatus.BLOCKED);
	}

	protected boolean mayUnlockBloquedExecution(ExecutionStatus formerStepStatus) {
		return (this != ExecutionStatus.BLOCKED && formerStepStatus == ExecutionStatus.BLOCKED);
	}

	// here we test if the former step status is the former execution status : this step was maybe the only one
	// responsible for the
	// former exec status (eg, the only one blocked).
	// we then now the new exec status must then be recomputed.
	protected boolean couldHaveSetExecStatusAlone(ExecutionStatus formerExecutionStatus,
			ExecutionStatus formerStepStatus) {
		return (formerExecutionStatus == formerStepStatus);
	}

	/* *************************** Utils ******************************************** */

	protected boolean isNoneOf(ExecutionStatus... status) {
		for (ExecutionStatus state : status) {
			if (this.equals(state)) {
				return false;
			}
		}
		return true;
	}

	protected boolean isOneOf(ExecutionStatus... status) {
		return (!isNoneOf(status));
	}

	/***
	 * This methods checks if the status is RUNNING or READY
	 *
	 * @return true if the status is not RUNNING or READY
	 */
	public boolean isTerminatedStatus() {
		return isNoneOf(ExecutionStatus.RUNNING, ExecutionStatus.READY);
	}

	@Override
	public String getI18nKey() {
		return I18N_KEY_ROOT + name();
	}
}