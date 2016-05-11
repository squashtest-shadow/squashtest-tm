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

import javax.inject.Named;

import org.springframework.context.annotation.*
import org.springframework.context.annotation.aspectj.EnableSpringConfigured
import org.springframework.security.acls.model.AclCache
import org.springframework.security.acls.model.AclService;
import org.squashtest.it.stub.security.StubAclService
import org.squashtest.it.stub.security.StubPermissionEvaluationService
import org.squashtest.it.stub.security.StubPermissionEvaluator
import org.squashtest.tm.service.internal.security.SquashUserDetailsManager
import org.squashtest.tm.service.internal.security.SquashUserDetailsManagerImpl
import org.squashtest.tm.service.security.PermissionEvaluationService
import org.squashtest.tm.service.security.acls.jdbc.JdbcManageableAclService
import org.squashtest.tm.service.security.acls.jdbc.ManageableAclService;
import org.squashtest.tm.service.security.acls.model.NullAclCache
import org.squashtest.tm.service.security.acls.model.ObjectAclService

/**
 * Configuration for Service specification. Instanciates service and repo layer beans
 * @author Gregory Fouquet
 * @since 1.13.0
 */
@Configuration
@ComponentScan(
basePackages = ["org.squashtest.tm.service.internal", "org.squashtest.tm.service.security",
	"org.squashtest.it.stub.validation"],
excludeFilters = [
	@ComponentScan.Filter(Configuration),
	@ComponentScan.Filter(pattern = "org\\.squashtest\\.tm\\.service\\.internal\\.security\\..*", type = FilterType.REGEX),
	@ComponentScan.Filter(pattern = ".*coercers.*", type = FilterType.REGEX)
]
)
@EnableSpringConfigured
class DisabledSecuritySpecConfig {
	
	@Bean
	AclCache aclCache() {
		new NullAclCache();
	}
	
	@Bean StubPermissionEvaluator permissionEvaluator() {
		new StubPermissionEvaluator()
	}
	
	
	@Bean(name = "squashtest.core.security.PermissionEvaluationService")
	@Primary
	PermissionEvaluationService permissionEvaluationService() {
		new StubPermissionEvaluationService()
	}
	
	
	/*
	 * The following implements both OjectAclService and ManageableAclService. 
	 */
		
	@Primary
	@Bean(name = "squashtest.core.security.AclService")
	StubAclService aclService(){
		new StubAclService();
	}
	

}
