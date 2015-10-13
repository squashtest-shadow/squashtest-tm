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
package org.squashtest.tm.web.internal.controller.customreport;

import java.util.ArrayList;
import java.util.List;

import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.tree.TreeLibraryNode;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;

public class CustomReportListTreeNodeBuilder {

	private List<JsTreeNode> builtNodes;
	private List<TreeLibraryNode> nodes;
	public CustomReportListTreeNodeBuilder(List<TreeLibraryNode> children) {
		super();
		this.nodes = children;
		builtNodes = new ArrayList<JsTreeNode>();
	}
	
	public List<JsTreeNode> build(){
		for (TreeLibraryNode tln : nodes) {
			CustomReportTreeNodeBuilder builder = new CustomReportTreeNodeBuilder();
			builtNodes.add(builder.build((CustomReportLibraryNode) tln));//NOSONAR cast is safe
		}
		return builtNodes;
	}
	
}
