var GobyWrapper = require("../gobyWrapper.js");
import Goby from "react-native-goby";

module.exports = {
    startTest: function(testApp) {
        testApp.readyAfterUpdate();
        Goby.notifyAppReady();
    },
    
    getScenarioName: function() {
        return "Good Update";
    }
};