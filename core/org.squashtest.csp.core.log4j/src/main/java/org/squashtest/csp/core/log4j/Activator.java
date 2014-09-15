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
package org.squashtest.csp.core.log4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);
	/***
	 * Name of the global variable to get properties location
	 */
	private static final String CONFIGURATION_LOCATION = "bundles.configuration.location";

	/***
	 * Specific location of log4j properties file
	 */
	private static final String LOG_PROPERTIES_FILE_LOCATION = "/services/log4j.properties";

	/***
	 * This method load the log4j properties file
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		Properties props;
		FileReader in = null; 
		
		try {
			in = new FileReader(new File(context.getProperty(CONFIGURATION_LOCATION) + LOG_PROPERTIES_FILE_LOCATION));
			props = new Properties();
			props.load(in);
			
		} catch (FileNotFoundException e) {
			LOGGER.error(e.getMessage(), e);
			throw e;
			
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			closeWithoutFailure(in);
			throw e;
			
		}
		PropertyConfigurator.configure(props);
	}

	private void closeWithoutFailure(FileReader in) {
		try {
			if (in != null) {
				in.close();
			}
		} catch (IOException e) {
			LOGGER.warn(e.getMessage(), e);
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// nothing to implement here...
	}
}
