/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
define([], function() {
    'use strict';
    var FAVORITE_DASHBOARD_KEY = "";


    function getAllPrefs(){
        return squashtm.app.userPrefs || null;
    }

    function getPref(key) {
        var prefs = getAllPrefs(); 
        if (prefs) {
            return prefs[key] || null;
        } else {
            return {};
        }
    }

    //as of squash 1.15 workspace should be home, tc, requirement or campaign
    function getFavoriteDashboard(workspace){
        var key = "";
        
        switch (workspace) {
            case "home":
                
                break;
        
            default:
                throw ("Unknown workspace type");
        }

    }

    return {
        getAllPrefs : getAllPrefs,
        getPref : getPref
    };
});