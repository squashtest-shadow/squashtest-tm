package org.squashtest.tm.domain.chart;

import static org.squashtest.tm.domain.chart.Operation.AVG;
import static org.squashtest.tm.domain.chart.Operation.BETWEEN;
import static org.squashtest.tm.domain.chart.Operation.BY_DAY;
import static org.squashtest.tm.domain.chart.Operation.BY_HOUR;
import static org.squashtest.tm.domain.chart.Operation.BY_MONTH;
import static org.squashtest.tm.domain.chart.Operation.BY_YEAR;
import static org.squashtest.tm.domain.chart.Operation.COUNT;
import static org.squashtest.tm.domain.chart.Operation.EQUALS;
import static org.squashtest.tm.domain.chart.Operation.GREATER;
import static org.squashtest.tm.domain.chart.Operation.GREATER_EQUALS;
import static org.squashtest.tm.domain.chart.Operation.LIKE;
import static org.squashtest.tm.domain.chart.Operation.LOWER;
import static org.squashtest.tm.domain.chart.Operation.LOWER_EQUAL;
import static org.squashtest.tm.domain.chart.Operation.MAX;
import static org.squashtest.tm.domain.chart.Operation.MIN;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

public enum DataType {

	// @formatter:off
	NUMERIC (AVG, BETWEEN, COUNT, EQUALS, GREATER, GREATER_EQUALS, LOWER, LOWER_EQUAL, MAX, MIN),
	STRING (EQUALS, LIKE, COUNT),
	DATE (AVG, BETWEEN, COUNT, EQUALS, GREATER, GREATER_EQUALS, LOWER, LOWER_EQUAL, MAX, MIN, BY_DAY, BY_HOUR, BY_MONTH, BY_YEAR),
	EXECUTION_STATUS (EQUALS),
	INFO_LIST_ITEM (EQUALS);
	// @formatter:on

	private EnumSet<Operation> operations;

	private DataType(Operation... operations) {
		this.operations = EnumSet.copyOf(Arrays.asList(operations));
	}

	@SuppressWarnings("unchecked")
	public Set<Operation> getOperations() {
		return (Set<Operation>) CollectionUtils.unmodifiableCollection(operations);
	}

}
