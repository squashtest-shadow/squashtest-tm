package org.squashtest.csp.tm.internal.repository.hibernate;

import org.hibernate.Query;

/**
 * @author Gregory
 *
 */
class SetFolderIdParameter implements SetQueryParametersCallback {
	/**
	 * 
	 */
	private final long folderId;

	/**
	 * @param folderId
	 */
	public SetFolderIdParameter(long folderId) {
		this.folderId = folderId;
	}

	@Override
	public void setQueryParameters(Query query) {
		query.setLong("folderId", folderId);
	}
}