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

/**
 * <p>This interface describes the contract for handlers responsible for deleting domain objects. Deleting an object is not straight forward
 * since one have to deal with the relationships among them, eg a TestCase and a Requirement being bound together. As a consequence the application must perform 
 * several checks beforehand in order to decide the actual course of action. </p> 
 * 
 * <p>As of Squash TM version 1.0, the specifications are the following :
 * <ol>
 * 	<li>A user requesting the removal of one or several entities, </li>
 *  <li>Depending on which relationships might be broken by removing one or several domain objects the operation might be allowed, allowed with a warning or denied.</li>
 *  <li>Batch removal is not atomic, eg when a subset of the operations are allowed while a second subset is denied, then the first subset will be processed anyway.</li>
 * </ol>
 * </p>
 * 
 * 
 * 
 * 
 * 
 */

package org.squashtest.csp.tm.internal.service;

import java.util.List;

import org.squashtest.csp.tm.domain.library.Folder;
import org.squashtest.csp.tm.domain.library.LibraryNode;
import org.squashtest.csp.tm.service.deletion.SuppressionPreviewReport;

public interface NodeDeletionHandler<NODE extends LibraryNode, FOLDER extends Folder<NODE>> {
	/**
	 * that method should investigate the consequences of the deletion request, and return a report
	 * about what will happen.
	 * 
	 * @param targetIds
	 * @return
	 */
	List<SuppressionPreviewReport> simulateDeletion(List<Long> targetIds);
	
	
	/**
	 * that method should delete the nodes. It still takes care of non deletable nodes so
	 * the implementation should filter out the ids who can't be deleted.
	 * 
	 * 
	 * @param targetIds
	 * @return the list of the ids of the nodes actually deleted.
	 */
	List<Long> deleteNodes(List<Long> targetIds);
	
}
