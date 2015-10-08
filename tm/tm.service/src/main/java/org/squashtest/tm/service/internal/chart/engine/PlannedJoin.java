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
package org.squashtest.tm.service.internal.chart.engine;

import org.squashtest.tm.core.foundation.lang.Couple;

class PlannedJoin extends Couple<InternalEntityType, InternalEntityType>{

	/**
	 * Describe how the two entities should be joined.
	 * <ul>
	 * 	<li>Natural : used an inner join over the attribute, nothing special about it</li>
	 * 	<li>Reversed Where : should be used when a natural join is not possible (because the relation is not mapped). </li>
	 * </ul>
	 * @author bsiri
	 *
	 */
	enum JoinType {
		NATURAL,
		WHERE
	}

	/**
	 * Name of the attribute of the source entity when the join type is NATURAL,
	 * or name of the foreign key of the dest entity when the join type is WHERE
	 */
	private String attribute;

	private JoinType type = JoinType.NATURAL;


	public PlannedJoin(InternalEntityType a1, InternalEntityType a2, String attribute) {
		super(a1, a2);
		this.attribute = attribute;
	}

	public PlannedJoin(InternalEntityType a1, InternalEntityType a2, String attribute, JoinType type) {
		super(a1, a2);
		this.attribute = attribute;
		this.type = type;
	}

	InternalEntityType getSrc(){
		return getA1();
	}

	InternalEntityType getDest(){
		return getA2();
	}

	String getAttribute(){
		return attribute;
	}

	JoinType getType(){
		return type;
	}

}