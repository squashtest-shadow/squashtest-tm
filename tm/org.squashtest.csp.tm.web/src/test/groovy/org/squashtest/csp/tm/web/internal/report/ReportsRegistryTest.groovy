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

package org.squashtest.csp.tm.web.internal.report


import org.squashtest.plugin.api.report.ReportDefinition;
import org.squashtest.plugin.api.report.ReportPlugin;
import org.squashtest.plugin.api.report.StandardReportCategory;

import spock.lang.Specification;

import static org.squashtest.plugin.api.report.StandardReportCategory.*

/**
 * @author Gregory Fouquet
 *
 */
class ReportsRegistryTest extends Specification {
	ReportsRegistry registry = new ReportsRegistry()

	def "should register category"() {
		given:
		ReportDefinition report = new ReportDefinition()
		report.category = StandardReportCategory.PREPARATION_PHASE

		ReportPlugin plugin = new ReportPlugin()
		plugin.report = report

		when:
		registry.pluginRegistered plugin, ['osgi.service.blueprint.compname' : 'bar']

		then:
		registry.categories == new HashSet([PREPARATION_PHASE])
	}

	def "should register report"() {
		given:
		ReportDefinition report = new ReportDefinition()
		report.labelKey = 'foo'
		report.category = StandardReportCategory.PREPARATION_PHASE

		ReportPlugin plugin = new ReportPlugin()
		plugin.report = report

		when:
		registry.pluginRegistered plugin, ['osgi.service.blueprint.compname' : 'bar']

		then:
		registry.getReports(PREPARATION_PHASE)*.labelKey == ['foo']
	}

	def "should unregister category"() {
		given:
		ReportDefinition report = new ReportDefinition()
		report.category = StandardReportCategory.PREPARATION_PHASE

		ReportPlugin plugin = new ReportPlugin()
		plugin.report = report

		when:
		registry.pluginRegistered plugin, ['osgi.service.blueprint.compname' : 'bar']
		registry.pluginUnregistered plugin, ['osgi.service.blueprint.compname' : 'bar']

		then:
		registry.categories.empty
	}
	
	def "should decorate reports with identifier"() {
		given:
		ReportDefinition report = new ReportDefinition()
		report.labelKey = 'foo'
		report.category = StandardReportCategory.PREPARATION_PHASE

		ReportPlugin plugin = new ReportPlugin()
		plugin.report = report

		when:
		registry.pluginRegistered plugin, ['osgi.service.blueprint.compname' : 'bar']

		then:
		registry.getReports(PREPARATION_PHASE)*.namespace == ['bar']
		registry.getReports(PREPARATION_PHASE)*.index == [0]
	}
}
