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
package org.squashtest.tm.service.internal.repository.hibernate;

import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.attachment.AttachmentList;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.internal.repository.AttachmentListDao;

@Repository
public class HibernateAttachmentListDao extends HibernateEntityDao<AttachmentList> implements AttachmentListDao {

	@Override
	public TestCase findAssociatedTestCaseIfExists(Long attachmentListId) {

		TestCase testCase = (TestCase) currentSession().createCriteria(TestCase.class)
				.createCriteria("attachmentList")
				.add(Restrictions.eq("id",attachmentListId))
				.uniqueResult();
		
		return testCase;
	}

	@Override
	public RequirementVersion findAssociatedRequirementVersionIfExists(Long attachmentListId) {

		RequirementVersion requirementVersion = (RequirementVersion) currentSession().createCriteria(RequirementVersion.class)
				.createCriteria("attachmentList")
				.add(Restrictions.eq("id",attachmentListId))
				.uniqueResult();
		
		return requirementVersion;
	}
}
