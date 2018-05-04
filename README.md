# CS408Ninjas

## Initially created using Android 3.1.2

## Project target Android 7.0.0.

## Tested to work okay on Android 7 (api 24)


## Install OpenCV manager on an emulator
0. Get OpenCV-android-sdk from opencv site
1. Go to AppData/Local/Android/Sdk/platform-tools and run
`adb install <path to OpenCV-android-sdk>/apk/<apk file>`
Choose the apk according to your platform

## Adding OpenCV to the project quick guide
0. Get OpenCV-android-sdk from oepncv site
1. Copy OpenCV-android-sdk/sdk/native/libs to app/src/main/jniLibs
2. Import module OpenCV-android-sdk/sdk/java
3. Fix build.gradle of the library to have same versioning as the application
4. Add module dependencies in the application via Project Structure
