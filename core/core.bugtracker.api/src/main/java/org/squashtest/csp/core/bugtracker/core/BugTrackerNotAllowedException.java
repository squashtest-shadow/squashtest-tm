package org.squashtest.csp.core.bugtracker.core;

public class BugTrackerNotAllowedException extends BugTrackerRemoteException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BugTrackerNotAllowedException(String message, Throwable cause) {
		super(message, cause);
	}

	public BugTrackerNotAllowedException(Throwable cause) {
		super(cause);
	}

}
