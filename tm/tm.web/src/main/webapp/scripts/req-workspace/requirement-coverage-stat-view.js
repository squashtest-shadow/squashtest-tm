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
define(["jquery", "backbone", "handlebars", "underscore", "workspace.routing", "squash.translator", "jquery.squash.formdialog"],
  function ($, Backbone, Handlebars, _, urlBuilder, translator) {
  var viewConstructor = Backbone.View.extend({

    el: "#coverage-stat",

    initialize: function () {
      console.log("INIT RATES");
      this.initializeData().render();
    },

    initializeData : function () {
      console.log("INIT RATES DATA");
      var url = urlBuilder.buildURL("requirements.coverageStats.model",this.model.get("id"));
      var self = this;
      console.log(url);

      $.ajax({
        url: url,
        type: 'GET'
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
      var source = $("#tpl-show-coverage-rate").html();
      var template = Handlebars.compile(source);
      var coverage = template(this.model.get("coverage"));
      this.$el.find("#coverage-rate").html(coverage);
    }
  });

  return viewConstructor;
});
