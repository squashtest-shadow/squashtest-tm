package org.squashtest.tm.web.internal.controller.testcase.parameters;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelHelper;
/**
 * Helps create the datas (for the jQuery DataTable) for the parameters table in the test case view.
 * @author mpagnon
 *
 */
public final class ParametersDataTableModelHelper extends DataTableModelHelper<Parameter> {

	private long ownerId;
	private MessageSource messageSource;
	private Locale locale;

	public ParametersDataTableModelHelper(long ownerId, MessageSource messageSource, Locale locale) {
		super();
		
		this.ownerId = ownerId;
		this.messageSource = messageSource;
		this.locale = locale;
	}

	@Override
	public Map<String, Object> buildItemData(Parameter item) {
		Map<String, Object> res = new HashMap<String, Object>();
		String testCaseName = buildTestCaseName(item);
		res.put(DataTableModelHelper.DEFAULT_ENTITY_ID_KEY, item.getId());
		res.put(DataTableModelHelper.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex());
		res.put(DataTableModelHelper.NAME_KEY, ParametersDataTableModelHelper.buildParameterName(item, ownerId, messageSource, locale));
		res.put("description", item.getDescription());
		res.put("test-case-name", testCaseName);
		res.put(DataTableModelHelper.DEFAULT_EMPTY_DELETE_HOLDER_KEY, "");
		return res;
	}
	
	public static String buildParameterName(Parameter item, Long ownerId2, MessageSource messageSource2, Locale locale2){
		String tcSmall = messageSource2.getMessage("label.testCases.short", null, locale2);
		TestCase paramTC = item.getTestCase();
		if(!ownerId2.equals(paramTC.getId())){
			return item.getName() + " ("+tcSmall+"_"+paramTC.getId()+")";
		}
		else{
			return item.getName();
		}
	}
	
	/**
	 * Will build the test case name for display in the table.
	 * The name will be : tReference-tcName (tcProjectName)
	 * @param item
	 * @return
	 */
	public static String buildTestCaseName(Parameter item) {
		TestCase testCase = item.getTestCase();
		Project project = testCase.getProject();
		String testCaseName = testCase.getName() + " (" + project.getName() + ')';
		if (testCase.getReference().length() > 0) {
			testCaseName = testCase.getReference() + '-' + testCaseName;
		}
		return testCaseName;
	}

}