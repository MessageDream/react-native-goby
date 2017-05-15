var GobyWrapper = require("../gobyWrapper.js");

module.exports = {
    startTest: function(testApp) {
        GobyWrapper.checkForUpdate(testApp,
            GobyWrapper.download.bind(undefined, testApp, undefined, undefined));
    },
    
    getScenarioName: function() {
        return "Download Update";
    }
};