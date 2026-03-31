# Indian Rail Kotlin Library

A lightweight, efficient, strictly typed, and **production-resilient** Kotlin library for scraping Indian Railways data. Designed for Android and JVM projects, this library allows you to search for trains, check PNR status, view live station activity, and track deep travel metrics—without needing a separate backend server.

[![JitPack](https://jitpack.io/v/ashu-choudhury/indian-rail-kotlin.svg)](https://jitpack.io/#ashu-choudhury/indian-rail-kotlin)

## 🚀 New in v1.2.0: Resilient Scraper Architecture

- **Automatic Failover**: The library now automatically pivots between multiple data sources (ConfirmTkt & Erail) to ensure maximum uptime even when individual third-party APIs change.
- **100% Strict Type Safety**: Replaced all unstructured `JsonElement` returns with dedicated Kotlin Data Classes (`PnrStatusData`, `SeatAvailabilityData`, `LiveStatusData`).
- **Production-Grade Test Suite**: Includes an exhaustive suite of **Real-Time Live Tests** that validate the library against actual Indian Railways production data every time you build.
- **Defensive Parsing**: Refactored logic leverages safe data-array retrievals (`.getOrNull()`) and `BaseResponse<T>` wrappers to eliminate runtime crashes like `IndexOutOfBoundsException`.

## Features

- **Train Search**: Find trains between stations with optional date filtering (Powered by Erail).
- **Live Train Status**: Real-time tracking of train position and delays (Powered by Erail Resiliency).
- **PNR Status**: Deep PNR tracking (Powered by ConfirmTkt scraping).
- **Seat Availability**: Check availability and fares across all classes.
- **Full Schedule**: Comprehensive multi-station stops and timings.
- **Live Station**: View upcoming trains at any station in real-time.

## Installation

### Step 1: Add the JitPack repository

In your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### Step 2: Add the dependency

```kotlin
dependencies {
    implementation("com.github.ashu-choudhury:indian-rail-kotlin:1.2.1")
}
```

## Usage

### 1. Initialize the Client

```kotlin
import com.github.ashuchoudhury.indianrail.IndianRailClient

// Default initialization uses OkHttp engine
val client = IndianRailClient()
```

### 2. Standard Query Flow with Type-Safety

All library methods return a `BaseResponse<T>`. Always check `.success` and `.data` for a safe experience.

```kotlin
suspend fun searchExample() {
    val response = client.getTrainsBetweenStations("NDLS", "SBC")
    
    if (response.success && response.data != null) {
        response.data.forEach { train ->
            println("${train.train_base.train_no}: ${train.train_base.train_name}")
        }
    } else {
        println("Error: ${response.error}")
    }
}
```

### 3. Real-Time Status Tracking (Resilient)

The library automatically scrapes the most reliable source available at runtime.

```kotlin
suspend fun trackTrain() {
    val status = client.getLiveTrainStatus("12628", "2026-03-31")
    
    if (status.success && status.data != null) {
        println("Train is currently at ${status.data.StationName}")
        println("Running Delay: ${status.data.DelayInMinutes} minutes")
    }
}
```

### 4. PNR Status Checking

```kotlin
suspend fun checkPnr() {
    val pnr = client.getPnrStatus("1234567890")
    if (pnr.success && pnr.data != null) {
        val passengers = pnr.data.PassengerStatus
        passengers.forEach { p -> println("Passenger ${p.Number}: ${p.CurrentStatus}") }
    }
}
```

## Running Tests

To run the exhaustive live test suite (requires internet connection):

```bash
./gradlew test --info
```

This will execute real-time validation against production servers for all scrapers and client workflows.

## License

This project is licensed under the MIT License.
