/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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


define(["jquery", "require", "./entity-manager.js"], function($, require){

	
	var EntityManager = require("./entity-manager.js");

	function makeTCConf(conf){		
		return $.extend({},conf.general, conf.tcSettings);
	};
	
	function makeReqConf(conf){
		return $.extend({},conf.general, conf.reqSettings);
	};
	
	function makeCampConf(conf){
		return $.extend({},conf.general, conf.campSettings);
	};
	
	function makeIterConf(conf){
		return $.extend({},conf.general, conf.iterSettings);
	};
	
	function makeTSConf(conf){
		return $.extend({},conf.general, conf.tsSettings);
	};

	var manager = {
		
		setConfig : function(conf){
			this.config = conf;
			return this;
		},
		
		init : function(){
		
			//test case
			var tcConf = makeTCConf(this.config);
			new EntityManager(tcConf);
			
			//requirement
			var reqConf = makeReqConf(this.config);
			new EntityManager(reqConf);
			
			//campaign
			var campConf = makeCampConf(this.config);
			new EntityManager(campConf);
			
			//iterartion
			var iterConf = makeIterConf(this.config);
			new EntityManager(iterConf);
			
			//test suite
			var tsConf = makeTSConf(this.config);
			new EntityManager(tsConf);
			
		}
	};
	
	return manager;
	
});
	
