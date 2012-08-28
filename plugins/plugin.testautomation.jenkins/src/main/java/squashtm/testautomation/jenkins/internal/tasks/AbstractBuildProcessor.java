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
package squashtm.testautomation.jenkins.internal.tasks;

import java.util.NoSuchElementException;

public abstract class AbstractBuildProcessor<RESULT> implements BuildProcessor {
	 
	protected StepScheduler scheduler = new SameThreadStepScheduler();

	protected StepSequence stepSequence = new EmptySequence();
	
	
	private int defaultReschedulingDelay = 30000;
	
	private String externalId;
	
	private int buildId;	//will be found during the process
	

	
	// ******* state variables *********
	
	protected BuildStep currentStep=null;
	
	protected StepFuture currentFuture=null;
	
	private boolean canceled;
	
	
	
	// ******** accessors **************
	
 
	public StepScheduler getScheduler() {
		return scheduler;
	}


	public void setScheduler(StepScheduler scheduler) {
		this.scheduler = scheduler;
	}
	
	public void setDefaultReschedulingDelay(int defaultReschedulingDelay) {
		this.defaultReschedulingDelay = defaultReschedulingDelay;
	}


	public void setStepSequence(StepSequence stepSequence) {
		this.stepSequence = stepSequence;
	}

	
	public String getExternalId() {
		return externalId;
	}


	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}


	public int getBuildId() {
		return buildId;
	}


	@Override
	public void setBuildId(int buildId) {
		this.buildId = buildId;
	}


	protected BuildStep getCurrentStep(){
		return currentStep;
	}
	
	protected StepFuture getCurrentFuture(){
		return currentFuture;
	}
	

	public int getDefaultReschedulingDelay() {
		return defaultReschedulingDelay;
	}

	
	//*********** code *************



	public boolean isCanceled() {
		return canceled;
	}

	@Override
	public void cancel(){
		currentFuture.cancel();
		canceled = true;
	}
	
	public boolean taskHasBegun(){
		return currentStep == null;
	}

	protected void scheduleNextStep(){
		
		BuildStep nextStep;		
		int delay = 0;
		
		if (! taskHasBegun()){
			delay = 0;
			nextStep = stepSequence.nextElement(); 
		}
		else if (currentStep.needsRescheduling()){
			delay = currentStep.suggestedReschedulingDelay();
			nextStep = currentStep;
		}
		else{
			delay = 0;
			nextStep = stepSequence.nextElement();
		}
		
		
		currentStep = nextStep;		
		currentFuture = scheduler.schedule(currentStep, delay);
		
	}
	
	
	
	public abstract void run();
	
	public abstract RESULT getResult();
	
	public abstract void buildResult();
	
	
	
	
	
	// ************** private static stuff *************************
	
	private static class EmptySequence implements StepSequence{

		private boolean wasCalled = false;
		
		@Override
		public boolean hasMoreElements() {
			return ! wasCalled;
		}
		
		@Override
		public BuildStep nextElement() {
			if (! wasCalled){
				wasCalled=true;
				return new EmptyStep();
			}
			else{
				throw new NoSuchElementException();
			}
		}
				
	}
	
	private static class EmptyStep extends BuildStep{

		@Override
		public boolean needsRescheduling() {
			return false;
		}

		@Override
		public boolean isFinalStep() {
			return true;
		}

		@Override
		public void perform() throws Exception {			
		}

		@Override
		public void reset() {			
		}

		@Override
		public int suggestedReschedulingDelay() {
			return 0;
		}



	}
	
}
