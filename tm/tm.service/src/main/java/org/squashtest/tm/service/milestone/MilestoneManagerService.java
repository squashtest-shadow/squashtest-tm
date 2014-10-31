package org.squashtest.tm.service.milestone;

import java.util.Date;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.dynamicmanager.annotation.DynamicManager;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneStatus;


@Transactional
@DynamicManager(name="squashtest.tm.service.MilestoneManagerService", entity = Milestone.class)
public interface MilestoneManagerService extends CustomMilestoneManager{
	void changeDescription(long milestoneId, String newDescription);
	void changeLabel(long milestoneId, String newLabel);
	void changeStatus(long milestoneId, MilestoneStatus newStatus);
	void changeEndDate(long milestoneId, Date newEndDate);
}
