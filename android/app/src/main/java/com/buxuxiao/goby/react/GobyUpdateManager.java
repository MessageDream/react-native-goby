package com.buxuxiao.goby.react;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;

public class GobyUpdateManager {

    private String mDocumentsDirectory;

    public GobyUpdateManager(String documentsDirectory) {
        mDocumentsDirectory = documentsDirectory;
    }

    private String getDownloadFilePath() {
        return GobyUtils.appendPathComponent(getGobyPath(), GobyConstants.DOWNLOAD_FILE_NAME);
    }

    private String getUnzippedFolderPath() {
        return GobyUtils.appendPathComponent(getGobyPath(), GobyConstants.UNZIPPED_FOLDER_NAME);
    }

    private String getDocumentsDirectory() {
        return mDocumentsDirectory;
    }

    private String getGobyPath() {
        String gobyPath = GobyUtils.appendPathComponent(getDocumentsDirectory(), GobyConstants.GOBY_FOLDER_PREFIX);
        if (Goby.isUsingTestConfiguration()) {
            gobyPath = GobyUtils.appendPathComponent(gobyPath, "TestPackages");
        }

        return gobyPath;
    }

    private String getStatusFilePath() {
        return GobyUtils.appendPathComponent(getGobyPath(), GobyConstants.STATUS_FILE);
    }

    public JSONObject getCurrentPackageInfo() {
        String statusFilePath = getStatusFilePath();
        if (!FileUtils.fileAtPathExists(statusFilePath)) {
            return new JSONObject();
        }

        try {
            return GobyUtils.getJsonObjectFromFile(statusFilePath);
        } catch (IOException e) {
            // Should not happen.
            throw new GobyUnknownException("Error getting current package info" , e);
        }
    }

    public void updateCurrentPackageInfo(JSONObject packageInfo) {
        try {
            GobyUtils.writeJsonToFile(packageInfo, getStatusFilePath());
        } catch (IOException e) {
            // Should not happen.
            throw new GobyUnknownException("Error updating current package info" , e);
        }
    }

    public String getCurrentPackageFolderPath() {
        JSONObject info = getCurrentPackageInfo();
        String packageHash = info.optString(GobyConstants.CURRENT_PACKAGE_KEY, null);
        if (packageHash == null) {
            return null;
        }

        return getPackageFolderPath(packageHash);
    }

    public String getCurrentPackageBundlePath(String bundleFileName) {
        String packageFolder = getCurrentPackageFolderPath();
        if (packageFolder == null) {
            return null;
        }

        JSONObject currentPackage = getCurrentPackage();
        if (currentPackage == null) {
            return null;
        }

        String relativeBundlePath = currentPackage.optString(GobyConstants.RELATIVE_BUNDLE_PATH_KEY, null);
        if (relativeBundlePath == null) {
            return GobyUtils.appendPathComponent(packageFolder, bundleFileName);
        } else {
            return GobyUtils.appendPathComponent(packageFolder, relativeBundlePath);
        }
    }

    public String getPackageFolderPath(String packageHash) {
        return GobyUtils.appendPathComponent(getGobyPath(), packageHash);
    }

    public String getCurrentPackageHash() {
        JSONObject info = getCurrentPackageInfo();
        return info.optString(GobyConstants.CURRENT_PACKAGE_KEY, null);
    }

    public String getPreviousPackageHash() {
        JSONObject info = getCurrentPackageInfo();
        return info.optString(GobyConstants.PREVIOUS_PACKAGE_KEY, null);
    }

    public JSONObject getCurrentPackage() {
        String packageHash = getCurrentPackageHash();
        if (packageHash == null) {
            return null;
        }
        
        return getPackage(packageHash);
    }
    
    public JSONObject getPreviousPackage() {
        String packageHash = getPreviousPackageHash();
        if (packageHash == null) {
            return null;
        }
        
        return getPackage(packageHash);
    }

    public JSONObject getPackage(String packageHash) {
        String folderPath = getPackageFolderPath(packageHash);
        String packageFilePath = GobyUtils.appendPathComponent(folderPath, GobyConstants.PACKAGE_FILE_NAME);
        try {
            return GobyUtils.getJsonObjectFromFile(packageFilePath);
        } catch (IOException e) {
            return null;
        }
    }

