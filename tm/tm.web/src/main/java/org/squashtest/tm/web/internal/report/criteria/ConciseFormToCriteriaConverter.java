/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.squashtest.tm.web.internal.report.criteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.api.report.Report;
import org.squashtest.tm.api.report.criteria.Criteria;
import org.squashtest.tm.api.report.form.CheckboxesGroup;
import org.squashtest.tm.api.report.form.DropdownList;
import org.squashtest.tm.api.report.form.Input;
import org.squashtest.tm.api.report.form.InputType;
import org.squashtest.tm.api.report.form.InputsGroup;
import org.squashtest.tm.api.report.form.OptionInput;
import org.squashtest.tm.api.report.form.RadioButtonsGroup;
import org.squashtest.tm.domain.project.Project;

import static org.squashtest.tm.api.report.form.InputType.*;

/**
 * Converts the post data of a "concise" report form into a Map of Criteria which can be given to a Report.
 * 
 * The former FormToCriteriaConverter was "verbose" ie it was able to reach the max allowed sise of an http request
 * (issue #3762)
 * 
 * @author Gregory Fouquet
 * 
 * 
 */
public class ConciseFormToCriteriaConverter {
	/*
	 * consts for concise form
	 */
	private static final String CON_VAL = "val";
	private static final String CON_TYPE = "type";
	/*
	 * consts for expanded form
	 */
	private static final String EXP_TYPE = "type";
	private static final String EXP_VALUE = "value";
	private static final String EXP_SEL = "selected";

	private static final Logger LOGGER = LoggerFactory.getLogger(ConciseFormToCriteriaConverter.class);

	private final FormToCriteriaConverter delegate = new FormToCriteriaConverter();
	private final Map<String, Input> flattenedInputByName = new HashMap<String, Input>();
	private final List<Project> projects;

	public ConciseFormToCriteriaConverter(@NotNull Report report, @NotNull List<Project> projects) {
		super();
		this.projects = projects;
		Collection<Input> flattenedInputs = flattenInputs(Arrays.asList(report.getForm()));
		for (Input input : flattenedInputs) {
			flattenedInputByName.put(input.getName(), input);
		}
	}

	public Map<String, Criteria> convert(Map<String, Object> conciseForm) {
		return delegate.convert(expand(conciseForm));
	}

	private Map<String, Object> expand(Map<String, Object> conciseForm) {
		Map<String, Object> expandedForm = expandedForm();
		populateExpandedForm(conciseForm, expandedForm);

		return expandedForm;
	}

	/**
	 * @param conciseForm
	 * @param expandedForm
	 */
	private void populateExpandedForm(Map<String, Object> conciseForm, Map<String, Object> expandedForm) {
		for (Entry<String, Object> conciseInput : conciseForm.entrySet()) {
			// Object expandedInput = expandedForm.get(conciseInput.getKey());
			//
			populateExpandedInput(conciseInput.getKey(), conciseInput.getValue(), expandedForm);
		}

	}

