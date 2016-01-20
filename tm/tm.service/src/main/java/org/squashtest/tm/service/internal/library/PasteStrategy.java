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
package org.squashtest.tm.service.internal.library;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Provider;

import org.squashtest.tm.domain.library.NodeContainer;
import org.squashtest.tm.domain.library.TreeNode;
import org.squashtest.tm.service.annotation.CacheScope;
import org.squashtest.tm.service.internal.repository.EntityDao;
import org.squashtest.tm.service.internal.repository.GenericDao;

/**
 * Careful : As of Squash TM 1.5.0 this object becomes stateful, in layman words you need one instance per operation. <br/>
 * <br/>
 * This class holds the logic for processing all nodes in operations that need to go throug a node branch. So far it is
 * used for copying and moving nodes. When moving nodes it can be necessary to go throug the branch to update cufs etc.
 * (see {@linkplain NextLayersTreeNodeMover}. <br/>
 * <br/>
 * To use the PasteStategy, you need to define the CONTAINER type, the {@linkplain #nodeDao} and the
 * {@linkplain #containerDao}. This is done in <b>bundle-context.xml</b>, look for examples there. <br/>
 * <br/>
 * You will also need to define the operations that needs to be done for the nodes in the first and next layers. This is
 * done when the PasteStrategy is being used (see
 * {@linkplain AbstractLibraryNavigationService#makeCopierStrategy(PasteStrategy)} and
 * {@linkplain AbstractLibraryNavigationService#makeMoverStrategy(PasteStrategy)} as examples).
 * <br><br>
 * 
 * <u>What is a layer ?</u><br>
 * A layer is a map holing
 * <ul>
 * <li>a key : the destination , a {@link NodeContainer}</li>
 * <li>a value : the list of source {@link TreeNode} that will be processed by a {@link PasteOperation} and which result will be
 * added to the destination</li>
 * </ul>
 * 
 * @author gfouquet, mpagnon, bsiri
 * 
 * @param <CONTAINER>
 * @param <NODE>
 */

/*
 * for documentation purposes :
 * 
 * @Scope("prototype")
 */
public class PasteStrategy<CONTAINER extends NodeContainer<NODE>, NODE extends TreeNode> {

	// **************** collaborators **************************

	private Provider<? extends PasteOperation> nextLayersOperationFactory;
	private Provider<? extends PasteOperation> firstLayerOperationFactory;
	@Inject
	private Provider<NextLayerFeeder> nextLayerFeederOperationFactory;
	private PasteOperation firstOperation;
	private PasteOperation nextsOperation;
	private GenericDao<Object> genericDao;
	private EntityDao<CONTAINER> containerDao;
	private EntityDao<NODE> nodeDao;

	public void setGenericDao(GenericDao<Object> genericDao) {
		this.genericDao = genericDao;
	}

	public <R extends EntityDao<CONTAINER>> void setContainerDao(R containerDao) {
		this.containerDao = containerDao;
	}

	public void setNodeDao(EntityDao<NODE> nodeDao) {
		this.nodeDao = nodeDao;
	}

	public void setNextLayersOperationFactory(Provider<? extends PasteOperation> nextLayersOperationFactory) {
		this.nextLayersOperationFactory = nextLayersOperationFactory;
	}

	public void setFirstLayerOperationFactory(Provider<? extends PasteOperation> firstLayerOperationFactory) {
		this.firstLayerOperationFactory = firstLayerOperationFactory;
	}

	public void setNextLayerFeederOperationFactory(Provider<NextLayerFeeder> nextLayerFeederOperationFactory) {
		this.nextLayerFeederOperationFactory = nextLayerFeederOperationFactory;
	}

	// ***************** treatment-scoped variables ****************

	private List<NODE> outputList;
	private Map<NodeContainer<TreeNode>, Collection<TreeNode>> nextLayer;
	private Map<NodeContainer<TreeNode>, Collection<TreeNode>> sourceLayer;
	private Map<NodeContainer<TreeNode>, Collection<TreeNode>> sourceLayerParents;

	// ******************* code *****************************
	@CacheScope
	public List<NODE> pasteNodes(long containerId, List<Long> list) {

		// fetch
		CONTAINER container = containerDao.findById(containerId);

		// proceed : will process the nodes layer by layer.
		init(list.size());

		// process the first layer and memorize processed entities
		processFirstLayer(container, list);

		// loop on all following generations
		while (!nextLayer.isEmpty()) {

			removeProcessedNodesFromCache();

			shiftToNextLayer();

			processLayer();

		}

		return outputList;
	}

