var GobyWrapper = require("../gobyWrapper.js");
import Goby from "react-native-goby";

module.exports = {
    startTest: function(testApp) {
        GobyWrapper.checkAndInstall(testApp, undefined, undefined, Goby.InstallMode.IMMEDIATE);
    },
    
    getScenarioName: function() {
        return "Install with Revert";
    }
};