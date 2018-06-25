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
package org.squashtest.tm.domain.dataset;

import org.squashtest.tm.domain.customreport.TreeEntityVisitor;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.tree.GenericTreeLibrary;
import org.squashtest.tm.domain.tree.TreeEntity;

import javax.persistence.*;

/**
 * @author aguilhem
 */
@Entity
public class DatasetLibrary extends GenericTreeLibrary {

	@Id
	@Column(name = "DL_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "dataset_library_dl_id_seq")
	@SequenceGenerator(name = "dataset_library_dl_id_seq", sequenceName = "dataset_library_dl_id_seq", allocationSize = 1)
	private Long id;

	@OneToOne(mappedBy = "datasetLibrary")
	private GenericProject project;

	@Override
	public String getName() {
		return project.getName();
	}

	@Override
	public void setName(String name) {

	}

	@Override
	public void accept(TreeEntityVisitor visitor) {

	}

	@Override
	public Project getProject() {
		return (Project) project;
	}

	@Override
	public void setProject(Project project) {

	}

	@Override
	public void notifyAssociatedWithProject(GenericProject p) {

	}

	@Override
	public TreeEntity createCopy() {
		return null;
	}

	@Override
	public Long getId() {
		return id;
	}
}
