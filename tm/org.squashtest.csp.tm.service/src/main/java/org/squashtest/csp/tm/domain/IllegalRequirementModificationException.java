package org.squashtest.csp.tm.domain;

public class IllegalRequirementModificationException extends ActionException {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 4901610054565947807L;
	private final String illegalReqModificationException = "squashtm.action.exception.illegalrequirementmodification.label";
	
	
	public IllegalRequirementModificationException(Exception ex){
		super(ex);
	}
	
	public IllegalRequirementModificationException(String message){
		super(message);
	}
	
	public IllegalRequirementModificationException(){
		
	}
	
	@Override
	public String getI18nKey() {
		return illegalReqModificationException;
	}
}
