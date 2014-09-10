/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.web.internal.model.customfield;

import java.util.LinkedList;
import java.util.List;

public class MultiSelectFieldModel extends CustomFieldModel{

	private List<CustomFieldOptionModel> options = new LinkedList<CustomFieldOptionModel>();

	public List<CustomFieldOptionModel> getOptions(){
		return options;
	}

	public void setOptions(List<CustomFieldOptionModel> options) {
		this.options = options;
	}

	public void addOption(CustomFieldOptionModel option){
		options.add(option);
	}


}
