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
package org.squashtest.csp.tm.domain.execution;

import org.squashtest.csp.tm.domain.Internationalizable;

/**
 *
 * This class declare the 5 executions status.
 *
 * Also, it declares and additional methods to update the new execution status of an execution, based on the former
 * states of the execution, of the step, and the new status of the step.
 *
 * Definition : * former execution status : former ExecutionStatus of the considered Execution * former step status :
 * former ExecutionStatus of the ExecutionStep (that belongs to the considered Execution) that just had been updated *
 * new step status : new ExecutionStatus of the ExecutionStep that just had been updated. From the object point of view,
 * it's "this" : the public methods here will consider the instance as the new step status. * new execution status : the
 * new ExecutionStatus of the considered Execution. It is what is being computed there.
 *
 */

/*
 * Tech documentation :
 *
 * computation here is roughly based on the truth table of : former execution status, former step status, new step
 * status.
 *
 *
 * we filter this table with trivial computations that do not depend on the new step status, but wrt the former states
 * and the knowledge that the step status changed (trivial deductions)
 *
 * tests are performed as follow : 1/ check for trivial deductions 2/ check if computation is mandatory 3/ if none
 * applied, resolve the situation considering the new step status
 *
 * step 1 will return the new execution status if deducted and we can exit the method step 2 will tell if a computation
 * is mandatory and we can exit the method step 3 will desambiguate if step 1 and 2 failed, using the new Step status
 * information. And will return a status or null (ie, needs computation)
 *
 * see the various tests to see what they do represent.
 *
 * Note 1 : the checks wont test impossible states like formerStepStatus = BLOCKED and formerExecStatus = SUCCESS.
 *
 * Note 2 : check the method computeNewStatus for the simplest statement about what this thing compute
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
		// the case 'former exec status bloqued' is already ruled out in the trivialDeductions
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
		// a lot of things were ruled out, and impossible states aren't treated either;
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
		// we ruled out a lot of things in the trivialAssertions
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
	 * @param formerExecutionStatus
	 *            : the former execution status
	 * @param formerStepStatus
	 *            : the former step status
	 * @return : the new execution status if deduction occured, null if the new status is ambiguous and needs to be
	 *         computed from scratch.
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
		// new step status = former step status : no change. Running is an exeception : the Execution might be set to
		// ready.
		else if (isSetToExecutionStatus(formerExecutionStatus)) {
			newStatus = formerExecutionStatus;
		}

		// if the former execution status was bloqued and the former step status wasn't bloqued, the execution will stay
		// bloqued
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

		// if the former Step status was bloqued and is now changing then computation is mandatory
		if (mayUnlockBloquedExecution(formerStepStatus)) {
			isMandatory = true;
		}

		// here we test if the former step status is the former execution status : this step was maybe the only one
		// responsible for the
		// former exec status (eg, the only one bloqued).
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

	// new step status = former step status : no change. new Step Status Running is an exeception : the Execution might
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
	// former exec status (eg, the only one bloqued).
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