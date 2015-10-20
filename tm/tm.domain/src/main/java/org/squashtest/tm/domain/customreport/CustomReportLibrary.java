/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.domain.customreport;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;

import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.tree.GenericTreeLibrary;
import org.squashtest.tm.domain.tree.TreeLibraryNode;

@Entity
public class CustomReportLibrary extends GenericTreeLibrary {
	
	private static final String CLASS_NAME = "org.squashtest.tm.domain.customreport.CustomReportLibrary";
	private static final String SIMPLE_CLASS_NAME = "CustomReportLibrary";

	@Id
	@Column(name="CRL_ID")
	@GeneratedValue(strategy=GenerationType.AUTO, generator="custom_report_library_crl_id_seq")
	@SequenceGenerator(name="custom_report_library_crl_id_seq", sequenceName="custom_report_library_crl_id_seq")
	private Long id;
	
	@OneToOne(mappedBy = "customReportLibrary")
	private Project project;

	@Override
	public void notifyAssociatedWithProject(GenericProject p) {
		throw new UnsupportedOperationException("NO IMPLEMENTATION... YET...");
	}

	@Override
	public Project getProject() {
		return project;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public String getName() {
		return project.getName();
	}

	@Override
	public void setName(String name) {
		throw new UnsupportedOperationException("A library cannot be renammed, please rename the project instead");
	}

	@Override
	public TreeLibraryNode getTreeNode() {
		throw new UnsupportedOperationException("NO IMPLEMENTATION... YET...");
	}

	@Override
	public void accept(TreeEntityVisitor visitor) {
		visitor.visit(this);
	}
	

}
