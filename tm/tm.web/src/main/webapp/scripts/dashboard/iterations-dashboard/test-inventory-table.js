/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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

define(["../basic-objects/table-view", "squash.translator"], function(TableView, translator){
	
	return TableView.extend({
		
		getData : function(){
			var inventory = this.model.get('testsuiteTestInventoryStatisticsList');
			
			var data = [],
				i = 0,
				len = inventory.length;
			
			if (len > 0){
				var totals = [
				              translator.get('dashboard.meta.labels.total'),
				              0, // total
				              0, // to execute
				              0, // executed
				              0, // ready
				              0, // running
				              0, // success
				              0, // failure
				              0, // blocked
				              0, // untestable
				              0, // progress
				              0, // pc success
				              0, // pc failure
				              0, // pc prev
				              0, // difference prev vs execute
				              0, // very high
				              0, // high
				              0, // medium
				              0  // low
				];
				
				for (i=0;i<len;i++){
					var m = inventory[i];
					var rowdata = [
					               m.testsuiteName,
					               m.nbTotal,
					               m.nbToExecute,
					               m.nbExecuted,
					               m.nbReady,
					               m.nbRunning,
					               m.nbSuccess,
					               m.nbFailure,
					               m.nbBlocked,
					               m.nbUntestable,
					               m.pcProgress,
					               m.pcSuccess,
					               m.pcFailure,
					               m.pcPrevProgress,
					               m.nbPrevToExecute,
					               m.nbVeryHigh,
					               m.nbHigh,
					               m.nbMedium,
					               m.nbLow
					               ];
					data.push(rowdata);
				
					// update th totals
					for (var j=1; j<19; j++){
						totals[j] += rowdata[j];
					}
					
					// for the percentage we'd rather save a weighted value
					totals[10] = rowdata[10] * m.nbTotal;
					totals[11] = rowdata[11] * m.nbTotal;
					totals[12] = rowdata[12] * m.nbTotal;
					totals[13] = rowdata[13] * m.nbTotal;
					
				}
				
				// finalize the totals and add them to the data, mostly the percentages
				var total = totals[1];
				
				totals[10] = (total>0) ? (totals[10] / total).toFixed(0) : 0.0;
				totals[11] = (total>0) ? (totals[11] / total).toFixed(0) : 0.0;
				totals[12] = (total>0) ? (totals[12] / total).toFixed(0) : 0.0;
				totals[13] = (total>0) ? (totals[13] / total).toFixed(0) : 0.0;
				
				data.push(totals);

				
			}
			
			return data;
		}
	});
	
});
