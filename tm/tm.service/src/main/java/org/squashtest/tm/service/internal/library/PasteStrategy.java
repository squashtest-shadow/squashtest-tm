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
package org.squashtest.tm.service.internal.library;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Session;
import org.squashtest.tm.domain.library.NodeContainer;
import org.squashtest.tm.domain.library.TreeNode;
import org.squashtest.tm.domain.project.GenericLibrary;
import org.squashtest.tm.service.advancedsearch.IndexationService;
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

	private static final Integer WHATEVER_POSITION = null;

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

	@Inject
	private IndexationService indexationService;

	@PersistenceContext
	private EntityManager em;

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
	private Collection<NodePairing> nextLayer;
	private Collection<NodePairing> sourceLayer;
	private Set<Long> tcIdsToIndex = new HashSet<>();
	private Set<Long> reqVersionIdsToIndex = new HashSet<>();

	// ******************* code *****************************

	@CacheScope
	public List<NODE> pasteNodes(long containerId, List<Long> list) {
		return internalPasteNodes(containerId, list, WHATEVER_POSITION);
	}

	@CacheScope
	public List<NODE> pasteNodes(long containerId, List<Long> list, Integer position) {
		return internalPasteNodes(containerId, list, position);
	}

	private List<NODE> internalPasteNodes(long containerId, List<Long> list, Integer position) {


		// proceed : will process the nodes layer by layer.
		init(containerId, list);

		// process the first layer and memorize processed entities
		processFirstLayer(position);

		// loop on all following generations
		while (!nextLayer.isEmpty()) {

			removeProcessedNodesFromCache();

			shiftToNextLayer();

			processLayer();

		}

		reindexAfterCopy();
		return outputList;
	}


	private void init(long containerId, List<Long> list) {
		firstOperation = createFirstLayerOperation();
		nextsOperation = createNextLayerOperation();
		outputList = new ArrayList<>(list.size());
		nextLayer = new ArrayList<>();

		// init the source layer
		sourceLayer = new HashSet<>();
		CONTAINER container = containerDao.findById(containerId);
		NodePairing pairing = new NodePairing((NodeContainer<TreeNode>)container);

		for (Long contentId : list){
			NODE srcNode = nodeDao.findById(contentId);
			pairing.addContent(srcNode);
		}
		sourceLayer.add(pairing);
	}


	private void shiftToNextLayer() {
		sourceLayer = nextLayer;
		nextLayer = new ArrayList<>();
	}


	@SuppressWarnings("unchecked")
	private void processFirstLayer(Integer position) {

		// yeah that's an obnoxious way to get the unique element of a collection
		// (sourceLayer contains only one pairing at that time)
		NodePairing pairing = sourceLayer.iterator().next();

		CONTAINER container = (CONTAINER)pairing.getContainer();
		Collection<TreeNode> newContent = pairing.getNewContent();

		for (NODE srcNode : (Collection<NODE>)newContent) {
			NODE outputNode = (NODE) firstOperation.performOperation(srcNode, (NodeContainer<TreeNode>) container,
					position);
			outputList.add(outputNode);
			if (position != null){
				position++;
			}
			if (firstOperation.isOkToGoDeeper()) {
				appendNextLayerNodes(srcNode, outputNode);
			}
		}

		reqVersionIdsToIndex.addAll(firstOperation.getRequirementVersionToIndex());
		tcIdsToIndex.addAll(firstOperation.getTestCaseToIndex());
	}



	/**
	 * Process non first layer.
	 */
	private void processLayer() {

		for (NodePairing pairing : sourceLayer){
			NodeContainer<TreeNode> destination = pairing.getContainer();
			Collection<TreeNode> sources = pairing.getNewContent();

			for (TreeNode source : sources) {
				TreeNode outputNode = nextsOperation.performOperation(source, destination, WHATEVER_POSITION);

				if (nextsOperation.isOkToGoDeeper()) {
					appendNextLayerNodes(source, outputNode);
				}
			}
		}



		reqVersionIdsToIndex.addAll(nextsOperation.getRequirementVersionToIndex());
		tcIdsToIndex.addAll(nextsOperation.getTestCaseToIndex());
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

	private void reindexAfterCopy() {
		//Flushing session now, as reindex will clear the HibernateSession when FullTextSession will be cleared.
		em.unwrap(Session.class).flush();
		indexationService.batchReindexTc(new ArrayList<>(tcIdsToIndex));
		indexationService.batchReindexReqVersion(new ArrayList<>(reqVersionIdsToIndex));
	}


	/*
	 * Here we want to evict the nodes already processed from the Hibernate cache. We do so only
	 * if that same node won't be processed again in the next iteration
	 *
	 */
	private void removeProcessedNodesFromCache() {

		// if we cont flush and then evict, some entities might not be persisted
		genericDao.flush();

		Collection<TreeNode> nextNodes = new HashSet<>();
		for (NodePairing nextPairing : nextLayer){
			nextNodes.add((TreeNode)nextPairing.getContainer());
			nextNodes.addAll(nextPairing.getNewContent());
		}


		/*
		 *  Now we evict what we can. Note that the collection toEvict is of generic type Object because not all nodes are TreeNode :
		 *  indeed a TestCaseLibrary is not a TreeNode.
		 */
		for (NodePairing processed : sourceLayer){
			Collection<Object> toEvict = new HashSet<>();
			toEvict.add(processed.getContainer());
			toEvict.addAll(processed.getNewContent());

			for (Object evi : toEvict){
				/*
				 * evict a node only if :
				 * - not evicted already,
				 * - not a library (or at flush time the project will rant)
				 */
				if (! nextNodes.contains(evi) &&
						!GenericLibrary.class.isAssignableFrom(evi.getClass())){
					genericDao.clearFromCache(evi);
				}
			}
		}

	}

}
