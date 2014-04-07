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
 * Coerces a cell to an Integer value. As values are extracted as float, they are rounded to the closest integer.
 * 
 * When a cell is of string type, this coercer shall try to parse the cell content as a number. When the cell is not
 * parseable, it throws TBD exception.
 * 
 * When a cell is empty, this coercer returns null
 * 
 * 
 * 
 * @author Gregory Fouquet
 * 
 */
public class OptionalIntegerCellCoercer implements CellValueCoercer<Integer> {
	public static final OptionalIntegerCellCoercer INSTANCE = new OptionalIntegerCellCoercer();

	protected OptionalIntegerCellCoercer() {
		super();
	}

	/**
	 * @return the cell content as an Integer, <code>null</code> when empty.
	 * @throws TBD
	 *             when non numeric cell is not parseable.
	 * @see org.squashtest.tm.service.internal.batchimport.excel.CellValueCoercer#coerce(org.apache.poi.ss.usermodel.Cell)
	 */
	@Override
	public Integer coerce(Cell cell) {
		int type = cell.getCellType();
		Integer res;

		switch (type) {
		case Cell.CELL_TYPE_NUMERIC:
			res = coerceNumericCell(cell);
			break;

		case Cell.CELL_TYPE_STRING:
			res = coerceStringCell(cell);
			break;

		case Cell.CELL_TYPE_BLANK:
			res = null;
			break;
		default:
			try {
				cell.getNumericCellValue();
			} catch (RuntimeException e) {
				throw new CannotCoerceException(e);
			}
			// we should never get here, ex should be thrown above
			throw new CannotCoerceException("Funky cell type " + type + " is not coercible to an Integer");
		}

		return res;
	}

	protected Integer coerceStringCell(Cell cell) throws NumberFormatException {
		Integer res;
		String val = cell.getStringCellValue();
		try {
			res = Integer.valueOf(val, 10);
		} catch (NumberFormatException e) {
			try {
				res = round(Double.valueOf(val));
			} catch (NumberFormatException ex) {
				throw new CannotCoerceException(ex);
			}
		}
		return res;
	}

	protected Integer coerceNumericCell(Cell cell) {
		Integer res;
		double val = cell.getNumericCellValue();
		res = round(val);
		return res;
	}

	private Integer round(double val) {
		Integer res;
		res = Integer.valueOf((int) Math.round(val));
		return res;
	}

}
