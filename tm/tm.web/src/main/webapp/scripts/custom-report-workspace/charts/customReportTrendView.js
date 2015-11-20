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
 * Subclasses must implement
 * - method getSeries(), as per contract defined in abstractCustomReportChart,
 * - the method getCategories (aka the labels of the axes) -> array of String
 */

//TODO : move to dashboard/basic-objects when ready
define(["jquery", "./abstractCustomReportChart",
        "jqplot-core",  "jqplot-category", "jqplot-bar","jqplot-legend"],
		function($, JqplotView){

	return JqplotView.extend({

		getCategories : function(){
			throw "attempted to create an abstract LineView !";
		},

    getConf : function(series){

			var ticks = this.getCategories();
      var axis = this.getAxis()[0];
      ticks = this.replaceInfoListDefaultLegend(ticks,axis);

      var legends = this.getSeriesLegends();
      var axis2 = this.getAxis()[1];

      var formatedLegends = this.replaceInfoListDefaultLegend(legends,axis2);

      formatedLegends = this.objectifyLegend(formatedLegends);
      var sizeDependantconf = this.getResizeConf(formatedLegends,ticks);

			return _.extend(this.getCommonConf(),{
				seriesDefaults : {
					rendererOptions : {
            smooth: false
					}
				},
        series: formatedLegends,

        legend : sizeDependantconf.legend,

				axes : {
					xaxis : {
						renderer : $.jqplot.CategoryAxisRenderer,
						ticks : ticks,
            tickOptions: {
              fontSize : sizeDependantconf.fontSize
            }
					},
          yaxis: {
            tickOptions: {
              fontSize : sizeDependantconf.fontSize
            }
          }

				},

				grid : {
          drawGridlines : false,
					background : '#FFFFFF',
					drawBorder : false,
					borderColor : 'transparent',
					shadow : false,
					shadowColor : 'transparent'
				}
			});

		}

	});
});
