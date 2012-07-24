package org.squashtest.csp.tm.service;

public interface CampaignLibraryFinderService {

	/**
	 * Returns the path of a CampaignLibraryNode given its id. The format is standard, beginning with /&lt;project-name&gt;
	 * 
	 * @param entityId the id of the node.
	 * @return the path of that node.
	 */
	String getPathAsString(long entityId);
}
