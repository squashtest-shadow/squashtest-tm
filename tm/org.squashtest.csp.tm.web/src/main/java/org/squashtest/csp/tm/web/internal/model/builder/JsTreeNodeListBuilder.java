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
package org.squashtest.csp.tm.web.internal.model.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.squashtest.csp.tm.web.internal.model.jstree.JsTreeNode;

/**
 * Decorates an instance of {@link JsTreeNodeBuilder} to build a list of {@link JsTreeNode} from a list of items.
 * 
 * @author Gregory Fouquet
 * 
 * @param <ITEM>
 */
public class JsTreeNodeListBuilder<ITEM> {
	private final JsTreeNodeBuilder<? super ITEM, ?> nodeBuilder;
	private Collection<ITEM> model;

	public JsTreeNodeListBuilder(JsTreeNodeBuilder<? super ITEM, ?> nodeBuilder) {
		super();
		this.nodeBuilder = nodeBuilder;
	}

	public final JsTreeNodeListBuilder<ITEM> setModel(Collection<ITEM> model) {
		this.model = model;
		return this;
	}

	public final List<JsTreeNode> build() {
		List<JsTreeNode> nodes = new ArrayList<JsTreeNode>();

		for (ITEM item : model) {
			nodes.add(nodeBuilder.setModel(item).build());
		}

		return nodes;
	}
}
