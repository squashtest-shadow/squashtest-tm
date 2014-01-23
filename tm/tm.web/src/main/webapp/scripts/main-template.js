/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
/**
 * This is a template for "main.js" files.
 */
require([ "common" ], function(common) {
	require([ "domReady", "jquery" ], function(domReady, $) {
		domReady(function() {
			// your code here
		});
	});
});
/*<![CDATA[*/
var squashtm = squashtm || {};
	squashtm.app = squashtm.app || {} ;
	squashtm.app.messages = squashtm.app.messages || {};
	squashtm.app.messages["message.notBlank"] = 'ne peut pas \u00EAtre vide';
	
	require(["common"], function() {
	require([ "domReady", "test-case-management" ], function(domReady, testCaseManagement) {
		var settings = {
			basic : {
				testCaseUrl : '\/squash\/test-cases\/238',
				testCaseId : 238,
				projectId : 14,
				parametersUrl : '\/squash\/parameters',
				datasetsUrl : '\/squash\/datasets',
				testCaseDatasetsUrl : '\/squash\/test-cases\/238\/datasets',
				dataTableLanguageUrl : '\/squash\/datatables\/messages',
				ckeConfigUrl : '\/squash\/styles\/ckeditor\/ckeditor-config.js',
				indicatorURl : '\/squash\/scripts\/jquery\/indicator.gif',
				parameterValuesUrl : '\/squash\/dataset-parameter-values' 
			},

			language : {
				cancellabel : 'Annuler',
				add : 'Ajouter',
				ckeLang : 'fr',
				placeholder : '(Cliquer pour \u00E9diter...)',
				submit : 'Valider',
				edit : '\u00C9diter',
				remove : 'Supprimer',
				parametersPanelTitle : 'Param\u00E8tres',
				datasetsPanelTitle : 'Jeux de donn\u00E9es'
			},
			
			datasetsAoColumnDefs : '[{\"bVisible\":false,\"bSortable\":false,\"sClass\":\"\",\"sWidth\":null,\"aTargets\":[0],\"mDataProp\":\"entity-id\"},{\"bVisible\":true,\"bSortable\":false,\"sClass\":\"select-handle centered\",\"sWidth\":\"2em\",\"aTargets\":[1],\"mDataProp\":\"entity-index\"},{\"bVisible\":true,\"bSortable\":true,\"sClass\":\"dataset-name\",\"sWidth\":null,\"aTargets\":[2],\"mDataProp\":\"name\"},{\"bVisible\":true,\"bSortable\":false,\"sClass\":\"delete-button\",\"sWidth\":\"2em\",\"aTargets\":[3],\"mDataProp\":\"empty-delete-holder\"}]',
			
			permissions : {
				isWritable : true 
			}, 
			
			parameters: {
				tabIndex: 2
			}
		};
						
		domReady(function() {		
			testCaseManagement.initParametersPanel(settings);
		});
	});
	});
/*]]>*/