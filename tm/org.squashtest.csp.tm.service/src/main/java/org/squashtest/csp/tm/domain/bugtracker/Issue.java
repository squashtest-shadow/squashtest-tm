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
package org.squashtest.csp.tm.domain.bugtracker;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.hibernate.annotations.ForeignKey;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;

@Entity
public class Issue {
	@Id
	@GeneratedValue
	@Column(name = "ISSUE_ID")
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "ISSUE_LIST_ID")
	private IssueList issueList;
	
	@OneToOne(optional = false)
	@ForeignKey(name="FK_Issue_Bugtracker")
	@JoinColumn(name="BUGTRACKER_ID")
	private BugTracker bugtracker;
	
	
	private String remoteIssueId;

	public Long getId() {
		return id;
	}

	public String getRemoteIssueId() {
		return remoteIssueId;
	}

	public void setRemoteIssueId(String btId) {
		this.remoteIssueId = btId;
	}

	public IssueList getIssueList(){
		return issueList;
	}
	
	void setIssueList(IssueList issueList){
		this.issueList = issueList;
	}

	public BugTracker getBugtracker() {
		return bugtracker;
	}

	public void setBugtracker(BugTracker bugtracker) {
		this.bugtracker = bugtracker;
	}
	
	
	
	
	
}
