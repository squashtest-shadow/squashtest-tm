package org.squashtest.tm.web.internal.controller.users;

import javax.validation.constraints.NotNull;

/**
 * Created by jthebault on 11/10/2016.
 */
public class PartyPreferenceModel {

	@NotNull
	private String key;

	@NotNull
	private String value;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
