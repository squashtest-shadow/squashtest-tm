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
define(['squash.translator','tree', './cr-treemenu', './init-actions',
        'squash/squash.tree-page-resizer', 'app/ws/squashtm.toggleworkspace',
        'milestone-manager/milestone-activation', 'milestones/milestones-tree-menu','./popups/init-all'],
		function(translator,tree, treemenu, actions, resizer, ToggleWorkspace, mstoneManager, mstoneTreeMenu,popups) {


	function initResizer(){
		var conf = {
			leftSelector : "#tree-panel-left",
			rightSelector : "#contextual-content"
		};
		resizer.init(conf);
	}

	function initTabbedPane() {
		$("#tabbed-pane").tabs();
	}

	function initMilestoneMenu(){
		if (mstoneManager.isEnabled()){
			mstoneTreeMenu.init();
		}
	}

  function initI18n() {
    translator.load({
      "date-format" : "squashtm.dateformat",
      "label-never": "label.lower.Never"
    });
  }

	function init(settings){
    initI18n();
		initResizer();
    initTabbedPane();
		initMilestoneMenu();
		ToggleWorkspace.init(settings.toggleWS);
		tree.initWorkspaceTree(settings.tree);
		treemenu.init(settings.treemenu);
		popups.init();
		actions.init();
    //For V1.13 we don't want copy/past or drag/drop inside tree. Cannot do it in workspace conf because we override some core function of jstree
    //with custom plugin so the conf will be overriden
    //It's safer to put deactivation here because of it's temporary nature and remove the line when copy/pasta will be done in custom-report-workspace
    $.jstree._fn.check_move = function(){
      return false;
    };
	}


	return {
		init : init
	};

});
