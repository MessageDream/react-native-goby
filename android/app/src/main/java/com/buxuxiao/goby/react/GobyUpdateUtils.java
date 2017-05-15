package com.buxuxiao.goby.react;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public class GobyUpdateUtils {

    private static void addContentsOfFolderToManifest(String folderPath, String pathPrefix, ArrayList<String> manifest) {
        File folder = new File(folderPath);
        File[] folderFiles = folder.listFiles();
        for (File file : folderFiles) {
            String fileName = file.getName();
            String fullFilePath = file.getAbsolutePath();
            String relativePath = (pathPrefix.isEmpty() ? "" : (pathPrefix + "/")) + fileName;

            if (fileName.equals(".DS_Store") || fileName.equals("__MACOSX")) {
                continue;
            } else if (file.isDirectory()) {
                addContentsOfFolderToManifest(fullFilePath, relativePath, manifest);
            } else {
                try {
                    manifest.add(relativePath + ":" + computeHash(new FileInputStream(file)));
                } catch (FileNotFoundException e) {
                    // Should not happen.
                    throw new GobyUnknownException("Unable to compute hash of update contents.", e);
                }
            }
        }
    }

    private static String computeHash(InputStream dataStream) {
        MessageDigest messageDigest = null;
        DigestInputStream digestInputStream = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            digestInputStream = new DigestInputStream(dataStream, messageDigest);
            byte[] byteBuffer = new byte[1024 * 8];
            while (digestInputStream.read(byteBuffer) != -1);
        } catch (NoSuchAlgorithmException | IOException e) {
            // Should not happen.
            throw new GobyUnknownException("Unable to compute hash of update contents.", e);
        } finally {
            try {
                if (digestInputStream != null) digestInputStream.close();
                if (dataStream != null) dataStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        byte[] hash = messageDigest.digest();
        return String.format("%064x", new java.math.BigInteger(1, hash));
    }

    public static void copyNecessaryFilesFromCurrentPackage(String diffManifestFilePath, String unzipPackageFolderPath, String currentPackageFolderPath, String newPackageFolderPath) throws IOException{
        FileUtils.copyDirectoryContents(currentPackageFolderPath, newPackageFolderPath);
        JSONObject diffManifest = GobyUtils.getJsonObjectFromFile(diffManifestFilePath);
        try {
            JSONArray deletedFiles = diffManifest.getJSONArray("deletedFiles");
            if (deletedFiles != null){
                for (int i = 0; i < deletedFiles.length(); i++) {
                    String fileNameToDelete = deletedFiles.getString(i);
                    File fileToDelete = new File(newPackageFolderPath, fileNameToDelete);
                    if (fileToDelete.exists()) {
                        fileToDelete.delete();
                    }
                }
            }

            JSONArray patchedFiles = diffManifest.getJSONArray("patchedFiles");
            if(patchedFiles != null){

                for (int i = 0; i < patchedFiles.length(); i++){
                    String fileNameToPatch = patchedFiles.getString(i);
                     String patchFilePath = GobyUtils.appendPathComponent(unzipPackageFolderPath, fileNameToPatch);
                     String patchContent = FileUtils.readFileToString(patchFilePath);

                     String bundleFilePatch = GobyUtils.appendPathComponent(newPackageFolderPath, fileNameToPatch.replaceFirst(".patch", ""));
                     String bundleContent = FileUtils.readFileToString(bundleFilePatch);

                     diff_match_patch dmp = new diff_match_patch();
                     LinkedList<diff_match_patch.Patch> patches = (LinkedList<diff_match_patch.Patch>)dmp.patch_fromText(patchContent);
                     Object[] resultArray = dmp.patch_apply(patches, bundleContent);

                     String resultBundleContent = (String)resultArray[0];

                     FileUtils.writeStringToFile(resultBundleContent, bundleFilePatch);

                     File fileToDelete = new File(patchFilePath);
                     if (fileToDelete.exists()) {
                         fileToDelete.delete();
                     }

                }
            }
        } catch (JSONException e) {
            throw new GobyUnknownException("Unable to copy files from current package during diff update", e);
        }
    }

    public static String findJSBundleInUpdateContents(String folderPath, String expectedFileName) {
        File folder = new File(folderPath);
        File[] folderFiles = folder.listFiles();
        for (File file : folderFiles) {
            String fullFilePath = GobyUtils.appendPathComponent(folderPath, file.getName());
            if (file.isDirectory()) {
                String mainBundlePathInSubFolder = findJSBundleInUpdateContents(fullFilePath, expectedFileName);
                if (mainBundlePathInSubFolder != null) {
                    return GobyUtils.appendPathComponent(file.getName(), mainBundlePathInSubFolder);
                }
            } else {
                String fileName = file.getName();
                if (fileName.equals(expectedFileName)) {
                    return fileName;
                }
            }
        }

        return null;
    }

    public static String getHashForBinaryContents(Context context, boolean isDebugMode) {
        try {
            return GobyUtils.getStringFromInputStream(context.getAssets().open(GobyConstants.GOBY_HASH_FILE_NAME));
        } catch (IOException e) {
            try {
                return GobyUtils.getStringFromInputStream(context.getAssets().open(GobyConstants.GOBY_OLD_HASH_FILE_NAME));
            } catch (IOException ex) {
                if (!isDebugMode) {
                    // Only print this message in "Release" mode. In "Debug", we may not have the
                    // hash if the build skips bundling the files.
                    GobyUtils.log("Unable to get the hash of the binary's bundled resources - \"goby.gradle\" may have not been added to the build definition.");
                }
            }
            return null;
        }
    }

    public static void verifyHashForDiffUpdate(String folderPath, String expectedHash) {
        ArrayList<String> updateContentsManifest = new ArrayList<>();
        addContentsOfFolderToManifest(folderPath, "", updateContentsManifest);
        Collections.sort(updateContentsManifest);
        JSONArray updateContentsJSONArray = new JSONArray();
        for (String manifestEntry : updateContentsManifest) {
            updateContentsJSONArray.put(manifestEntry);
        }

        // The JSON serialization turns path separators into "\/", e.g. "Goby\/assets\/image.png"
        String updateContentsManifestString = updateContentsJSONArray.toString().replace("\\/", "/");
        String updateContentsManifestHash = computeHash(new ByteArrayInputStream(updateContentsManifestString.getBytes()));
        if (!expectedHash.equals(updateContentsManifestHash)) {
            throw new GobyInvalidUpdateException("The update contents failed the data integrity check.");
        }
    }
}