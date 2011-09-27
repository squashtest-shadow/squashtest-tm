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
package org.squashtest.csp.core.log4j;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	/***
	 * Name of the global variable to get properties location
	 */
	private static String CONFIGURATION_LOCATION = "bundles.configuration.location";

	/***
	 * Specific location of log4j properties file
	 */
	private static String LOG_PROPERTIES_FILE_LOCATION = "/services/log4j.properties";

	/***
	 * This method load the log4j properties file
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		FileReader in = new FileReader(new File(context.getProperty(CONFIGURATION_LOCATION)
				+ LOG_PROPERTIES_FILE_LOCATION));
		Properties props = new Properties();
		props.load(in);
		PropertyConfigurator.configure(props);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// nothing to implement here...
	}
}
