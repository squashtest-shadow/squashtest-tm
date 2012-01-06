/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.internal.service


import javax.inject.Inject;

import org.hibernate.SessionFactory;
import org.spockframework.util.NotThreadSafe;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.report.common.dto.ExProgressProjectDto;
import org.squashtest.csp.tm.domain.report.common.dto.ReqCoverageByTestProjectDto;
import org.squashtest.csp.tm.domain.report.common.hibernate.HibernateExecutionProgressQuery;
import org.squashtest.csp.tm.domain.report.common.hibernate.HibernateRequirementCoverageByTestsQuery;
import org.squashtest.csp.tm.service.ReportService;
import org.unitils.dbunit.annotation.DataSet;

import spock.unitils.UnitilsSupport;

@NotThreadSafe
@UnitilsSupport
@Transactional
class ReportServiceIT_Disabled extends DbunitServiceSpecification {

	@Inject
	private ReportService reportService;

	
	@Inject
	private SessionFactory factory;
	
	
	
	def setup(){

	}

	@DataSet("ReportServiceIT.should generate list of ExProgressProjectDto.xml")
	def "should generate list of ExProgressProjectDto"(){
		given :
		HibernateExecutionProgressQuery request = new HibernateExecutionProgressQuery();
		when :
		List<ExProgressProjectDto> projDtoList = reportService.executeQuery(request);
		then :
		notThrown(Exception);
		projDtoList.size()>0;

		def project = projDtoList.get(0);
		project.name=="project 1"

		def campaign = project.getCampaigns().get(0)
		campaign.name == "Pharos"
	}

	@DataSet("ReportServiceIT.should filter HibernateExecutionProgressQuery with campaignIds.xml")
	def "should filter HibernateExecutionProgressQuery with campaignIds.xml"(){
		given :
		HibernateExecutionProgressQuery query = new HibernateExecutionProgressQuery();
		Object[] values = [1, 2, 4]
		query.setCriterion("campaignIds[]", values)

		when :
		List<ExProgressProjectDto> projDtoList = reportService.executeQuery(query);

		then :
		def names = projDtoList.collect {
			it.campaigns.collect{ that-> that.name }
		};
		names.flatten() == ["Deimos", "Pharos", "Phobos"]
	}
	
	@DataSet("ReportServiceIT.should sort HibernateExecutionProgressQuery by project name and campaign name.xml")
	def "should sort HibernateExecutionProgressQuery by project name and campaign name.xml"(){
		
		
		
		
	}

	@DataSet("ReportServiceIT.should generate list of ReqCoverageByTestProjectDto.xml")
	def "should generate list of ReqCoverageByTestProjectDto"(){
		given :
		HibernateRequirementCoverageByTestsQuery query = new HibernateRequirementCoverageByTestsQuery();

		when :
		List<ReqCoverageByTestProjectDto> results = reportService.executeQuery(query);

		then :
		def names = results.collect{ it.projectName};
		names.flatten() == [
			"project 1",
			"project 2",
			"TOTAL"
		]
		results[0].totalRequirementNumber == 6
		results[0].totalVerifiedRequirementNumber == 4
		results[0].undefinedRequirementNumber == 2
		results[0].undefinedVerifiedRequirementNumber == 2
		results[0].criticalRequirementNumber == 2
		results[0].criticalVerifiedRequirementNumber == 1
		results[0].majorRequirementNumber == 1
		results[0].majorVerifiedRequirementNumber == 1
		results[0].minorRequirementNumber == 1
		results[0].minorVerifiedRequirementNumber == 0
		//Total
		results[2].totalRequirementNumber == 13
		results[2].totalVerifiedRequirementNumber == 7
		results[2].undefinedRequirementNumber == 5
		results[2].undefinedVerifiedRequirementNumber == 2
		results[2].criticalRequirementNumber == 3
		results[2].criticalVerifiedRequirementNumber == 2
		results[2].majorRequirementNumber == 2
		results[2].majorVerifiedRequirementNumber == 2
		results[2].minorRequirementNumber == 3
		results[2].minorVerifiedRequirementNumber == 1
		//rate
		results[2].getGlobalRequirementCoverage() == 54
	}

	@DataSet("ReportServiceIT.should generate list of ReqCoverageByTestProjectDto filter by project id.xml")
	def "should generate list of ReqCoverageByTestProjectDto filter by project id"(){
		given :
		HibernateRequirementCoverageByTestsQuery query = new HibernateRequirementCoverageByTestsQuery();
		Object[] values = ["1"]
		query.setCriterion("projectIds[]", values)

		when :
		List<ReqCoverageByTestProjectDto> results = reportService.executeQuery(query);

		then :
		def names = results.collect{ it.projectName};
		names.flatten() == [
			"project 1",
			"TOTAL"
		]
		results[0].totalRequirementNumber == 6
		results[0].totalVerifiedRequirementNumber == 4
		results[0].undefinedRequirementNumber == 2
		results[0].undefinedVerifiedRequirementNumber == 2
		results[0].criticalRequirementNumber == 2
		results[0].criticalVerifiedRequirementNumber == 1
		results[0].majorRequirementNumber == 1
		results[0].majorVerifiedRequirementNumber == 1
		results[0].minorRequirementNumber == 1
		results[0].minorVerifiedRequirementNumber == 0
		//Total
		results[1].totalRequirementNumber == 6
		//rate
		results[1].getGlobalRequirementCoverage() == 67
	}
}
