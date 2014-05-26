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
/*
 *API : 
 *
 *	translate(statusName) -> returns the i18n version of this status name
 *	reverseTranslate(i18n) -> return the real name of the status given its translation 
 *	getHtmlFor(status) -> given a statusname OR its translation, returns a html string to render it
 *
 */
define(["squash.translator"], function(translator){
	return {
		
		/*
		 * PUBLIC API
		 * 
		 */
		
		getHtmlFor : function(status){
			
			var realStatusName;
			
			var css,
				text;
			
			// lets check whether the argument is a real status name or a translation
			if (this._conf[status.toUpperCase()] !== undefined){
				realStatusName = status;
			}
			else{
				realStatusName = this.reverseTranslate(status);
			}
			
			// process if found
			if (!! realStatusName){
				css = 'exec-status-' + realStatusName.toLowerCase();
				text = this.translate(realStatusName);
				
				return '<span class="exec-status-label ' + css + '">' + text + '</span>';
			}
			// if unknown, frack it
			else{
				return status;
			}
		},
		
		
		translate : function(statusName){
			return this._conf[statusName.toUpperCase()];
		},
		
		reverseTranslate : function(translated){
			for (var ppt in this._conf){
				if (this._conf[ppt]===translated){
					return ppt;
				}
			}
			return undefined;
		},
		
		
		/*
		 * PRIVATE STUFFS 
		 * 
		 */
		
		_conf : translator.get({
			UNTESTABLE : "execution.execution-status.UNTESTABLE",
			SETTLED : "execution.execution-status.SETTLED",
			BLOCKED : "execution.execution-status.BLOCKED",
			FAILURE : "execution.execution-status.FAILURE",
			SUCCESS : "execution.execution-status.SUCCESS",
			RUNNING : "execution.execution-status.RUNNING",
			READY	: "execution.execution-status.READY",
			WARNING : "execution.execution-status.WARNING",
			NOT_RUN : "execution.execution-status.NOT_RUN",
			NOT_FOUND : "execution.execution-status.NOT_FOUND",
			ERROR : "execution.execution-status.ERROR"
		})
		
	};
});