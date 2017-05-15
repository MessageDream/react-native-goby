# React Native Module for [Goby](https://github.com/MessageDream/goby.git)

*Note: 

* This Project is forked from [react-native-code-push](https://github.com/Microsoft/react-native-code-push.git)
* This README is only relevant to the latest version of our plugin.* 


## Supported React Native platforms

- iOS (7+)
- Android (4.1+)

We try our best to maintain backwards compatability of our plugin with previous versions of React Native, but due to the nature of the platform, and the existence of breaking changes between releases, it is possible that you need to use a specific version of the Goby plugin in order to support the exact version of React Native you are using. The following table outlines which Goby plugin versions officially support the respective React Native versions:

| React Native version(s) | Supporting Goby version(s)                       |
|-------------------------|------------------------------------------------------|
| v0.43                   | v0.0.1+ *(RN refactored uimanager dependencies)*       |
| v0.44+                  | TBD :)                                               |

## Getting Started

```shell
npm install --save react-native-goby@latest
```

Then continue with installing the native module
  * [iOS Setup](docs/setup-ios.md)
  * [Android Setup](docs/setup-android.md)

## Plugin Usage

With the Goby plugin downloaded and linked, and your app asking Goby where to get the right JS bundle from, the only thing left is to add the necessary code to your app to control the following policies:

1. When (and how often) to check for an update? (e.g. app start, in response to clicking a button in a settings page, periodically at some fixed interval)

2. When an update is available, how to present it to the end user?

The simplest way to do this is to "Goby-ify" your app's root component. To do so, you can choose one of the following two options:

* **Option 1: Wrap your root component with the `goby` higher-order component:**

    ```javascript
    import goby from "react-native-goby";

    class MyApp extends Component {
    }

    MyApp = goby(MyApp);
    ```

