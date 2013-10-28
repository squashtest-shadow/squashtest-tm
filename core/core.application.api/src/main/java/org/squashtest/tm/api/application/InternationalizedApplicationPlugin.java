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
package org.squashtest.tm.api.application;

import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.osgi.context.BundleContextAware;
import org.squashtest.tm.api.widget.NavigationButton;
import org.squashtest.tm.core.foundation.i18n.ContextBasedInternationalized;

/**
 * @author mpagnon
 * 
 */
public class InternationalizedApplicationPlugin extends ContextBasedInternationalized implements ApplicationPlugin,
		BeanNameAware, BundleContextAware {

private String id;

	private String filename;
	private String version;
	private NavigationButton navbarMenu;

	public void setBundleContext(BundleContext bundleContext) {
		version = bundleContext.getBundle().getVersion().toString();
		filename = bundleContext.getBundle().getSymbolicName(); // TODO : make better than this
	}

	/**
	 * i18n key of this wizard's name
	 */
	private String nameKey;


	/**
	 * @see org.squashtest.tm.api.application.ApplicationPlugin#getName()
	 */
	@Override
	public String getName() {
		return getMessage(nameKey);
	}
	/**
	 * @param navBarMenu
	 *            the navBarMenu to set
	 */
	public void setNavBarMenu(NavigationButton navbarMenu) {
		this.navbarMenu = navbarMenu;
	}
	/**
	 * @see org.squashtest.tm.api.wizard.ApplicationPlugin#getNavBarMenu()
	 */
	@Override
	public NavigationButton getNavBarMenu() {
		return navbarMenu;
	}

	/**
	 * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
	 */
	@Override
	public void setBeanName(String name) {
		id = name;

	}

	/**
	 * @see org.squashtest.tm.api.application.ApplicationPlugin#getId()
	 */
	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getFilename() {
		return filename;
	}

	@Override
	public String getVersion() {
		return version;
	}

	/**
	 * @param nameKey
	 *            the nameKey to set
	 */
	public void setNameKey(String nameKey) {
		this.nameKey = nameKey;
	}



}
