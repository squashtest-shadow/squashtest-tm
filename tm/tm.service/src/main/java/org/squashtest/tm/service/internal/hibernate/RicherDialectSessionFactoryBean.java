/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.service.internal.hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.type.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;

/**
 * The purpose of this class is to add more things to the config of the session factory, when there is no other way to do so.
 * 
 * @author bsiri
 *
 */
public class RicherDialectSessionFactoryBean extends LocalSessionFactoryBean {
	private static final Logger LOGGER = LoggerFactory.getLogger(RicherDialectSessionFactoryBean.class);

	private static final String HIBERNATE_PROPERTIES_DIALECT = "hibernate.dialect";
	
	private List<String> dialectsSupportingGroupConcat = new ArrayList<String>();
	
	public void setDialectsSupportingGroupConcat(List<String> dialectsSupportingGroupConcat) {
		this.dialectsSupportingGroupConcat = dialectsSupportingGroupConcat;
	}

	public List<String> getDialectsSupportingGroupConcat() {
		return dialectsSupportingGroupConcat;
	}
	
	protected SessionFactory buildSessionFactory(LocalSessionFactoryBuilder sfb) {
		
		// check that the underlying base supports the dialect extensions : let's see what's the dialect is
		checkDialectSupport();
		
		// add the support for group concat
		Configuration config = getConfiguration();
		config.addSqlFunction("group_concat", new GroupConcatFunction("group_concat", new StringType()));
		
		
		// resume normal session factory initialization
		return super.buildSessionFactory(sfb);
	}

	
	private void checkDialectSupport() throws IllegalArgumentException{
		
		Properties hibernateProperties = getHibernateProperties(); 
		String choosenDialect = hibernateProperties.getProperty(HIBERNATE_PROPERTIES_DIALECT);

		boolean supported = false;
		for (String supportingDialect : dialectsSupportingGroupConcat){
			if (choosenDialect.equals(supportingDialect)){
				supported = true;
				break;
			}
		}
		
		if (! supported){
			LOGGER.warn("RicherDialectSessionFactory : selected hibernate Dialect '"+choosenDialect+
					   "' is not reputed to support the sql function 'group_concat()'. If you "+
					   "are sure that your dialect (and the underlying database) supports this function,"+
					   " please add to RicherDialectSessionFactory.dalectsSupportingGroupConcat"+
					   "(see xml configuration)");
//			throw new IllegalArgumentException("RicherDialectSessionFactory : selected hibernate Dialect '"+choosenDialect+
//											   "' is not reputed to support the sql function 'group_concat()'. If you "+
//											   "are sure that your dialect (and the underlying database) supports this function,"+
//											   " please add to RicherDialectSessionFactory.dalectsSupportingGroupConcat"+
//											   "(see xml configuration)");
		}
	}
	

	
}
