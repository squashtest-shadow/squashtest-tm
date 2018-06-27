/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) Henix, henix.fr
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
package org.squashtest.tm.web.internal.model.builder;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.dataset.DatasetLibraryNode;
import org.squashtest.tm.domain.dataset.DatasetTreeDefinition;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;
import org.squashtest.tm.service.security.PermissionEvaluationService;

import javax.inject.Inject;

@Component("globalDataset.nodeBuilder")
@Scope("prototype")
public class GlobalDatasetTreeNodeBuilder extends GenericTreeNodeBuilder<DatasetLibraryNode> {

	@Inject
	public GlobalDatasetTreeNodeBuilder(PermissionEvaluationService permissionEvaluationService) {
		super(permissionEvaluationService);
	}

	@Override
	public JsTreeNode build(DatasetLibraryNode dln) {
		JsTreeNode builtNode = buildGenericNode(dln);

		//A visitor would be elegant here and allow interface type development but we don't want hibernate to fetch each linked entity
		//for each node and we don't want subclass for each node type. sooooo the good old switch on enumerated type will do the job...
		DatasetTreeDefinition entityType = (DatasetTreeDefinition) dln.getEntityType();//NO SONAR the argument for this method is a DatasetLibraryNode so entity type is a DatasetTreeDefinition

		switch (entityType) {
			case LIBRARY:
				doLibraryBuild(builtNode,dln);
				break;
			case FOLDER:
				doFolderBuild(builtNode,dln);
				break;
			case GLOBAL_DATASET:
				doGlobalDatasetBuild(builtNode,dln);
				break;
			case COMPOSITE:
				doCompositeBuild(builtNode,dln);
				break;
			case TEMPLATE:
				doTemplateBuild(builtNode,dln);
				break;
			default:
				throw new UnsupportedOperationException("The node builder isn't implemented for node of type : " + entityType);
		}

		return builtNode;
	}

	private void doLibraryBuild(JsTreeNode builtNode, DatasetLibraryNode dln) {
		setNodeHTMLId(builtNode, "DatasetLibrary-"+dln.getId());
		setNodeRel(builtNode, "drive");
		setNodeResType(builtNode, "dataset-libraries");
		setStateForNodeContainer(builtNode, dln);
	}

	private void doFolderBuild(JsTreeNode builtNode, DatasetLibraryNode dln) {
		setNodeHTMLId(builtNode, "DatasetFolder-"+dln.getId());
		setNodeRel(builtNode, "folder");
		setNodeResType(builtNode, "dataset-folders");
		setStateForNodeContainer(builtNode, dln);
	}

	private void doGlobalDatasetBuild(JsTreeNode builtNode, DatasetLibraryNode dln) {
		setNodeHTMLId(builtNode, "DatasetGlobal-"+dln.getId());
		setNodeRel(builtNode, "globalDataset");
		setNodeResType(builtNode, "dataset-composite");
		setStateForNodeContainer(builtNode, dln);
	}

	private void doCompositeBuild(JsTreeNode builtNode, DatasetLibraryNode dln) {
		setNodeHTMLId(builtNode, "DatasetComposite-"+dln.getId());
		setNodeRel(builtNode, "composite");
		setNodeResType(builtNode, "dataset-composite");
		setStateForNodeContainer(builtNode, dln);
	}

	private void doTemplateBuild(JsTreeNode builtNode, DatasetLibraryNode dln) {
		setNodeHTMLId(builtNode, "DatasetTemplate-"+dln.getId());
		setNodeRel(builtNode, "template");
		setNodeResType(builtNode, "dataset-template");
		setStateForNodeContainer(builtNode, dln);
	}
}
