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
package org.squashtest.tm.service;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * TODO nosgi that's kind of ugly, have to find something better
 *
 * @author Gregory Fouquet
 * @since 1.13.0
 */
//@Configuration
public class DataSourceConfig {
	@Value("${datasource.db.driverClassName}")
	private String driverClassName;
	@Value("${datasource.db.url}")
	private String url;
	@Value("${datasource.db.username}")
	private String username;
	@Value("${datasource.db.password}")
	private String password;
	@Value("${datasource.db.validationQuery}")
	private String validationQuery;
	@Value("${datasource.db.maxActive}")
	private int maxActive;
	@Value("${datasource.db.initialSize}")
	private int initialSize;

	@Bean(name = "squashtest.core.persistence.jdbc.DataSource", destroyMethod = "close")
	public DataSource dataSource() {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(driverClassName);
		dataSource.setUrl(url);
		dataSource.setUsername(username);
		dataSource.setPassword(password);
		dataSource.setValidationQuery(validationQuery);
		dataSource.setMaxTotal(maxActive);
		dataSource.setInitialSize(initialSize);
		return dataSource;
	}
}
