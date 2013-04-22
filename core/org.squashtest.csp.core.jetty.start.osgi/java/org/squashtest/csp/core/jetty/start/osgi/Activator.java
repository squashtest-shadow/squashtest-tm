/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package org.squashtest.csp.core.jetty.start.osgi;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.mortbay.component.LifeCycle;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.HandlerContainer;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.HandlerWrapper;
import org.mortbay.log.Log;
import org.mortbay.log.Logger;
import org.mortbay.util.Attributes;
import org.mortbay.xml.XmlConfiguration;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

/**
 * Simple activator for starting Jetty similar to <code>start.jar</code>. The standard is not used since it expects a
 * file system structure and thus it is not usable inside OSGi. Moreover, a hook to the server lifecycle is required.
 *
 * @author Costin Leau
 *
 *         xpetitrenaud : update this class to externalize Jetty configuration file
 *
 */
public class Activator implements BundleActivator {

	/** logger */
	private static final Logger log = Log.getLogger(Activator.class.getName());

	/** default configuration present in the activator bundle */
	private static final String DEFAULT_CONFIG_LOCATION = "/etc/default-jetty.xml";

	/***
	 * Name of the global variable to get properties location
	 */
	private static String CONFIGURATION_LOCATION = "bundles.configuration.location";

	/***
	 * The name of the external config file
	 */
	private static String EXTERNAL_CONFIG_LOCATION = "/services/squash-jetty.xml";

	/** standard jetty configuration file */
	/* TODO change the path */
	private static final String USER_CONFIG_LOCATION = "/etc/jetty.xml";

	private XmlConfiguration xmlConfig;

	private Server server;

	private BundleContext bundleContext;

	private ServiceRegistration registration;

	private Thread startupThread;

	@Override
	public void start(BundleContext context) throws Exception {
		this.bundleContext = context;
		Bundle bundle = context.getBundle();

		// first try to use the user XML file
		//URL xmlConfiguration = bundle.getResource(USER_CONFIG_LOCATION);
		//Get the external file
		URL xmlConfiguration = new File(context.getProperty(CONFIGURATION_LOCATION) + EXTERNAL_CONFIG_LOCATION).toURI()
				.toURL();

		if (xmlConfiguration != null) {
			log.info("Using custom XML configuration " + xmlConfiguration, null, null);
		} else {
			xmlConfiguration = bundle.getResource(DEFAULT_CONFIG_LOCATION);
			if (xmlConfiguration == null) {
				log.warn("No XML configuration found; bailing out...", null, null);
				throw new IllegalArgumentException("Cannot find a suitable jetty configuration at "
						+ USER_CONFIG_LOCATION + " or " + DEFAULT_CONFIG_LOCATION);
			} else {
				log.info("Using default XML configuration " + xmlConfiguration, null, null);
			}
		}

		final URL config = xmlConfiguration;

		// do the initialization on a different thread
		// so the activator finishes fast
		startupThread = new Thread(new Runnable() {

			@Override
			public void run() {
				log.info("Starting Jetty " + Server.getVersion() + " ...", null, null);

				// create logging directory first
				createLoggingDirectory();

				// default startup procedure
				ClassLoader cl = Activator.class.getClassLoader();
				Thread current = Thread.currentThread();
				ClassLoader old = current.getContextClassLoader();

				try {
					// current.setContextClassLoader(cl);
					// reset CCL
					current.setContextClassLoader(null);
					if (log.isDebugEnabled()) {
						log.debug("Reading Jetty config " + config.toString(), null, null);
					}

					xmlConfig = new XmlConfiguration(config);
					Object root = xmlConfig.configure();
					if (!(root instanceof Server)) {
						throw new IllegalArgumentException(
								"expected a Server object as a root for server configuration");
					}
					server = (Server) root;
					server.start();
					log.info("Succesfully started Jetty " + Server.getVersion(), null, null);

					// publish server as an OSGi service
					registration = publishServerAsAService(server);

					log.info("Published Jetty " + Server.getVersion() + " as an OSGi service", null, null);

					server.join();
				} catch (Exception ex) {
					String msg = "Cannot start Jetty " + Server.getVersion();
					log.warn(msg, ex);
					throw new RuntimeException(msg, ex);
				} finally {
					current.setContextClassLoader(old);
				}
			}
		}, "Jetty Start Thread");

		startupThread.start();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// unpublish service first
		registration.unregister();
		log.info("Unpublished Jetty " + Server.getVersion() + " OSGi service", null, null);

		// default startup procedure
		ClassLoader cl = Activator.class.getClassLoader();
		Thread current = Thread.currentThread();
		ClassLoader old = current.getContextClassLoader();

		try {
			log.info("Stopping Jetty " + Server.getVersion() + " ...", null, null);
			// current.setContextClassLoader(cl);
			// reset CCL
			current.setContextClassLoader(null);
			server.stop();
			log.info("Succesfully stopped Jetty " + Server.getVersion() + " ...", null, null);
		} catch (Exception ex) {
			log.warn("Cannot stop Jetty " + Server.getVersion(), ex);
			throw ex;
		} finally {
			current.setContextClassLoader(old);
		}
	}

	private ServiceRegistration publishServerAsAService(Server server) {
		Properties props = new Properties();
		// put some extra properties to easily identify the service
		props.put(Constants.SERVICE_VENDOR, "Spring Dynamic Modules");
		props.put(Constants.SERVICE_DESCRIPTION, "Jetty " + Server.getVersion());
		props.put(Constants.BUNDLE_VERSION, Server.getVersion());
		props.put(Constants.BUNDLE_NAME, bundleContext.getBundle().getSymbolicName());

		// spring-dm specific property
		props.put("org.springframework.osgi.bean.name", "jetty-server");

		// publish just the interfaces and the major classes (server/handlerWrapper)
		String[] classes = new String[] { Server.class.getName(), HandlerWrapper.class.getName(),
				Attributes.class.getName(), HandlerContainer.class.getName(), Handler.class.getName(),
				LifeCycle.class.getName() };
		return bundleContext.registerService(classes, server, props);
	}

	private void createLoggingDirectory() {
		try {
			File logs = new File(".", "logs");
			if (!logs.exists()) {
				logs.mkdir();
			}
			String path = logs.getCanonicalPath();
			System.setProperty("jetty.logs", path);
			log.info("Created Jetty logging folder " + path, null, null);
		} catch (IOException ex) {
			log.warn("Cannot create logging folder", ex);
		}

	}
}