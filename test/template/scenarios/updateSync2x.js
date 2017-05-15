var GobyWrapper = require("../gobyWrapper.js");

module.exports = {
    startTest: function(testApp) {
        testApp.readyAfterUpdate();
        GobyWrapper.sync(testApp);
        GobyWrapper.sync(testApp);
    },
    
    getScenarioName: function() {
        return "Good Update (w/ Sync 2x)";
    }
};