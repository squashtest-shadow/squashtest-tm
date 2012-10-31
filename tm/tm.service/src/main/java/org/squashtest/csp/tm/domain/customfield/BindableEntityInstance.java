/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.csp.tm.domain.customfield;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;


@Embeddable
public class BindableEntityInstance {

	private Long boundEntityId;
	
	@Enumerated(EnumType.STRING)
	private BindableEntity boundEntity;

	
	public BindableEntityInstance(){
		super();
	}
		
	public BindableEntityInstance(Long boundEntityId,
			BindableEntity boundEntity) {
		super();
		this.boundEntityId = boundEntityId;
		this.boundEntity = boundEntity;
	}

	public Long getBoundEntityId() {
		return boundEntityId;
	}
	
	public void setBoundEntityId(Long boundEntityId) {
		this.boundEntityId = boundEntityId;
	}
	
	public BindableEntity getBoundEntity() {
		return boundEntity;
	}
	
	public void setBoundEntity(BindableEntity boundEntity) {
		this.boundEntity = boundEntity;
	}
	
}
