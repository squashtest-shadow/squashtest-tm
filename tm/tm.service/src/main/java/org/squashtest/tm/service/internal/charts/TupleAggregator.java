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
package org.squashtest.tm.service.internal.charts;

import org.squashtest.tm.service.charts.PerimeterQuery;

class TupleAggregator {

	// TODO : something smarter than merely adding the data.
	// the operation in the select clause might be different than count()
	Object[] aggregate(PerimeterQuery perimeter, Object[] tuple1, Object[] tuple2){

		Object[] newRes = new Object[tuple1.length];

		int axesend = perimeter.getAxes().size();
		int dataend = perimeter.getData().size();

		// about the axes : just copy them. They are supposed to be the same for tuple1 and tuple 2
		for (int i=0; i< axesend; i++){
			newRes[i] = tuple1[i];
		}

		// about the data : just add them for now
		for (int i=axesend; i<dataend; i++){
			newRes[i] = (Long)tuple1[i] + (Long)tuple2[i];
		}

		// the rest : add them
		for (int i=dataend; i< tuple1.length; i++){
			newRes[i] = (Long)tuple1[i] + (Long)tuple2[i];
		}

		return newRes;
	}

}
