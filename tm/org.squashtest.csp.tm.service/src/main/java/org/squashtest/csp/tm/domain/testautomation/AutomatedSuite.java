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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@NamedQueries({
	@NamedQuery(name="automatedSuite.findById", query="from AutomatedSuite where id = :suiteId"),
	/*@NamedQuery(name="automatedSuite.findAllExecutions", query="select e from AutomatedExecution e join e.automatedSuite s where s.id = :suiteId"),
	@NamedQuery(name="automatedSuite.findAllExecutionWhereStatusIs", query="select e from AutomatedExecution e join e.automatedSuite s where s.id = :suiteId and e.executionStatus in (:statusList)")
*/})
@Entity
public class AutomatedSuite  {

	@Id
	@GeneratedValue
	@Column(name = "SUITE_ID")
	private Long id;

	
	public Long getId(){
		return id;
	}
}
