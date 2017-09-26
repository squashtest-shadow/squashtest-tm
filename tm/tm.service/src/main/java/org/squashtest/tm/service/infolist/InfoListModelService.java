package org.squashtest.tm.service.infolist;


import org.squashtest.tm.service.internal.dto.json.JsonInfoList;

import java.util.List;
import java.util.Map;

public interface InfoListModelService {
	Map<Long, JsonInfoList> findUsedInfoList(List<Long> projectIds);

	Map<String, String> findSystemInfoListItemLabels();
}
