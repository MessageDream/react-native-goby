var fs = require("fs");
var glob = require("glob");
var path = require("path");

var ignoreFolders = { ignore: ["node_modules/**", "**/build/**"] };
var buildGradlePath = path.join("android", "app", "build.gradle");
var manifestPath = glob.sync("**/AndroidManifest.xml", ignoreFolders)[0];

function findMainApplication() {
    if (!manifestPath) {
        return null;
    }

    var manifest = fs.readFileSync(manifestPath, "utf8");

    // Android manifest must include single 'application' element
    var matchResult = manifest.match(/application\s+android:name\s*=\s*"(.*?)"/);
    if (matchResult) {
        var appName = matchResult[1];
    } else {
        return null;
    }
    
    var nameParts = appName.split('.');
    var searchPath = glob.sync("**/" + nameParts[nameParts.length - 1] + ".java", ignoreFolders)[0];
    return searchPath;
}

var mainApplicationPath = findMainApplication() || glob.sync("**/MainApplication.java", ignoreFolders)[0];

// 1. Add the getJSBundleFile override
var getJSBundleFileOverride = `
    @Override
    protected String getJSBundleFile() {
      return Goby.getJSBundleFile();
    }
`;

function isAlreadyOverridden(codeContents) {
    return /@Override\s*\n\s*protected String getJSBundleFile\(\)\s*\{[\s\S]*?\}/.test(codeContents);
}

if (mainApplicationPath) {
    var mainApplicationContents = fs.readFileSync(mainApplicationPath, "utf8");
    if (isAlreadyOverridden(mainApplicationContents)) {
        console.log(`"getJSBundleFile" is already overridden`);
    } else {
        var reactNativeHostInstantiation = "new ReactNativeHost(this) {";
        mainApplicationContents = mainApplicationContents.replace(reactNativeHostInstantiation,
            `${reactNativeHostInstantiation}\n${getJSBundleFileOverride}`);
        fs.writeFileSync(mainApplicationPath, mainApplicationContents);
    }
} else {
    var mainActivityPath = glob.sync("**/MainActivity.java", ignoreFolders)[0];
    if (mainActivityPath) {
        var mainActivityContents = fs.readFileSync(mainActivityPath, "utf8");
        if (isAlreadyOverridden(mainActivityContents)) {
            console.log(`"getJSBundleFile" is already overridden`);
        } else {
            var mainActivityClassDeclaration = "public class MainActivity extends ReactActivity {";
            mainActivityContents = mainActivityContents.replace(mainActivityClassDeclaration,
                `${mainActivityClassDeclaration}\n${getJSBundleFileOverride}`);
            fs.writeFileSync(mainActivityPath, mainActivityContents);
        }
    } else {
        console.error(`Couldn't find Android application entry point. You might need to update it manually. \
Please refer to plugin configuration section for Android at \
https://github.com/buxuxiao/react-native-goby#plugin-configuration-android for more details`);
    }
}

if (!fs.existsSync(buildGradlePath)) {
    console.error(`Couldn't find build.gradle file. You might need to update it manually. \
Please refer to plugin installation section for Android at \
https://github.com/buxuxiao/react-native-goby#plugin-installation-android---manual`);
    return;
}

// 2. Add the goby.gradle build task definitions
var buildGradleContents = fs.readFileSync(buildGradlePath, "utf8");
var reactGradleLink = buildGradleContents.match(/\napply from: ["'].*?react\.gradle["']/)[0];
var gobyGradleLink = `apply from: "../../node_modules/react-native-goby/android/goby.gradle"`;
if (~buildGradleContents.indexOf(gobyGradleLink)) {
    console.log(`"goby.gradle" is already linked in the build definition`);
} else {
    buildGradleContents = buildGradleContents.replace(reactGradleLink,
        `${reactGradleLink}\n${gobyGradleLink}`);
    fs.writeFileSync(buildGradlePath, buildGradleContents);
}