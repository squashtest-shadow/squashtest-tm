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
package org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks;

public abstract class BuildStep implements Runnable{
	
	protected BuildProcessor buildProcessor;
	

	public void setBuildProcessor(BuildProcessor processor){
		this.buildProcessor = processor;
	}

	
	public BuildStep(BuildProcessor processor){
		super();
		this.buildProcessor = processor;
	}
	
	
	@Override
	public void run(){
		try{
			perform();
			buildProcessor.notifyStepDone();
		}
		catch(Exception ex){
			buildProcessor.notifyException(ex);
		}
	}
	
	
	
	/**
	 * Tells whether the current step is complete, or the same step needs to be executed again at a later time.
	 * 
	 * @return true if needs rescheduling, false if we can move to the next step
	 */
	public abstract boolean needsRescheduling();
	

	
	/**
	 * do the job
	 * @throws Exception
	 */
	public abstract void perform() throws Exception;
	
	/**
	 * sets the same object ready for reuse
	 * 
	 */
	public abstract void reset();
	
	
	/**
	 * Returns a positive or null integer if it can suggest an adequate delay before next execution, if the task 
	 * is unconclusive and must be rescheduled. Null should be returned if it has no opinion and let the processor decide
	 * instead. 
	 * 
	 * @return
	 */
	public abstract Integer suggestedReschedulingInterval();
	

}
