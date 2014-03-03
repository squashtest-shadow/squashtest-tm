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
package org.squashtest.tm.web.internal.controller.project;

import org.squashtest.tm.api.wizard.WorkspaceWizard;
import org.squashtest.tm.api.workspace.WorkspaceType;

public class WorkspaceWizardModel {

	private String id;
	private String label;
	private String type;
	private String version = "TODO";
	private String filename = "TODO";
	private WorkspaceType displayWorkspace;
	
	private String displayableName;
	
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getVersion() {
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	


	public String getDisplayableName() {
		if (displayableName==null){
			//default displayable name
			displayableName = type+" "+label+" v"+version+" ("+filename+")";
		}
		return displayableName;
	}

	public void setDisplayableName(String displayableName) {
		this.displayableName = displayableName;
	}


	public WorkspaceType setDisplayWorkspace() {
		return displayWorkspace;
	}

	public void setDisplayWorkspace(WorkspaceType workspaceType) {
		this.displayWorkspace = workspaceType;
	}

	public WorkspaceWizardModel(){
		super();
	}
	
	public WorkspaceWizardModel(WorkspaceWizard wizard){
		this.id = wizard.getId();
		this.label = wizard.getName();
		this.displayWorkspace = wizard.getDisplayWorkspace();
		this.version = wizard.getVersion();
		this.filename = wizard.getFilename();
	}
		
	
}
