var GobyWrapper = require("../gobyWrapper.js");
import Goby from "react-native-goby";

module.exports = {
    startTest: function(testApp) {
        GobyWrapper.sync(testApp, undefined, undefined,
            { installMode: Goby.InstallMode.ON_NEXT_RESTART,
                minimumBackgroundDuration: 15 });
    },
    
    getScenarioName: function() {
        return "Sync Restart Delay";
    }
};