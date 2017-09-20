package org.squashtest.tm.web.internal.model.rest;

public class RestLibrary {
	private long id;
	private RestProject project;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public RestProject getProject() {
		return project;
	}

	public void setProject(RestProject project) {
		this.project = project;
	}
}
