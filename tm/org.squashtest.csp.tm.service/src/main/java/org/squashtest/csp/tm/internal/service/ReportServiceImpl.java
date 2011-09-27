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
package org.squashtest.csp.tm.internal.service;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.squashtest.csp.tm.domain.report.Report;
import org.squashtest.csp.tm.domain.report.ReportCategory;
import org.squashtest.csp.tm.domain.report.ReportFactory;
import org.squashtest.csp.tm.domain.report.query.ReportQuery;
import org.squashtest.csp.tm.domain.report.query.UnsupportedFlavorException;
import org.squashtest.csp.tm.internal.repository.ReportQueryDao;
import org.squashtest.csp.tm.service.DataFilteringService;
import org.squashtest.csp.tm.service.ReportService;

@Service("squashtest.tm.service.ReportService")
public class ReportServiceImpl implements ReportService{

	private final ReportFactory reportFactory = ReportFactory.getInstance();

	@Inject
	private ReportQueryDao reportQueryDao;
	
	@Inject
	private DataFilteringService filterService;


	@Override
	public List<ReportCategory> findAllReportCategories() {
		return reportFactory.getAllReportCategories();
	}




	@Override
	public List<?> executeQuery(ReportQuery query) {
		try {
			query.SetDataFilteringService(filterService);
			return reportQueryDao.executeQuery(query);
		}catch(UnsupportedFlavorException ufe){
			throw new RuntimeException(ufe);
		}
	}




	@Override
	public Report findReportById(Integer reportId) {
		return reportFactory.findReportById(reportId);
	}









}
