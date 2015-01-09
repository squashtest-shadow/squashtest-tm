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
package org.squashtest.tm.web.internal.helper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonHelper {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private JsonHelper() {
		super();
	}

	public static String serialize(Object value) throws JsonMarshallerException {
		try {
			return OBJECT_MAPPER.writeValueAsString(value);
		} catch (IOException e) {
			throw new JsonMarshallerException(e);
		}
	}

	public static Map<String, Object> deserialize(String json) throws JsonParseException, JsonMappingException,
	IOException {
		TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
		};
		return OBJECT_MAPPER.readValue(json, typeRef);
	}

	/**
	 * alias for {@link #serialize(Object)}
	 * 
	 * @param value
	 * @return
	 */
	public static String marshall(Object value) throws JsonMarshallerException {
		return serialize(value);
	}

	/**
	 * alias for {@link #deserialize(String)}
	 * 
	 * @param json
	 * @return
	 */
	public static Map<String, Object> unmarshall(String json) throws JsonParseException, JsonMappingException, IOException {
		return deserialize(json);
	}
}
