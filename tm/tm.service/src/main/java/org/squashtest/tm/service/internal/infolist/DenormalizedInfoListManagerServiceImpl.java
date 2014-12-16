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
package org.squashtest.tm.service.internal.infolist;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.infolist.DenormalizedInfoList;
import org.squashtest.tm.domain.infolist.DenormalizedInfoListItem;
import org.squashtest.tm.domain.infolist.DenormalizedListItemReference;
import org.squashtest.tm.domain.infolist.DenormalizedUserListItem;
import org.squashtest.tm.domain.infolist.InfoList;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.service.infolist.DenormalizedInfoListManagerService;
import org.squashtest.tm.service.internal.repository.DenormalizedInfoListDao;
import org.squashtest.tm.service.internal.repository.DenormalizedInfoListItemDao;
import org.squashtest.tm.service.internal.repository.InfoListDao;

@Transactional
@Service("squashtest.tm.service.DenormalizedInfoListManagerService")
public class DenormalizedInfoListManagerServiceImpl implements DenormalizedInfoListManagerService{

	@Inject
	private DenormalizedInfoListItemDao itemDao;

	@Inject
	private DenormalizedInfoListDao denoListDao;

	@Inject
	private InfoListDao infoListDao;


	@Override
	public DenormalizedInfoListItem findOrDenormalize(DenormalizedListItemReference reference) {

		DenormalizedInfoListItem found = itemDao.findByReference(reference);

		if ( found == null ){
			found = createDenormalizedInfoList(reference);
		}

		return found;
	}


	/*
	 * TODO
	 * 
	 * Note about the new RuntimeException below :
	 * 
	 * When you think about it : is asking to create a new object that turns out to exist already an error ?
	 * At the time this code is written, yes it is the case, and for the reason stated in the exception.
	 * What about letting the context that invoked the method decide if this is actually an error ?
	 * 
	 * Don't think too much though, because it's never supposed to happen.
	 * 
	 */
	private DenormalizedInfoListItem createDenormalizedInfoList(DenormalizedListItemReference reference){

		DenormalizedInfoList existing = denoListDao.findByItemReference(reference);

		if (existing != null){
			// this case is odd : it means that the reference references a denormalized
			// list that exists, but within which the referenced item doesn't
			throw new RuntimeException("refused to create the denormalized list : it already exists. This operation was triggered by a DenormalizedListItemReference that could not " +
					"be resolved ");
		}

		InfoList origList = infoListDao.findById(reference.getOriginalListId());
		DenormalizedInfoList newList = new DenormalizedInfoList(origList.getId(), origList.getVersion());
		DenormalizedInfoListItem referencedItem = null;

		newList.setLabel(origList.getLabel());
		newList.setCode(origList.getCode());
		newList.setDescription(origList.getDescription());

		for (InfoListItem it : origList.getItems()){

			/*
			 * we choose here the DenormalizedUserListItem as the implementation
			 * because all instances of DenormalizedSystemListItem
			 * have already been created
			 */
			DenormalizedInfoListItem denoIt = new DenormalizedUserListItem();

			denoIt.setLabel(it.getLabel());
			denoIt.setCode(it.getCode());
			denoIt.setIconName(it.getIconName());
			denoIt.setIsDefault(it.isDefault());
			denoIt.setInfoList(newList);

			if (reference.references(denoIt)){
				referencedItem = denoIt;
			}

			newList.addItem(denoIt);
		}

		// persist
		denoListDao.persist(newList);

		// return
		return referencedItem;

	}

}
