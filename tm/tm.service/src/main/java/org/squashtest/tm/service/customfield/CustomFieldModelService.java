package org.squashtest.tm.service.customfield;

import org.squashtest.tm.service.internal.dto.CustomFieldBindingModel;

import java.util.List;
import java.util.Map;

public interface CustomFieldModelService {

	/**
	 * Find all cuf bindings as {@link CustomFieldBindingModel}, hydrated with their cuf as {@link org.squashtest.tm.service.internal.dto.CustomFieldModel} for a list of projects designed by their ids.
	 * Method is not secured, you must provide projectIds checked previously for ACLs
	 * @param projectIds The readables {@link org.squashtest.tm.domain.project.Project} ids.
	 * @return a map with {@link CustomFieldBindingModel} grouped by {@link org.squashtest.tm.domain.project.Project} id and {@link org.squashtest.tm.domain.customfield.BindableEntity}
	 * Example if i call this method for {@link org.squashtest.tm.domain.project.Project} 1 and 32
	 * PROJECT_ID : 1 The key for first project : 1
	 * 		TEST-CASE : {@link List} of {@link CustomFieldBindingModel} bound to {@link org.squashtest.tm.domain.testcase.TestCase} for the project designed by id 1.
	 * 		REQUIREMENT : {@link List} of {@link CustomFieldBindingModel} bound to {@link org.squashtest.tm.domain.requirement.Requirement} for the project designed by id 1.
	 * 	... (All other {@link org.squashtest.tm.domain.customfield.BindableEntity})
	 *
	 * PROJECT_ID : 32 The key for second project : 32
	 * ... all {@link CustomFieldBindingModel} grouped by {@link org.squashtest.tm.domain.customfield.BindableEntity} for {@link org.squashtest.tm.domain.project.Project} 32
	 */
	Map<Long, Map<String, List<CustomFieldBindingModel>>> findCustomFieldsBindingsByProject(List<Long> projectIds);
}
