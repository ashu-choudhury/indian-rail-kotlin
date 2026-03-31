package com.github.ashuchoudhury.example

import com.github.ashuchoudhury.indianrail.IndianRailClient
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun main() {
    println("--- Indian Rail Kotlin Example Client ---")
    val client = IndianRailClient()

    runBlocking {
        try {
            // 1. Search for trains between stations
            val fromSource = "NDLS" // New Delhi
            val toDestination = "SBC" // Bengaluru
            println("\nSearching for trains from $fromSource to $toDestination...")
            
            val searchResponse = client.getTrainsBetweenStations(fromSource, toDestination)
            
            if (searchResponse.success && searchResponse.data != null) {
                val trains = searchResponse.data!!
                println("Found ${trains.size} trains:")
                
                // Display first 5 trains
                trains.take(5).forEach { combined ->
                    val t = combined.train_base
                    println("  - [${t.train_no}] ${t.train_name} (${t.from_time} -> ${t.to_time})")
                }

                // 2. Track the live status of the first result
                val firstTrain = trains.first().train_base.train_no
                val date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                
                println("\nTracking Live Status for $firstTrain on $date...")
                val liveResponse = client.getLiveTrainStatus(firstTrain, date)
                
                if (liveResponse.success && liveResponse.data != null) {
                    val status = liveResponse.data!!
                    println("SUCCESS: ${status.TrainNo} is currently at ${status.StationName}")
                    println("  Delay: ${status.DelayInMinutes} minutes")
                    println("  Updated at: ${status.UpdateTime}")
                } else {
                    println("FAILED to track status: ${liveResponse.error}")
                }

            } else {
                println("FAILED to find trains: ${searchResponse.error}")
            }

            // 3. Check PNR (with a dummy or known PNR)
            val pnr = "1234567890" // Example invalid PNR
            println("\nChecking PNR: $pnr...")
            val pnrResponse = client.getPnrStatus(pnr)
            if (pnrResponse.success) {
                println("PNR Info: ${pnrResponse.data}")
            } else {
                println("Expected failure for invalid PNR: ${pnrResponse.error}")
            }

        } catch (e: Exception) {
            println("Unexpected Error: ${e.message}")
        } finally {
            client.close()
            println("\n--- Example Finished ---")
        }
    }
}
