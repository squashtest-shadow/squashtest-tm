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

public abstract class RemoteBuildWatcher<RESULT> {
	 
	protected StepScheduler scheduler;
	
	protected StepFactory stepFactory;
	
	protected StepFuture currentFuture;
	
	protected RemoteBuildStep<?> currentStep;
	
	protected RESULT result;
	

	public StepScheduler getScheduler() {
		return scheduler;
	}


	public void setScheduler(StepScheduler scheduler) {
		this.scheduler = scheduler;
	}


	public StepFactory getStepFactory() {
		return stepFactory;
	}

	
	public void setStepFactory(StepFactory stepFactory) {
		this.stepFactory = stepFactory;
	}
	
	public RESULT getResult(){
		return result;
	}
	
	
	public abstract void run();

	public abstract void notifyStepDone(RemoteBuildStep<?> step);
	
	public abstract void notifyException(RemoteBuildStep<?> step, Exception ex);
	
	public abstract void buildResult();
	
	
}
