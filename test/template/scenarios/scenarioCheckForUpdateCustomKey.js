var GobyWrapper = require("../gobyWrapper.js");

module.exports = {
    startTest: function(testApp) {
        GobyWrapper.checkForUpdate(testApp, undefined, undefined, "CUSTOM-DEPLOYMENT-KEY");
    },
    
    getScenarioName: function() {
        return "Check for Update Custom Key";
    }
};