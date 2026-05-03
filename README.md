# FocusGuard++

FocusGuard++ is an Android focus assistant that detects the foreground app, blocks distracting apps with an overlay, and stores restricted apps locally with Room.

> Detects foreground app to help you stay focused by blocking distracting apps.

## What It Does

- Monitors foreground app changes through an AccessibilityService.
- Lets the user select installed apps to restrict.
- Uses an overlay challenge to pause access to restricted apps.
- Stores restricted-app data locally with Room.
- Requests the required Android permissions for usage stats, overlays, and accessibility.

## Tech Stack

- Kotlin
- Jetpack Compose
- Room
- AccessibilityService
- Foreground Service

## Future Scope

This can grow into a startup-ready full-stack productivity platform with a broader product vision:

- User accounts with cloud sync so restrictions follow the user across devices.
- A web admin dashboard for analytics, schedules, and focus-policy management.
- Team and organization plans for schools, startups, and remote teams.
- AI-powered distraction insights that recommend better focus windows.
- Smart rules based on time, location, calendar events, and usage patterns.
- Cross-platform support for Android, iOS, web, and desktop clients.
- Subscription billing, family plans, and enterprise policy controls.
- Push notifications, progress streaks, and gamified focus goals.

## Local Development

Build the Android app with Gradle from the project root:

```bash
./gradlew assembleDebug
```

On Windows, use:

```powershell
gradlew.bat assembleDebug
```

## Notes

The project currently focuses on the Android client experience. A full startup-scale version would add backend APIs, authentication, analytics, and multi-device sync.