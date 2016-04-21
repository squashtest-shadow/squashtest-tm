package org.squashtest.tm.service.internal.milestone;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder;
import org.squashtest.tm.service.milestone.MilestoneFinderService;

import com.google.common.base.Optional;

@Component
public class ActiveMilestoneHolderImpl implements ActiveMilestoneHolder {

	@Inject
	private MilestoneFinderService milestoneFinderService;

	private final ThreadLocal<Optional<Milestone>> activeMilestoneHolder = new ThreadLocal<>();

	private final ThreadLocal<Long> activeMilestoneIdHolder = new ThreadLocal<>();

	public Optional<Milestone> getActiveMilestone() {

		if (activeMilestoneHolder.get() == null) {
			List<Milestone> visibles = milestoneFinderService.findAllVisibleToCurrentUser();
			final Long milestoneId = activeMilestoneIdHolder.get();
			Milestone milestone = (Milestone) CollectionUtils.find(visibles, new Predicate() {
				@Override
				public boolean evaluate(Object milestone) {
					return ((Milestone) milestone).getId().equals(milestoneId);
				}
			});
			activeMilestoneHolder.set(Optional.fromNullable(milestone));
		}



		return activeMilestoneHolder.get();
	}


	public void setActiveMilestone(final Long milestoneId) {
		// just set the id. They milestone will be fetched from database only when asked
		activeMilestoneIdHolder.set(milestoneId);
	}

	public void clearContext() {
		activeMilestoneHolder.remove();
		activeMilestoneIdHolder.remove();
	}

}