    public void downloadPackage(JSONObject updatePackage, String expectedBundleFileName,
                                DownloadProgressCallback progressCallback) throws IOException {
        String newUpdateHash = updatePackage.optString(GobyConstants.PACKAGE_HASH_KEY, null);
        String newUpdateFolderPath = getPackageFolderPath(newUpdateHash);
        String newUpdateMetadataPath = GobyUtils.appendPathComponent(newUpdateFolderPath, GobyConstants.PACKAGE_FILE_NAME);
        if (FileUtils.fileAtPathExists(newUpdateFolderPath)) {
            // This removes any stale data in newPackageFolderPath that could have been left
            // uncleared due to a crash or error during the download or install process.
            FileUtils.deleteDirectoryAtPath(newUpdateFolderPath);
        }

        String downloadUrlString = updatePackage.optString(GobyConstants.DOWNLOAD_URL_KEY, null);
        HttpURLConnection connection = null;
        BufferedInputStream bin = null;
        FileOutputStream fos = null;
        BufferedOutputStream bout = null;
        File downloadFile = null;
        boolean isZip = false;

        // Download the file while checking if it is a zip and notifying client of progress.
        try {
            URL downloadUrl = new URL(downloadUrlString);
            connection = (HttpURLConnection) (downloadUrl.openConnection());

            long totalBytes = connection.getContentLength();
            long receivedBytes = 0;

            bin = new BufferedInputStream(connection.getInputStream());
            File downloadFolder = new File(getGobyPath());
            downloadFolder.mkdirs();
            downloadFile = new File(downloadFolder, GobyConstants.DOWNLOAD_FILE_NAME);
            fos = new FileOutputStream(downloadFile);
            bout = new BufferedOutputStream(fos, GobyConstants.DOWNLOAD_BUFFER_SIZE);
            byte[] data = new byte[GobyConstants.DOWNLOAD_BUFFER_SIZE];
            byte[] header = new byte[4];

            int numBytesRead = 0;
            while ((numBytesRead = bin.read(data, 0, GobyConstants.DOWNLOAD_BUFFER_SIZE)) >= 0) {
                if (receivedBytes < 4) {
                    for (int i = 0; i < numBytesRead; i++) {
                        int headerOffset = (int)(receivedBytes) + i;
                        if (headerOffset >= 4) {
                            break;
                        }

                        header[headerOffset] = data[i];
                    }
                }

                receivedBytes += numBytesRead;
                bout.write(data, 0, numBytesRead);
                progressCallback.call(new DownloadProgress(totalBytes, receivedBytes));
            }

            if (totalBytes != receivedBytes) {
                throw new GobyUnknownException("Received " + receivedBytes + " bytes, expected " + totalBytes);
            }

            isZip = ByteBuffer.wrap(header).getInt() == 0x504b0304;
        } catch (MalformedURLException e) {
            throw new GobyMalformedDataException(downloadUrlString, e);
        } finally {
            try {
                if (bout != null) bout.close();
                if (fos != null) fos.close();
                if (bin != null) bin.close();
                if (connection != null) connection.disconnect();
            } catch (IOException e) {
                throw new GobyUnknownException("Error closing IO resources.", e);
            }
        }

        if (isZip) {
            // Unzip the downloaded file and then delete the zip
            String unzippedFolderPath = getUnzippedFolderPath();
            FileUtils.unzipFile(downloadFile, unzippedFolderPath);
            FileUtils.deleteFileOrFolderSilently(downloadFile);

            // Merge contents with current update based on the manifest
            String diffManifestFilePath = GobyUtils.appendPathComponent(unzippedFolderPath,
                    GobyConstants.DIFF_MANIFEST_FILE_NAME);
            boolean isDiffUpdate = FileUtils.fileAtPathExists(diffManifestFilePath);
            if (isDiffUpdate) {
                String currentPackageFolderPath = getCurrentPackageFolderPath();
                GobyUpdateUtils.copyNecessaryFilesFromCurrentPackage(diffManifestFilePath, unzippedFolderPath, currentPackageFolderPath, newUpdateFolderPath);
                File diffManifestFile = new File(diffManifestFilePath);
                diffManifestFile.delete();
            }

            FileUtils.copyDirectoryContents(unzippedFolderPath, newUpdateFolderPath);
            FileUtils.deleteFileAtPathSilently(unzippedFolderPath);

            // For zip updates, we need to find the relative path to the jsBundle and save it in the
            // metadata so that we can find and run it easily the next time.
            String relativeBundlePath = GobyUpdateUtils.findJSBundleInUpdateContents(newUpdateFolderPath, expectedBundleFileName);

            if (relativeBundlePath == null) {
                throw new GobyInvalidUpdateException("Update is invalid - A JS bundle file named \"" + expectedBundleFileName + "\" could not be found within the downloaded contents. Please check that you are releasing your Goby updates using the exact same JS bundle file name that was shipped with your app's binary.");
            } else {
                if (FileUtils.fileAtPathExists(newUpdateMetadataPath)) {
                    File metadataFileFromOldUpdate = new File(newUpdateMetadataPath);
                    metadataFileFromOldUpdate.delete();
                }

                if (isDiffUpdate) {
                    GobyUpdateUtils.verifyHashForDiffUpdate(newUpdateFolderPath, newUpdateHash);
                }

                GobyUtils.setJSONValueForKey(updatePackage, GobyConstants.RELATIVE_BUNDLE_PATH_KEY, relativeBundlePath);
            }
        } else {
            // File is a jsbundle, move it to a folder with the packageHash as its name
            FileUtils.moveFile(downloadFile, newUpdateFolderPath, expectedBundleFileName);
        }

        // Save metadata to the folder.
        GobyUtils.writeJsonToFile(updatePackage, newUpdateMetadataPath);
    }

