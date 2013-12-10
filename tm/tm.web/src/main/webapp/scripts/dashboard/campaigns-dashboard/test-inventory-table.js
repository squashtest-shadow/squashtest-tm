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


define(["../basic-objects/table-view"], function(TableView){
	
	return TableView.extend({
		
		getData : function(){
			var inventory = this.model.get('iterationTestInventoryStatisticsList');
			
			var data = [],
				i = 0,
				len = inventory.length;
			for (i=0;i<len;i++){
				var m = inventory[i];
				var _nbterm = m.nbSuccess + m.nbFailure + m.nbBlocked + m.nbUntestable + m.nbWarning + m.nbError;
				var total = _nbterm + m.nbReady + m.nbRunning;
				var progress = (total>0) ? (_nbterm * 100 / total).toFixed(0) + ' %' : '0%';
				var rowdata = [
				               m.iterationName,
				               m.nbReady,
				               m.nbRunning,
				               m.nbSuccess + m.nbWarning,
				               m.nbFailure,
				               m.nbBlocked + m.nbError,
				               m.nbUntestable,
				               total,
				               progress
				               ];
				data.push(rowdata);
			}
			
			return data;
		}
	});
	
});
