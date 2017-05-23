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
package org.squashtest.tm.domain.requirement;

import com.sun.javafx.beans.IDProperty;

import javax.persistence.*;

/**
 * Created by jlor on 09/05/2017.
 */

@Entity
public class RequirementVersionLink {

	@Id
	@Column(name = "REQUIREMENT_VERSION_LINK_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "requirement_version_link_link_id_seq")
	@SequenceGenerator(name = "requirement_version_link_link_id_seq", sequenceName = "requirement_version_link_link_id_seq", allocationSize = 1)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "REQUIREMENT_VERSION_1_ID", referencedColumnName = "RES_ID")
	private RequirementVersion requirementVersion1;

	@ManyToOne
	@JoinColumn(name = "REQUIREMENT_VERSION_2_ID", referencedColumnName = "RES_ID")
	private RequirementVersion requirementVersion2;

	@ManyToOne
	@JoinColumn(name="LINK_TYPE_ID", referencedColumnName = "REQUIREMENT_VERSION_LINK_TYPE_ID")
	private RequirementVersionLinkType linkType;


	public Long getId() {
		return id;
	}

	public RequirementVersionLink() {
	}

	public RequirementVersionLink(
		RequirementVersion requirementVersion1,
		RequirementVersion requirementVersion2) {

		this.requirementVersion1 = requirementVersion1;
		this.requirementVersion2 = requirementVersion2;
	}

	public RequirementVersion getRequirementVersion1() {
		return requirementVersion1;
	}

	public RequirementVersion getRequirementVersion2() {
		return requirementVersion2;
	}

	public void setRequirementVersion1(RequirementVersion requirementVersion1) {
		this.requirementVersion1 = requirementVersion1;
	}

	public void setRequirementVersion2(RequirementVersion requirementVersion2) {
		this.requirementVersion2 = requirementVersion2;
	}

	public RequirementVersionLinkType getLinkType() {
		return linkType;
	}

	public void setLinkType(RequirementVersionLinkType linkType) {
		this.linkType = linkType;
	}
}
