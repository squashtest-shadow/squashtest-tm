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