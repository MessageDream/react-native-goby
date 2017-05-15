var GobyWrapper = require("../gobyWrapper.js");

module.exports = {
    startTest: function(testApp) {
        testApp.readyAfterUpdate();
    },
    
    getScenarioName: function() {
        return "Bad Update";
    }
};