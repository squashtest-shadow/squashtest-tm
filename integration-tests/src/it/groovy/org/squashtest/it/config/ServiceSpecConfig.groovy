/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.it.config

import org.springframework.context.annotation.*
import org.springframework.context.annotation.aspectj.EnableSpringConfigured
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.security.authentication.encoding.PasswordEncoder
import org.springframework.security.authentication.encoding.ShaPasswordEncoder
import org.squashtest.csp.core.bugtracker.service.BugTrackersService
import org.squashtest.csp.core.bugtracker.service.StubBugTrackerService
import org.squashtest.it.stub.security.StubUserDetailsManager;
import org.squashtest.tm.service.internal.security.AdministratorAuthenticationServiceImpl
import org.squashtest.tm.service.internal.security.ObjectIdentityServiceImpl;
import org.squashtest.tm.service.internal.security.SpringSecurityUserContextService
import org.squashtest.tm.service.internal.security.SquashUserDetailsManager
import org.squashtest.tm.service.internal.security.SquashUserDetailsManagerImpl
import org.squashtest.tm.service.security.AdministratorAuthenticationService
import org.squashtest.tm.service.security.ObjectIdentityService;
import org.squashtest.tm.service.security.UserContextService

/**
 * Configuration for Service specification. Instanciates service and repo layer beans
 * @author Gregory Fouquet
 * @since 1.13.0
 */
@Configuration
@ComponentScan(
basePackages = ["org.squashtest.tm.service.internal", "org.squashtest.it.stub.validation"],
excludeFilters = [
	@ComponentScan.Filter(Configuration),
	@ComponentScan.Filter(pattern = "org\\.squashtest\\.tm\\.service\\.internal\\.security\\..*", type = FilterType.REGEX),
	@ComponentScan.Filter(pattern = ".*coercers.*", type = FilterType.REGEX)
]
)
@EnableSpringConfigured
class ServiceSpecConfig {

	
	@Bean
	@Primary
	BugTrackersService bugTrackerService() {
		new StubBugTrackerService();
	}

	@Bean
	UserContextService userContextService(){
		new SpringSecurityUserContextService();
	}
	
	@Bean
	static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
		new PropertySourcesPlaceholderConfigurer();
	}
	


	
	@Bean
	ObjectIdentityService objectIdentityService(){
		new ObjectIdentityServiceImpl()
	}
	
}
