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
define(["jquery", "workspace.routing", 'app/util/URLUtils', "./prologue-handler", "./step-handler"], 
		function($, routing, Urls, prologueHandler, stepHandler){


	function init(){
		
		// first : we must identify what is the context. Here we do so by analyzing the url.
		// TODO : the same job for the OER.
		
		var url = Urls.extractPath(document.location.href);
		
		if (routing.matches('execute.prologue', url, false)){
			var decomp = routing.unbuildURL('execute.prologue', url);
			var execID = decomp[0];
			prologueHandler.init(execID);
		}
		else if (routing.matches('execute.stepbyindex', url, false)){
			var decomp = routing.unbuildURL('execute.stepbyindex', url);
			var execID = decomp[0];
			var stepIndex = decomp[1];
			stepHandler.init(execID, stepIndex);
		}
		
		
	}
	
	
	function getJunk(){
		var junk = $("#testadvisorjunk");
		if (junk.length===0){
			var container = $("<div/>", { 'class' : 'not-displayed' });
			var junk = $("<iframe/>", { 'width' : '0%', 'height' : '0%', 'id' : 'testadvisorjunk' });
			$("body").append(container);
			container.append(junk);
		}
		
		return junk;
	}


	function start(code, label){
		$.ajax({
			url :'http://localhost:8080/squash/testadvisor/start',
			type : 'POST',
			dataType : 'text',
			data : { code: code, label: label }
		}).success(function(uuid){
			squashtm.uuid = uuid;
			var junk = getJunk();
			var url = 'http://otherdomain:5096/api/tce/addCookie?uuid='+uuid;
			junk.attr('src', url);
		})
	}


	function stop(status){
		var junk = getJunk();
		junk.attr('src', 'http://otherdomain:5096/api/tce/stop?uuid='+squashtm.uuid+'&successful='+status);
	}

	
	return {
		
		init : init,
		
		start : start,
		stop : stop
	};
	
});