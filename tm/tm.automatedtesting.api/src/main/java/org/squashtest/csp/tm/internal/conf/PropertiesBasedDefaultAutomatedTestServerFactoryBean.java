/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.csp.tm.internal.conf;

import java.util.Properties;

import javax.inject.Inject;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.squashtest.csp.tm.domain.automatest.AutomatedTestServer;

public class PropertiesBasedDefaultAutomatedTestServerFactoryBean implements FactoryBean<AutomatedTestServer>{

	@Inject
	@Qualifier("squashtest.tm.ta.defaults")
	private Properties defaultsProperties;
	
	
	private AutomatedTestServer defaultServer = null;
	


	@Override
	public AutomatedTestServer getObject() throws Exception {
		return null;
	}

	@Override
	public Class<?> getObjectType() {
		return AutomatedTestServer.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	
	
}
