/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.domain.requirement;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.validation.constraints.NotNull;

import org.squashtest.csp.tm.domain.attachment.Attachable;
import org.squashtest.csp.tm.domain.attachment.Attachment;
import org.squashtest.csp.tm.domain.attachment.AttachmentList;
import org.squashtest.csp.tm.domain.testcase.TestCase;

/**
 * Entity requirement
 *
 *
 *
 * @author bsiri
 *
 */

@Entity
@PrimaryKeyJoinColumn(name = "RLN_ID")
public class Requirement extends RequirementLibraryNode implements Attachable {
	/**
	 * Collection of {@link Test Cases} verifying by this {@link Requirement}
	 */
	@ManyToMany(mappedBy = "verifiedRequirements", cascade = {CascadeType.ALL})
	private final Set<TestCase> verifyingTestCases = new HashSet<TestCase>();

	/***
	 * The requirement reference
	 */
	@Basic(optional = true)
	private String reference;

	@Enumerated(EnumType.STRING)
	private RequirementCriticality criticality = RequirementCriticality.UNDEFINED;

	@Enumerated(EnumType.STRING)
	@Column(name = "REQUIREMENT_STATUS")
	private RequirementStatus status = RequirementStatus.WORK_IN_PROGRESS;
	
	@OneToOne(cascade = { CascadeType.ALL }, orphanRemoval=true)
	@JoinColumn(name = "ATTACHMENT_LIST_ID")
	private final AttachmentList attachmentCollection = new AttachmentList();

	public Requirement() {
		super();
	}

	public Requirement(String name, String description) {
		super(name, description);
	}

	@Override
	public void accept(RequirementLibraryNodeVisitor visitor) {
		visitor.visit(this);

	}

	public Set<TestCase> getVerifyingTestCase() {
		return verifyingTestCases;
	}

	public void addVerifyingTestCase(@NotNull TestCase testcase) {
		getVerifyingTestCase().add(testcase);
		testcase.getVerifiedRequirements().add(this);
	}

	public void removeVerifyingTestCase(@NotNull TestCase testcase) {
		getVerifyingTestCase().remove(testcase);
		testcase.removeVerifiedRequirement(this);
	}

	@Override
	public Long getAttachmentCollectionId() {
		return attachmentCollection.getId();
	}

	@Override
	public AttachmentList getAttachmentCollection() {
		return attachmentCollection;
	}

	@Override
	public boolean hasAttachments(){
		return (getNbAttachments()>0);
	}

	@Override
	public int getNbAttachments() {

		return getAttachmentCollection().size();
	}


	/***
	 * @return the reference of the requirement
	 */
	public String getReference() {
		return reference;
	}

	/***
	 * Set the requirement reference
	 *
	 * @param reference
	 */
	public void setReference(String reference) {
		this.reference = reference;
	}

	@Override
	public Requirement createCopy() {
		Requirement clone = new Requirement();

		clone.setName(this.getName());
		clone.setDescription(this.getDescription());
		clone.setReference(this.getReference());
		clone.setCriticality(this.getCriticality());

		for (TestCase testCase : this.verifyingTestCases) {
			clone.addVerifyingTestCase(testCase);
		}

		for (Attachment tcAttach : this.getAttachmentCollection().getAllAttachments()) {
			Attachment atCopy = tcAttach.hardCopy();
			clone.getAttachmentCollection().addAttachment(atCopy);
		}		
		
		clone.notifyAssociatedWithProject(this.getProject());
		return clone;
	}

	/***
	 * @return the requirement criticality
	 */
	public RequirementCriticality getCriticality() {
		return criticality;
	}

	/***
	 * Set the requirement criticality
	 * @param criticality
	 */
	public void setCriticality(RequirementCriticality criticality) {
		this.criticality = criticality;
	}
	
	public void setStatus(RequirementStatus status){
		this.status=status;
	}
	
	public RequirementStatus getStatus(){
		return status;
	}

}
