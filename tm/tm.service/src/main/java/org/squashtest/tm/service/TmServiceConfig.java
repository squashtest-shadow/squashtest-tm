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

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.squashtest.tm.domain.campaign.*;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseFolder;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.service.internal.event.StatusBasedRequirementAuditor;
import org.squashtest.tm.service.internal.library.*;
import org.squashtest.tm.service.internal.repository.*;
import org.squashtest.tm.service.internal.repository.hibernate.HibernateCampaignLibraryNodeDao;
import org.squashtest.tm.service.internal.repository.hibernate.HibernateObjectDao;
import org.squashtest.tm.service.internal.repository.hibernate.HibernateRequirementLibraryNodeDao;
import org.squashtest.tm.service.project.ProjectFilterModificationService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.domain.event.RequirementModificationEventPublisherAspect;
import org.squashtest.tm.service.internal.event.RequirementCreationEventPublisherAspect;
import org.squashtest.tm.validation.ValidatorFactoryBean;

import javax.inject.Inject;
import javax.validation.ValidatorFactory;

/**
 * Spring configuration for tm.service subsystem
 *
 * @author Gregory Fouquet
 * Rem : @Configurable is used in tm.domain by hibernate search bridges
 */
@Configuration
@ComponentScan
@EnableSpringConfigured
public class TmServiceConfig {
    @Inject
    private TestCaseLibraryDao testCaseLibraryDao;
    @Inject
    private RequirementLibraryDao requirementLibraryDao;
    @Inject
    private CampaignLibraryDao campaignLibraryDao;

    @Inject
    private TestCaseDao testCaseDao;
    @Inject
    private RequirementDao requirementDao;
    @Inject
    private CampaignDao campaignDao;
    @Inject
    private IterationDao iterationDao;
    @Inject
    private TestSuiteDao testSuiteDao;

    @Inject
    private TestCaseFolderDao testCaseFolderDao;
    @Inject
    private RequirementFolderDao requirementFolderDao;
    @Inject
    private CampaignFolderDao campaignFolderDao;

    @Inject
    private TestCaseLibraryNodeDao testCaseLibraryNodeDao;
    @Inject
    private HibernateRequirementLibraryNodeDao requirementLibraryNodeDao;
    @Inject
    private HibernateCampaignLibraryNodeDao campaignLibraryNodeDao;

    @Inject
    private LibrarySelectionStrategy<TestCaseLibrary, TestCaseLibraryNode> testCaseLibrarySelector;
    @Inject
    private LibrarySelectionStrategy<RequirementLibrary, RequirementLibraryNode> requirementLibrarySelector;
    @Inject
    private LibrarySelectionStrategy<CampaignLibrary, CampaignLibraryNode> campaignLibrarySelector;

    @Inject
    private ProjectFilterModificationService projectFilterManager;

    @Inject
    private PermissionEvaluationService permissionEvaluationService;

    @Inject
    private HibernateObjectDao hibernateObjectDao;

    @Inject
    private StatusBasedRequirementAuditor statusBasedRequirementAuditor;

    @Bean public static ConfigFileApplicationListener configFileApplicationListener() {
        ConfigFileApplicationListener listener = new ConfigFileApplicationListener();
        listener.setSearchNames("squashtest.core.datasource.jdbc.config, squashtest.tm.cfg");
        return listener;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new ShaPasswordEncoder();
    }

    @Bean
    public RequirementCreationEventPublisherAspect requirementCreationEventPublisherAspect() {
        RequirementCreationEventPublisherAspect aspect = RequirementCreationEventPublisherAspect.aspectOf();
        aspect.setAuditor(statusBasedRequirementAuditor);
        return aspect;
    }

    @Bean
    public RequirementModificationEventPublisherAspect requirementModificationEventPublisherAspect() {
        RequirementModificationEventPublisherAspect aspect = RequirementModificationEventPublisherAspect.aspectOf();
        aspect.setAuditor(statusBasedRequirementAuditor);
        return aspect;
    }

    @Bean(name = "squashtest.tm.service.TestCasesWorkspaceService")
    public GenericWorkspaceService<TestCaseLibrary, TestCaseLibraryNode> testCaseWorkspaceManager() {
        GenericWorkspaceService<TestCaseLibrary, TestCaseLibraryNode> manager = new GenericWorkspaceService<>();
        manager.setLibraryDao(testCaseLibraryDao);
        manager.setLibraryStrategy(testCaseLibrarySelector);
        configure(manager);
        return manager;
    }

