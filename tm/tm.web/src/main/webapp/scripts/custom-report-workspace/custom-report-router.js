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
define([ "jquery", 'backbone', "domReady","workspace.routing","./views/libraryView","./views/folderView","./views/dashboardView","./views/chartView" ],
  function($, Backbone,domReady,urlBuilder,libraryView,folderView,dashboardView,chartView) {

      var router = Backbone.Router.extend({

      activeView : null,

      initialize: function() {
      },

      routes: {
        "test":"showLibraryDetails",
        "custom-report-library/:query":"showLibraryDetails",
        "custom-report-folder/:query":"showFolderDetails",
        "custom-report-dashboard/:query":"showDashboardDetails",
        "custom-report-chart/:query":"showChartDetails"
      },

      showLibraryDetails : function (id) {
        console.log("INTERCEPTED : showLibraryDetails" + id);
        var resourceUrl = urlBuilder.buildURL("custom-report-library-server",id);
        this.cleanContextContent();

        var modelDef = Backbone.Model.extend({
          defaults: {
            id : id
          },
          urlRoot : function () {
            return urlBuilder.buildURL("custom-report-library-server");
          },
          parse : function(response) {//flattening the embeded project from server in backbone model...
            var attr = response && _.clone(response) || {};
            if (response.project) {
               for (var key in response.project) {
                 if (response.project.hasOwnProperty(key)) {
                   attr["project-" + key] = response.project[key];
                 }
               }
               delete attr.project;
            }
            return attr;
          }
        });

        var activeModel = new modelDef();

        this.activeView = new libraryView({
          model : activeModel
        });

      },

      showFolderDetails : function (id) {
        console.log("INTERCEPTED : showFolderDetails" + id);
        this.cleanContextContent();
        var modelDef = Backbone.Model.extend({
          defaults: {
            id : id
          },
          urlRoot : function () {
            return urlBuilder.buildURL("custom-report-folder-server");
          }
        });

        var activeModel = new modelDef();

        this.activeView = new folderView({
          model : activeModel
        });
      },

      showDashboardDetails : function (id) {
        console.log("INTERCEPTED : showDashboardDetails " + id);
        this.cleanContextContent();
        var modelDef = Backbone.Model.extend({
          defaults: {
            id : id
          }
        });

        var activeModel = new modelDef();

        this.activeView = new dashboardView({
          model : activeModel
        });
      },

      showChartDetails : function (id) {
        console.log("INTERCEPTED : showChartDetails " + id);
        this.cleanContextContent();
        var modelDef = Backbone.Model.extend({
          defaults: {
            id : id
          }
        });

        var activeModel = new modelDef();

        this.activeView = new chartView({
          model : activeModel
        });
      },

      //Will clean the contextual part and restore the contextual div
      cleanContextContent : function () {
        if (this.activeView!==null) {
          this.activeView.remove();
        }
        //recreating the context div to allow new view to target the context div as el
        $("#contextual-content").html("<div id='contextual-content-wrapper'></div>");
      }
    });

    function init() {
      return new router();
    }

    return {
      init:init
    };
});
