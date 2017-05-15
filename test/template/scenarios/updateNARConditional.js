var GobyWrapper = require("../gobyWrapper.js");
import Goby from "react-native-goby";

module.exports = {
    startTest: function(testApp) {
        testApp.readyAfterUpdate((responseBody) => {
            if (responseBody !== "SKIP_NOTIFY_APPLICATION_READY") {
                Goby.notifyAppReady();
                GobyWrapper.checkAndInstall(testApp, undefined, undefined, Goby.InstallMode.ON_NEXT_RESTART);
            } else {
                testApp.setStateAndSendMessage("Skipping notifyApplicationReady!", "SKIPPED_NOTIFY_APPLICATION_READY");
            }
        });
    },
    
    getScenarioName: function() {
        return "Conditional Update";
    }
};