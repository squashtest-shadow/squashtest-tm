package org.squashtest.csp.tm.internal.repository.hibernate;

import org.hibernate.Query;
import org.squashtest.csp.tm.domain.library.LibraryNode;
import org.squashtest.csp.tm.domain.requirement.RequirementLibraryNode;

/**
 * @author Gregory
 *
 */
class SetNodeContentParameter implements SetQueryParametersCallback {
	/**
	 * 
	 */
	private final LibraryNode node;

	/**
	 * @param node
	 */
	public SetNodeContentParameter(LibraryNode node) {
		this.node = node;
	}

	@Override
	public void setQueryParameters(Query query) {
		query.setParameter("content", node);
	}
}