package org.squashtest.tm.web.internal.model.json;

import java.util.List;

import org.squashtest.tm.domain.chart.AxisColumn;
import org.squashtest.tm.domain.chart.Filter;
import org.squashtest.tm.domain.chart.MeasureColumn;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonAutoDetect
public abstract class ChartQueryMixin {

	@JsonDeserialize(contentAs = Filter.class)
	private List<Filter> filters;

	@JsonDeserialize(contentAs = AxisColumn.class)
	private List<AxisColumn> axis;

	@JsonDeserialize(contentAs = MeasureColumn.class)
	private List<MeasureColumn> measures;
}
