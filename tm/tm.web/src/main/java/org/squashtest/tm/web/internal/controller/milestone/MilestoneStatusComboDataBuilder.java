package org.squashtest.tm.web.internal.controller.milestone;

import javax.inject.Inject;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.LevelComparator;
import org.squashtest.tm.domain.milestone.MilestoneStatus;
import org.squashtest.tm.web.internal.helper.LevelLabelFormatter;
import org.squashtest.tm.web.internal.model.builder.EnumJeditableComboDataBuilder;

@Component
@Scope("prototype")
public class MilestoneStatusComboDataBuilder extends EnumJeditableComboDataBuilder<MilestoneStatus, MilestoneStatusComboDataBuilder>{
	public MilestoneStatusComboDataBuilder() {
		super();
		setModel(MilestoneStatus.values());
		setModelComparator(LevelComparator.getInstance());
	}

	@Inject
	public void setLabelFormatter(LevelLabelFormatter formatter) {
		super.setLabelFormatter(formatter);
	}

	/**
	 * @see org.squashtest.tm.web.internal.model.builder.EnumJeditableComboDataBuilder#itemKey(java.lang.Enum)
	 */
	@Override
	protected String itemKey(MilestoneStatus item) {
		return item.name();
	}
}
