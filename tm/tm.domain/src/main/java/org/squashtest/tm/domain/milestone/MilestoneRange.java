package org.squashtest.tm.domain.milestone;

import org.squashtest.tm.domain.Level;

public enum MilestoneRange implements Level{

GLOBAL(1), RESTRICTED(2);
	
	
	private static final String I18N_KEY_ROOT = "milestone.range.";

	private final int level;

	private MilestoneRange(int level) {
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
