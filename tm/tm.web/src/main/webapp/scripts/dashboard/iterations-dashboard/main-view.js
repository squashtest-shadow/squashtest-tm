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


define(["jquery", 'squash.attributeparser',  
        "./nonexecuted-testcase-importance-pie",
        "./testcase-status-pie",
        "dashboard/SuperMasterView"],
        function($, attrparser, ImportancePie, StatusPie, SuperMasterView){
	
	return SuperMasterView.extend({
		
		initViews : function(master){
			var self = this;
			var views = [
							
				new ImportancePie({
					 el : "#dashboard-nonexecuted-testcase-importance",
					model : self.bbModel
				}),
				
				new StatusPie({
					 el : "#dashboard-testcase-status",
					model : self.bbModel
				})
							
			];
			self.views = $.merge( self.getBasicViews(), views) ;
		}
	});
	
});
