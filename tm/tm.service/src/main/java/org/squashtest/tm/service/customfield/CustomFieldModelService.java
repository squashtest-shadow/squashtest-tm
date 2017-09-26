package org.squashtest.tm.service.customfield;

import org.squashtest.tm.service.internal.dto.CustomFieldModel;

import java.util.List;
import java.util.Map;

public interface CustomFieldModelService {

	Map<Long, CustomFieldModel> findUsedCustomFields(List<Long> projectIds);

}
