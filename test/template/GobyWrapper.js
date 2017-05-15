import Goby from "react-native-goby";

// This module wraps Goby API calls to add test message callbacks to every function for simpler test code.

module.exports = {
    checkForUpdate: function(testApp, onSuccess, onError, deploymentKey) {
        return Goby.checkForUpdate(deploymentKey)
            .then((remotePackage) => {
                return testApp.checkUpdateSuccess(remotePackage).then(() => { return onSuccess && onSuccess(remotePackage); });
            }, (error) => {
                return testApp.checkUpdateError(error).then(() => { return onError && onError(error); });
            });
    },
    
    download: function(testApp, onSuccess, onError, remotePackage) {
        return remotePackage.download()
            .then((localPackage) => {
                return testApp.downloadSuccess(localPackage).then(() => { return onSuccess && onSuccess(localPackage); });
            }, (error) => {
                return testApp.downloadError(error).then(() => { return onError && onError(error); });
            });
    },
    
    install: function(testApp, onSuccess, onError, installMode, minBackgroundDuration, localPackage) {
        return localPackage.install(installMode, minBackgroundDuration)
            .then(() => {
                // Since immediate installs cannot be reliably logged (due to async network calls), we only log "UPDATE_INSTALLED" if it is a resume or restart update.
                if (installMode !== Goby.InstallMode.IMMEDIATE) return testApp.installSuccess().then(() => { return onSuccess && onSuccess(); });
                return onSuccess && onSuccess();
            }, () => {
                return testApp.installError().then(() => { return onError && onError(); });
            });
    },
    
    checkAndInstall: function(testApp, onSuccess, onError, installMode, minBackgroundDuration) {
        var installUpdate = this.install.bind(this, testApp, onSuccess, onError, installMode, minBackgroundDuration);
        var downloadUpdate = this.download.bind(this, testApp, installUpdate, onError);
        return this.checkForUpdate(testApp, downloadUpdate, onError);
    },
    
    sync: function(testApp, onSyncStatus, onSyncError, options) {
        return Goby.checkForUpdate()
            .then(
                (remotePackage) => {
                    // Since immediate installs cannot be reliably logged (due to async network calls), we don't log "UPDATE_INSTALLED" when the installation is immediate.
                    // However, to determine this, we must first figure out whether or not the package is mandatory because mandatory packages use a different install mode than regular updates.
                    // This requires an additional call to checkForUpdate before syncing.
                    var regularUpdateIsImmediate = options && options.installMode === Goby.InstallMode.IMMEDIATE;
                    var mandatoryUpdateIsImmediate = !options || (options && (!options.mandatoryInstallMode || options.mandatoryInstallMode === Goby.InstallMode.IMMEDIATE));
                    var isInstallImmediate = (remotePackage && remotePackage.isMandatory) ? mandatoryUpdateIsImmediate : regularUpdateIsImmediate;
                    
                    return Goby.sync(options)
                        .then((status) => {
                            if (!(isInstallImmediate && status === Goby.SyncStatus.UPDATE_INSTALLED)) {
                                return testApp.onSyncStatus(status).then(() => { return onSyncStatus(status); });
                            }
                            return onSyncStatus(status);
                        }, (error) => {
                            return testApp.onSyncError(error).then(() => { return onSyncError(error); });
                        });
                },
                (error) => {
                    return Goby.sync(options)
                        .then((status) => {
                            // Should fail because the check for update failed, so no need to check whether the install is immediate.
                            return testApp.onSyncStatus(status).then(() => { return onSyncStatus(status); });
                        }, (error) => {
                            return testApp.onSyncError(error).then(() => { return onSyncError(error); });
                        });
                }
            );
    }
}