# To-Do Application

An Android task manager built with Kotlin and Jetpack Compose, with account registration and sign-in backed by a Spring Boot API and PostgreSQL.

## Features

- Register with an email address and password, sign in, and log out
- Store password hashes in PostgreSQL and authenticate API requests with expiring JWT sessions
- Revoke the current API session when logging out
- Add, complete, remove, and filter tasks
- See a completion progress bar and remaining task count
- Keep each account's task list saved locally on the device

## Technology

- Android: Kotlin, Jetpack Compose, Material 3
- Backend: Java 17+, Spring Boot 4, Spring Security, Spring Data JPA
- Database: PostgreSQL
- Session tokens: signed JWTs

## Run the Android app

1. Open the repository in Android Studio and sync Gradle.
2. Set `BASE_URL` in [AuthClient.kt](app/src/main/java/com/example/myapplication1/AuthClient.kt) to your Spring Boot API's HTTPS address.
3. Select an emulator or connected Android device and click **Run**.

The app requires Android 7.0 (API 24) or newer.

## Run the backend locally

Requirements: Java 17 or newer, Maven, and Docker Compose.

1. Start PostgreSQL from the repository root:

   ```bash
   docker compose -f backend/docker-compose.yml up -d
   ```

2. Set a private JWT signing secret in your shell. Generate a value with:

   ```bash
   openssl rand -base64 32
   ```

   Copy the generated value, then export it:

   ```bash
   export JWT_SECRET="paste-generated-value-here"
   ```

3. Start the API:

   ```bash
   mvn -f backend/pom.xml spring-boot:run
   ```

The local PostgreSQL compose credentials are for development only. Use HTTPS and private database/JWT credentials for any deployed environment. The Android client is configured to use HTTPS; point it at an HTTPS address that can reach your API.

## Build the Android APK

From the repository root:

```bash
./gradlew assembleDebug
```

The APK is created at `app/build/outputs/apk/debug/app-debug.apk`.
