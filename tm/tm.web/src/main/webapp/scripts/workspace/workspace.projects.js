/*
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


define(["jquery"], function($){
	
	function findProject(idOrName){
		var matches,
			project,
			projects = squashtm.workspace.projects;
		
		if (! isNaN(parseInt(idOrName, 10))){
			var id = parseInt(idOrName, 10);
			matches = function(p){return p.id === id;};
		}
		else{
			matches = function(p){return p.name === idOrName;};
		}
		
		for (var i=0; i<projects.length; i++){
			if (matches(projects[i])){
				project = projects[i];
				break;
			}
		}
		
		return project;
		
	}
	
	/*
	 * Given the ID of a reference project, and the ID/array of ID of more projects,
	 * tells whether at list one of the other projects differs from the reference project
	 * regarding the info list configuration
	 */
	function haveDifferentInfolists(reference, otherProjects){
		var areDifferent =  false;
		var refproject = findProject(reference), 
			cats = refproject.requirementCategories.code,
			nats = refproject.testCaseNatures.code,
			typs = refproject.testCaseTypes.code;
		
		var others = (otherProjects instanceof Array ) ? otherProjects : [ otherProjects ];
			
		for ( var i = 0; i < others.length; i++){
			var oProj = findProject(others[i]);
			if ((cats !== oProj.requirementCategories.code) ||
				(nats !== oProj.testCaseNatures.code) ||
				(typs !== oProj.testCaseTypes.code)
			){
				areDifferent = true;
				break;
			}
		}
		
		return areDifferent;
	}
	

	return {
		findProject : findProject,
		haveDifferentInfolists : haveDifferentInfolists
	};
	
	
});