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
define(["jquery", "backbone", "handlebars", "underscore", "workspace.routing", "squash.translator","tree","workspace.storage","jquery.squash.formdialog"],
  function ($, Backbone, Handlebars, _, urlBuilder, translator, tree, storage) {
  var viewConstructor = Backbone.View.extend({

    el: "#coverage-stat",
    treeSelector : "#perimeter-tree",
    storagePrefix : "requirement-coverage-stat-perimeter",

    events : {
        "click #change-perimeter-button" :"showSelectPerimeter"
    },

    initialize: function () {
        console.log("INIT RATES");
        this.initializeRate();
        this.initPerimeterDialog();
    },

    initializeRate : function () {
        this.initializeData().render();
    },

    initializeData : function () {
        console.log("INIT RATES DATA");
        var url = urlBuilder.buildURL("requirements.coverageStats.model",this.model.get("id"));
        var self = this;
        var key = this.getStorageKey();
        var value = storage.get(key) ? storage.get(key) : "";
        var data = {
        perimeter : value
        };
        console.log(url);

        $.ajax({
        url: url,
        type: 'GET',
        data : data
        })
        .done(function(response) {
        console.log(response);
        console.log("success");
        self.model.set("coverage",response.rates.coverage);
        self.render();
        });
        return this;
    },

    render : function () {
        var templated = this.makeTemplating("#tpl-show-coverage-rate",this.model.get("coverage"));
        this.$el.find("#coverage-rate").html(templated);
    },

    initPerimeterDialog : function () {
        var self = this;
        var templated = this.makeTemplating("#tpl-dialog-select-perimeter");
        this.$el.find("#dialog-select-perimeter-wrapper").html(templated);
        var dialog = this.$el.find("#dialog-select-perimeter").formDialog();

        //Init popup events
        dialog.on('formdialogconfirm', function(){
        self.changePerimeter();
            dialog.formDialog('close');
        });

        dialog.on('formdialogcancel', function(){
            dialog.formDialog('close');
        });

        $.ajax({
            url : squashtm.app.contextRoot + "/" + 'campaign-workspace/tree/0',
            datatype : 'json'


        }).done(function(model){

        var treeConfig = {
            model : model,
            treeselector: self.treeSelector,
            workspace: "campaign-it",
            canSelectProject:false,
            forbidSelectFolder:true
        };
        tree.initLinkableTree(treeConfig);
      });
    },

    makeTemplating : function (selector, data) {
        var source = $(selector).html();
        var template = Handlebars.compile(source);
        return template(data);
    },

    showSelectPerimeter : function () {
        console.log("click");
        $("#dialog-select-perimeter").formDialog("open");
    },

    changePerimeter : function () {
        var selectedNode = $(this.treeSelector).jstree("get_selected");
        var key = this.getStorageKey();
        var value = selectedNode.getDomId();

        storage.set(key,value);
        console.log(storage.get(key));
    },

    getStorageKey : function () {
        return this.storagePrefix + this.model.get("projectId");
    }
  });

  return viewConstructor;
});
