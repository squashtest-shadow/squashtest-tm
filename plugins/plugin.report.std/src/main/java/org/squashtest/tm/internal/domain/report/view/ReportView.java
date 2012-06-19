/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.tm.internal.domain.report.view;

/**
 * This class defines a view for a given report. Note that many of its informations are given as 
 * a key for a MessageSource if needed.
 * 
 * @author bsiri
 *
 */

/*
 * FIXME : for now the "model" is just the name of the bean for the view.
 * 
 */

public class ReportView {
	
	
	private String titleKey;
	private String codeKey;
	private String model;
	private String[] formats;
	
	public ReportView(){
		
	}
	
	public ReportView(String titleKey, String codeKey, String model,
			String[] formats) {
		super();
		this.titleKey = titleKey;
		this.codeKey = codeKey;
		this.model = model;
		this.formats = formats;
	}


	public String getTitleKey() {
		return titleKey;
	}
	
	
	public String getCodeKey() {
		return codeKey;
	}
	
	
	public String getModel() {
		return model;
	}
	
	
	public String[] getFormats() {
		return formats;
	}


	public ReportView setTitleKey(String titleKey) {
		this.titleKey = titleKey;
		return this;
	}


	public ReportView setCodeKey(String codeKey) {
		this.codeKey = codeKey;
		return this;
	}


	public ReportView setModel(String model) {
		this.model = model;
		return this;
	}


	public ReportView setFormats(String... formats) {
		this.formats = formats;
		return this;
	}
	
	
	
}
