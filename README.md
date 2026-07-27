# Block Launcher — MCPE Version Switcher

A Kotlin + Jetpack Compose Android app that lets you keep a library of
Minecraft: Bedrock Edition (MCPE) APKs and switch between them.

## How it works

Android only allows one installed copy of a given package name at a
time, and it won't silently swap one APK for another with a different
signature. So this app wraps the normal install/uninstall flow in a
nicer UI:

1. Add Version — pick an MCPE .apk file you already have. The app
   copies it into its private storage and reads its version name/package name.
2. Switch To This — if a different build of Minecraft is currently
   installed, the app first sends you to the system uninstall dialog,
   then opens the system install dialog for the version you picked.
3. Play — once a version is the one currently installed, tapping the
   card launches it directly.

## Building it

This repo includes a GitHub Actions workflow that builds a debug APK
automatically. Check the Actions tab after all files are uploaded.
