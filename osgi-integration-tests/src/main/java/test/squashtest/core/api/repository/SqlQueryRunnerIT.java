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
package test.squashtest.core.api.repository;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Component;
import org.squashtest.tm.api.repository.SqlQueryRunner;
import org.squashtest.tool.osgitests.OsgiIntegrationTest;

/**
 * @author Gregory Fouquet
 * 
 */
@Component
public class SqlQueryRunnerIT {

	private static final Logger LOGGER = LoggerFactory.getLogger(SqlQueryRunnerIT.class);
	private static final Object[] NO_ARGS = {};

	private SqlQueryRunner queryRunner;

	@PostConstruct
	public void runOsgiIntegrationTests() {
		Method[] methods = this.getClass().getDeclaredMethods();
		List<Object[]> failures = new ArrayList<Object[]>(methods.length);
		int runned = 0;

		for (Method method : methods) {
			if (method.getAnnotation(OsgiIntegrationTest.class) != null) {
				runned++;

				try {
					method.setAccessible(true);
					method.invoke(this, NO_ARGS);
				} catch (IllegalAccessException e) {
					failures.add(new Object[] { method.getName(), e });
				} catch (InvocationTargetException e) {
					failures.add(new Object[] { method.getName(), e.getTargetException() });
				}
			}
		}

		LOGGER.info("-----------------------------");
		LOGGER.info("{} :  runned {} tests, {} failures", new Object[] { this.getClass().getSimpleName(), runned,
				failures.size() });
		for (Object[] failure : failures) {
			String methodName = (String) failure[0];
			Throwable exception = (Throwable) failure[1];
			String message = methodName + " : " + exception.getMessage();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(message, exception);
			} else {
				LOGGER.info(message + " : " + exception.getMessage());
			}
		}
		LOGGER.info("-----------------------------");
	}

	@OsgiIntegrationTest
	public void query_runner_should_be_injected() {
		assert queryRunner != null;
	}

	@OsgiIntegrationTest
	public void should_query_config_table() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("key", "squashtest.tm.database.version");
		List<String> res = queryRunner.executeSelect("select * from CORE_CONFIG where STR_KEY = :key", params);
		assert res.size() == 1 : "expected 1 row, got " + res.size();
	}
	/**
	 * @param queryRunner the queryRunner to set
	 */
	@ServiceReference
	public void setQueryRunner(SqlQueryRunner queryRunner) {
		this.queryRunner = queryRunner;
	}

}
