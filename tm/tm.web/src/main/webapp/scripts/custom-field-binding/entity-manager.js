/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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


define (function(require){
	
	var Panel = require("./panel.js");

	function getPanelConf(settings){
		return { 
			'selector' : settings.panelSelector,
			'initiallyOpen' : settings.panelInitiallyOpen, 
			'title' : settings.panelTitle
		};
	}
	
	return function(settings){
		var panelConf = getPanelConf(settings);
		this.panel = new Panel(panelConf);
	};

});