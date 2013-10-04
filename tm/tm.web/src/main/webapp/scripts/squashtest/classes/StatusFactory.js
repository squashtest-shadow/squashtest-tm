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
(function() {
	squashtm = squashtm || {};

	squashtm.StatusFactory = squashtm.StatusFactory || function(conf) {

		this._conf = {};
		//convert the status to lowercase
		for (var status in conf){
			this._conf[status.toLowerCase()] = conf[status];
		}
		
		this.getHtmlFor = function(translatedTextStatus, status) {
			var css;
			if (!! status) {
				css = "exec-status-" + status.toLowerCase();
			} else {
				css = this.lookupCss(translatedTextStatus);
			}
			return makeHtml(css, translatedTextStatus);
		};

		this.lookupCss = function(translatedTextStatus) {
			var css,
				lowerCaseStatus = translatedTextStatus.toLowerCase(),
				conf = this._conf;

			
			switch (lowerCaseStatus) {
			case conf.blocked:
				css = "exec-status-blocked";
				break;

			case conf.failure:
				css = "exec-status-failure";
				break;

			case conf.success:
				css = "exec-status-success";
				break;

			case conf.running:
				css = "exec-status-running";
				break;

			case conf.ready:
				css = "exec-status-ready";
				break;

			case conf.error:
				css = "exec-status-error";
				break;

			case conf.warning:
				css = "exec-status-warning";
				break;

			case conf.untestable:
				css = "exec-status-untestable";
				break;

			default:
				css = "";
				break;

			}

			return css;
		}

		function makeHtml(cssClass, text) {
			return '<span class="exec-status-label ' + cssClass + '">' + text + '</span>';
		}

	};

	squashtm.statusFactory = new squashtm.StatusFactory();
})();
