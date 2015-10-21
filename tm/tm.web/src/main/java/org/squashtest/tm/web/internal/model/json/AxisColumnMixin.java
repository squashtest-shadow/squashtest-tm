package org.squashtest.tm.web.internal.model.json;

import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.chart.DataType;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

@JsonAutoDetect
public abstract class AxisColumnMixin {

	@JsonIgnore
	public abstract EntityType getEntityType();

	@JsonIgnore
	public abstract DataType getDataType();

}
