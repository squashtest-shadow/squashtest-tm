/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.chart.AxisColumn;
import org.squashtest.tm.domain.chart.ChartDefinition;
import org.squashtest.tm.domain.chart.ChartInstance;
import org.squashtest.tm.domain.chart.ChartSeries;
import org.squashtest.tm.domain.chart.ChartType;
import org.squashtest.tm.domain.chart.ColumnPrototype;
import org.squashtest.tm.domain.chart.DataType;
import org.squashtest.tm.domain.chart.Filter;
import org.squashtest.tm.domain.chart.MeasureColumn;
import org.squashtest.tm.domain.chart.Operation;
import org.squashtest.tm.domain.chart.SpecializedEntityType;
import org.squashtest.tm.domain.chart.SpecializedEntityType.EntityRole;



public class JsonChartInstance {

	private String name;

	private ChartType type;

	private String createdBy;

	private String lastModifiedBy;

	private Date createdOn;

	private Date lastModifiedOn;

	private List<JsonMeasureColumn> measures = new ArrayList<>();

	private List<JsonAxisColumn> axes = new ArrayList<>();

	private List<JsonFilter> filters = new ArrayList<>();

	private List<Object[]> abscissa = new ArrayList<>();

	private Map<String, List<Object>> series = new HashMap<>();

        private List<String> projectScope = new ArrayList<>();
        
	private List<JsonEntityReference> scope = new ArrayList<>();

	public JsonChartInstance() {
		super();
	}

	public JsonChartInstance(ChartInstance instance){
		ChartDefinition def = instance.getDefinition();
		this.name = def.getName();
		this.type = def.getType();
		doAuditableAttributes(def);

		for (AxisColumn ax : def.getAxis()){
			axes.add(new JsonAxisColumn(ax));
		}

		for (MeasureColumn me : def.getMeasures()){
			measures.add(new JsonMeasureColumn(me));
		}

		for (Filter filter : def.getFilters()) {
			filters.add(new JsonFilter(filter));
		}

		for (EntityReference ref : def.getScope()) {
			scope.add(new JsonEntityReference(ref,def));
		}
                
                this.projectScope = instance.getDefinition().getProjectScope();

		ChartSeries series = instance.getSeries();

		this.abscissa = series.getAbscissa();

		this.series = series.getSeries();

	}

	private void doAuditableAttributes(ChartDefinition def) {
		AuditableMixin audit = (AuditableMixin) def;
		this.createdBy = audit.getCreatedBy();
		this.lastModifiedBy = audit.getLastModifiedBy();
		this.createdOn = audit.getCreatedOn();
		this.lastModifiedOn = audit.getLastModifiedOn();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public Date getLastModifiedOn() {
		return lastModifiedOn;
	}

	public void setLastModifiedOn(Date lastModifiedOn) {
		this.lastModifiedOn = lastModifiedOn;
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



	public List<JsonFilter> getFilters() {
		return filters;
	}

	public void setFilters(List<JsonFilter> filters) {
		this.filters = filters;
	}



	public List<JsonEntityReference> getScope() {
		return scope;
	}

	public void setScope(List<JsonEntityReference> scope) {
		this.scope = scope;
	}

        public List<String> getProjectScope() {
            return projectScope;
        }

        public void setProjectScope(List<String> projectScope) {
            this.projectScope = projectScope;
        }
        
	public static final class JsonMeasureColumn{

		private String label;

		private JsonColumnPrototype columnPrototype;

		private JsonOperation operation;

		public JsonMeasureColumn() {
			super();
		}

		public JsonMeasureColumn(MeasureColumn measure){
			super();
			this.label = measure.getLabel();
			this.columnPrototype = new JsonColumnPrototype(measure.getColumn());
			this.operation = new JsonOperation(measure.getOperation());
		}

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

		public JsonOperation getOperation() {
			return operation;
		}

		public void setOperation(JsonOperation operation) {
			this.operation = operation;
		}


	}

	public static final class JsonAxisColumn{

		private String label;
		private JsonColumnPrototype columnPrototype;
		private JsonOperation operation;

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

		public JsonOperation getOperation() {
			return operation;
		}

		public void setOperation(JsonOperation operation) {
			this.operation = operation;
		}

		public JsonAxisColumn() {
			super();
		}

		public JsonAxisColumn(AxisColumn axis){
			super();
			this.label = axis.getLabel();
			this.setColumnPrototype(new JsonColumnPrototype(axis.getColumn()));
			this.setOperation(new JsonOperation(axis.getOperation()));
		}

	}

	public static final class JsonColumnPrototype{

		private String label;

		private JsonSpecializedEntityType specializedEntityType;

		private DataType dataType;

		public JsonColumnPrototype(ColumnPrototype column) {
			this.label = column.getLabel();
			this.specializedEntityType = new JsonSpecializedEntityType(column.getSpecializedType());
			this.dataType = column.getDataType();
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public JsonSpecializedEntityType getSpecializedEntityType() {
			return specializedEntityType;
		}

		public void setSpecializedEntityType(
				JsonSpecializedEntityType specializedEntityType) {
			this.specializedEntityType = specializedEntityType;
		}

		public DataType getDataType() {
			return dataType;
		}

		public void setDataType(DataType dataType) {
			this.dataType = dataType;
		}

	}

	public static final class JsonOperation{

		private String name;

		public JsonOperation(Operation operation) {
			this.name = operation.name();
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

	}

	public static final class JsonEntityReference{
		private EntityType entityType;
		private long id;
		private String name;

		public JsonEntityReference(EntityReference entityReference, ChartDefinition def) {
			this.entityType = entityReference.getType();
			this.id = entityReference.getId();
			findName(def);
		}

		private void findName(ChartDefinition def) {
			if (entityType == EntityType.PROJECT &&def.getProject()!=null) {
				name = def.getProject().getName();
			}
		}

		public EntityType getEntityType() {
			return entityType;
		}

		public void setEntityType(EntityType entityType) {
			this.entityType = entityType;
		}

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	public static final class JsonSpecializedEntityType {
		private EntityType entityType;
		private EntityRole entityRole;

		public JsonSpecializedEntityType(SpecializedEntityType specializedEntityType) {
			this.entityRole = specializedEntityType.getEntityRole();
			this.entityType = specializedEntityType.getEntityType();
		}

		public EntityType getEntityType() {
			return entityType;
		}

		public void setEntityType(EntityType entityType) {
			this.entityType = entityType;
		}

		public EntityRole getEntityRole() {
			return entityRole;
		}

		public void setEntityRole(EntityRole entityRole) {
			this.entityRole = entityRole;
		}
	}

	public static final class JsonFilter{

		private List<String> values;
		private JsonColumnPrototype columnPrototype;
		private JsonOperation operation;

		public JsonFilter(Filter filter) {
			this.columnPrototype = new JsonColumnPrototype(filter.getColumn());
			this.operation = new JsonOperation(filter.getOperation());
			this.values = filter.getValues();
		}

		public List<String> getValues() {
			return values;
		}
		public void setValues(List<String> values) {
			this.values = values;
		}
		public JsonColumnPrototype getColumnPrototype() {
			return columnPrototype;
		}
		public void setColumnPrototype(JsonColumnPrototype columnPrototype) {
			this.columnPrototype = columnPrototype;
		}
		public JsonOperation getOperation() {
			return operation;
		}
		public void setOperation(JsonOperation operation) {
			this.operation = operation;
		}
	}

}
