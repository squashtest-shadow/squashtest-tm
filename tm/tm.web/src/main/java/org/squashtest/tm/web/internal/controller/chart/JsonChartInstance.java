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
package org.squashtest.tm.web.internal.controller.chart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.squashtest.tm.domain.chart.AxisColumn;
import org.squashtest.tm.domain.chart.ChartDefinition;
import org.squashtest.tm.domain.chart.ChartInstance;
import org.squashtest.tm.domain.chart.ChartSeries;
import org.squashtest.tm.domain.chart.ChartType;
import org.squashtest.tm.domain.chart.ColumnPrototype;
import org.squashtest.tm.domain.chart.MeasureColumn;


/*
 * THIS CLASS IS DISPOSABLE AND WAS CREATED FOR QUCIK'N DIRTY RESTITUTION PURPOSES
 */
public class JsonChartInstance {

	private String name;

	private ChartType type;

	private List<JsonMeasureColumn> measures = new ArrayList<>();

	private List<JsonAxisColumn> axes = new ArrayList<>();

	private List<Object[]> abscissa = new ArrayList<>();

	private Map<String, List<Object>> series = new HashMap<>();


	public JsonChartInstance() {
		super();
	}

	public JsonChartInstance(ChartInstance instance){
		ChartDefinition def = instance.getDefinition();
		this.name = def.getName();
		this.type = def.getType();

		for (AxisColumn ax : def.getAxis()){
			axes.add(new JsonAxisColumn(ax));
		}

		for (MeasureColumn me : def.getMeasures()){
			measures.add(new JsonMeasureColumn(me));
		}

		ChartSeries series = instance.getSeries();

		this.abscissa = series.getAbscissa();

		this.series = series.getSeries();


	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ChartType getType() {
		return type;
	}

	public void setType(ChartType type) {
		this.type = type;
	}

	public List<JsonMeasureColumn> getMeasures() {
		return measures;
	}

	public void setMeasures(List<JsonMeasureColumn> measures) {
		this.measures = measures;
	}

	public List<JsonAxisColumn> getAxes() {
		return axes;
	}

	public void setAxes(List<JsonAxisColumn> axes) {
		this.axes = axes;
	}

	public List<Object[]> getAbscissa() {
		return abscissa;
	}

	public void setAbscissa(List<Object[]> abscissa) {
		this.abscissa = abscissa;
	}

	public Map<String, List<Object>> getSeries() {
		return series;
	}

	public void setSeries(Map<String, List<Object>> series) {
		this.series = series;
	}



	public static final class JsonMeasureColumn{

		private String label;

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public JsonMeasureColumn() {
			super();
		}

		public JsonMeasureColumn(MeasureColumn measure){
			super();
			this.label = measure.getLabel();
		}


	}

	public static final class JsonAxisColumn{

		private String label;
		private JsonColumnPrototype columnPrototype;

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public JsonColumnPrototype getColumnPrototype() {
			return columnPrototype;
		}

		public void setColumnPrototype(JsonColumnPrototype columnPrototype) {
			this.columnPrototype = columnPrototype;
		}

		public JsonAxisColumn() {
			super();
		}

		public JsonAxisColumn(AxisColumn axis){
			super();
			this.label = axis.getLabel();
			this.setColumnPrototype(new JsonColumnPrototype(axis.getColumn()));
		}

	}
	
	public static final class JsonColumnPrototype{
		
		private String label;
		
		public JsonColumnPrototype(ColumnPrototype column) {
			this.label = column.getLabel();
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}
		
	}

}
