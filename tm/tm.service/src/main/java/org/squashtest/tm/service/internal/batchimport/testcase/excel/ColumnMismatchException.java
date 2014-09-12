/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.service.internal.batchimport.testcase.excel;

import org.squashtest.tm.service.batchimport.excel.ColumnMismatch;

/**
 * Thrown when an import file's worksheet has a {@link ColumnMismatch}
 * 
 */
public class ColumnMismatchException extends RuntimeException {

	private ColumnMismatch type ;
	private TemplateColumn colType;
	public ColumnMismatchException() {
		super();
	}


	public ColumnMismatchException(ColumnMismatch type, TemplateColumn colType) {
		this();
		this.type = type;
		this.setColType(colType);
	}

	public ColumnMismatch getType() {
		return type;
	}
	public void setType(ColumnMismatch type) {
		this.type = type;
	}


	public TemplateColumn getColType() {
		return colType;
	}


	public void setColType(TemplateColumn colType) {
		this.colType = colType;
	}

}
