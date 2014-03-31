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

package org.squashtest.tm.service.internal.batchimport.excel;

import org.apache.poi.ss.usermodel.Cell;

/**
 * Coerces a plain string cell to a string
 * 
 * @author Gregory Fouquet
 * 
 */
public class StringCellCoercer implements CellValueCoercer<String> {
	public static final StringCellCoercer INSTANCE = new StringCellCoercer();

	private StringCellCoercer() {
		super();
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.excel.CellValueCoercer#coerce(org.apache.poi.ss.usermodel.Cell)
	 */
	@Override
	public String coerce(Cell cell) {
		try {
			return cell.getStringCellValue();
		} catch (IllegalStateException e) {
			throw new CannotCoerceException(e);
		}
	}

}
