import { AcquisitionManager as Sdk } from "./acquisition-sdk";
import { NativeEventEmitter } from "react-native";
import RestartManager from "./RestartManager";

// This function is used to augment remote and local
// package objects with additional functionality/properties
// beyond what is included in the metadata sent by the server.
module.exports = (NativeGoby) => {
  const remote = (reportStatusDownload) => {
    return {
      async download(downloadProgressCallback) {
        if (!this.downloadUrl) {
          throw new Error("Cannot download an update without a download url");
        }

        let downloadProgressSubscription;
        if (downloadProgressCallback) {
          const gobyEventEmitter = new NativeEventEmitter(NativeGoby);
          // Use event subscription to obtain download progress.
          downloadProgressSubscription = gobyEventEmitter.addListener(
            "GobyDownloadProgress",
            downloadProgressCallback
          );
        }

        // Use the downloaded package info. Native code will save the package info
        // so that the client knows what the current package version is.
        try {
          const downloadedPackage = await NativeGoby.downloadUpdate(this, !!downloadProgressCallback);
          reportStatusDownload && reportStatusDownload(this);
          return { ...downloadedPackage, ...local };
        } finally {
          downloadProgressSubscription && downloadProgressSubscription.remove();
        }
      },

      isPending: false // A remote package could never be in a pending state
    };
  };

  const local = {
    async install(installMode = NativeGoby.gobyInstallModeOnNextRestart, minimumBackgroundDuration = 0, updateInstalledCallback) {
      const localPackage = this;
      const localPackageCopy = Object.assign({}, localPackage); // In dev mode, React Native deep freezes any object queued over the bridge
      await NativeGoby.installUpdate(localPackageCopy, installMode, minimumBackgroundDuration);
      updateInstalledCallback && updateInstalledCallback();
      if (installMode == NativeGoby.gobyInstallModeImmediate) {
        RestartManager.restartApp(false);
      } else {
        RestartManager.clearPendingRestart();
        localPackage.isPending = true; // Mark the package as pending since it hasn't been applied yet
      }
    },

    isPending: false // A local package wouldn't be pending until it was installed
  };

  return { local, remote };
};