/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.web.internal.context;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * This specialization of {@link ReloadableResourceBundleMessageSource} registers <strong>message.properties</strong>
 * files from fragments looking up into standardized folders "/WEB-INF/messages/<wizard-name>/"
 * 
 * @author Gregory Fouquet
 * 
 */
public class ReloadableSquashTmMessageSource extends ReloadableResourceBundleMessageSource implements
		ResourceLoaderAware, InitializingBean {
	private static final Logger LOGGER = LoggerFactory.getLogger(ReloadableSquashTmMessageSource.class);
	/**
	 * Resource pattern to scan for messages embedded into plugins / fragments
	 */
	private static final String PLUGIN_MESSAGES_SCAN_PATTERN = "WEB-INF/messages/**";
	/**
	 * Base path for looked up message.properties files
	 */
	private static final String MESSAGES_BASE_PATH = "/WEB-INF/messages/";
	private ResourcePatternResolver resourcePatternResolver;
	private String[] basenames;

	/**
	 * @see org.springframework.context.support.ReloadableResourceBundleMessageSource#setResourceLoader(org.springframework.core.io.ResourceLoader)
	 */
	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		super.setResourceLoader(resourceLoader);
		if (resourceLoader instanceof ResourcePatternResolver) {
			this.resourcePatternResolver = (ResourcePatternResolver) resourceLoader;
		} else {
			this.resourcePatternResolver = new PathMatchingResourcePatternResolver(resourceLoader);
		}
	}

	/**
	 * @see org.springframework.context.support.ReloadableResourceBundleMessageSource#setBasenames(java.lang.String[])
	 */
	@Override
	public void setBasenames(String... basenames) {
		this.basenames = basenames;
		super.setBasenames(basenames);
	}

	private void registerFragmentMessageProperties() {
		try {
			Set<String> consolidatedBasenames = new LinkedHashSet<String>();

			LOGGER.debug("About to register configured basenames to build MessageSource");
			addConfiguredBasenames(consolidatedBasenames);

			// in runtime environment, directories are not resolved using path "WEB-INF/messages/*", hence the catch-all
			// pattern and then filter on directories
			LOGGER.debug("About to scan {} for additional fragment / plugin basenames", PLUGIN_MESSAGES_SCAN_PATTERN);
			addLookedUpBasenames(consolidatedBasenames);

			super.setBasenames(consolidatedBasenames.toArray(new String[] {}));
		} catch (IOException e) {
			LOGGER.warn("Error during bean initialization, no fragment messages will be registered.", e);
		}
	}

	private void addLookedUpBasenames(Set<String> consolidatedBasenames) throws IOException {
		Resource[] resources = resourcePatternResolver.getResources(PLUGIN_MESSAGES_SCAN_PATTERN);

		for (Resource resource : resources) {
			if (isFirstLevelDirectory(resource)) {
				String basename = MESSAGES_BASE_PATH + resource.getFilename() + "/messages";
				consolidatedBasenames.add(basename);

				LOGGER.info("Registering *discovered* path {} as a basename for application MessageSource", basename);
			}

		}
	}

	private void addConfiguredBasenames(Set<String> consolidatedBasenames) {
		if (basenames != null) {
			for (String basename : basenames) {
				consolidatedBasenames.add(basename);
				LOGGER.info("Registering *configured* path {} as a basename for application MessageSource", basename);
			}
		}
	}

	private boolean isFirstLevelDirectory(Resource resource) throws IOException {
		if (!resource.exists()) {
			return false;
		}

		// in runtime env we do not work with file (exception) so we have to use URLs
		URL url = resource.getURL();
		return url.getPath().endsWith(MESSAGES_BASE_PATH + resource.getFilename() + '/');
	}

	/**
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public final void afterPropertiesSet() {
		registerFragmentMessageProperties();
		
	}
}
