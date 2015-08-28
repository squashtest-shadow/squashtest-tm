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

define(['jquery', 'workspace.event-bus', 'jqueryui'], function($, eventBus){
	
	
	function initPauseButton(pauseButton, conf){
		
	}
	
	
	function buildPrologueButtons(){
		
		var beginButton = $("#execute-begin-button");
		
		var pauseButton = $("<a>", {
			'text' : 'pause'
		});
		
		var startButton = $("<a>", {
			'text' : 'start'
		});
		
		
		startButton.insertBefore(beginButton);
		pauseButton.insertBefore(startButton);
		
		startButton.squashButton();
		pauseButton.squashButton();
		
	}
	
	function buildStepButtons(){
		var insertionLabel = $("#evaluation-label-status");
		
		var newCell = $("<td>");
		
		var pauseButton = $("<a>", {
			'text' : 'pause'
		});
		
		var startButton = $("<a>", {
			'text' : 'start'
		}); 

		
		startButton.insertBefore(insertionLabel);
		pauseButton.insertBefore(insertionLabel);
		
		startButton.squashButton();
		pauseButton.squashButton();
		
	}
	
	return {
		buildPrologueButtons : buildPrologueButtons,
		buildStepButtons : buildStepButtons
	};
	
});
