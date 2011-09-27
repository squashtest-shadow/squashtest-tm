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
package org.squashtest.csp.tm.internal.service.deletion;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.squashtest.csp.tm.domain.requirement.RequirementFolder;
import org.squashtest.csp.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.csp.tm.internal.repository.FolderDao;
import org.squashtest.csp.tm.internal.repository.RequirementDeletionDao;
import org.squashtest.csp.tm.internal.repository.RequirementFolderDao;
import org.squashtest.csp.tm.internal.service.RequirementNodeDeletionHandler;
import org.squashtest.csp.tm.service.deletion.SuppressionPreviewReport;

@Component("squashtest.tm.service.deletion.RequirementNodeDeletionHandler")
public class RequirementDeletionHandlerImpl extends
		AbstractNodeDeletionHandlerImpl<RequirementLibraryNode, RequirementFolder> implements
		RequirementNodeDeletionHandler {

	@Inject
	private RequirementFolderDao folderDao;
	
	
	@Inject
	private RequirementDeletionDao deletionDao;
	
	@Override
	protected FolderDao<RequirementFolder, RequirementLibraryNode> getFolderDao() {
		return folderDao;
	}


	@Override
	protected List<SuppressionPreviewReport> diagnoseSuppression(
			List<Long> nodeIds) {
		List<SuppressionPreviewReport> preview = new LinkedList<SuppressionPreviewReport>();
		
		//TODO : perform an actual verification
		
		return preview;
	}

	@Override
	protected List<Long> detectLockedNodes(List<Long> nodeIds) {
		List<Long> lockedIds = new LinkedList<Long>();
		
		//TODO : up to now a requirement is never locked for deletion (safe for security check)
		//however if it may change later put something here.
		
		return lockedIds;
	}




	@Override
	/*y.
	 * 
	 * 
	 * Thus, removing a list of RequirementLibraryNodes means :
	 * 	- find all the attachment lists,
	 *  - remove them,
	 *  - remove the nodes themselves
	 * 
	 */
	protected void batchDeleteNodes(List<Long> ids) {
		if (!ids.isEmpty()) {
			
			List<Long> requirementAttachmentIds = deletionDao.findRequirementAttachmentListIds(ids);
			
			deletionDao.removeFromVerifiedTRequirementLists(ids);
			
			deletionDao.removeEntities(ids);
			
			deletionDao.removeAttachmentsLists(requirementAttachmentIds);

		}
	}


}
