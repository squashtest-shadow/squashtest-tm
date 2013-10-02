package org.squashtest.tm.api.report.form;

import java.util.List;

import org.squashtest.tm.core.foundation.i18n.Labelled;

public class ProjectInputBlock extends Labelled implements Input{
	
	private String name;
	private List<ProjectPicker> pickers;
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	

	public List<ProjectPicker> getPickers() {
		return pickers;
	}

	public void setPickers(List<ProjectPicker> pickers) {
		this.pickers = pickers;
	}

	@Override
	public InputType getType() {
		return InputType.PROJECT_INPUT_BLOCK;
	}
}
