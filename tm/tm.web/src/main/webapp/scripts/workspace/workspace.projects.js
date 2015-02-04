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

define(["jquery"], function($){
	
	function getAll(){
		return squashtm.workspace.projects;
	}
	
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
	 * given their ids, tells whether at least two projects have different configurations
	 * regarding the info list configuration
	 * 
	 * as an additional argument, an array of any combination of "nature"|"type"|"category".
	 * if specified, only the given attributes will be checked.
	 */
	function haveDifferentInfolists(projectIds, attributes){
		
		if ( projectIds.length === 0 ){
			return false;
		}
		
		var areDifferent =  false,
			attrs = attributes || ["nature", "type", "category"];
				
		var firstP = findProject(projectIds[0]); 
		var nats = firstP.testCaseNatures.code,
			typs = firstP.testCaseTypes.code, 
			cats = firstP.requirementCategories.code;
		
		var chkNat = (attrs.indexOf("nature") > -1),
			chkTyp = (attrs.indexOf("type") > -1),
			chkCat = (attrs.indexOf("category") > -1);
		
		for (var i=1; i<projectIds.length; i++){
			var p = findProject(projectIds[i]);
			
			if ( chkNat ){
				if (nats !== p.testCaseNatures.code){
					areDifferent = true;
					break;
				}
			}
			
			if ( chkTyp ){
				if (typs !== p.testCaseTypes.code){
					areDifferent = true;
					break;
				}
			}
			
			if ( chkCat ){
				if (cats !== p.requirementCategories.code){
					areDifferent = true;
					break;
				}
			}
		}
		
		return areDifferent;
	}
	

	return {
		getAll : getAll,
		findProject : findProject,
		haveDifferentInfolists : haveDifferentInfolists
	};
	
	
});