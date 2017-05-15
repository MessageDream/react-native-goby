var GobyWrapper = require("../gobyWrapper.js");
import Goby from "react-native-goby";

module.exports = {
    startTest: function(testApp) {
        Goby.restartApp(true);
        GobyWrapper.checkAndInstall(testApp, 
            () => {
                Goby.restartApp(true);
            }
        );
    },
    
    getScenarioName: function() {
        return "Restart2x";
    }
};