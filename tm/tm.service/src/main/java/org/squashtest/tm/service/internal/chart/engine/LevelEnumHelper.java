package org.squashtest.tm.service.internal.chart.engine;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.squashtest.tm.domain.Level;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.domain.testcase.TestCaseExecutionMode;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseStatus;

public class LevelEnumHelper {

	private static final Map<String, Enum<? extends Level>> map = new HashMap<String, Enum<? extends Level>>();

	private static final List<Class<? extends Enum<? extends Level>>> enums = Arrays
			.<Class<? extends Enum<? extends Level>>> asList(
			TestCaseExecutionMode.class, RequirementCriticality.class, RequirementStatus.class, TestCaseStatus.class,
			TestCaseImportance.class);

	static {
		for (Class<? extends Enum<? extends Level>> c : enums) {
			for (Enum<? extends Level> val : c.getEnumConstants()) {
				map.put(val.name(), val);
			}
		}
	}

	public static Object valueOf(String val) {
		Enum<? extends Level> level = map.get(val);
		return Enum.valueOf(level.getDeclaringClass(), val);
	}

}