    public void installPackage(JSONObject updatePackage, boolean removePendingUpdate) {
        String packageHash = updatePackage.optString(GobyConstants.PACKAGE_HASH_KEY, null);
        JSONObject info = getCurrentPackageInfo();

        String currentPackageHash = info.optString(GobyConstants.CURRENT_PACKAGE_KEY, null);
        if (packageHash != null && packageHash.equals(currentPackageHash)) {
            // The current package is already the one being installed, so we should no-op.
            return;
        }

        if (removePendingUpdate) {
            String currentPackageFolderPath = getCurrentPackageFolderPath();
            if (currentPackageFolderPath != null) {
                FileUtils.deleteDirectoryAtPath(currentPackageFolderPath);
            }
        } else {
            String previousPackageHash = getPreviousPackageHash();
            if (previousPackageHash != null && !previousPackageHash.equals(packageHash)) {
                FileUtils.deleteDirectoryAtPath(getPackageFolderPath(previousPackageHash));
            }

            GobyUtils.setJSONValueForKey(info, GobyConstants.PREVIOUS_PACKAGE_KEY, info.optString(GobyConstants.CURRENT_PACKAGE_KEY, null));
        }

        GobyUtils.setJSONValueForKey(info, GobyConstants.CURRENT_PACKAGE_KEY, packageHash);
        updateCurrentPackageInfo(info);
    }

    public void rollbackPackage() {
        JSONObject info = getCurrentPackageInfo();
        String currentPackageFolderPath = getCurrentPackageFolderPath();
        FileUtils.deleteDirectoryAtPath(currentPackageFolderPath);
        GobyUtils.setJSONValueForKey(info, GobyConstants.CURRENT_PACKAGE_KEY, info.optString(GobyConstants.PREVIOUS_PACKAGE_KEY, null));
        GobyUtils.setJSONValueForKey(info, GobyConstants.PREVIOUS_PACKAGE_KEY, null);
        updateCurrentPackageInfo(info);
    }

    public void downloadAndReplaceCurrentBundle(String remoteBundleUrl, String bundleFileName) throws IOException {
        URL downloadUrl;
        HttpURLConnection connection = null;
        BufferedInputStream bin = null;
        FileOutputStream fos = null;
        BufferedOutputStream bout = null;
        try {
            downloadUrl = new URL(remoteBundleUrl);
            connection = (HttpURLConnection) (downloadUrl.openConnection());
            bin = new BufferedInputStream(connection.getInputStream());
            File downloadFile = new File(getCurrentPackageBundlePath(bundleFileName));
            downloadFile.delete();
            fos = new FileOutputStream(downloadFile);
            bout = new BufferedOutputStream(fos, GobyConstants.DOWNLOAD_BUFFER_SIZE);
            byte[] data = new byte[GobyConstants.DOWNLOAD_BUFFER_SIZE];
            int numBytesRead = 0;
            while ((numBytesRead = bin.read(data, 0, GobyConstants.DOWNLOAD_BUFFER_SIZE)) >= 0) {
                bout.write(data, 0, numBytesRead);
            }
        } catch (MalformedURLException e) {
            throw new GobyMalformedDataException(remoteBundleUrl, e);
        } finally {
            try {
                if (bout != null) bout.close();
                if (fos != null) fos.close();
                if (bin != null) bin.close();
                if (connection != null) connection.disconnect();
            } catch (IOException e) {
                throw new GobyUnknownException("Error closing IO resources.", e);
            }
        }
    }

    public void clearUpdates() {
        FileUtils.deleteDirectoryAtPath(getGobyPath());
    }
}