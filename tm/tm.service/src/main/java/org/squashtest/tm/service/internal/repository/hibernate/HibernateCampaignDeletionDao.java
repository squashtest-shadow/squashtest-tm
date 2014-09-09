/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.service.internal.repository.hibernate;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.campaign.CampaignFolder;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.service.internal.repository.CampaignDeletionDao;

@Repository
public class HibernateCampaignDeletionDao extends HibernateDeletionDao
		implements CampaignDeletionDao {


	
	@Override
	public void removeEntities(List<Long> entityIds) {
		if (!entityIds.isEmpty()) {

			Query query = null;
			for(Long entityId : entityIds){
				
				query = getSession().getNamedQuery("campaignLibraryNode.findById");
				query.setParameter("libraryNodeId", entityId);
				CampaignLibraryNode node = (CampaignLibraryNode) query.uniqueResult();
				
				removeEntityFromParentLibraryIfExists(entityId, node);

				removeEntityFromParentFolderIfExists(entityId, node);
				
				if(node != null){
					getSession().delete(node);
					getSession().flush();
				}
			}
								
	
		}
	}
	
	private void removeEntityFromParentLibraryIfExists(Long entityId, CampaignLibraryNode node){
		Query query = getSession().getNamedQuery("campaignLibraryNode.findParentLibraryIfExists");
		query.setParameter("libraryNodeId", entityId);
		CampaignLibrary library = (CampaignLibrary) query.uniqueResult();
		if(library != null){
			ListIterator<CampaignLibraryNode> iterator = library.getContent().listIterator();
			while (iterator.hasNext()) {
				CampaignLibraryNode tcln = iterator.next();
				if (tcln.getId().equals(node.getId())) {
					library.removeContent(tcln);
					break;
				}
			}
		}
	}
	
	private void removeEntityFromParentFolderIfExists(Long entityId, CampaignLibraryNode node){
		Query query = getSession().getNamedQuery("campaignLibraryNode.findParentFolderIfExists");
		query.setParameter("libraryNodeId", entityId);
		CampaignFolder folder = (CampaignFolder) query.uniqueResult();
		if(folder != null){
			ListIterator<CampaignLibraryNode> iterator = folder.getContent().listIterator();
			while (iterator.hasNext()) {
				CampaignLibraryNode tcln = iterator.next();
				if (tcln.getId().equals(node.getId())) {
					folder.removeContent(tcln);
					break;
				}
			}
		}
	}
	
	@Override
	public List<Long>[] separateFolderFromCampaignIds(List<Long> originalIds) {
		List<Long> folderIds = new ArrayList<Long>();
		List<Long> campaignIds = new ArrayList<Long>();
		
		List<BigInteger> filtredFolderIds = executeSelectSQLQuery(
						NativeQueries.CAMPAIGNLIBRARYNODE_SQL_FILTERFOLDERIDS, "campaignIds", originalIds);
		
		for (Long oId : originalIds){
			if (filtredFolderIds.contains(BigInteger.valueOf(oId))){
				folderIds.add(oId);
			}
			else{
				campaignIds.add(oId);
			}
		}
		
		List<Long>[] result = new List[2];
		result[0] = folderIds;
		result[1] = campaignIds;
		
		return result;
	}

}
