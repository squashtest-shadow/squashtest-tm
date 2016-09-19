package org.squashtest.tm.domain.chart;

/**
 * Created by jthebault on 19/09/2016.
 */
public enum ScopeType {
	// @formatter:off
	DEFAULT,//The perimeter will be the current project of the chart if user look just the chart or the dashboard's project id the chart is looked into a dashboard
	PROJECTS,//The perimeter will be a fix selection of project
	CUSTOM;//the perimeter is a custom selection of entities.
	// All joins on other entities will be performed on all database.
	//So the final perimeter is selected entities + all entities linked to them
	// @formatter:on
}
