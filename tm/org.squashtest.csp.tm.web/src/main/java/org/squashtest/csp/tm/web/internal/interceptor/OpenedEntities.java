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
package org.squashtest.csp.tm.web.internal.interceptor;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.csp.core.domain.Identified;
import org.squashtest.csp.tm.domain.campaign.Campaign;
import org.squashtest.csp.tm.domain.testcase.TestCase;

public class OpenedEntities {
	private static final Logger LOGGER = LoggerFactory.getLogger(OpenedEntities.class);
	
	private Map<Long, OpenedEntity> entitiesViewers;
	
	public static final List<String> MANAGED_ENTITIES_LIST = Arrays.asList(TestCase.class.getSimpleName());
	
	public OpenedEntities(){
		entitiesViewers = new HashMap<Long, OpenedEntity>();
	}
	
	public synchronized boolean addViewerToEntity(Identified object, String userLogin) {
		//get the entity || create one if none
		OpenedEntity openedEntity = findOpenedEntity(object);
		
		//add viewer to entity and return true if viewer is not the only one
		return openedEntity.addViewForViewer(userLogin);
	}
	
	
	private synchronized OpenedEntity findOpenedEntity(Identified object) {
		OpenedEntity openedEntity = this.entitiesViewers.get(object.getId());
		if(openedEntity == null){
			LOGGER.debug("Entity was not listed => new Entity");
			openedEntity = new OpenedEntity();
			this.entitiesViewers.put(object.getId(), openedEntity);
		}else{
			LOGGER.debug("Entity was already listed");
		}
		return openedEntity;
	}
	
	
	public synchronized void removeViewer(String viewerLogin){
		for(Entry<Long, OpenedEntity> entityViewers : entitiesViewers.entrySet()){
			OpenedEntity openedEntity = entityViewers.getValue();
			openedEntity.removeAllViewsForViewer(viewerLogin);
		}
	}

	public synchronized void removeView(String name, Long id) {
		OpenedEntity openedEntity = this.entitiesViewers.get(id);
		if(openedEntity != null){
			openedEntity.removeViewForViewer(name);
		}
		
	}
}
