package org.squashtest.tm.api.report.form;

import java.util.List;

import org.squashtest.tm.core.foundation.i18n.Labelled;

public class DateInputBlock extends Labelled implements Input{
	
	private String name;
	private List<DateInput> dates;
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	public List<DateInput> getDates() {
		return dates;
	}
	
	public void setDates(List<DateInput> dates) {
		this.dates = dates;
	}
	
	@Override
	public InputType getType() {
		return InputType.DATE_INPUT_BLOCK;
	}
}
