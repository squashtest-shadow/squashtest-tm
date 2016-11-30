package org.squashtest.tm.domain.chart;

/**
 * Created by jthebault on 29/11/2016.
 */
public enum QueryStrategy {
	/*
     * This query is a main query : it is the main entry point of a chart definition
     */
	MAIN,
	/*
     * This query corresponds to a "calculated" column prototype and will be added as a subquery
     */
	SUBQUERY,
	/*
     * This query can be inlined in the main query
     */
	INLINED;
}