    private void configure(GenericWorkspaceService<?, ?> manager) {
        manager.setProjectFilterModificationService(projectFilterManager);
    }

    @Bean(name = "squashtest.tm.service.RequirementsWorkspaceService")
    public GenericWorkspaceService<RequirementLibrary, RequirementLibraryNode> requirementWorkspaceManager() {
        GenericWorkspaceService<RequirementLibrary, RequirementLibraryNode> manager = new GenericWorkspaceService<>();
        manager.setLibraryDao(requirementLibraryDao);
        manager.setLibraryStrategy(requirementLibrarySelector);
        configure(manager);
        return manager;
    }

    @Bean(name = "squashtest.tm.service.CampaignsWorkspaceService")
    public GenericWorkspaceService<CampaignLibrary, CampaignLibraryNode> campaignWorkspaceManager() {
        GenericWorkspaceService<CampaignLibrary, CampaignLibraryNode> manager = new GenericWorkspaceService<>();
        manager.setLibraryDao(campaignLibraryDao);
        manager.setLibraryStrategy(campaignLibrarySelector);
        configure(manager);
        return manager;
    }

    @Bean(name = "squashtest.tm.service.TestCaseFolderModificationService")
    public GenericFolderModificationService<TestCaseFolder, TestCaseLibraryNode> testCaseFolderManager() {
        GenericFolderModificationService<TestCaseFolder, TestCaseLibraryNode> manager = new GenericFolderModificationService<>();
        manager.setLibraryDao(testCaseLibraryDao);
        manager.setFolderDao(testCaseFolderDao);
        configure(manager);
        return manager;
    }

    private void configure(GenericFolderModificationService<?, ?> manager) {
        manager.setPermissionService(permissionEvaluationService);
    }

    @Bean(name = "squashtest.tm.service.RequirementFolderModificationService")
    public GenericFolderModificationService<RequirementFolder, RequirementLibraryNode> requirementFolderManager() {
        GenericFolderModificationService<RequirementFolder, RequirementLibraryNode> manager = new GenericFolderModificationService<>();
        manager.setLibraryDao(requirementLibraryDao);
        manager.setFolderDao(requirementFolderDao);
        configure(manager);
        return manager;
    }

    @Bean(name = "squashtest.tm.service.CampaignFolderModificationService")
    public GenericFolderModificationService<CampaignFolder, CampaignLibraryNode> campaignFolderManager() {
        GenericFolderModificationService<CampaignFolder, CampaignLibraryNode> manager = new GenericFolderModificationService<>();
        manager.setLibraryDao(campaignLibraryDao);
        manager.setFolderDao(campaignFolderDao);
        configure(manager);
        return manager;
    }

    @Bean(name = "squashtest.tm.service.internal.TestCaseManagementService")
    public GenericNodeManagementService<TestCase, TestCaseLibraryNode, TestCaseFolder> testCaseManager() {
        GenericNodeManagementService<TestCase, TestCaseLibraryNode, TestCaseFolder> manager = new GenericNodeManagementService<>();
        manager.setLibraryDao(testCaseLibraryDao);
        manager.setFolderDao(testCaseFolderDao);
        configure(manager);
        return manager;
    }

    private void configure(GenericNodeManagementService<?, ?, ?> manager) {
        manager.setPermissionService(permissionEvaluationService);
    }

    @Bean(name = "squashtest.tm.service.internal.RequirementManagementService")
    public GenericNodeManagementService<Requirement, RequirementLibraryNode, RequirementFolder> requirementManager() {
        GenericNodeManagementService<Requirement, RequirementLibraryNode, RequirementFolder> manager = new GenericNodeManagementService<>();
        manager.setLibraryDao(requirementLibraryDao);
        manager.setFolderDao(requirementFolderDao);
        configure(manager);
        return manager;
    }

    @Bean(name = "squashtest.tm.service.internal.CampaignManagementService")
    public GenericNodeManagementService<Campaign, CampaignLibraryNode, CampaignFolder> campaignManager() {
        GenericNodeManagementService<Campaign, CampaignLibraryNode, CampaignFolder> manager = new GenericNodeManagementService<>();
        manager.setLibraryDao(campaignLibraryDao);
        manager.setFolderDao(campaignFolderDao);
        configure(manager);
        return manager;
    }

