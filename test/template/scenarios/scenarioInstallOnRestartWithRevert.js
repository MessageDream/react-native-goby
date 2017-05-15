var GobyWrapper = require("../gobyWrapper.js");
import Goby from "react-native-goby";

module.exports = {
    startTest: function(testApp) {
        GobyWrapper.checkAndInstall(testApp, undefined, undefined, Goby.InstallMode.ON_NEXT_RESTART);
    },
    
    getScenarioName: function() {
        return "Install on Restart with Revert";
    }
};