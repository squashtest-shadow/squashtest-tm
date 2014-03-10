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
package org.squashtest.tm.service.internal.batchimport;

public class LogEntry implements Comparable<LogEntry>{
	private Integer line;
	private Target target;
	private ImportMode mode;
	private ImportStatus status;
	private String i18nError;
	private String i18nImpact;
	
	private Object[] errorArgs;
	private Object[] impactArgs;
	
	
	public LogEntry() {
		super();
	}
	
	
	

	public LogEntry(Target target, ImportStatus status,
			String i18nError) {
		super();
		this.target = target;
		this.status = status;
		this.i18nError = i18nError;
	}




	public LogEntry(Target target, ImportStatus status, String i18nError,
			String i18nImpact) {
		super();
		this.target = target;
		this.status = status;
		this.i18nError = i18nError;
		this.i18nImpact = i18nImpact;
	}


	


	public LogEntry(Target target, ImportStatus status, String i18nError,
			Object[] errorArgs) {
		super();
		this.target = target;
		this.status = status;
		this.i18nError = i18nError;
		this.errorArgs = errorArgs;
	}




	public LogEntry(Target target, ImportStatus status,
			String i18nError, Object[] errorArgs, String i18nImpact,
			Object[] impactArgs) {
		super();
		this.target = target;
		this.status = status;
		this.i18nError = i18nError;
		this.i18nImpact = i18nImpact;
		this.errorArgs = errorArgs;
		this.impactArgs = impactArgs;
	}




	public LogEntry(Integer line, Target target, ImportMode mode,
			ImportStatus status, String i18nError, String i18nImpact) {
		super();
		this.line = line;
		this.target = target;
		this.mode = mode;
		this.status = status;
		this.i18nError = i18nError;
		this.i18nImpact = i18nImpact;
	}

	
	


	public Object[] getErrorArgs() {
		return errorArgs;
	}




	public void setErrorArgs(Object[] errorArgs) {
		this.errorArgs = errorArgs;
	}




	public Object[] getImpactArgs() {
		return impactArgs;
	}




	public void setImpactArgs(Object[] impactArgs) {
		this.impactArgs = impactArgs;
	}




	@Override
	public int compareTo(LogEntry o) {
		return line - o.line;
	}

	public Integer getLine() {
		return line;
	}
	
	public void setLine(Integer line) {
		this.line = line;
	}
	
	public ImportMode getMode() {
		return mode;
	}
	
	public void setMode(ImportMode mode) {
		this.mode = mode;
	}
	
	public Target getTarget() {
		return target;
	}
	
	public void setTarget(Target target) {
		this.target = target;
	}
	
	public ImportStatus getStatus() {
		return status;
	}
	
	public void setStatus(ImportStatus status) {
		this.status = status;
	}
	
	public String getI18nError() {
		return i18nError;
	}
	
	public void setI18nError(String i18nError) {
		this.i18nError = i18nError;
	}
	
	public String getI18nImpact() {
		return i18nImpact;
	}
	
	public void setI18nImpact(String i18nImpact) {
		this.i18nImpact = i18nImpact;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 47;
		result = prime * result + line;
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LogEntry other = (LogEntry) obj;
		if (line != other.line)
			return false;
		return true;
	}
	


}
