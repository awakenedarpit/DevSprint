# DevSprint

Your personal hackathon command centre — manage competitions, tasks, timelines, submissions, projects, and achievements.

## Android APK

This repository contains a native Android application built with Kotlin and Jetpack Compose.

Every push, pull request, or manual workflow run builds a debug APK using GitHub Actions.

1. Open the **Actions** tab in this repository.
2. Select **Build DevSprint Android APK**.
3. Open the latest successful workflow run.
4. Under **Artifacts**, download **DevSprint-debug-APK**.
5. Extract the ZIP and install the APK on your Android device.

The debug APK is intended for personal testing and sideloading. It is not a Play Store release.

## Build locally

Requirements:
- JDK 17
- Android SDK with Android 36.1 platform and Build Tools 36.0.0
- Gradle 9.3.1

Run:

```bash
gradle --no-daemon assembleDebug
```

APK output:

```
app/build/outputs/apk/debug/app-debug.apk
```

## App identity

- Display name: DevSprint
- Android application ID: `com.aistudio.hacktrack.kzqrvm`

The application ID is intentionally retained to preserve the existing Firebase Android app registration and app identity.
