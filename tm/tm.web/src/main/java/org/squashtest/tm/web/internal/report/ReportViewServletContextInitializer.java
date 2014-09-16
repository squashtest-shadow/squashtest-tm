/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.web.internal.report;

import java.util.Map;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ServletContextAware;
import org.squashtest.tm.api.report.Report;
import org.squashtest.tm.api.report.ReportPlugin;
import org.squashtest.tm.api.report.ReportView;

/**
 * This class registers / unregisters Jasper Reports view definitinos from report plugin under a view name.
 * 
 * @author Gregory Fouquet
 * 
 */
public class ReportViewServletContextInitializer implements ServletContextAware {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReportViewServletContextInitializer.class);

	private ServletContext servletContext;

	private static enum RegistrationAction {
		BIND_CONTEXT() {
			@Override
			public void apply(ServletContext servletContext, ReportView view) {
				if (view.getSpringView() instanceof ServletContextAware) {
					ServletContextAware springView = (ServletContextAware) view.getSpringView(); 
					springView.setServletContext(servletContext);
					LOGGER.info("Bound ServletContext to view [{}]", view);
				}
			}
		},
		UNBIND_CONTEXT() {
			@Override
			public void apply(ServletContext servletContext, ReportView view) {
				if (view.getSpringView() instanceof ServletContextAware) {
					ServletContextAware springView = (ServletContextAware) view.getSpringView(); 
					springView.setServletContext(null);
					LOGGER.info("Unbound ServletContext from view [{}]", view);
				}
			}
		};

		/**
		 * @param servletContext
		 * @param view
		 */
		public abstract void apply(ServletContext servletContext, ReportView view);
	}

	public synchronized void registerViews(ReportPlugin plugin, Map<?, ?> properties) {
		// sometimes, plugin is null
		if (plugin != null) {
			apply(RegistrationAction.BIND_CONTEXT, plugin, properties);
		}
	}

	private void apply(RegistrationAction action, ReportPlugin plugin, Map<?, ?> properties) {
		Report[] reports = plugin.getReports();

		for (int reportIndex = 0; reportIndex < reports.length; reportIndex++) {
			Report report = reports[reportIndex];

			apply(action, report);
		}
	}

	private void apply(RegistrationAction action, Report report) {
		for (int viewIndex = 0; viewIndex < report.getViews().length; viewIndex++) {
			ReportView view = report.getViews()[viewIndex];
			action.apply(servletContext, view);
		}
	}

	public void unregisterViews(ReportPlugin plugin, Map<?, ?> properties) {
		// sometimes, plugin is null
		if (plugin != null) {
			apply(RegistrationAction.UNBIND_CONTEXT, plugin, properties);
		}
	}

	/**
	 * @see org.springframework.web.context.ServletContextAware#setServletContext(javax.servlet.ServletContext)
	 */
	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;

	}
}
