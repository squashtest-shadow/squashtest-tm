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

import org.hibernate.Query;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.service.internal.repository.CampaignDeletionDao;

@Repository
public class HibernateCampaignDeletionDao extends HibernateDeletionDao
		implements CampaignDeletionDao {

	@Override
	public void removeEntities(List<Long> entityIds) {
		if (!entityIds.isEmpty()) {
			
			Query query=getSession().createSQLQuery(NativeQueries.CAMPAIGN_SQL_REMOVEFROMFOLDER);
			query.setParameterList("ancIds", entityIds, LongType.INSTANCE);
			query.setParameterList("descIds", entityIds, LongType.INSTANCE);
			query.executeUpdate();
			
			query=getSession().createSQLQuery(NativeQueries.CAMPAIGN_SQL_REMOVEFROMLIBRARY);
			query.setParameterList("campaignIds", entityIds, LongType.INSTANCE);
			query.executeUpdate();		
			
			query = getSession().createSQLQuery(
					NativeQueries.CAMPAIGNFOLDER_SQL_REMOVE);
			query.setParameterList("nodeIds", entityIds, new LongType());
			query.executeUpdate();

			query = getSession().createSQLQuery(
					NativeQueries.CAMPAIGN_SQL_REMOVE);
			query.setParameterList("nodeIds", entityIds, new LongType());
			query.executeUpdate();

			query = getSession().createSQLQuery(
					NativeQueries.CAMPAIGNLIBRARYNODE_SQL_REMOVE);
			query.setParameterList("nodeIds", entityIds, new LongType());
			query.executeUpdate();
			
			
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
