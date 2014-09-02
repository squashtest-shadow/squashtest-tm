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
package org.eclipse.jetty.osgi.boot.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Fouquet
 * 
 */
public class BundleClassLoaderHelperImpl implements BundleClassLoaderHelper {

	private static boolean identifiedOsgiImpl;
	private static boolean isFelix;
	private static Method getRevisionMethod;
	private static Method getWiringMethod;
	private static Method getClassLoaderMethod;

	private static final Logger LOGGER = LoggerFactory.getLogger(BundleClassLoaderHelper.class);

	/**
	 * 
	 */
	public BundleClassLoaderHelperImpl() {
		super();
	}

	/**
	 * @see org.eclipse.jetty.osgi.boot.utils.BundleClassLoaderHelper#getBundleClassLoader(org.osgi.framework.Bundle)
	 */
	@Override
	public ClassLoader getBundleClassLoader(Bundle bundle) {
		String bundleActivator = (String) bundle.getHeaders().get("Bundle-Activator");
		if (bundleActivator == null) {
			bundleActivator = (String) bundle.getHeaders().get("Jetty-ClassInBundle");
		}
		if (bundleActivator != null) {
			try {
				return bundle.loadClass(bundleActivator).getClassLoader();
			} catch (ClassNotFoundException e) {
				// should not happen as we are called if the bundle is started
				// anyways.
				LOGGER.error(e.getMessage(), e);
			}
		}
		// resort to introspection
		if (!identifiedOsgiImpl) {
			init(bundle);
		}
		if (isFelix) {
			return internalGetFelixBundleClassLoader(bundle);
		}
		return null;
	}

	private static void init(Bundle bundle) {
		identifiedOsgiImpl = true;
		try {
			isFelix = bundle.getClass().getClassLoader().loadClass("org.apache.felix.framework.BundleImpl") != null;
		} catch (Exception t) {
			throw new IllegalStateException(
					"'org.apache.felix.framework.BundleImpl' could not be loaded. Does this bundle run inside a Felix 4.2 container ?",
					t);
		}
	}

	private static ClassLoader internalGetFelixBundleClassLoader(Bundle bundle) {
		// assume felix:

		try {
			// now get the current module from the bundle.
			// and return the private field m_classLoader of ModuleImpl
			if (getRevisionMethod == null) {
				try {
					getRevisionMethod = loadClass(bundle, "org.apache.felix.framework.BundleImpl").getDeclaredMethod(
							"getRevisions");
				} catch (NoSuchMethodException e) {
					throw new IllegalStateException(
							"BundleImpl.getRevsions() method could not be found. Does this bundle run inside a Felix 4.2 container ?",
							e);
				}
				getRevisionMethod.setAccessible(true);
			}

			// Figure out which version of the modules is exported
			List<?> revisions = (List<?>) getRevisionMethod.invoke(bundle);
			Object currentRevision = revisions.get(0);

			if (getWiringMethod == null && currentRevision != null) {
				try {
					getWiringMethod = loadClass(bundle, "org.osgi.framework.wiring.BundleRevision").getDeclaredMethod(
							"getWiring");
				} catch (NoSuchMethodException e) {
					throw new IllegalStateException(
							"BundleRevision.getWiring() method could not be found. Does this bundle run inside a Felix 4.2 container ?",
							e);
				}
				getWiringMethod.setAccessible(true);
			}

			Object wiring = getWiringMethod.invoke(currentRevision);	// NOSONAR the code above guarantees that an exception would be thrown before that line

			if (getClassLoaderMethod == null && wiring != null) {
				try {
					getClassLoaderMethod = loadClass(bundle, "org.osgi.framework.wiring.BundleWiring")
							.getDeclaredMethod("getClassLoader");
				} catch (NoSuchMethodException e) {
					throw new IllegalStateException(
							"BundleWiring.getClassLoader() method could not be found. Does this bundle run inside a Felix 4.2 container ?",
							e);
				}
				getClassLoaderMethod.setAccessible(true);
			}

			ClassLoader cl = (ClassLoader) getClassLoaderMethod.invoke(wiring);

			return cl;

		} catch (IllegalAccessException e) {
			// should not happen
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();

			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			}
			throw new RuntimeException(cause);
		}
	}

	private static Class<?> loadClass(Bundle bundle, String className) {
		try {
			return bundle.getClass().getClassLoader().loadClass(className);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(className
					+ " could not be loaded. Does this bundle run inside a Felix 4.2 container ?", e);
		}
	}
}
