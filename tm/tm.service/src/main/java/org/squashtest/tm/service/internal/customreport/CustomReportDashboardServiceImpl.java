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
package org.squashtest.tm.service.internal.customreport;

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

import java.util.List;

import javax.inject.Inject;

import org.hibernate.SessionFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.chart.ChartDefinition;
import org.squashtest.tm.domain.customreport.CustomReportChartBinding;
import org.squashtest.tm.domain.customreport.CustomReportDashboard;
import org.squashtest.tm.service.customreport.CustomReportDashboardService;
import org.squashtest.tm.service.customreport.CustomReportLibraryNodeService;
import org.squashtest.tm.service.internal.repository.CustomReportChartBindingDao;
import org.squashtest.tm.service.internal.repository.CustomReportDashboardDao;
import org.squashtest.tm.service.security.PermissionEvaluationService;

@Service("org.squashtest.tm.service.customreport.CustomReportDashboardService")
public class CustomReportDashboardServiceImpl implements
		CustomReportDashboardService {
	
	@Inject
	CustomReportDashboardDao customReportDashboardDao;
	
	@Inject
	CustomReportChartBindingDao bindingDao;
	
	@Inject
	CustomReportLibraryNodeService crlnService;
	
	@Inject
	private SessionFactory sessionFactory;
	
	@Inject
	protected PermissionEvaluationService permissionService;
	
	@Override
	public CustomReportDashboard findById(Long id) {
		return customReportDashboardDao.findById(id);
	}

	@Override
	@PreAuthorize("hasPermission(#newBinding.dashboard.id, 'org.squashtest.tm.domain.customreport.CustomReportDashboard' ,'WRITE') "
			+ OR_HAS_ROLE_ADMIN)
	public void bindChart(CustomReportChartBinding newBinding) {
		bindingDao.persist(newBinding);
		sessionFactory.getCurrentSession().flush();
	}

	@Override
	public void updateGridPosition(List<CustomReportChartBinding> transientBindings) {
		for (CustomReportChartBinding transientBinding : transientBindings) {
			updateBinding(transientBinding);
		}
		
	}

	private void updateBinding(CustomReportChartBinding transientBinding) {
		CustomReportChartBinding persistedBinding = bindingDao.findById(transientBinding.getId());
		if(permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN","WRITE",persistedBinding.getDashboard())
				&& persistedBinding.hasMoved(transientBinding)){
			persistedBinding.move(transientBinding);
		}
	}

	@Override
	@PreAuthorize("hasPermission(#id, 'org.squashtest.tm.domain.customreport.CustomReportChartBinding' ,'WRITE') "
			+ OR_HAS_ROLE_ADMIN)
	public void unbindChart(Long id) {
		CustomReportChartBinding chartBinding = bindingDao.findById(id);
		bindingDao.remove(chartBinding);
	}

	@Override
	@PreAuthorize("hasPermission(#bindingId, 'org.squashtest.tm.domain.customreport.CustomReportChartBinding' ,'WRITE') "
			+ OR_HAS_ROLE_ADMIN)
	public CustomReportChartBinding changeBindedChart(long bindingId,
			long chartNodeId) {
		CustomReportChartBinding chartBinding = bindingDao.findById(bindingId);
		ChartDefinition chartDefinition = crlnService.findChartDefinitionByNodeId(chartNodeId);
		chartBinding.setChart(chartDefinition);
		return chartBinding;
	}
	
}
