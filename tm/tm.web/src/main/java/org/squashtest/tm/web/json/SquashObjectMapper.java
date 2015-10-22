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
package org.squashtest.tm.web.json;

import org.squashtest.tm.domain.chart.AxisColumn;
import org.squashtest.tm.domain.chart.ChartDefinition;
import org.squashtest.tm.domain.chart.ChartQuery;
import org.squashtest.tm.domain.chart.ColumnPrototype;
import org.squashtest.tm.domain.chart.Filter;
import org.squashtest.tm.domain.chart.MeasureColumn;
import org.squashtest.tm.domain.customreport.CustomReportFolder;
import org.squashtest.tm.domain.customreport.CustomReportLibrary;
import org.squashtest.tm.domain.infolist.InfoList;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.web.internal.model.json.AxisColumnMixin;
import org.squashtest.tm.web.internal.model.json.ChartDefinitionMixin;
import org.squashtest.tm.web.internal.model.json.ChartQueryMixin;
import org.squashtest.tm.web.internal.model.json.ColumnPrototypeMixin;
import org.squashtest.tm.web.internal.model.json.CustomReportFolderMixin;
import org.squashtest.tm.web.internal.model.json.CustomReportLibraryMixin;
import org.squashtest.tm.web.internal.model.json.FilterMixin;
import org.squashtest.tm.web.internal.model.json.GenericProjectMixin;
import org.squashtest.tm.web.internal.model.json.InfoListItemMixin;
import org.squashtest.tm.web.internal.model.json.InfoListMixin;
import org.squashtest.tm.web.internal.model.json.MeasureColumnMixin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module.Feature;

/**
 * Custom implementation of Json deserializer to suit our needs.
 *
 * @author Gregory Fouquet
 *
 */
public class SquashObjectMapper extends ObjectMapper {

	public SquashObjectMapper() {
		super();
		Hibernate4Module module = new Hibernate4Module();
		//Setting jackson tu eager on hibernate proxy... take care to your Mixins to avoid massive request ^^
		module.configure(Feature.FORCE_LAZY_LOADING, true);
		registerModule(module);
		// serializes dates as ISO timestamps in GMT timezone
		configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false);

		// configures various domain objects (un)marshalling w/O the use of DTOs or jackson annotations
		addMixInAnnotations(InfoList.class, InfoListMixin.class);
		addMixInAnnotations(InfoListItem.class, InfoListItemMixin.class);
		addMixInAnnotations(CustomReportLibrary.class, CustomReportLibraryMixin.class);
		addMixInAnnotations(Project.class, GenericProjectMixin.class);
		addMixInAnnotations(CustomReportFolder.class, CustomReportFolderMixin.class);
		addMixInAnnotations(ChartDefinition.class, ChartDefinitionMixin.class);
		addMixInAnnotations(ChartQuery.class, ChartQueryMixin.class);
		addMixInAnnotations(Filter.class, FilterMixin.class);
		addMixInAnnotations(AxisColumn.class, AxisColumnMixin.class);
		addMixInAnnotations(MeasureColumn.class, MeasureColumnMixin.class);
		addMixInAnnotations(ColumnPrototype.class, ColumnPrototypeMixin.class);
	}

}
