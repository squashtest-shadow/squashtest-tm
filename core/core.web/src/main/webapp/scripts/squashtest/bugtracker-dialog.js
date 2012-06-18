/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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

function buildSelectOption(jsonOption) {
	var htmlOption = "<option value=\"";
	htmlOption += jsonOption.id + "\">";
	htmlOption += jsonOption.name + "</option>";
	return htmlOption;
}

function flushSelect(jqSelect) {
	jqSelect.find("option").remove();
}

function populateSelect(jqSelect, jsonOptions) {

	var i = 0;
	for (i = 0; i < jsonOptions.length; i++) {
		var htmlContent = buildSelectOption(jsonOptions[i]);
		jqSelect.append(htmlContent);
	}
	return false;

}

function btEntity(argId, argName) {
	this.id = argId;
	this.name = argName;
	this.format = function () {
		return "id=" + this.id + ",name=" + this.name;
	};
}

function extractSelectData(jqSelect) {
	var id = jqSelect.val();
	var name = jqSelect.find("option:selected").text();

	return new btEntity(id, name);
}

function handleEmptyList(jsList, emptyListLabel) {
	if (jsList[0].empty == true) {
		var stubArray = new Array();
		var stub = new btEntity(jsList[0].id, emptyListLabel);
		stubArray.push(stub);
		return stubArray;
	} else {
		return jsList;
	}
}