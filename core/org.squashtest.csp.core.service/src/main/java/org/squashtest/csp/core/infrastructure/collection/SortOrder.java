package org.squashtest.csp.core.infrastructure.collection;

import javax.validation.constraints.NotNull;

public enum SortOrder {
	ASCENDING("asc"), DESCENDING("desc");

	private final String code;

	private SortOrder(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static SortOrder coerceFromCode(@NotNull String code) {
		for (SortOrder order : values()) {
			if (order.getCode().equals(code)) {
				return order;
			}
		}

		throw new IllegalArgumentException("Code '" + code + "' is unknown of SortingOrder enum");
	}
}
