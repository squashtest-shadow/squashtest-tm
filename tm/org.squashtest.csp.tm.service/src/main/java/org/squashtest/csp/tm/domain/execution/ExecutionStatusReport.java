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

/* a good old bean used as a dto */
public class ExecutionStatusReport {

	private int bloqued;
	private int failure;
	private int success;
	private int running;
	private int ready;

	public int getBloqued() {
		return bloqued;
	}

	public void setBloqued(int bloqued) {
		this.bloqued = bloqued;
	}

	public int getFailure() {
		return failure;
	}

	public void setFailure(int failure) {
		this.failure = failure;
	}

	public int getSuccess() {
		return success;
	}

	public void setSuccess(int success) {
		this.success = success;
	}

	public int getRunning() {
		return running;
	}

	public void setRunning(int running) {
		this.running = running;
	}

	public int getReady() {
		return ready;
	}

	public void setReady(int ready) {
		this.ready = ready;
	}

	public boolean areAllSuccess() {
		if ((bloqued == 0) && (failure == 0) && (running == 0) && (ready == 0)) {
			return true;
		}
		return false;
	}

	public ExecutionStatusReport() {

	}

	public ExecutionStatusReport(int bloqued, int failure, int success, int running, int ready) {
		super();
		this.bloqued = bloqued;
		this.failure = failure;
		this.success = success;
		this.running = running;
		this.ready = ready;
	}

}
