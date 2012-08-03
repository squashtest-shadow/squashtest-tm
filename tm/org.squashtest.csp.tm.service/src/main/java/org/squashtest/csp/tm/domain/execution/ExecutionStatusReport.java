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

	private int bloqued=0;
	private int failure=0;
	private int success=0;
	private int running=0;
	private int ready=0;
	private int warning=0;
	private int error=0;

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
	

	public int getWarning() {
		return warning;
	}

	public void setWarning(int warning) {
		this.warning = warning;
	}

	public int getError() {
		return error;
	}

	public void setError(int error) {
		this.error = error;
	}

	public boolean areAllSuccess() {
		if (
			(! hasAggregatedBlocked())  && 
			(! hasFailure()) 			&&	
		    (! hasRunning())			&&
		    (! hasReady())
		  ){
			return true;
		}
		return false;
	}
	
	public boolean hasBlocked(){
		return bloqued > 0;
	}
	
	public boolean hasError(){
		return error > 0;
	}
	
	public boolean hasFailure(){
		return failure > 0;
	}
	
	public boolean hasRunning(){
		return running > 0;
	}
	
	public boolean hasReady(){
		return ready > 0;
	}
	
	public boolean hasSuccess(){
		return success > 0 ;
	}
	
	public boolean hasWarning(){
		return warning > 0;
	}
	
	public boolean hasAggregatedBlocked(){
		return (hasBlocked() || hasError());
	}
	
	public boolean hasAggregatedSuccess(){
		return (hasSuccess() || hasWarning());
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

	public ExecutionStatusReport(int bloqued, int failure, int success,
			int running, int ready, int warning, int error) {
		super();
		this.bloqued = bloqued;
		this.failure = failure;
		this.success = success;
		this.running = running;
		this.ready = ready;
		this.warning = warning;
		this.error = error;
	}

	
	

}
