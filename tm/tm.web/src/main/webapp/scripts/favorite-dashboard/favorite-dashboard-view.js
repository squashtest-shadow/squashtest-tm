/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
define(["backbone","custom-report-workspace/views/dashboardView","./cant-show-favorite-view","../user-account/user-prefs","app/AclModel","tree"], 
    function(Backbone,DashboardView,CantShowView,userPrefs,AclModel,zetree) {
    'use strict';
     var View = Backbone.View.extend({
            el: "#favorite-dashboard-wrapper",

            initialize : function(options) {
                console.log("backbone view initialized");
                this.canShowDashboard = squashtm.workspace.canShowFavoriteDashboard==="true";
                this.initializeEvents();
                this.tree = zetree.get();
                this.initView();
                this.model = Backbone.Model.extend({timestamp : new Date()});
                
            },
            events : {
                "click .favorite-dashboard-refresh-button": "triggerRefresh",
                "click .show-default-dashboard-button": "showDefault"
            },

            initView : function() {
                if(this.canShowDashboard){
                    this.showDashboard();
                    //setting a global param so the workspace can keep trace of view all ready loaded
                    squashtm.workspace.favoriteViewLoaded = true;
                    var selected =  this.tree.jstree("get_selected");
                    if(selected.length > 1){
                        squashtm.workspace.multipleSelectionDashboard = true;
                    } else {
                        squashtm.workspace.multipleSelectionDashboard = false;
                    }
                } else {
                    this.activeView = new CantShowView();
                }
            },

            showDashboard : function() {
                var id = userPrefs.getFavoriteDashboardId("tc");
                var dynamicScopeModel = this.generateDynamicScopeModel();

                if(id){
                    id = Number(id);
                    var modelDef = Backbone.Model.extend({
                        defaults: {
                            id: id,
                            showInClassicWorkspace :true,
                            dynamicScopeModel : dynamicScopeModel
                        }
                    });

                    var activeModel = new modelDef();
                    var acls = new AclModel({type: "custom-report-library-node", id: id});

                    this.activeView = new DashboardView({
                        model: activeModel,
                        acls: acls
                    });
                }
        
            },

            generateDynamicScopeModel : function() {
                //ugly reference to the external global tree
                //maybe resolvable by passing tree in constructor ?
                var selected =  this.tree.jstree("get_selected");
                var self = this;
                var dynamicScopeModel = {
                    testCaseLibraryIds : self.filterByType(selected,"test-case-libraries"),
                    testCaseFolderIds : self.filterByType(selected,"test-case-folders"),
                    testCaseIds : self.filterByType(selected,"test-cases")
                };
                return dynamicScopeModel;
            },

            filterByType : function(selectedNodes,type) {
                var selector = "[restype='" + type + "']";
                var nodeIds = selectedNodes.filter(selector).map(function(i,e){
					return $(e).attr("resid");
				}).get();

                return _.map(nodeIds,function(id) {
                    return parseInt(id);
                });
            },

            initializeEvents : function() {
                var wreqr = squashtm.app.wreqr;
                var self = this;
			    wreqr.on("favoriteDashboard.reload", function () {
                    //removing the active view and reinitialize a dashboard with new selection
                    self.activeView.remove();
                    self.$el.html('<div id="contextual-content-wrapper" class="dashboard-grid-in-classic-workspace ui-corner-all"> </div>');
                    self.initView();
                });

                wreqr.on("contextualContent.loadWith", function () {
                    self.remove();
                });
            },

            triggerRefresh : function() {
               var wreqr = squashtm.app.wreqr;
               wreqr.trigger("favoriteDashboard.reload");
            },

            showDefault : function() {
                var wreqr = squashtm.app.wreqr;
                var self = this;

                var callback = function() {
                    wreqr.trigger("favoriteDashboard.showDefault");
                    //destroying the backbone view
                    self.remove();
                };

                userPrefs.chooseDefaultContentInWorkspace(callback);
              
            },

            remove :  function() {
                squashtm.workspace.favoriteViewLoaded = false;
                 //removing listener on event bus
                var wreqr = squashtm.app.wreqr;
                wreqr.off("favoriteDashboard.reload");
                wreqr.off("contextualContent.loadWith");
                this.activeView.remove();
                Backbone.View.prototype.remove.call(this);
            }

           
    });

    return View;
});