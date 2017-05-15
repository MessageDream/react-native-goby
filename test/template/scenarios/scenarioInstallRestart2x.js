var GobyWrapper = require("../gobyWrapper.js");
import Goby from "react-native-goby";

module.exports = {
    startTest: function(testApp) {
        GobyWrapper.checkAndInstall(testApp, 
            () => {
                Goby.restartApp();
                Goby.restartApp();
            }
        );
    },
    
    getScenarioName: function() {
        return "Install and Restart 2x";
    }
};