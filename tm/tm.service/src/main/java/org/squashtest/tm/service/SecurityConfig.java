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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.acls.domain.AclAuthorizationStrategy;
import org.springframework.security.acls.domain.AclAuthorizationStrategyImpl;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.squashtest.tm.security.acls.Slf4jAuditLogger;
import org.squashtest.tm.service.security.acls.jdbc.JdbcManageableAclService;
import sun.security.util.PermissionFactory;

import javax.inject.Inject;
import javax.sql.DataSource;

/**
 * Partial Spring Sec config. should be with the rest of spring sec's config now that we dont have osgi bundles segregation
 * @author  Gregory Fouquet
 */
@Configuration
public class SecurityConfig {
    @Inject
    private DataSource dataSource;
    @Inject private AclCache aclCache;
    @Inject private PermissionFactory permissionFactory;

        @Bean
    public GrantedAuthority aclAdminAuthority() {
        return new GrantedAuthorityImpl("ROLE_ADMIN");
    }

    @Bean(name="lookupStrategy")
    public BasicLookupStrategy lookupStrategy() {
        AclAuthorizationStrategy aclAuthorizationStrategy = new  AclAuthorizationStrategyImpl(aclAdminAuthority(), aclAdminAuthority(), aclAdminAuthority());

        BasicLookupStrategy strategy = new BasicLookupStrategy(dataSource, aclCache, aclAuthorizationStrategy, new Slf4jAuditLogger());
        strategy.setSelectClause(
                "select oid.IDENTITY as object_id_identity,\n" +
                "  gp.PERMISSION_ORDER,\n" +
                "  oid.ID as acl_id,\n" +
                "  null as parent_object, /* oid.parent */\n" +
                "  true as entries_inheriting, /* oid.entries_inheriting*/\n" +
                "  rse.ID as ace_id,\n" +
                "  gp.PERMISSION_MASK as mask,\n" +
                "  gp.GRANTING as granting,\n" +
                "  true as audit_success, /* audit success */\n" +
                "  false as audit_failure, /* audit failure */\n" +
                "  true as ace_principal, /* sid is principal */\n" +
                "  u.LOGIN as ace_sid,\n" +
                "  true as acl_principal, /* owner is prinipal */\n" +
                "  u.LOGIN as acl_sid, /* owner sid */\n" +
                "  ocl.CLASSNAME as class\n" +
                "from ACL_OBJECT_IDENTITY oid\n" +
                "  left join ACL_CLASS ocl on ocl.ID = oid.CLASS_ID\n" +
                "  left join ACL_GROUP_PERMISSION gp on gp.CLASS_ID = ocl.ID\n" +
                "  left join ACL_GROUP g on g.ID = gp.ACL_GROUP_ID\n" +
                "  left join ACL_RESPONSIBILITY_SCOPE_ENTRY rse on rse.ACL_GROUP_ID = g.ID and rse.OBJECT_IDENTITY_ID = oid.ID\n" +
                "  inner join CORE_PARTY party on party.PARTY_ID = rse.PARTY_ID\n" +
                "  left join CORE_TEAM team on team.PARTY_ID = party.PARTY_ID\n" +
                "  left join CORE_TEAM_MEMBER tmemb on tmemb.TEAM_ID = team.PARTY_ID,\n" +
                "  CORE_USER u \n" +
                "where((u.PARTY_ID = tmemb.USER_ID) or (u.PARTY_ID = party.PARTY_ID)) and u.ACTIVE = true and (\n");

        strategy.setLookupObjectIdentitiesWhereClause("(oid.IDENTITY = ? and ocl.CLASSNAME = ?)");
        strategy.setLookupPrimaryKeysWhereClause("(oid.ID = ?)");
        strategy.setOrderByClause(") order by oid.IDENTITY asc, gp.PERMISSION_ORDER asc");

        return strategy;
    }

    @Bean(name="squashtest.core.security.AclService")
    public JdbcManageableAclService aclService() {
        JdbcManageableAclService aclService = new JdbcManageableAclService(dataSource, lookupStrategy());
        aclService.setAclCache(aclCache);
        aclService.setFindChildrenQuery(
                "select null as obj_id,\n" +
                "  null as class\n" +
                "from ACL_OBJECT_IDENTITY\n" +
                "where 0 = 1\n");
        return aclService;
    }
}
