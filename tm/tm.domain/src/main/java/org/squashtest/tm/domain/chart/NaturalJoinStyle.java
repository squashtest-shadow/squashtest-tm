package org.squashtest.tm.domain.chart;

/**
 * Created by jthebault on 29/11/2016.
 */
public enum NaturalJoinStyle {
	/*
 * Use inner joins when a natural join is possible
 */
	INNER_JOIN,
	/*
     * Use left outer join when natural join is possible
     */
	LEFT_JOIN;
}
