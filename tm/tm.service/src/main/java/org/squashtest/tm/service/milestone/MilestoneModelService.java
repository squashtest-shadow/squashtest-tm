package org.squashtest.tm.service.milestone;

import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.service.internal.dto.json.JsonMilestone;

import java.util.List;
import java.util.Map;

/**
 * Service Class dedicated to find, populate and return {@link JsonMilestone}.
 */
public interface MilestoneModelService {

	/**
	 * Find all {@link JsonMilestone} corresponding to {@link Milestone} linked to projects designed by ids
	 * @param readableProjectIds the ids of projects.
	 * @return a {@link Map} containing the milestones. Key = Milestone Id. Value = JsonMilestone
	 */
	Map<Long, JsonMilestone> findUsedMilestones(List<Long> readableProjectIds);

	/**
	 * Find the {@link JsonMilestone} corresponding to {@link Milestone} designed by the given id.
	 * @param milestoneId The id of the {@link Milestone}
	 * @return The {@link JsonMilestone}
	 */
	JsonMilestone findMilestoneModel(Long milestoneId);


}
