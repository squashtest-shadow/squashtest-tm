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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.squashtest.csp.tm.domain.library.Folder;
import org.squashtest.csp.tm.domain.library.LibraryNode;
import org.squashtest.csp.tm.internal.repository.FolderDao;
import org.squashtest.csp.tm.internal.service.NodeDeletionHandler;
import org.squashtest.csp.tm.service.deletion.SuppressionPreviewReport;



public abstract class AbstractNodeDeletionHandlerImpl<NODE extends LibraryNode, FOLDER extends Folder<NODE>>
		implements NodeDeletionHandler<NODE, FOLDER>{

	protected abstract FolderDao<FOLDER, NODE> getFolderDao();


	@Override
	public List<SuppressionPreviewReport> simulateDeletion(List<Long> targetIds){
		List<Long> nodeIds = findNodeHierarchy(targetIds);
		return  diagnoseSuppression(nodeIds);
	}

	/**
	 * will perform the actual deletion and return the list of the nodes that were eventually deleted
	 *
	 * @param targetIds
	 * @return the ids of the nodes actually deleted
	 */
	@Override
	public List<Long> deleteNodes(List<Long> targetIds){

		//phase 1 : find all the nodes and build the tree
		List<Long[]> hierarchy = findPairedNodeHierarchy(targetIds);

		LockedFolderInferenceTree tree = new LockedFolderInferenceTree();
		tree.build(hierarchy);

		//phase 2 : find the nodes that aren't deletable and mark them as such in the tree
		List<Long> candidateNodeIds = tree.collectKeys();
		List<Long> lockedNodeIds = detectLockedNodes(candidateNodeIds);

		//phase 3 : resolve which folders are locked with respect to the locked content.
		tree.markNodesAsLocked(lockedNodeIds);
		tree.resolveLockedFolders();


		//phase 4 : now the dependencies between the nodes are resolved we may collect the ids of deletable nodes
		//and batch - delete them
		List<Long> deletableNodeIds =  tree.collectDeletableIds();

		batchDeleteNodes(deletableNodeIds);

		return deletableNodeIds;
	}


	/**
	 * returns the node hierarchy as a list of pairs. Each pair is an array of long (node ids) such as [ parent.id, child.id ].
	 *
	 * @param rootNodesIds the ids from which we start the hierarchy
	 * @return the list of pairs of id described above.
	 */
	@SuppressWarnings("unchecked")
	protected List<Long[]> findPairedNodeHierarchy(List<Long> rootNodeIds){

		if (rootNodeIds.isEmpty()) {
			return Collections.emptyList();
		}

		List<Long[]> nodeHierarchy = new ArrayList<Long[]>();

		for (Long id : rootNodeIds){
			nodeHierarchy.add(new Long[]{null, id});
		}

		//now we loop over the folder structure.
		List<Long> currentLayer = rootNodeIds;

		//let's loop
		while (! currentLayer.isEmpty()){
			List<Long[]> nextPairedLayer = getFolderDao().findPairedContentForList(currentLayer);

			nodeHierarchy.addAll(nextPairedLayer);

			currentLayer = new LinkedList<Long>(CollectionUtils.collect(nextPairedLayer, new Transformer() {
				@Override
				public Object transform(Object input) {
					Long value = (Long)((Object[])input)[1];
					return value;
				}
			}));
		}

		return nodeHierarchy;
	}

	protected List<Long> findNodeHierarchy(List<Long> rootNodeIds){
		if (rootNodeIds.isEmpty()) {
			return Collections.emptyList();
		}

		List<Long> nodeHierarchy = new ArrayList<Long>();

		nodeHierarchy.addAll(rootNodeIds);

		//now we loop over the folder structure.
		List<Long> currentLayer = rootNodeIds;

		//let's loop
		while (! currentLayer.isEmpty()){
			List<Long> nextPairedLayer = getFolderDao().findContentForList(currentLayer);

			nodeHierarchy.addAll(nextPairedLayer);

			currentLayer = nextPairedLayer;
		}

		return nodeHierarchy;
	}



	/**
	 * given the node ids, should return a report of the consequences of the suppression asked by the caller.
	 * For instance : impossible suppressions, linked slave data and such.
	 *
	 * @param nodeIds
	 * @return a list of reports summarizing in human readable format what will happen.
	 */
	protected abstract List<SuppressionPreviewReport> diagnoseSuppression(List<Long> nodeIds);

	/**
	 * given a list of node ids, returns a sublist corresponding to the ids of the nodes which
	 * cannot be deleted according to the specs.
	 *
	 * You do not have to worry about folders, the abstract handler will infer the consequences on the
	 * hierarchy later.
	 *
	 * @param nodeIds
	 * @return the sublist of node ids that should be deleted.
	 */
	protected abstract List<Long> detectLockedNodes(List<Long> nodeIds);


	/**
	 * will forcibly delete the nodes identified by the ids parameter.
	 *
	 * @param ids
	 */
	protected abstract void batchDeleteNodes(List<Long> ids);


}
