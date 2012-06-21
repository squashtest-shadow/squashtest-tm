package org.squashtest.csp.tm.domain;

public class IssueAlreadyBoundException extends DomainException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String key = "issue.exception.report.alreadybound";
	
	
	public IssueAlreadyBoundException(){
		super();
		setField("bugtracker");
	}
	
	@Override
	public String getI18nKey() {
		return key;
	}

}
