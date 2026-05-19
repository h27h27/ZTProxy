ZTProxy
=======

This repository is an Android app scaffold that runs a local SOCKS5 proxy and is intended to integrate with `libzt` (ZeroTier) AAR to provide networking over ZeroTier.

What is included
- Minimal Android app using Kotlin
- `SocksProxyService` — a lightweight SOCKS5 (CONNECT-only) implementation running on port 1080
- `LibZtManager` — a stub for integrating `libzt` Java bindings from an AAR
- GitHub Actions workflow to download or build `libzt` AAR and build an APK

How to provide `libzt.aar`
- Option A (recommended): set a repository secret `LIBZT_AAR_URL` pointing to a downloadable libzt AAR (release URL or storage URL). The workflow will download it and place it into `app/libs/libzt.aar`.
- Option B: the workflow attempts to clone `https://github.com/h27h27/libzt.git` and build the AAR if a Gradle wrapper and build scripts exist. This may require changes to that repo's build configuration.
- Option C: the repo already includes `app/libs/libzt.aar` for CI and local reference.

Notes
- `LibZtManager` is a stub — replace TODOs with actual calls to the libzt Java API provided in the AAR.
- The SOCKS server is minimal (CONNECT-only, no auth). It's meant as a starting point; production code should handle IPv6 properly, implement resource cleanup, and possibly run via TUN interfaces if needed.

Building locally
1. Place `libzt.aar` into `app/libs/`.
2. Run Gradle build (Android SDK/NDK required):

```bash
./gradlew assembleRelease
```

CI
The workflow `.github/workflows/android-build.yml` will build on pushes to `main` and uploads APK artifacts.
# ZTProxy