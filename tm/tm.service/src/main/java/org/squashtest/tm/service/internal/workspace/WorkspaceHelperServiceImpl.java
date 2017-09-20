package org.squashtest.tm.service.internal.workspace;

import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.dto.FilterModel;
import org.squashtest.tm.dto.UserDto;
import org.squashtest.tm.service.project.CustomProjectModificationService;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.service.user.UserAccountService;
import org.squashtest.tm.service.workspace.WorkspaceHelperService;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.squashtest.tm.domain.project.Project.PROJECT_TYPE;
import static org.squashtest.tm.jooq.domain.Tables.PROJECT;
import static org.squashtest.tm.jooq.domain.Tables.PROJECT_FILTER;
import static org.squashtest.tm.jooq.domain.Tables.PROJECT_FILTER_ENTRY;

@Component
@Transactional(readOnly = true)
public class WorkspaceHelperServiceImpl implements WorkspaceHelperService {

	@Inject
	DSLContext DSL;

	@Inject
	private CustomProjectModificationService projectService;

	@Inject
	private UserAccountService userAccountService;

	@Inject
	protected ProjectFinder projectFinder;

	@Override
	public FilterModel findFilterModel() {
		UserDto currentUser = userAccountService.findCurrentUserDto();
		List<Long> projectIds = projectFinder.findAllReadableIds(currentUser);
		return doFindFilterModel(currentUser, projectIds);
	}

	protected FilterModel doFindFilterModel(UserDto currentUser, List<Long> projectIds) {
		Map<FilterModel, List<Long>> filterModels = DSL.select(PROJECT_FILTER.PROJECT_FILTER_ID, PROJECT_FILTER.ACTIVATED
			, PROJECT_FILTER_ENTRY.PROJECT_ID)
			.from(PROJECT_FILTER)
			.join(PROJECT_FILTER_ENTRY).on(PROJECT_FILTER.PROJECT_FILTER_ID.eq(PROJECT_FILTER_ENTRY.FILTER_ID))
			.where(PROJECT_FILTER.USER_LOGIN.eq(currentUser.getUsername()))
			.fetch()
			.stream()
			.collect(groupingBy((r) -> {
				FilterModel filterModel = new FilterModel();
				filterModel.setId(r.get(PROJECT_FILTER.PROJECT_FILTER_ID));
				filterModel.setEnabled(r.get(PROJECT_FILTER.ACTIVATED));
				return filterModel;
			}, mapping((r) -> r.get(PROJECT_FILTER_ENTRY.PROJECT_ID), toList())));

		//for now, an user can only have one filter so we can get the first or default model if the user have no filter
		FilterModel filterModel;
		List<Long> selectedProjectIds;
		if (filterModels.size() == 0) {
			filterModel = new FilterModel();
			filterModel.setEnabled(false);
			selectedProjectIds = projectIds;
		} else {
			filterModel = filterModels.keySet().iterator().next();
			selectedProjectIds = filterModels.get(filterModel);
		}

		//fetch the necessary data for all readable projects
		DSL.select(PROJECT.PROJECT_ID, PROJECT.PROJECT_TYPE, PROJECT.NAME, PROJECT.LABEL)
			.from(PROJECT)
			.where(PROJECT.PROJECT_ID.in(projectIds)).and(PROJECT.PROJECT_TYPE.eq(PROJECT_TYPE))
			.fetch()
			.forEach(r -> {
				boolean selected = selectedProjectIds.contains(r.get(PROJECT.PROJECT_ID));
				filterModel.addProject(r.get(PROJECT.PROJECT_ID), r.get(PROJECT.NAME), selected, r.get(PROJECT.LABEL));
			});
		return filterModel;
	}

}
