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
package org.squashtest.tm.service.internal.hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.validation.ValidatorFactory;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.type.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;

/**
 * The purpose of this class is to add more things to the config of the session factory, when there is no other way to
 * do so.
 * 
 * @author bsiri
 * 
 */
public class SquashSessionFactoryBean extends LocalSessionFactoryBean {
	private static final String FN_NAME_GROUP_CONCAT = "group_concat";

	private static final Logger LOGGER = LoggerFactory.getLogger(SquashSessionFactoryBean.class);

	private static final String HIBERNATE_PROPERTIES_DIALECT = "hibernate.dialect";
	private static final String JAVAX_VALIDATION_FACTORY = "javax.persistence.validation.factory";

	private List<String> dialectsSupportingGroupConcat = new ArrayList<String>();
	private List<String> dialectsSupportingStringAgg = new ArrayList<String>();

	private ValidatorFactory validatorFactory;


	protected SessionFactory buildSessionFactory(LocalSessionFactoryBuilder sfb) {

		// the validator factory
		addValidatorFactory();

		// group concat
		addGroupConcatToConfiguration();

		// resume normal session factory initialization
		return super.buildSessionFactory(sfb);
	}



	/* ********************************************************************
	 * 		About ValidatorFactory
	 ******************************************************************* */


	protected void addValidatorFactory(){
		Configuration config = getConfiguration();
		Properties p = config.getProperties();
		p.put(JAVAX_VALIDATION_FACTORY, validatorFactory);
	}


	public ValidatorFactory getValidatorFactory() {
		return validatorFactory;
	}

	public void setValidatorFactory(ValidatorFactory validatorFactory) {
		this.validatorFactory = validatorFactory;
	}


	/* ********************************************************************
	 * 		About GroupConcat
	 ******************************************************************* */



	protected void addGroupConcatToConfiguration(){

		// check that the underlying base supports the dialect extensions : let's see what's the dialect is
		SQLFunction sqlFunction = getSQLFunctionForDialect();
		Configuration config = getConfiguration();

		if (sqlFunction != null) {
			// add the support for group concat
			config.addSqlFunction(FN_NAME_GROUP_CONCAT, sqlFunction);
		} else {
			config.addSqlFunction(FN_NAME_GROUP_CONCAT, new GroupConcatFunction(FN_NAME_GROUP_CONCAT, new StringType()));
		}
	}

	public void setDialectsSupportingStringAgg(List<String> dialectsSupportingStringAgg) {
		this.dialectsSupportingStringAgg = dialectsSupportingStringAgg;
	}

	public List<String> getDialectsSupportingStringAgg() {
		return dialectsSupportingStringAgg;
	}

	public void setDialectsSupportingGroupConcat(List<String> dialectsSupportingGroupConcat) {
		this.dialectsSupportingGroupConcat = dialectsSupportingGroupConcat;
	}

	public List<String> getDialectsSupportingGroupConcat() {
		return dialectsSupportingGroupConcat;
	}



	private SQLFunction getSQLFunctionForDialect() throws IllegalArgumentException {

		Properties hibernateProperties = getHibernateProperties();
		String choosenDialect = hibernateProperties.getProperty(HIBERNATE_PROPERTIES_DIALECT);

		if (isPlaceholder(choosenDialect)) {
			String propName = choosenDialect.substring(2, choosenDialect.length() - 1);
			String sysProp = System.getProperty(propName);

			if (sysProp != null) {
				choosenDialect = sysProp;
			} else {
				LOGGER.warn("Could not find {} in system properties, HQL function 'group_concat' will not ba available. Did you correctly configure db dialect ?");
			}

		}

		if (dialectsSupportingGroupConcat.contains(choosenDialect)) {
			return new GroupConcatFunction(FN_NAME_GROUP_CONCAT, new StringType());
		}
		if (dialectsSupportingStringAgg.contains(choosenDialect)) {
			return new StringAggFunction(FN_NAME_GROUP_CONCAT, new StringType());
		}

		LOGGER.error(
				"RicherDialectSessionFactory : selected hibernate Dialect '{}' is not reputed to support the sql function 'group_concat()'. If you are sure that your dialect (and the underlying database) supports this function, please add to RicherDialectSessionFactory.dalectsSupportingGroupConcat (see xml configuration)",
				choosenDialect);

		return null;
	}

	private boolean isPlaceholder(String str) {
		return str.indexOf('$') == 0 && str.indexOf('{') == 1
				&& str.indexOf('}') == str.length() - 1;
	}

}