    @Bean(name = "squashtest.tm.service.internal.PasteToTestCaseFolderStrategy")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public PasteStrategy<TestCaseFolder, TestCaseLibraryNode> pasteToTestCaseFolderStrategy() {
        PasteStrategy<TestCaseFolder, TestCaseLibraryNode> paster = new PasteStrategy<>();
        paster.setContainerDao(testCaseFolderDao);
        paster.setNodeDao(testCaseLibraryNodeDao);
        configure(paster);
        return paster;
    }

    private void configure(PasteStrategy<?, ?> paster) {
        paster.setGenericDao(hibernateObjectDao);
    }

    @Bean(name = "squashtest.tm.service.internal.PasteToTestCaseLibraryStrategy")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public PasteStrategy<TestCaseLibrary, TestCaseLibraryNode> pasteToTestCaseLibraryStrategy() {
        PasteStrategy<TestCaseLibrary, TestCaseLibraryNode> paster = new PasteStrategy<>();
        paster.setContainerDao(testCaseLibraryDao);
        paster.setNodeDao(testCaseLibraryNodeDao);
        configure(paster);
        return paster;
    }

    @Bean(name = "squashtest.tm.service.internal.PasteToRequirementFolderStrategy")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public PasteStrategy<RequirementFolder, RequirementLibraryNode> pasteToRequirementFolderStrategy() {
        PasteStrategy<RequirementFolder, RequirementLibraryNode> paster = new PasteStrategy<>();
        paster.setContainerDao(requirementFolderDao);
        paster.setNodeDao(requirementLibraryNodeDao);
        configure(paster);
        return paster;
    }

    @Bean(name = "squashtest.tm.service.internal.PasteToRequirementLibraryStrategy")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public PasteStrategy<RequirementLibrary, RequirementLibraryNode> pasteToRequirementLibraryStrategy() {
        PasteStrategy<RequirementLibrary, RequirementLibraryNode> paster = new PasteStrategy<>();
        paster.setContainerDao(requirementLibraryDao);
        paster.setNodeDao(requirementLibraryNodeDao);
        configure(paster);
        return paster;
    }

    @Bean(name = "squashtest.tm.service.internal.PasteToRequirementStrategy")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public PasteStrategy<Requirement, Requirement> pasteToRequirementStrategy() {
        PasteStrategy<Requirement, Requirement> paster = new PasteStrategy<>();
        paster.setContainerDao(requirementDao);
        paster.setNodeDao(requirementDao);
        configure(paster);
        return paster;
    }

    @Bean(name = "squashtest.tm.service.internal.PasteToCampaignFolderStrategy")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public PasteStrategy<CampaignFolder, CampaignLibraryNode> pasteToCampaignFolderStrategy() {
        PasteStrategy<CampaignFolder, CampaignLibraryNode> paster = new PasteStrategy<>();
        paster.setContainerDao(campaignFolderDao);
        paster.setNodeDao(campaignLibraryNodeDao);
        configure(paster);
        return paster;
    }

    @Bean(name = "squashtest.tm.service.internal.PasteToCampaignLibraryStrategy")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public PasteStrategy<CampaignLibrary, CampaignLibraryNode> pasteToCampaignLibraryStrategy() {
        PasteStrategy<CampaignLibrary, CampaignLibraryNode> paster = new PasteStrategy<>();
        paster.setContainerDao(campaignLibraryDao);
        paster.setNodeDao(campaignLibraryNodeDao);
        configure(paster);
        return paster;
    }

    @Bean(name = "squashtest.tm.service.internal.PasteToCampaignStrategy")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public PasteStrategy<Campaign, Iteration> pasteToCampaignStrategy() {
        PasteStrategy<Campaign, Iteration> paster = new PasteStrategy<>();
        paster.setContainerDao(campaignDao);
        paster.setNodeDao(iterationDao);
        configure(paster);
        return paster;
    }

    @Bean(name = "squashtest.tm.service.internal.PasteToIterationStrategy")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public PasteStrategy<Iteration, TestSuite> pasteToIterationStrategy() {
        PasteStrategy<Iteration, TestSuite> paster = new PasteStrategy<>();
        paster.setContainerDao(iterationDao);
        paster.setNodeDao(testSuiteDao);
        configure(paster);
        return paster;
    }

    /**
     * @deprecated TODO enlever quand plus d'OSGI
     */
    @Deprecated
    @Bean
    public ValidatorFactory validatorFactory() {
        return ValidatorFactoryBean.getInstance();
    }
}