	@CacheScope
	public List<NODE> pasteNodes(long containerId, List<Long> list, int position) {

		// fetch
		CONTAINER container = containerDao.findById(containerId);

		// proceed : will process the nodes layer by layer.
		init(list.size());

		// process the first layer and memorize processed entities
		processFirstLayer(container, list, position);

		// loop on all following generations
		while (!nextLayer.isEmpty()) {

			removeProcessedNodesFromCache();

			shiftToNextLayer();

			processLayer();

		}

		return outputList;
	}

	private void init(int nbCopiedNodes) {
		firstOperation = createFirstLayerOperation();
		nextsOperation = createNextLayerOperation();
		outputList = new ArrayList<NODE>(nbCopiedNodes);
		nextLayer = new HashMap<NodeContainer<TreeNode>, Collection<TreeNode>>();
		sourceLayer = null;
		sourceLayerParents = null;
	}

	private void removeProcessedNodesFromCache() {
		if (sourceLayerParents != null) {
			// if we cont flush and then evict, some entities might not be persisted
			genericDao.flush();

			Collection<TreeNode> evicted = new HashSet<TreeNode>();
			// when moving to a next layer, evict the nodes that won't be used anymore - those who will not receive
			// content anymore.
			// note: will note evict the nodes to return because they never been in the "sourceLayer" map.
			for (Entry<NodeContainer<TreeNode>, Collection<TreeNode>> processedLayerEntry : sourceLayerParents
					.entrySet()) {
				NodeContainer<TreeNode> container = processedLayerEntry.getKey();
				Collection<TreeNode> content = processedLayerEntry.getValue();
				genericDao.clearFromCache(container);
				for (TreeNode node : content) {
					if (!evicted.contains(node)) {
						evicted.add(node);
						genericDao.clearFromCache(node);
					}
				}
			}
		}
	}

	private void shiftToNextLayer() {
		sourceLayerParents = sourceLayer;
		sourceLayer = nextLayer;
		nextLayer = new HashMap<NodeContainer<TreeNode>, Collection<TreeNode>>();
	}

	@SuppressWarnings("unchecked")
	private void processFirstLayer(CONTAINER container, List<Long> list) {
		for (Long id : list) {
			NODE srcNode = nodeDao.findById(id);
			NODE outputNode = (NODE) firstOperation.performOperation(srcNode, (NodeContainer<TreeNode>) container);
			outputList.add(outputNode);
			if (firstOperation.isOkToGoDeeper()) {
				appendNextLayerNodes(srcNode, outputNode);
			}
		}
		firstOperation.reindexAfterCopy();
	}

	@SuppressWarnings("unchecked")
	private void processFirstLayer(CONTAINER container, List<Long> list, int position) {
		for (Long id : list) {
			NODE srcNode = nodeDao.findById(id);
			NODE outputNode = (NODE) firstOperation.performOperation(srcNode, (NodeContainer<TreeNode>) container,
					position);
			outputList.add(outputNode);
			position++;
			if (firstOperation.isOkToGoDeeper()) {
				appendNextLayerNodes(srcNode, outputNode);
			}
		}
		firstOperation.reindexAfterCopy();
	}

	/**
	 * Process non first layer.
	 */
	private void processLayer() {

		for (Entry<NodeContainer<TreeNode>, Collection<TreeNode>> sourceEntry : sourceLayer.entrySet()) {

			Collection<TreeNode> sources = sourceEntry.getValue();
			NodeContainer<TreeNode> destination = sourceEntry.getKey();

			for (TreeNode source : sources) {
				TreeNode outputNode = nextsOperation.performOperation(source, destination);
				if (nextsOperation.isOkToGoDeeper()) {
					appendNextLayerNodes(source, outputNode);
				}
			}
		}

		nextsOperation.reindexAfterCopy();

	}


	private PasteOperation createNextLayerOperation() {
		return nextLayersOperationFactory.get();
	}

	private PasteOperation createFirstLayerOperation() {
		return firstLayerOperationFactory.get();
	}

	/**
	 * feeds next layer avoiding nodes from the outputList.
	 * 
	 * @param destNode
	 * @param sourceNode
	 */
	private void appendNextLayerNodes(TreeNode sourceNode, TreeNode destNode) {
		NextLayerFeeder feeder = nextLayerFeederOperationFactory.get();
		feeder.feedNextLayer(destNode, sourceNode, this.nextLayer, this.outputList);
	}

}
