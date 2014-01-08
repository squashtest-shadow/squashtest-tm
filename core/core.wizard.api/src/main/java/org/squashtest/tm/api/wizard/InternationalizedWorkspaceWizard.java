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
package org.squashtest.tm.api.wizard;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.osgi.context.BundleContextAware;
import org.squashtest.tm.api.plugin.EntityReference;
import org.squashtest.tm.api.widget.MenuItem;
import org.squashtest.tm.api.workspace.WorkspaceType;
import org.squashtest.tm.core.foundation.i18n.ContextBasedInternationalized;
import org.squashtest.tm.core.foundation.lang.Assert;

/**
 * @author Gregory Fouquet
 * 
 */
public class InternationalizedWorkspaceWizard extends ContextBasedInternationalized implements WorkspaceWizard,
		BeanNameAware, BundleContextAware {

	private WorkspaceType displayWorkspace;
	private MenuItem wizardMenu;
	private String id;

	private String filename;
	private String version;

	public void setBundleContext(BundleContext bundleContext) {
		version = bundleContext.getBundle().getVersion().toString();
		filename = bundleContext.getBundle().getSymbolicName(); // TODO : make better than this
	}

	/**
	 * i18n key of this wizard's name
	 */
	private String nameKey;

	
	@Override
	public Map<String, String> getProperties(){
		return new HashMap<String, String>();
	}
	
	/**
	 * @see org.squashtest.tm.api.wizard.WorkspaceWizard#getDisplayWorkspace()
	 */
	@Override
	public WorkspaceType getDisplayWorkspace() {
		return displayWorkspace;
	}

	/**
	 * @see org.squashtest.tm.api.wizard.WorkspaceWizard#getWizardMenu()
	 */
	@Override
	public MenuItem getWizardMenu() {
		return wizardMenu;
	}

	/**
	 * @see org.squashtest.tm.api.wizard.WorkspaceWizard#getName()
	 */
	@Override
	public String getName() {
		return getMessage(nameKey);
	}

	/**
	 * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
	 */
	@Override
	public void setBeanName(String name) {
		id = name;

	}

	/**
	 * @see org.squashtest.tm.api.wizard.WizardPlugin#getId()
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

	/**
	 * @param wizardMenu
	 *            the wizardMenu to set
	 */
	public void setWizardMenu(MenuItem wizardMenu) {
		this.wizardMenu = wizardMenu;
	}

	/**
	 * @param displayWorkspace
	 *            the displayWorkspace to set
	 */
	public void setDisplayWorkspace(WorkspaceType displayWorkspace) {
		this.displayWorkspace = displayWorkspace;
	}

	/**
	 * This default validation always passes.
	 * 
	 * @see org.squashtest.tm.api.wizard.WorkspaceWizard#validate(EntityReference)
	 */
	@Override
	public void validate(EntityReference reference) {
		// defaults : allways passes
	}

	@PostConstruct
	public void checkBeanState() {
		Assert.propertyNotBlank(nameKey, "nameKey property should not be blank");
		Assert.propertyNotNull(wizardMenu, "wizardMenu property should not be null");
		Assert.propertyNotNull(displayWorkspace, "displayWorkspace property should not be null");
	}
}
