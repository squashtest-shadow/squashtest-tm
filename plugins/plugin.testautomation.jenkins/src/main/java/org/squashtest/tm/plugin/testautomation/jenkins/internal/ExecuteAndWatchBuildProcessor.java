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
package org.squashtest.tm.plugin.testautomation.jenkins.internal;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.HttpClientProvider;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.DelayedBuildProcessor;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.StepEventListener;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.StepSequence;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.GetBuildID;

/**
 * Instance are created / injected by Spring, yet they are not fully configured. To do so, one should use configuration
 * dsl :
 * 
 * <pre>
 * processorProvider.get()
 *   .configuration()
 *     .buildDef(buildDefinition)
 *     .externalId(externalId)
 *   .configure()
 *   .run()
 * </pre>
 * 
 * @author bsiri, Gregory Fouquet
 * 
 */
@Component
@Scope("prototype")
public class ExecuteAndWatchBuildProcessor extends DelayedBuildProcessor implements ExecuteAndWatchContext {

	public class ProcessorConfigurer {
		private BuildDef buildDef;
		private String externalId;
		private StepEventListener<GetBuildID> buildIdListener;

		private ProcessorConfigurer() {
			super();
		}

		public ProcessorConfigurer buildDef(BuildDef buildDef) {
			this.buildDef = buildDef;
			return this;
		}

		public ProcessorConfigurer externalId(String externalId) {
			this.externalId = externalId;
			return this;
		}

		public ProcessorConfigurer buildIdListener(StepEventListener<GetBuildID> listener) {
			this.buildIdListener = listener;
			return this;
		}

		/**
		 * Configures the outer {@link ExecuteAndWatchBuildProcessor} and returns it.
		 * 
		 * @return
		 */
		public ExecuteAndWatchBuildProcessor configure() {
			stepSequence = new ExecuteAndWatchStepSequence(ExecuteAndWatchBuildProcessor.this, buildDef, externalId, buildIdListener);
			configurer = new ProcessorConfigurer(); // releases resources
			return ExecuteAndWatchBuildProcessor.this;
		}
	}

	@Inject
	private HttpClientProvider httpClientProvider;

	private ExecuteAndWatchStepSequence stepSequence;
	private ProcessorConfigurer configurer = new ProcessorConfigurer();

	/**
	 * Opens the configuration dsl. should be
	 * 
	 * @return
	 */
	public ProcessorConfigurer configuration() {
		return configurer;
	}

	// *********************** ctor *************
	@Inject
	public ExecuteAndWatchBuildProcessor(TaskScheduler scheduler) {
		super(scheduler);
	}

	@Override
	protected StepSequence getStepSequence() {
		return stepSequence;
	}

	/**
	 * Checks this processor has correctly been configured and then runs it.
	 * 
	 * @see org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.DelayedBuildProcessor#run()
	 */
	@Override
	public void run() {
		Assert.state(stepSequence != null,
				"Step sequence should not be null. Did you programmatically configure this processor ?");
		super.run();
	}

	/**
	 * @return the httpClientProvider
	 */
	public HttpClientProvider getHttpClientProvider() {
		return httpClientProvider;
	}

	@Value("${tm.test.automation.pollinterval.millis:5000}")
	public void setDefaultReschedulingDelay(int defaultReschedulingDelay) {
		super.setDefaultReschedulingDelay(defaultReschedulingDelay);
	}

}
