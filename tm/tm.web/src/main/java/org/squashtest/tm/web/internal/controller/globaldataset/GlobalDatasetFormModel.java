/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.globaldataset;

import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.tm.domain.dataset.GlobalDataset;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author aguilhem
 */
public class GlobalDatasetFormModel {

	@NotBlank
	@NotNull
	@Size(max = GlobalDataset.MAX_NAME_SIZE)
	private String name;

	@Size(max = GlobalDataset.MAX_REF_SIZE)
	private String reference;

	private String description;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public GlobalDataset getGlobalDataset(){
		GlobalDataset newGlobalDataset = new GlobalDataset();
		newGlobalDataset.setName(name);
		newGlobalDataset.setReference(reference);
		newGlobalDataset.setDescription(description);
		return newGlobalDataset;
	}
}
