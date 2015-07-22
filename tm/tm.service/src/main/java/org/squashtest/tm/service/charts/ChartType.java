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
package org.squashtest.tm.service.charts;

public enum ChartType {

	SINGLE_TABLE(){

		@Override
		int getAxesNumber() {
			return 1;
		}

		@Override
		int getDataNumber() {
			return 1;
		}

	},CROSS_TABLE(){

		@Override
		int getAxesNumber() {
			return 2;
		}

		@Override
		int getDataNumber() {
			return 1;
		}

	},
	LINE_CHART(){

		@Override
		int getAxesNumber() {
			return 1;
		}

		@Override
		int getDataNumber() {
			return -1;
		}

	},
	PIE_CHART(){

		@Override
		int getAxesNumber() {
			return -1;
		}

		@Override
		int getDataNumber() {
			return 1;
		}

	},
	BAR_CHART(){

		@Override
		int getAxesNumber() {
			return 1;
		}

		@Override
		int getDataNumber() {
			return -1;
		}

	};


	/**
	 * Says how many axis may appear in that chart. -1 means "unlimited".
	 * 
	 * @return
	 */
	abstract int getAxesNumber();

	/**
	 * Says how many data (lines, cell values etc) are associated to this chart type.
	 * -1 means "unlimited".
	 * @return
	 */
	abstract int getDataNumber();

}
