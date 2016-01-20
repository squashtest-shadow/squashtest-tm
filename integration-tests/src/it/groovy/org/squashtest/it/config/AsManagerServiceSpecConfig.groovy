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
package org.squashtest.it.config

import javax.inject.Inject
import javax.sql.DataSource

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.aspectj.EnableSpringConfigured
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.security.acls.jdbc.LookupStrategy
import org.springframework.security.acls.model.AclCache
import org.springframework.security.authentication.encoding.PasswordEncoder
import org.springframework.security.authentication.encoding.ShaPasswordEncoder
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService
import org.squashtest.csp.core.bugtracker.service.BugTrackersService
import org.squashtest.csp.core.bugtracker.service.StubBugTrackerService
import org.squashtest.it.stub.security.StubChefContextService
import org.squashtest.it.stub.security.StubObjectAclService
import org.squashtest.it.stub.security.StubObjectIdentityService
import org.squashtest.it.stub.security.StubPermissionEvaluator
import org.squashtest.it.stub.security.StubPermissionFactory
import org.squashtest.it.stub.security.StubPermissionOnOddEntitiesEvaluationService
import org.squashtest.it.stub.user.StubChefAccountService
import org.squashtest.tm.service.internal.security.AdministratorAuthenticationServiceImpl
import org.squashtest.tm.service.internal.security.AffirmativeBasedCompositePermissionEvaluator
import org.squashtest.tm.service.internal.security.SquashUserDetailsManager
import org.squashtest.tm.service.internal.security.SquashUserDetailsManagerImpl
import org.squashtest.tm.service.security.AdministratorAuthenticationService
import org.squashtest.tm.service.security.ObjectIdentityService
import org.squashtest.tm.service.security.PermissionEvaluationService
import org.squashtest.tm.service.security.StubLookupStrategy
import org.squashtest.tm.service.security.UserContextService
import org.squashtest.tm.service.security.acls.model.NullAclCache
import org.squashtest.tm.service.security.acls.model.ObjectAclService
import org.squashtest.tm.service.user.UserAccountService

/**
 * @author Gregory Fouquet
 * @since 1.13.00
 */
@Configuration
@ComponentScan(
basePackages = ["org.squashtest.tm.service.internal", "org.squashtest.tm.service.security",
	"org.squashtest.it.stub.security", "org.squashtest.it.stub.validation"],
excludeFilters = [
	@ComponentScan.Filter(Configuration),
	@ComponentScan.Filter(pattern = "org\\.squashtest\\.tm\\.service\\.internal\\.security\\..*", type = FilterType.REGEX),
]
)
@EnableSpringConfigured
class AsManagerServiceSpecConfig {
	@Inject DataSource dataSource

	@Bean
	@Primary
	BugTrackersService bugTrackerService() {
		new StubBugTrackerService();
	}

	@Bean
	AclCache aclCache() {
		new NullAclCache();
	}

	//	@Bean(name = "squashtest.core.security.JdbcUserDetailsManager")
	//	@Primary
	//	SquashUserDetailsManager userDetailsManager() {
	//		new StubUserDetailsManager();
	//	}

	@Bean(name = "squashtest.core.user.UserContextService")
	@Primary
	UserContextService userContextService() {
		new StubChefContextService()
	}

	@Bean
	static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		new ShaPasswordEncoder()
	}

	@Bean(name = "squashtest.core.security.PermissionEvaluationService")
	@Primary
	PermissionEvaluationService permissionEvaluationService() {
		new StubPermissionOnOddEntitiesEvaluationService()
	}

	@Bean StubPermissionFactory permissionFactory() {
		new StubPermissionFactory()
	}

	@Bean(name = "squashtest.tm.service.UserAccountService")
	@Primary
	UserAccountService userAccountService() {
		new StubChefAccountService()
	}

	@Bean
	AdministratorAuthenticationService administratorAuthenticationService() {
		new AdministratorAuthenticationServiceImpl();
	}


	@Bean JdbcClientDetailsService clientDetails() {
		new JdbcClientDetailsService(dataSource)
	}

	@Bean AffirmativeBasedCompositePermissionEvaluator permissionEvaluator() {
		new StubPermissionEvaluator()
	}

	@Bean(name = "squashtest.core.security.JdbcUserDetailsManager")
	@Primary SquashUserDetailsManager userDetailsManager() {
		SquashUserDetailsManagerImpl manager = new SquashUserDetailsManagerImpl()
		manager.setDataSource(dataSource)
		return manager
	}

	@Primary
	@Bean ObjectIdentityService objectIdentityService() {
		new StubObjectIdentityService()
	}

	@Primary
	@Bean ObjectAclService objectAclService(){
		new StubObjectAclService();
	}
	@Bean
	LookupStrategy lookupStrategy() {
		new StubLookupStrategy();
	}

}
