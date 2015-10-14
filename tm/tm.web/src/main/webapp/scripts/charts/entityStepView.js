/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
define(["jquery", "backbone", "./abstractStepView"],
	function($, backbone, AbstractStepView) {
	"use strict";

	var entityStepView = AbstractStepView.extend({
		
		initialize : function(data, wizrouter) {
			this.tmpl = "#entity-step-tpl";
			this.model = data;
			data.nextStep = "scope";
			data.prevStep = "";
			this._initialize(data, wizrouter);
		},
		
		updateModel : function() {
		    var entity = $("input[name='entity']:checked").val();
		    var name = $("#chart-name").val();
			this.model.set({selectedEntity : entity, name : name });  

		}
		
	});

	return entityStepView;

});