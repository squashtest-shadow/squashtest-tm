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
package org.squashtest.tm.service.importer;

public class LogEntry implements Comparable<LogEntry> {
	private Integer line;
	private Target target;
	private ImportMode mode;
	private ImportStatus status;
	private String i18nError;
	private String i18nImpact;

	private Object[] errorArgs;
	private Object[] impactArgs;

	public static final class Builder {
		private final LogEntry product;

		private Builder(ImportStatus status) {
			product = new LogEntry(null, status, null);
		}

		public Builder forTarget(Target tgt) {
			product.target = tgt;
			return this;
		}

		public Builder atLine(int line) {
			product.line = line;
			return this;
		}

		public Builder withMessage(String key, Object... args) {
			product.i18nError = key;
			product.errorArgs = args;
			return this;
		}

		public Builder withImpact(String key, Object... args) {
			product.i18nImpact = key;
			product.impactArgs = args;
			return this;
		}

		public LogEntry build() {
			return product;
		}
	}

	public static Builder failure() {
		return new Builder(ImportStatus.FAILURE);
	}

	public static Builder warning() {
		return new Builder(ImportStatus.WARNING);
	}

	public LogEntry(Target target, ImportStatus status, String i18nError) {
		super();
		this.target = target;
		this.status = status;
		this.i18nError = i18nError;
	}

	public LogEntry(Target target, ImportStatus status, String i18nError, String i18nImpact) {
		this(target, status, i18nError);
		this.i18nImpact = i18nImpact;
	}

	public LogEntry(Target target, ImportStatus status, String i18nError, Object[] errorArgs) {
		this(target, status, i18nError);
		this.errorArgs = errorArgs;
	}

	public LogEntry(Target target, ImportStatus status, String i18nError, Object[] errorArgs, String i18nImpact,
			Object[] impactArgs) {
		this(target, status, i18nError, i18nImpact);
		this.errorArgs = errorArgs;
		this.impactArgs = impactArgs;
	}

	public LogEntry(Integer line, Target target, ImportMode mode, ImportStatus status, String i18nError,
			String i18nImpact) {
		this(target, status, i18nError, i18nImpact);
		this.mode = mode;
		this.line = line;
	}

	public Object[] getErrorArgs() {
		return errorArgs;
	}

	public void setErrorArgs(Object... errorArgs) {
		this.errorArgs = errorArgs;
	}

	public Object[] getImpactArgs() {
		return impactArgs;
	}

	public void setImpactArgs(Object... impactArgs) {
		this.impactArgs = impactArgs;
	}





	@Override
	public int compareTo(LogEntry o) {
		if (!line.equals(o.line)) {
			return line - o.line;
		} else if (status != o.getStatus()){
			return (status.getLevel() - o.getStatus().getLevel());
		}
		else{
			// even when two instances have strictly same content we don't want to consider them equal.
			// note that returning -1 is not an ideal solution because it violates the Comparable contract
			// x.compareTo(y) == - y.compareTo(x) but it's good enough here
			return -1;
		}
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

	public ImportStatus getStatus() {
		return status;
	}

	public String getI18nError() {
		return i18nError;
	}

	public String getI18nImpact() {
		return i18nImpact;
	}

	public void setI18nImpact(String i18nImpact) {
		this.i18nImpact = i18nImpact;
	}

	public void setTarget(Target target) {
		this.target = target;
	}
}
