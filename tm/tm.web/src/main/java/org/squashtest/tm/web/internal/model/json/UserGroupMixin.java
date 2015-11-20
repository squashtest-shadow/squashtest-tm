package org.squashtest.tm.web.internal.model.json;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

@JsonAutoDetect
public abstract class UserGroupMixin {

	@JsonIgnore
	public abstract String getSimpleName();
}
