package org.squashtest.it.stub.milestone;

import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder;

import com.google.common.base.Optional;

public class StubActiveMilestoneHolder implements ActiveMilestoneHolder {

	@Override
	public Optional<Milestone> getActiveMilestone() {
		// TODO Auto-generated method stub
		return Optional.absent();
	}

	@Override
	public void setActiveMilestone(Long milestoneId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearContext() {
		// TODO Auto-generated method stub

	}

}
