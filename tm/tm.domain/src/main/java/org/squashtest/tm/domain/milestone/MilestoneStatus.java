package org.squashtest.tm.domain.milestone;

import org.squashtest.tm.domain.Level;

public enum MilestoneStatus implements Level {
	
	STATUS_1(1), STATUS_2(2), STATUS_3(3);
	
	
	private static final String I18N_KEY_ROOT = "milestone.status.";

	private final int level;

	private MilestoneStatus(int level) {
		this.level = level;
	}

	@Override
	public String getI18nKey() {
		return I18N_KEY_ROOT + name();
	}

	@Override
	public int getLevel() {
		return level;
	}

}
