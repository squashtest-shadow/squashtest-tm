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
package org.squashtest.tm.service.internal.chart.engine;

import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.bugtracker.Issue;
import org.squashtest.tm.domain.bugtracker.QIssue;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.QCampaign;
import org.squashtest.tm.domain.campaign.QIteration;
import org.squashtest.tm.domain.campaign.QIterationTestPlanItem;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.QExecution;
import org.squashtest.tm.domain.requirement.QRequirement;
import org.squashtest.tm.domain.requirement.QRequirementVersion;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.QRequirementVersionCoverage;
import org.squashtest.tm.domain.testcase.QTestCase;
import org.squashtest.tm.domain.testcase.RequirementVersionCoverage;
import org.squashtest.tm.domain.testcase.TestCase;

import com.querydsl.core.types.dsl.EntityPathBase;


/**
 * This enum extends {@link EntityType} and includes table real names and hidden tables that aren't officially
 * disclosed to the end user. Internal usage only.
 * 
 * 
 * @author bsiri
 *
 */
enum InternalEntityType {
	// @formatter:off
	REQUIREMENT(){

		@Override
		Class<?> getEntityClass() {
			return Requirement.class;
		}

		@Override
		EntityPathBase<?> getQBean() {
			return QRequirement.requirement;
		}

		@Override
		EntityPathBase<?> getAliasedQBean(String alias) {
			return new QRequirement(alias);
		}

	},
	REQUIREMENT_VERSION(){
		@Override
		Class<?> getEntityClass() {
			return RequirementVersion.class;
		}

		@Override
		EntityPathBase<?> getQBean() {
			return QRequirementVersion.requirementVersion;
		}

		@Override
		EntityPathBase<?> getAliasedQBean(String alias) {
			return new QRequirementVersion(alias);
		}
	},
	REQUIREMENT_VERSION_COVERAGE(){
		@Override
		Class<?> getEntityClass() {
			return RequirementVersionCoverage.class;
		}

		@Override
		EntityPathBase<?> getQBean() {
			return QRequirementVersionCoverage.requirementVersionCoverage;
		}

		@Override
		EntityPathBase<?> getAliasedQBean(String alias) {
			return new QRequirementVersionCoverage(alias);
		}
	},
	TEST_CASE(){
		@Override
		Class<?> getEntityClass() {
			return TestCase.class;
		}

		@Override
		EntityPathBase<?> getQBean() {
			return QTestCase.testCase;
		}

		@Override
		EntityPathBase<?> getAliasedQBean(String alias) {
			return new QTestCase(alias);
		}
	},
	CAMPAIGN(){
		@Override
		Class<?> getEntityClass() {
			return Campaign.class;
		}

		@Override
		EntityPathBase<?> getQBean() {
			return QCampaign.campaign;
		}

		@Override
		EntityPathBase<?> getAliasedQBean(String alias) {
			return new QCampaign(alias);
		}
	},
	ITERATION(){
		@Override
		Class<?> getEntityClass() {
			return Iteration.class;
		}

		@Override
		EntityPathBase<?> getQBean() {
			return QIteration.iteration;
		}

		@Override
		EntityPathBase<?> getAliasedQBean(String alias) {
			return new QIteration(alias);
		}
	},
	ITEM_TEST_PLAN(){
		@Override
		Class<?> getEntityClass() {
			return IterationTestPlanItem.class;
		}

		@Override
		EntityPathBase<?> getQBean() {
			return QIterationTestPlanItem.iterationTestPlanItem;
		}

		@Override
		EntityPathBase<?> getAliasedQBean(String alias) {
			return new QIterationTestPlanItem(alias);
		}
	},
	EXECUTION(){
		@Override
		Class<?> getEntityClass() {
			return Execution.class;
		}

		@Override
		EntityPathBase<?> getQBean() {
			return QExecution.execution;
		}

		@Override
		EntityPathBase<?> getAliasedQBean(String alias) {
			return new QExecution(alias);
		}
	},
	ISSUE(){
		@Override
		Class<?> getEntityClass() {
			return Issue.class;
		}

		@Override
		EntityPathBase<?> getQBean() {
			return QIssue.issue;
		}

		@Override
		EntityPathBase<?> getAliasedQBean(String alias) {
			return new QIssue(alias);
		}
	};
	// @formatter:on


	abstract Class<?> getEntityClass();

	// fun fact : in the querydsl domain a QBean is not exactly an EntityPathBase
	abstract EntityPathBase<?> getQBean();

	abstract EntityPathBase<?> getAliasedQBean(String alias);


	static InternalEntityType fromDomainType(EntityType domainType) {// NOSONAR this switch is not so complex...
		InternalEntityType converted;

		switch(domainType){
		case REQUIREMENT : converted = REQUIREMENT; break;
		case REQUIREMENT_VERSION : converted = REQUIREMENT_VERSION; break;
		case TEST_CASE : converted = TEST_CASE; break;
		case CAMPAIGN : converted = CAMPAIGN; break;
		case ITERATION : converted = ITERATION; break;
		case ITEM_TEST_PLAN : converted = ITEM_TEST_PLAN; break;
		case EXECUTION : converted = EXECUTION; break;
		case ISSUE : converted = ISSUE; break;
		default : throw new IllegalArgumentException("Unimplemented : cannot convert type '"+domainType+"' to a corresponding internal type");
		}

		return converted;
	}


}
