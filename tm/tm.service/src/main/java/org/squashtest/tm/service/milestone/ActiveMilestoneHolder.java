package org.squashtest.tm.service.milestone;

import org.squashtest.tm.domain.milestone.Milestone;

import com.google.common.base.Optional;

public interface ActiveMilestoneHolder {
	Optional<Milestone> getActiveMilestone();

	void setActiveMilestone(Long milestoneId);

	void clearContext();

}
