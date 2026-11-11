# To-Do Application

A simple Android to-do list app built with Kotlin and Jetpack Compose. Add tasks, track your progress, and keep your list saved on your device.

## Features

- Add tasks and remove them when they are no longer needed
- Mark tasks as complete
- Filter the list by all tasks, tasks to do, or completed tasks
- View a progress bar and remaining task count
- Save tasks and their completion status locally between app launches

## Built with

- Kotlin
- Jetpack Compose
- Material 3
- Android Gradle Plugin

## Requirements

- Android Studio
- Android SDK 37
- Android 7.0 (API 24) or newer to run the app

## Run the app

1. Clone this repository and open the project in Android Studio.
2. Let Gradle sync.
3. Select an emulator or connected Android device.
4. Click **Run**.

You can also build a debug APK from the project root:

```bash
./gradlew assembleDebug
```

The APK is created at `app/build/outputs/apk/debug/app-debug.apk`.
