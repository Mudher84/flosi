# Flosi v1.0 — Final Build Configuration

This package replaces the previous Gradle/JDK experiments.

Pinned stack:
- Android Gradle Plugin: 8.13.2
- Gradle distribution: 8.13
- Android compileSdk/targetSdk: 36
- Android Studio runtime JDK: bundled JBR 21 is allowed
- Java source/target bytecode: 17
- Kotlin JVM target: 17
- Kotlin: 2.0.21
- KSP: 2.0.21-1.0.28

Important:
- There is NO `jvmToolchain(17)`.
- There is NO Foojay plugin.
- Gradle does not need to locate or download a separate JDK 17.
- JDK 21 runs Gradle while Java/Kotlin compile to JVM 17 bytecode.
- The project avoids Gradle 9, which was the source of the Foojay/JvmVendorSpec incompatibility.

Before opening this project, close the older v0.8/v0.9 project. Open THIS v1.0 folder as a new project.
