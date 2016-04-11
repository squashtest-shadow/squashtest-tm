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
package org.squashtest.tm.core.foundation.collection;

import java.util.List;

import org.springframework.data.domain.Page;

public class PageCollectionHolderWrapper<ENTITY> implements PagedCollectionHolder<List<ENTITY>> {

	private Page<ENTITY> page;
	
	public PageCollectionHolderWrapper(Page<ENTITY> page){
		this.page = page;
	}
	
	@Override
	public long getFirstItemIndex() {
		return page.getNumber()*page.getSize();
	}

	@Override
	public long getTotalNumberOfItems() {
		return page.getTotalElements();
	}

	@Override
	public List<ENTITY> getPagedItems() {
		return page.getContent();
	}

}
