var GobyWrapper = require("../gobyWrapper.js");
import Goby from "react-native-goby";

module.exports = {
    startTest: function(testApp) {
        GobyWrapper.sync(testApp, undefined, undefined,
            { installMode: Goby.InstallMode.ON_NEXT_RESUME,
                minimumBackgroundDuration: 5 });
    },
    
    getScenarioName: function() {
        return "Sync Resume Delay";
    }
};