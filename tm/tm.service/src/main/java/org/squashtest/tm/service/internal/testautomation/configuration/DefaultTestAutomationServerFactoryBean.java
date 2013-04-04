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
package org.squashtest.tm.service.internal.testautomation.configuration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;


/*
 * That class exists because the good old PropertyPlaceholderConfigurer won't work. There is another one 
 * configured in the context of tm.service (the one configuring the bugtracker) that will fail 
 * when the property is not set.
 * 
 */
@Component("tm.testautomation.server.default.factory")
public class DefaultTestAutomationServerFactoryBean implements FactoryBean<TestAutomationServer>{

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTestAutomationServerFactoryBean.class);
	
	private static final String DEFAULT_URL_KEY = "tm.test.automation.server.defaulturl";
	private static final String DEFAULT_LOGIN_KEY = "tm.test.automation.server.defaultlogin";
	private static final String DEFAULT_PASSWORD_KEY = "tm.test.automation.server.defaultpassword";
	
	
	@Inject
	@Qualifier("squashtest.tm.ta.defaults")
	private Properties defaultsProperties;
	
	
	public void setDefaultsProperties(Properties defaultsProperties) {
		this.defaultsProperties = defaultsProperties;
	}

	@Override
	public TestAutomationServer getObject() throws Exception {
		//there's no point in going through all the singleton synchronizing plumbing
		//here because there will be only one call to that method.
		
		
		
		//default url
		String baseStrUrl = defaultsProperties.getProperty(DEFAULT_URL_KEY, "");	
		URL baseURL=null;
		try{
			baseURL = new URL(baseStrUrl);
		}catch(MalformedURLException ex){
			if (LOGGER.isErrorEnabled()){
				LOGGER.error("default automated test server configuration : malformed url '"+baseStrUrl+"', proceeding with empty url");
			}
			baseURL=new URL("http://localhost");
		}
	
		String defaultLogin = defaultsProperties.getProperty(DEFAULT_LOGIN_KEY, "");
		String defaultPass = defaultsProperties.getProperty(DEFAULT_PASSWORD_KEY, "");
		

		TestAutomationServer defaultServer = new TestAutomationServer(baseURL, defaultLogin, defaultPass);
		
		if (LOGGER.isInfoEnabled()){
			String displayablePassword = (defaultPass.length() > 0) ? defaultPass.substring(0,1)+"****" : "(hidden)";
			LOGGER.info("default automated test server configuration : url = '"+baseURL.toExternalForm()+"', login : '"+defaultLogin+"', password : '"+displayablePassword+"...'");
		}
		
		return defaultServer;
	}

	@Override
	public Class<?> getObjectType() {
		return TestAutomationServer.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	
	
}
