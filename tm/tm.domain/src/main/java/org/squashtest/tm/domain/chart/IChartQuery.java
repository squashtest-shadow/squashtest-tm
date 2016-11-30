package org.squashtest.tm.domain.chart;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by jthebault on 29/11/2016.
 */
public interface IChartQuery {

	List<Filter> getFilters();

	List<AxisColumn> getAxis();

	List<MeasureColumn> getMeasures();

	QueryStrategy getStrategy();

	NaturalJoinStyle getJoinStyle();

	Map<ColumnRole, Set<SpecializedEntityType>> getInvolvedEntities();
}
