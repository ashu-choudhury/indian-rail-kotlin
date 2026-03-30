# Indian Rail Kotlin Library

A lightweight, efficient, and type-safe Kotlin library for scraping Indian Railways data. Designed for Android and JVM projects, this library allows you to search for trains, check PNR status, view live station activity, and more—without needing a separate HTTP server.

[![JitPack](https://jitpack.io/v/ashu-choudhury/indian-rail-kotlin.svg)](https://jitpack.io/#ashu-choudhury/indian-rail-kotlin)

## Features

- **Train Search**: Find trains between stations with optional date filtering.
- **Smart Date Filtering**: Automatically filters trains based on their running days for a specific date.
- **Detailed Train Info**: Get specifics like training name, speed, distance, and running days.
- **Full Route**: Fetch comprehensive stop-by-stop route information.
- **Live Train Status**: Real-time tracking of exactly where the train is currently situated.
- **Seat Availability & Fare**: Check availability across classes (SL, 3A, 2A) and quotas.
- **Full Schedule**: Detailed schedule including platform info and station-wise distances.
- **PNR Status**: Get real-time updates on your booking via ConfirmTkt.
- **Android Friendly**: Built with Ktor (OkHttp) for low memory footprint and high performance.

## Installation

### Step 1: Add the JitPack repository to your build file

In your `settings.gradle.kts` (recommended for modern projects):

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

Or in your root `build.gradle`:

```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

### Step 2: Add the dependency

```kotlin
dependencies {
    implementation("com.github.ashu-choudhury:indian-rail-kotlin:1.0.0")
}
```

## Usage

### 1. Initialize the Client

```kotlin
import com.github.ashuchoudhury.indianrail.IndianRailClient

val client = IndianRailClient()
```

### 2. Search Trains Between Stations

```kotlin
// Basic search
val response = client.getTrainsBetweenStations("NDLS", "HWH")
if (response.success) {
    response.data.forEach { combined ->
        println("${combined.train_base.train_no}: ${combined.train_base.train_name}")
    }
}

// Search for a specific date (handles running days automatically)
val responseOnDate = client.getTrainsBetweenStationsOnDate("NDLS", "HWH", "25-12-2026")
```

### 3. Get Train Route

```kotlin
val routeResponse = client.getTrainRoute("12301")
if (routeResponse.success) {
    routeResponse.data.forEach { stop ->
        println("${stop.source_stn_name}: Arr ${stop.arrive}, Dep ${stop.depart}")
    }
}
```

### 4. Live Train Status

```kotlin
// Date format: YYYY-MM-DD (The date the train started its journey)
val liveStatus = client.getLiveTrainStatus("12301", "2026-03-30")
if (liveStatus.success) {
    println("Current Status: ${liveStatus.data}")
}
```

### 5. Seat Availability

```kotlin
// Check SL class, General quota
val availability = client.getSeatAvailability("12301", "NDLS", "HWH", "2026-04-15")
// Or specify class and quota
val avail2 = client.getSeatAvailability("12301", "NDLS", "HWH", "2026-04-15", "GN", "3A")
```

### 6. Full Train Schedule

```kotlin
val schedule = client.getFullTrainSchedule("12301")
```

### 7. Check PNR Status

```kotlin
val liveResponse = client.getLiveStation("DNR")
if (liveResponse.success) {
    liveResponse.data.forEach { train ->
        println("${train.train_no}: ${train.train_name} arriving at ${train.time_at}")
    }
}
```

## Memory & Performance

- The library uses **Coroutines** for non-blocking I/O.
- **Ktor (OkHttp)** engine is used for efficient network handling on Android.
- **Jsoup** is used sparingly for HTML parsing where structured data isn't available.

## License

This project is licensed under the MIT License.
