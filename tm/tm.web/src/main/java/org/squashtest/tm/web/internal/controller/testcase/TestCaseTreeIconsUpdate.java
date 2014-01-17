/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.testcase;

import org.squashtest.tm.domain.testcase.TestCaseImportance;

public class TestCaseTreeIconsUpdate {
	private long id;
	private String req = "same";
	private String importance = "same";

	public TestCaseTreeIconsUpdate(long id, boolean req, TestCaseImportance importance) {
		super();
		this.id = id;
		doSetReq(req);
		doSetImportance(importance);
	}
	public TestCaseTreeIconsUpdate(long id, TestCaseImportance importance) {
		super();
		this.id = id;
		doSetImportance(importance);
	}
	
	public TestCaseTreeIconsUpdate(long id ,boolean req) {
		super();
		this.id = id;
		doSetReq(req);
	}
	public long getId() {
		return id;
	}
	public String getReq() {
		return req;
	}
	private void doSetReq(boolean req) {
		if(req){
			this.req = "ok";
		}else{
			this.req = "ko";
		}
	}
	public String getImportance() {
		return importance;
	}
	private void doSetImportance(TestCaseImportance importance) {
		this.importance = importance.toString().toLowerCase();
	}
	
	
}

