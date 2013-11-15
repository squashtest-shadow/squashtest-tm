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