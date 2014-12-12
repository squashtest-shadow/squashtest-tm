package org.squashtest.tm.domain.infolist;

public enum SystemInfoListCode {
	TEST_CASE_NATURE("DEF_TC_NAT"), TEST_CASE_TYPE("DEF_TC_TYP"), REQUIREMENT_CATEGORY("DEF_REQ_CAT");

	private final String code;

	private SystemInfoListCode(String code) {
		this.code = code;
	}

	
	public static void verifyModificationPermission(InfoList infoList) {

		for (SystemInfoListCode id : SystemInfoListCode.values()) {
			if (id.getCode().equals(infoList.getCode())) {
				throw new IllegalAccessError("You shall not pass ! This is a system info list, go away ! Play with your own info lists");
			}
		}
	}


	public String getCode() {
		return code;
	}
	
	
}
