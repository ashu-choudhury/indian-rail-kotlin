plugins {
    // this is necessary to avoid the plugins being loaded multiple times
    // in each subproject's classloader
    kotlin("jvm") version "2.0.20" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
}

allprojects {
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
