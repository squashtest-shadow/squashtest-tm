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
package org.squashtest.tm.service.internal.charts

import spock.lang.Specification
import org.squashtest.tm.service.charts.Column
import org.squashtest.tm.service.charts.PerimeterQuery
import static org.squashtest.tm.service.charts.Datatype.*


class TupleAggregatorTest extends Specification {

	def "should aggregate two tuples"(){

		given :
		def tuple1 = ["Berlin", 15, 15] as Object[]
		def tuple2 = ["Berlin", 2, 2] as Object[]

		and :
		def axes = [ new Column("town-name", STRING, "town name", "town.name")];
		def data = [ new Column("person-name", STRING, "person name", "p.name" )];
		def perimeter = new PerimeterQuery(axes : axes, data : data);

		and :
		def agg = new TupleAggregator()

		when :
		def res = agg.aggregate(perimeter, tuple1, tuple2)

		then :
		res == ["Berlin", 17, 17]

	}

}
