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

import org.squashtest.tm.domain.infolist.InfoList;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.web.internal.model.json.InfoListItemMixin;
import org.squashtest.tm.web.internal.model.json.InfoListMixin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Custom implementation of Json deserializer to suit our needs.
 * 
 * @author Gregory Fouquet
 * 
 */
public class SquashObjectMapper extends ObjectMapper {

	private SquashObjectMapper() {
		super();
		// serializes dates as ISO timestamps in GMT timezone
		configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false);

		// configures various domain objects (un)marshalling w/O the use of DTOs or jackson annotations
		addMixInAnnotations(InfoList.class, InfoListMixin.class);
		addMixInAnnotations(InfoListItem.class, InfoListItemMixin.class);
	}

}
