#!/bin/bash

# Copyright (c) 2015-present, MessageDream.
# All rights reserved.
#
# This source code is licensed under the BSD-style license found in the
# LICENSE file in the root directory of this source tree. An additional grant
# of patent rights can be found in the PATENTS file in the same directory.

echo 'Goby + RN sample app generation script';
echo

rm -rf testapp_rn
 
echo '************************ Configuration ***********************************';

####################  Configure versions  #################################################

read -p "Enter React Native version (default: latest):" react_native_version
read -p "Enter Goby version (default: latest): " react_native_goby_version

echo

if [ ! $react_native_version]; then
	react_native_version=`npm view react-native version`
fi
echo 'React Native version: ' + $react_native_version

if [ ! $react_native_goby_version ]; then
	react_native_goby_version=`npm view react-native-goby version`
fi
echo 'React Native Code Push version: ' + $react_native_goby_version
echo

####################  Create app  #########################################################

echo '********************* Creating app ***************************************';

current_dir=`pwd`;
echo 'Current directory: ' + $current_dir;

echo 'Create testapp_rn app';
rninit init testapp_rn --source react-native@$react_native_version

cd testapp_rn

echo 'Install React Native Code Push Version $react_native_goby_version' 
npm install --save react-native-goby@$react_native_goby_version

echo 'react native link to react native code push'
react-native link react-native-goby

rm index.android.js
rm index.ios.js
cp ../GobyDemoApp/*js .
mkdir images
cp ../GobyDemoApp/images/* images

# Make changes required to test Goby in debug mode (see OneNote)
sed -ie '162s/AppRegistry.registerComponent("GobyDemoApp", () => GobyDemoApp);/AppRegistry.registerComponent("testapp_rn", () => GobyDemoApp);/' demo.js
perl -i -p0e 's/#ifdef DEBUG.*?#endif/jsCodeLocation = [Goby bundleURL];/s' ios/testapp_rn/AppDelegate.m
sed -ie '17,20d' node_modules/react-native/packager/react-native-xcode.sh
sed -ie '90s/targetName.toLowerCase().contains("release")/true/' node_modules/react-native/react.gradle
