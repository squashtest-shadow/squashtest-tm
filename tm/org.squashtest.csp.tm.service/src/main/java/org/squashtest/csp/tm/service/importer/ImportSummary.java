package org.squashtest.csp.tm.service.importer;

public interface ImportSummary {
	int getTotalTestCases();
	int getSuccess();
	int getWarnings();
	int getFailures();
}
