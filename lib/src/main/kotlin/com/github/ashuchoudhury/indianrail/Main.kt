package com.github.ashuchoudhury.indianrail

import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val client = IndianRailClient()
    
    println("Fetching trains between New Delhi (NDLS) and Howrah (HWH)...")
    val trains = client.getTrainsBetweenStations("NDLS", "HWH")
    if (trains.success) {
        println("Found ${trains.data.size} trains.")
        trains.data.take(5).forEach { 
            println("- ${it.train_base.train_no}: ${it.train_base.train_name} (${it.train_base.from_time} -> ${it.train_base.to_time})")
        }
    } else {
        println("Failed to fetch trains.")
    }

    println("\nFetching route for 12301 (Rajdhani Express)...")
    val route = client.getTrainRoute("12301")
    if (route.success) {
        println("Route found with ${route.data.size} stops.")
        route.data.take(5).forEach {
            println("- ${it.source_stn_name} (${it.source_stn_code}): ARR ${it.arrive} DEP ${it.depart}")
        }
    } else {
        println("Failed to fetch route.")
    }

    client.close()
}
