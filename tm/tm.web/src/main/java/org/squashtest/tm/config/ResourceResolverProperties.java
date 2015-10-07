/**
 * This file is part of the Squashtest platform.
 * Copyright (C) 2010 - 2015 Henix, henix.fr
 * <p/>
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * <p/>
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * this software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software.  If not, see &lt;http://www.gnu.org/licenses/&gt;.
 */
package org.squashtest.tm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Gregory Fouquet
 * @since 1.13.0
 */
@ConfigurationProperties(prefix = "spring.resource-resolvers", ignoreUnknownFields = false)
public class ResourceResolverProperties {
	private boolean cache;

	public boolean isCache() {
		return cache;
	}

	public void setCache(boolean cache) {
		this.cache = cache;
	}
}
