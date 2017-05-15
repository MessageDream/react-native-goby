var GobyWrapper = require("../gobyWrapper.js");

module.exports = {
    startTest: function(testApp) {
        GobyWrapper.checkForUpdate(testApp);
    },
    
    getScenarioName: function() {
        return "Check for Update";
    }
};