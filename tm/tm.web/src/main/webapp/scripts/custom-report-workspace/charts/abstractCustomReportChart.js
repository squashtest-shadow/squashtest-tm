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
/*
* This view is used to plot graph in custom report workspace. Derivated from jqplot-view.
* The mains changes are :
*     - No more reference to a model binded to tree. The original jqplot-view is made in a context where selection in tree
*       can change chart.
*     - No more reference to resize window event. Resize will be handled by dashboardView
*			-	this view and derivated are meant to be instancied in callback after an ajax call for data. so series should never be undefined but can be empty...
*
*
*/
define(["jquery", "backbone", "squash.attributeparser", "workspace.event-bus", "underscore"],
		function($, Backbone, attrparser, eventbus, _){

	return Backbone.View.extend({
		//************************** commons variables **************************

		// ************************* abstract functions *****************

		getSeries : function(){
			throw "dashboard : attempted to instanciate an abstract jqplot view !";
		},

		getConf : function(series){
			throw "dashboard : attempted to instanciate an abstract jqplot view !";
		},

    getTitle : function () {
      return "<div><b>"+this.model.get('title')+"</b>";
    },

    getAxis : function(){
      return this.model.get('axis');
    },

    getCommonConf : function () {
      return {
        title : {
          textColor: "slategray",
          fontFamily: "Verdana,Arial,Helvetica,sans-serif",
          show : true,
          text : this.getTitle(),
          textAlign: "center",
          fontSize: "14px",
        }
      };
    },

    replaceInfoListDefaultLegend : function (legends) {
      var axis = this.getAxis()[0];
      var protoLabel = axis.columnPrototype.label;

      switch (protoLabel) {
        case "TEST_CASE_NATURE":
        case "TEST_CASE_TYPE":
        case "REQUIREMENT_VERSION_CATEGORY":
          return this._getI18nLegends(legends, squashtm.app.defaultInfoList);
        case "TEST_CASE_IMPORTANCE":
          return this._getI18nLegends(legends, squashtm.app.testCaseImportance);
        case "TEST_CASE_STATUS":
          return this._getI18nLegends(legends, squashtm.app.testCaseStatus);
        case "REQUIREMENT_VERSION_CRITICALITY":
          return this._getI18nLegends(legends, squashtm.app.requirementCriticality);
        case "REQUIREMENT_VERSION_STATUS":
          return this._getI18nLegends(legends, squashtm.app.requirementStatus);
        case "TEST_CASE_CREATED_ON":
          return this._formatDateLegend(legends, axis);
        default:
          return legends;
      }

    },

    _getI18nLegends : function (legends, i18nLegends) {
      return _.map(legends,function (legend) {
        if (i18nLegends[legend]) {
          return i18nLegends[legend];
        }
        return legend;
      });
    },

    _formatDateLegend : function (legends,axis) {
      var operation = axis.operation.name;
      switch (operation) {
        case "BY_DAY":
          return legends;
        case "BY_MONTH":
          return this._formatDateLegendByMonth(legends);
        case "BY_YEAR":
          return legends;
        default:
          return legends;
      }
    },

    //In chart instance date by month are returned with format YYYYMM
    _formatDateLegendByMonth : function (legends,axis) {
      var self = this;
      return _.map( legends, function(legend){
        if (Array.isArray(legend)) {
          return self._formatDateLegendByMonth(legend);
        }
        var legendString = legend.toString();
        var year = legendString.substring(0, 4);
        var month = legendString.substring(4, 6);
        return month + "/" + year;
      });
    },


		// ************************* core functions *********************

		initialize : function(options){

			// reassign this.options because they'll all be shared across instances
			this.options = options;

			//configure
			//this._readDOM();

			//create. This may abort if the model is not available yet.
			this._requestRender();

			// events
			this._bindEvents();

      _.bindAll(this, "draw");
		},


		_readDOM : function(){

			//reads the data-def from the master element
			var strconf = this.$el.data('def');
			var domconf = attrparser.parse(strconf);
			$.extend(this.options, domconf);

		},

		_bindEvents : function(){


		},

		_requestRender : function(){
			this.options.requestRendering = true;

			if (this.$el.is(':visible')){
				this._performRender();
			}
		},

		_performRender : function(){
			if (this.options.requestRendering === true){
				this.render();
				this.options.requestRendering = false;
			}
		},

		render : function(){

			var series = this.getSeries();
			var conf = this.getConf(series);

			this.draw(series, conf);

		},

		draw : function(series, conf){

			if (this.plot === undefined){
				var viewId = this.$el.attr('id');
				this.plot = $.jqplot(viewId, series, conf);
			}

			else{
				conf.data = series;
				this.plot.replot(conf);
			}

		},

		remove : function(){
			this.undelegateEvents();
			if (!! this.plot){
				this.plot.destroy();
			}
			Backbone.View.prototype.remove.call(this);
		}


	});

});
