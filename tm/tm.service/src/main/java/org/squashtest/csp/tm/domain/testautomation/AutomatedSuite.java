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
package org.squashtest.csp.tm.domain.testautomation;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

import org.hibernate.annotations.GenericGenerator;
import org.squashtest.csp.tm.domain.execution.ExecutionStatus;

@NamedQueries({
	@NamedQuery(name="automatedSuite.findAll", query="from AutomatedSuite"),
	@NamedQuery(name="automatedSuite.findById", query="from AutomatedSuite where id = :suiteId"),
	@NamedQuery(name="automatedSuite.findAllById", query="from AutomatedSuite where id in (:suiteIds)"),
	@NamedQuery(name="automatedSuite.findAllExtenders", query="select ext from AutomatedExecutionExtender ext join ext.automatedSuite s where s.id = :suiteId"),
	@NamedQuery(name="automatedSuite.findAllExtendersHavingStatus", query="select ext from AutomatedExecutionExtender ext join ext.execution exe join ext.automatedSuite s where s.id = :suiteId and exe.executionStatus in (:statusList)")
	})
@Entity
public class AutomatedSuite  {

	@Id
	@Column(name = "SUITE_ID")
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name="system-uuid", strategy = "uuid")
	private String id;

	@OneToMany(mappedBy="automatedSuite", cascade = {CascadeType.ALL})
	public Collection<AutomatedExecutionExtender> executionExtenders = new ArrayList<AutomatedExecutionExtender>();

	public String getId(){
		return id;
	}


	public Collection<AutomatedExecutionExtender> getExecutionExtenders() {
		return executionExtenders;
	}


	public void setExecutionExtenders(
			Collection<AutomatedExecutionExtender> executionExtenders) {
		this.executionExtenders = executionExtenders;
	}
	
	public void addExtender(AutomatedExecutionExtender extender){
		executionExtenders.add(extender);
		extender.setAutomatedSuite(this);
	}
	
	public void addExtenders(Collection<AutomatedExecutionExtender> extenders){
		for (AutomatedExecutionExtender extender : extenders){
			executionExtenders.add(extender);
		}
	}
	
	public boolean hasStarted(){
		for (AutomatedExecutionExtender extender : executionExtenders){
			if (extender.getExecution().getExecutionStatus() != ExecutionStatus.READY){
				return true; 
			}
		}
		return false;
	}
	
	public boolean hasEnded(){
		for (AutomatedExecutionExtender extender : executionExtenders){
			if (! extender.getExecution().getExecutionStatus().isTerminatedStatus()){
				return false; 
			}
		}
		return true;
	}
	
}
