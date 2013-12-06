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

define(["../basic-objects/donut-view"], function(DonutView){

	return DonutView.extend({
		
		render : function(){
			
			if (! this.model.isAvailable()){
				return;
			}
			
			DonutView.prototype.render.call(this);
			this._renderSubplot();
		},
		
		_renderSubplot : function(){

			var model = this.model.get('campaignTestCaseSuccessRateStatistics');
			
			var totalSuccess = this._sumAllSuccess(model),
				totalFailures = this._sumAllFailures(model),
				total = totalSuccess + totalFailures + this._sumAllOther(model);
			
			var percentSuccess = (total !== 0 ) ? totalSuccess * 100 / total : 0,
				percentFailures = (total !== 0) ? totalFailures * 100 / total : 0;
			
			this.$el.find('.success-rate-total-success').text(percentSuccess.toFixed(0)+'%');
			this.$el.find('.success-rate-total-failure').text(percentFailures.toFixed(0)+'%');			
		},
		
		_sumAllSuccess : function(model){
			return model.nbVeryHighSuccess + model.nbHighSuccess  + model.nbMediumSuccess + model.nbLowSuccess;
		},
		
		_sumAllFailures : function(model){
			return model.nbVeryHighFailure + model.nbHighFailure + model.nbMediumFailure + model.nbLowFailure;
		},
		
		_sumAllOther : function(model){
			return model.nbVeryHighOther + model.nbHighOther + model.nbMediumOther + model.nbLowOther;
		},
		
		getSeries : function(){
			var stats = this.model.get('campaignTestCaseSuccessRateStatistics');
			return [ [["",stats.nbVeryHighSuccess], ["",stats.nbVeryHighFailure], ["",stats.nbVeryHighOther]],
					 [["",stats.nbHighSuccess], ["",stats.nbHighFailure], ["",stats.nbHighOther]], 
					 [["",stats.nbMediumSuccess], ["",stats.nbMediumFailure], ["",stats.nbMediumOther]], 
			         [["",stats.nbLowSuccess], ["",stats.nbLowFailure], ["",stats.nbLowOther]]
			];
		}
	});
});