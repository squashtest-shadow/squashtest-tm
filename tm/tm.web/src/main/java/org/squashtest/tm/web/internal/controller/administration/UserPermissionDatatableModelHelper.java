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
package org.squashtest.tm.web.internal.controller.administration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.squashtest.tm.domain.users.UserProjectPermissionsBean;
import org.squashtest.tm.service.security.acls.PermissionGroup;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelHelper;

public class UserPermissionDatatableModelHelper extends DataTableModelHelper<UserProjectPermissionsBean>{

	public List<Map<?,?>> buildAllData(List<UserProjectPermissionsBean> source){
		List<Map<?,?>> result = new ArrayList<Map<?,?>>(source.size());
		for (UserProjectPermissionsBean item : source){
			incrementIndex();
			Map<?,?> itemData = buildItemData(item);
			result.add(itemData);
		}
		return result;
	}
	
	
	@Override
	protected Map<?,?> buildItemData(UserProjectPermissionsBean item) {
		
		Map<Object, Object> result = new HashMap<Object, Object>();
		UserModel userModel = new UserModel(item.getUser());
		PermissionGroup group = item.getPermissionGroup();
		
		result.put("user", userModel);
		result.put("user-index", getCurrentIndex());
		result.put("permission-group", group);
		result.put("empty-delete-holder", null);
		
		return result;
		
	}


	
}
