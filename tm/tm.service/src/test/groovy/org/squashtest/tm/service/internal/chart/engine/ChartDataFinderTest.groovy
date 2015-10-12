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
package org.squashtest.tm.service.internal.chart.engine

import spock.lang.Specification
import com.querydsl.core.Tuple
import org.squashtest.tm.domain.chart.AxisColumn
import org.squashtest.tm.domain.chart.ChartSeries;
import org.squashtest.tm.domain.chart.MeasureColumn


class ChartDataFinderTest extends Specification {

	def "should build a ChartSeries from a result set"(){

		given : "the definition"
		DetailedChartDefinition definition = new DetailedChartDefinition(
				measures : [
					measure("total steps"),
					measure("total requirements")
				],
				axis : [
					axis("project label"),
					axis("test case importance")
				]
				)


		and : "the tuples"
		def tuples = [
			tuple("project1", "HIGH", 40, 8),
			tuple("project1", "MEDIUM", 84, 15),
			tuple("project1", "LOW", 100, 35),
			tuple("project2", "VERY_HIGH", 15, 56),
			tuple("project2", "HIGH", 35, 10)
		]

		and : "the rest"

		ChartDataFinder finder = new ChartDataFinder()

		when :

		ChartSeries series = finder.makeSeries(definition, tuples)

		then :
		series.abscissa == [
			["project1", "HIGH"] as Object[],
			["project1", "MEDIUM"] as Object[],
			["project1", "LOW"] as Object[],
			["project2", "VERY_HIGH"] as Object[],
			["project2", "HIGH"] as Object[],
		]

		series.series == [
			"total steps" : [40,84,100,15,35],
			"total requirements" : [8,15,35,56,10]
		]




	}

	def measure(label) {
		MeasureColumn m = Mock(MeasureColumn)
		m.getLabel() >> label
		m
	}

	def axis(label){
		AxisColumn a = Mock(AxisColumn)
		a.getLabel()>>label
		a
	}

	def tuple(Object... values){
		Tuple t = Mock(Tuple)
		t.get(_,_) >> { idx, type -> values[idx]}
		return t
	}
}
