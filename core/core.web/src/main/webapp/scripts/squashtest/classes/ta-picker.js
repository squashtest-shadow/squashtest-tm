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


/*
	settings : 
		- selector : an appropriate selector for the popup.
		- testAutomationURL : the url where to GET - POST things.
		- baseURL : the base url of the app.
		- messages : 
			- noTestSelected : message that must be displayed when nothing is selected
*/
function TestAutomationPicker(settings){

	var self=this;
	
	var instance = $(settings.selector);

	var testAutomationURL = settings.testAutomationURL;
	var baseURL = settings.baseURL;
	
	var pleaseWaitPanel = instance.find(".pleasewait");
	var mainPanel = instance.find(".structure-treepanel");
	var tree = instance.find(".structure-tree");
	
	
	var error = instance.find(".error").popupError();

	
	//cache
	this.modelCache = undefined;

	
	// ************ state handling ******************
	
	var flipToPleaseWait = function(){
		pleaseWaitPanel.removeClass("not-displayed");
		mainPanel.addClass("not-displayed");
		error.popup('hide');
	};
	
	var flipToMain = function(){
		pleaseWaitPanel.addClass("not-displayed");
		mainPanel.removeClass("not-displayed");
	};
	
	var getPostParams = function(){
		
		var node = tree.jstree('get_selected');
		
		if (node.length <1 ) {
			throw "no-selection";
		};
		
		return {
			path : node.getPath(),
			libId : node.getLibrary().getDomId();
		};
	}
	
	
	// ************ model *****************************
	
	
	
	var init = function(){
		$.ajax({
			url : testAutomationURL,
			type : 'GET',
			dataType : 'json'
		})
		.done(function(json){
			setCache(json);
			createTree();
			flipToMain();
		})
		.fail(function(jsonError){
			var message = squashtm.notification.getErrorMessage(jsonError);
			fatalError.find('span').text(message);
			fatalError.popupError('show');				
		});
	};
	
	
	var setCache = function(model){
		self.modelCache = model;
	};
	
	var createTree = function(){
		var icons = {
			drive : baseURL+"/images/root.png",
			folder :  baseURL+"/images/Icon_Tree_Folder.png",
			file : baseURL+"/images/Icon_Tree_TestSuite.png",
			mainstyle : baseURL+"/styles/squashtree.css"					
		};
	
		tree.jstree({				
			"json_data" : {
				"data" : self.modelCache
			},
			
			"types" : {
				"types" : {
					"drive" : {
						"valid_children" : [ "file", "folder"],
						"icon" : {
							"image" : icons.drive
						},
						"select_node" : false
					},
					"file" : {
						"valid_chidlren" : "none",
						"icon" : {
							"image" : icons.file
						}
					},
					"folder" : {
						"valid_children" : [ "file", "folder" ],
						"icon" : {
							"image" : icons.folder
						},
						"select_node" : false
					}
				}
			},
			
			"ui" :{
				select_multiple_modifier: false
			},
			
			"themes" : {
				"theme" : "squashtest",
				"dots" : true,
				"icons" : true,
				"url" : icons.mainstyle		
			},
			
			"core" : {
				"animation" : 0
			},
			
			"plugins" : ["json_data", "types", "ui", "themes", "squash" ]
			
			
		});			
	
	};
	
	
	var reset = function(){
		tree.jstree('get_selected').deselect();	
	};
	
	
	var submit = function(){
	
		var name = getFullTestName();
	
		try{
	
		
			$.ajax({
				url : testAutomationURL,
				type : 'POST',
				params : { 
				
			});
		}catch(exception){
			if (exception == "no-selection"){
				error.find("span").text(settings.messages.noTestSelected);
				error.popupError('show');
			};
		}
		
	};
	
	
	// ************ events *********************
	
	var buttons = instance.dialog('option', 'buttons');
	
	buttons[0].click = submit;
	
	buttons[1].click = function(){
		instance.dialog('close');
	};
	

	/** put that on later
	
	instance.bind("dialogopen", function(){
		if (self.modelCache===undefined){
			init();
		}else{
			reset();
		}
	});
	
	***/
	
	//debug
	
	this.modelCache = [{"state":"closed","data":{"title":"deuxième job"},"children":[{"state":"closed","data":{"title":"tests"},"children":[{"state":"closed","data":{"title":"moartests"},"children":[{"state":"leaf","data":{"title":"evenmoaar.txt"},"children":[],"attr":{"name":"evenmoaar.txt","id":null,"rel":"file","restype":"ta-test"}},{"state":"leaf","data":{"title":"moaaar.txt"},"children":[],"attr":{"name":"moaaar.txt","id":null,"rel":"file","restype":"ta-test"}}],"attr":{"name":"moartests","id":null,"rel":"folder","restype":"ta-folder"}},{"state":"leaf","data":{"title":"root-test.txt"},"children":[],"attr":{"name":"root-test.txt","id":null,"rel":"file","restype":"ta-test"}},{"state":"closed","data":{"title":"ça va être long"},"children":[{"state":"leaf","data":{"title":"testlong-1.txt"},"children":[],"attr":{"name":"testlong-1.txt","id":null,"rel":"file","restype":"ta-test"}},{"state":"leaf","data":{"title":"testlong-2.txt"},"children":[],"attr":{"name":"testlong-2.txt","id":null,"rel":"file","restype":"ta-test"}}],"attr":{"name":"ça va être long","id":null,"rel":"folder","restype":"ta-folder"}}],"attr":{"name":"tests","id":null,"rel":"folder","restype":"ta-folder"}}],"attr":{"name":"deuxième job","id":"2","rel":"drive","restype":"ta-project"}},{"state":"closed","data":{"title":"job-1"},"children":[{"state":"closed","data":{"title":"tests"},"children":[{"state":"closed","data":{"title":"autrestests"},"children":[{"state":"leaf","data":{"title":"othertest1.txt"},"children":[],"attr":{"name":"othertest1.txt","id":null,"rel":"file","restype":"ta-test"}}],"attr":{"name":"autrestests","id":null,"rel":"folder","restype":"ta-folder"}},{"state":"closed","data":{"title":"database-tests"},"children":[{"state":"leaf","data":{"title":"dbtest-1.txt"},"children":[],"attr":{"name":"dbtest-1.txt","id":null,"rel":"file","restype":"ta-test"}},{"state":"leaf","data":{"title":"dbtest-2.txt"},"children":[],"attr":{"name":"dbtest-2.txt","id":null,"rel":"file","restype":"ta-test"}}],"attr":{"name":"database-tests","id":null,"rel":"folder","restype":"ta-folder"}},{"state":"leaf","data":{"title":"vcs.txt"},"children":[],"attr":{"name":"vcs.txt","id":null,"rel":"file","restype":"ta-test"}}],"attr":{"name":"tests","id":null,"rel":"folder","restype":"ta-folder"}}],"attr":{"name":"job-1","id":"1","rel":"drive","restype":"ta-project"}}];
	
	createTree();
	
	flipToMain();
	
}


