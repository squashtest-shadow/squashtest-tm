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
package org.squashtest.tm.service.internal.campaign;


import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.service.campaign.CampaignAdvancedSearchService;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.project.ProjectManagerService;

@Transactional(readOnly=true)
@Service("CampaignAdvancedSearchService")
public class CampaignAdvancedSearchServiceImpl implements CampaignAdvancedSearchService {

	@Inject
	protected ProjectManagerService projectFinder;

	@Inject
	private SessionFactory sessionFactory;

	@Inject
	private ProjectDao projectDao;

	@Override
	public List<String> findAllAuthorizedUsersForACampaign() {
		List<Project> readableProjects = projectFinder.findAllReadable();
		List<Long> projectIds = new ArrayList<Long>(readableProjects.size());
		for (Project project : readableProjects) {
			projectIds.add(project.getId());
		}
		/* return projectDao.findUsersWhoCanAccessProject(projectIds); */
		List<String> temp = new ArrayList<String>();
		return temp;

	}

}