* **Option 2: Use the [ES7 decorator](https://github.com/wycats/javascript-decorators) syntax:**

    *NOTE: Decorators are not yet supported in Babel 6.x pending proposal update.* You may need to enable it by installing and using [babel-preset-react-native-stage-0](https://github.com/skevy/babel-preset-react-native-stage-0#babel-preset-react-native-stage-0).

    ```javascript
    import goby from "react-native-goby";

    @goby
    class MyApp extends Component {
    }
    ```

By default, Goby will check for updates on every app start. If an update is available, it will be silently downloaded, and installed the next time the app is restarted (either explicitly by the end user or by the OS), which ensures the least invasive experience for your end users. If an available update is mandatory, then it will be installed immediately, ensuring that the end user gets it as soon as possible.

If you would like your app to discover updates more quickly, you can also choose to sync up with the Goby server every time the app resumes from the background.

```javascript
let gobyOptions = { checkFrequency: goby.CheckFrequency.ON_APP_RESUME };

class MyApp extends Component {
}

MyApp = goby(gobyOptions)(MyApp);
```

Alternatively, if you want fine-grained control over when the check happens (e.g. a button press or timer interval), you can call [`Goby.sync()`](docs/api-js.md#gobysync) at any time with your desired `SyncOptions`, and optionally turn off Goby's automatic checking by specifying a manual `checkFrequency`:

```javascript
let gobyOptions = { checkFrequency: goby.CheckFrequency.MANUAL };

class MyApp extends Component {
    onButtonPress() {
        goby.sync({
            updateDialog: true,
            installMode: goby.InstallMode.IMMEDIATE
        });
    }

    render() {
        <View>
            <TouchableOpacity onPress={this.onButtonPress}>
                <Text>Check for updates</Text>
            </TouchableOpacity>
        </View>
    }
}

MyApp = goby(gobyOptions)(MyApp);
```

### Android

The [Android Gradle plugin](http://google.github.io/android-gradle-dsl/current/index.html) allows you to define custom config settings for each "build type" (e.g. debug, release), which in turn are generated as properties on the `BuildConfig` class that you can reference from your Java code. This mechanism allows you to easily configure your debug builds to use your Goby staging deployment key and your release builds to use your Goby production deployment key.

To set this up, perform the following steps:

1. Open your app's `build.gradle` file (e.g. `android/app/build.gradle` in standard React Native projects)

2. Find the `android { buildTypes {} }` section and define `buildConfigField` entries for both your `debug` and `release` build types, which reference your `Staging` and `Production` deployment keys respectively. If you prefer, you can define the key literals in your `gradle.properties` file, and then reference them here. Either way will work, and it's just a matter of personal preference.

    ```groovy
    android {
        ...
        buildTypes {
            debug {
                ...
                // Note: Goby updates should not be tested in Debug mode as they are overriden by the RN packager. However, because Goby checks for updates in all modes, we must supply a key.
                buildConfigField "String", "GOBY_KEY", '""'
                ...
            }

            releaseStaging {
                ...
                buildConfigField "String", "GOBY_KEY", '"<INSERT_STAGING_KEY>"'
                ...
            }

            release {
                ...
                buildConfigField "String", "GOBY_KEY", '"<INSERT_PRODUCTION_KEY>"'
                ...
            }
        }
        ...
    }
    ```

    *NOTE: As a reminder, you can retrieve these keys by running `goby deployment ls <APP_NAME> -k` from your terminal.*

    *NOTE: The naming convention for `releaseStaging` is significant due to [this line](https://github.com/facebook/react-native/blob/e083f9a139b3f8c5552528f8f8018529ef3193b9/react.gradle#L79).*

4. Pass the deployment key to the `Goby` constructor via the build config you just defined, as opposed to a string literal.

**For React Native >= v0.29**

Open up your `MainApplication.java` file and make the following changes:

 ```java
@Override
protected List<ReactPackage> getPackages() {
     return Arrays.<ReactPackage>asList(
         ...
         new Goby(BuildConfig.GOBY_KEY, MainApplication.this, BuildConfig.DEBUG), // Add/change this line.
         ...
     );
}
 ```

*Note: If you gave your build setting a different name in your Gradle file, simply make sure to reflect that in your Java code.*

And that's it! Now when you run or build your app, your debug builds will automatically be configured to sync with your `Staging` deployment, and your release builds will be configured to sync with your `Production` deployment.

*NOTE: By default, the `react-native run-android` command builds and deploys the debug version of your app, so if you want to test out a release/production build, simply run `react-native run-android --variant release. Refer to the [React Native docs](http://facebook.github.io/react-native/docs/signed-apk-android.html#conten) for details about how to configure and create release builds for your Android apps.*

If you want to be able to install both debug and release builds simultaneously on the same device (highly recommended!), then you need to ensure that your debug build has a unique identity and icon from your release build. Otherwise, neither the OS nor you will be able to differentiate between the two. You can achieve this by performing the following steps:

1. In your `build.gradle` file, specify the [`applicationIdSuffix`](http://google.github.io/android-gradle-dsl/current/com.android.build.gradle.internal.dsl.BuildType.html#com.android.build.gradle.internal.dsl.BuildType:applicationIdSuffix) field for your debug build type, which gives your debug build a unique identity for the OS (e.g. `com.foo` vs. `com.foo.debug`).

```groovy
buildTypes {
    debug {
        applicationIdSuffix ".debug"
    }
}
```

2. Create the `app/src/debug/res` directory structure in your app, which allows overriding resources (e.g. strings, icons, layouts) for your debug builds

3. Create a `values` directory underneath the debug res directory created in #2, and copy the existing `strings.xml` file from the `app/src/main/res/values` directory

4. Open up the new debug `strings.xml` file and change the `<string name="app_name">` element's value to something else (e.g. `foo-debug`). This ensures that your debug build now has a distinct display name, so that you can differentiate it from your release build.

5. Optionally, create "mirrored" directories in the `app/src/debug/res` directory for all of your app's icons that you want to change for your debug build. This part isn't technically critical, but it can make it easier to quickly spot your debug builds on a device if its icon is noticeable different.

And that's it! View [here](http://tools.android.com/tech-docs/new-build-system/resource-merging) for more details on how resource merging works in Android.

### iOS

Xcode allows you to define custom build settings for each "configuration" (e.g. debug, release), which can then be referenced as the value of keys within the `Info.plist` file (e.g. the `GobyDeploymentKey` setting). This mechanism allows you to easily configure your builds to produce binaries, which are configured to synchronize with different Goby deployments.

To set this up, perform the following steps:

1. Open up your Xcode project and select your project in the `Project navigator` window

2. Ensure the project node is selected, as opposed to one of your targets

3. Select the `Info` tab

4. Click the `+` button within the `Configurations` section and select `Duplicate "Release" Configuration`

5. Name the new configuration `Staging` (or whatever you prefer)

6. Select the `Build Settings` tab

7. Go to `Build Location -> Per-configuration Build Products Path -> Staging` and change `Staging` value from `$(BUILD_DIR)/$(CONFIGURATION)$(EFFECTIVE_PLATFORM_NAME)` to `$(BUILD_DIR)/Release$(EFFECTIVE_PLATFORM_NAME)`

   *NOTE: Due to https://github.com/facebook/react-native/issues/11813, we have to do this step to make it possible to use other configurations than Debug or Release on RN 0.40.0 or higher.*

8. Click the `+` button on the toolbar and select `Add User-Defined Setting`

9. Name this new setting something like `GOBY_KEY`, expand it, and specify your `Staging` deployment key for the `Staging` config and your `Production` deployment key for the `Release` config.

    *NOTE: As a reminder, you can retrieve these keys by running `code-push deployment ls <APP_NAME> -k` from your terminal.*

10. Open your project's `Info.plist` file and change the value of your `GobyDeploymentKey` entry to `$(GOBY_KEY)`

And that's it! Now when you run or build your app, your staging builds will automatically be configured to sync with your `Staging` deployment, and your release builds will be configured to sync with your `Production` deployment.

---

## API Reference

* [JavaScript API](docs/api-js.md)
* [Objective-C API Reference (iOS)](docs/api-ios.md)
* [Java API Reference (Android)](docs/api-android.md)
