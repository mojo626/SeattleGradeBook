This is a Kotlin Multiplatform project targeting Android, iOS, Desktop.

* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - `commonMain` is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    `iosMain` would be the right folder for such calls.

* `/iosApp` contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform, 
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.


Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…


#Useful commands

For running on an android emulator or physical device without android studio, you can first build the project with 

```
./gradlew assembleDebug
```

And then deploy to the device with 

```
adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk      
```

If you want to be able to see logs, you can use logcat and filter out everything but debug level logs from com.chrissytopher.source with

```
adb logcat com.chrissytopher.source:D "*:S"
```