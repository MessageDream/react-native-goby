var GobyWrapper = require("../gobyWrapper.js");
import Goby from "react-native-goby";

module.exports = {
    startTest: function(testApp) {
        testApp.sendCurrentAndPendingPackage()
            .then(() => {
                GobyWrapper.sync(testApp, (status) => {
                    if (status === Goby.SyncStatus.UPDATE_INSTALLED) {
                        testApp.sendCurrentAndPendingPackage().then(Goby.restartApp);
                    }
                }, undefined, { installMode: Goby.InstallMode.ON_NEXT_RESTART });
            });
    },
    
    getScenarioName: function() {
        return "Restart";
    }
};