	/**
	 * @param key
	 * @param value
	 * @param expandedForm
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void populateExpandedInput(String inputName, Object inputValue, Map<String, Object> expandedForm) {
		Map concise = (Map) inputValue;
		InputType type = InputType.valueOf((String) concise.get(CON_TYPE));

		Object expanded = null;

		switch (type) {
		case TEXT:
		case DATE: {
			Map exp = new HashMap();
			exp.put(EXP_TYPE, concise.get(CON_TYPE));
			exp.put(EXP_VALUE, concise.get(CON_VAL));
			expanded = exp;

			break;
		}
		case CHECKBOX: {
			Map exp = expandedCheckbox(concise);
			expanded = exp;
			break;
		}
		case RADIO_BUTTONS_GROUP: {
			List exp = expandedRadioButtonGroup(inputName, concise);

			expanded = exp;
			break;
		}

		case DROPDOWN_LIST: {
			List exp = expandedDropdownList(inputName, concise);

			expanded = exp;
			break;
		}
		case CHECKBOXES_GROUP: {
			List exp = expandedCheckboxesGroup(inputName, concise);

			expanded = exp;
			break;
		}

		case PROJECT_PICKER: {
			List exp = expandedProjectPicker(concise);

			expanded = exp;
			break;
		}
		case TREE_PICKER: {
			Collection<Map> selNodes = (Collection<Map>) concise.get(CON_VAL);

			if (selNodes.isEmpty()) {
				return;
			}

			List exp = new ArrayList();

			for (Map node : selNodes) {
				Map expOpt = new HashMap();
				expOpt.put(EXP_TYPE, concise.get(CON_TYPE));
				expOpt.put(EXP_VALUE, node.get("resid"));
				expOpt.put("nodeType", node.get("restype"));

				exp.add(expOpt);
			}

			expanded = exp;
			break;
		}
		}

		expandedForm.put(inputName, expanded);
	}

	@SuppressWarnings("unchecked")
	private Map expandedCheckbox(Map concise) {
		Map exp = new HashMap();
		exp.put(EXP_TYPE, concise.get(CON_TYPE));
		exp.put(EXP_SEL, concise.get(CON_VAL));
		exp.put(EXP_VALUE, "");
		return exp;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List expandedProjectPicker(Map concise) {
		List exp = new ArrayList();
		Collection selVals = (Collection) concise.get(CON_VAL);

		for (Project p : projects) {
			Map expOpt = new HashMap();
			expOpt.put(EXP_TYPE, concise.get(CON_TYPE));
			expOpt.put(EXP_VALUE, String.valueOf(p.getId()));
			expOpt.put(EXP_SEL, selVals.contains(String.valueOf(p.getId())));

			exp.add(expOpt);
		}
		return exp;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List expandedRadioButtonGroup(String inputName, Map concise) {
		RadioButtonsGroup reportInput = (RadioButtonsGroup) flattenedInputByName.get(inputName);
		List exp = new ArrayList();
		Object selVal = concise.get(CON_VAL);

		for (OptionInput opt : reportInput.getOptions()) {
			Map expOpt = new HashMap();
			expOpt.put(EXP_TYPE, concise.get(CON_TYPE));
			expOpt.put(EXP_VALUE, opt.getValue());
			expOpt.put(EXP_SEL, selVal.equals(opt.getValue()));

			exp.add(expOpt);
		}
		return exp;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List expandedDropdownList(String inputName, Map concise) {
		DropdownList reportInput = (DropdownList) flattenedInputByName.get(inputName);
		List exp = new ArrayList();
		Object selVal = concise.get(CON_VAL);

		for (OptionInput opt : reportInput.getOptions()) {
			Map expOpt = new HashMap();
			expOpt.put(EXP_TYPE, concise.get(CON_TYPE));
			expOpt.put(EXP_VALUE, opt.getValue());
			expOpt.put(EXP_SEL, selVal.equals(opt.getValue()));

			exp.add(expOpt);
		}
		return exp;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List expandedCheckboxesGroup(String inputName, Map concise) {
		CheckboxesGroup reportInput = (CheckboxesGroup) flattenedInputByName.get(inputName);
		List exp = new ArrayList();
		Collection selVals = (Collection) concise.get(CON_VAL);

		for (OptionInput opt : reportInput.getOptions()) {
			Map expOpt = new HashMap();
			expOpt.put(EXP_TYPE, concise.get(CON_TYPE));
			expOpt.put(EXP_VALUE, opt.getValue());
			expOpt.put(EXP_SEL, selVals.contains(opt.getValue()));

			exp.add(expOpt);
		}
		return exp;
	}


	/**
	 * @return
	 */
	private Map<String, Object> expandedForm() {
		return new HashMap<String, Object>();
	}

	Collection<Input> flattenInputs(List<Input> inputs) {
		Collection<Input> res = new ArrayList<Input>();

		for (Input input : inputs) {
			if (INPUTS_GROUP.equals(input.getType())) {
				res.addAll(flattenInputs(((InputsGroup) input).getInputs()));
			} else {
				res.add(input);
			}
		}

		return res;
	}

}
