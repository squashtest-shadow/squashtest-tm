/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.service.internal.repository;

import java.util.List;

public interface RequirementDeletionDao extends DeletionDao {
	
	/**
	 * Given a list of RequirementLibraryNode ids, will tell which ones are folder ids and which ones are requirements.
	 * 
	 * @param originalIds the requirement library node ids we want to sort out.
	 * @return an array of list of ids : result[0] are the folder ids and result[1] are the requirement ids.
	 */
	List<Long>[] separateFolderFromRequirementIds(List<Long> originalIds);
	
	List<Long> findRequirementAttachmentListIds(List<Long> requirementIds);
	List<Long> findRequirementFolderAttachmentListIds(List<Long> folderIds);
	
	void removeFromVerifiedRequirementLists(List<Long> requirementIds);
	
	void deleteRequirementAuditEvents(List<Long> requirementIds);
	
	List<Long> findVersionIds(List<Long> requirementIds);
	
	/**
	 * @param versionsIds
	 */
	void removeTestStepsCoverageByRequirementVersionIds(List<Long> versionsIds);

}
