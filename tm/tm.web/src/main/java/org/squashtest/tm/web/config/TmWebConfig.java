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
package org.squashtest.tm.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.multipart.MultipartResolver;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.service.configuration.ConfigurationService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.web.internal.fileupload.MultipartResolverDispatcher;
import org.squashtest.tm.web.internal.fileupload.SquashMultipartResolver;
import org.squashtest.tm.web.internal.model.builder.CampaignLibraryTreeNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.RequirementLibraryTreeNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.TestCaseLibraryTreeNodeBuilder;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.HashMap;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * Spring configuration for web layer
 *
 * @author Gregory Fouquet
 * @since 1.13.0
 */
@Configuration
public class TmWebConfig {
	@Inject
	private PermissionEvaluationService permissionEvaluationService;

	@Inject
	private ConfigurationService configurationService;

	@Inject
	Provider<TestCaseLibraryTreeNodeBuilder> testCaseLibraryTreeNodeBuilderProvider;

	@Inject
	Provider<RequirementLibraryTreeNodeBuilder> requirementLibraryTreeNodeBuilderProvider;

	@Inject
	Provider<CampaignLibraryTreeNodeBuilder> campaignLibraryTreeNodeBuilderProvider;

	@Bean(name = "testCase.driveNodeBuilder")
	@Scope(SCOPE_PROTOTYPE)
	public DriveNodeBuilder<TestCaseLibraryNode> testCaseDriveNodeBuilder() {
		return new DriveNodeBuilder<TestCaseLibraryNode>(permissionEvaluationService, testCaseLibraryTreeNodeBuilderProvider);
	}

	@Bean(name = "requirement.driveNodeBuilder")
	@Scope(SCOPE_PROTOTYPE)
	public DriveNodeBuilder<RequirementLibraryNode> requirementDriveNodeBuilder() {
		return new DriveNodeBuilder<RequirementLibraryNode>(permissionEvaluationService, requirementLibraryTreeNodeBuilderProvider);
	}

	@Bean(name = "campaign.driveNodeBuilder")
	@Scope(SCOPE_PROTOTYPE)
	public DriveNodeBuilder<CampaignLibraryNode> campaignDriveNodeBuilder() {
		return new DriveNodeBuilder<CampaignLibraryNode>(permissionEvaluationService, campaignLibraryTreeNodeBuilderProvider);
	}

	/**
	 * This overrides spring boot's default (servlet 3 based) multipart resolver.
	 * @return
	 */
	@Bean
	public MultipartResolver multipartResolver() {
		MultipartResolverDispatcher bean = new MultipartResolverDispatcher();
		bean.setDefaultResolver(defaultMultipartResolver());
		HashMap<String, SquashMultipartResolver> resolverMap = new HashMap<>();
		resolverMap.put(".*/import/upload.*", importMultipartResolver());
		resolverMap.put(".*/importer/.*", importMultipartResolver());
		bean.setResolverMap(resolverMap);
		return bean;
	}

	@Bean
	public SquashMultipartResolver defaultMultipartResolver() {
		return buildSquashMultipartResolver();
	}

	@Bean
	public SquashMultipartResolver importMultipartResolver() {
		SquashMultipartResolver bean = buildSquashMultipartResolver();
		bean.setMaxUploadSizeKey(ConfigurationService.Properties.IMPORT_SIZE_LIMIT);
		return bean;
	}

	private SquashMultipartResolver buildSquashMultipartResolver() {
		SquashMultipartResolver bean = new SquashMultipartResolver();
		bean.setConfig(configurationService);
		bean.setDefaultEncoding("UTF-8");
		return bean;
	}
}
