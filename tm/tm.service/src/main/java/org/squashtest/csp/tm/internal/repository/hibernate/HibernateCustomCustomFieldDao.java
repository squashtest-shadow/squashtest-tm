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
package org.squashtest.csp.tm.internal.repository.hibernate;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.squashtest.csp.tm.domain.customfield.CustomField;
import org.squashtest.csp.tm.domain.customfield.SingleSelectField;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.internal.repository.CustomCustomFieldDao;

@Repository("CustomCustomFieldDao")
public class HibernateCustomCustomFieldDao extends HibernateEntityDao<CustomField> implements CustomCustomFieldDao {

	@Override
	public List<CustomField> finAllOrderedByName() {
		return executeListNamedQuery("customField.findAllOrderedByName");
	}

	@Override
	public List<CustomField> findSortedCustomFields(CollectionSorting filter) {
		return findSorted(filter, CustomField.class, "CustomField");
	}

	@Override
	public long countCustomFields() {
		return executeEntityNamedQuery("customField.count");
	}

	@Override
	public CustomField findByName(String name) {
		return executeEntityNamedQuery("customField.findByName", "name", name );
	}

	@Override
	public SingleSelectField findSingleSelectFieldById(Long customFieldId) {
		return executeEntityNamedQuery("singleSelectField.findById", "id", customFieldId );
	}

	

}
