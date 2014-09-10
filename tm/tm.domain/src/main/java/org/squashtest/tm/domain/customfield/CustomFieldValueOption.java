package org.squashtest.tm.domain.customfield;

import javax.persistence.Embeddable;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

@Embeddable
public class CustomFieldValueOption {
	public static final int MAX_SIZE = 255;
	
	@NotBlank
	@Size(min = 0, max = MAX_SIZE)
	@Pattern(regexp = CustomField.OPTION_REGEXP)
	private String option;

	public String getOption() {
		return option;
	}

	public void setOption(String option) {
		this.option = option;
	}

}
