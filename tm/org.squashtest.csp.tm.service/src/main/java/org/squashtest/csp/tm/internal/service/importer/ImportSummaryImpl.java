package org.squashtest.csp.tm.internal.service.importer;

import org.squashtest.csp.tm.service.importer.ImportSummary;

public class ImportSummaryImpl implements ImportSummary {

	private int totalTestCases=0;
	private int success=0;
	private int warnings=0;
	private int failures=0;
	
	public ImportSummaryImpl(){
		
	}

	public void incrTotal(){
		totalTestCases++;
	}
	
	public void incrSuccess(){
		success++;
	}
	
	public void incrWarnings(){
		warnings++;
	}
	
	public void incrFailures(){
		failures++;
	}
	
	@Override
	public int getTotalTestCases() {
		return totalTestCases;
	}

	@Override
	public int getSuccess() {
		return success;
	}

	@Override
	public int getWarnings() {
		return warnings;
	}

	@Override
	public int getFailures() {
		return failures;
	}

	

}
