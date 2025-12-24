package com.example.gonote.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * Monitors GPS/Location services status changes in real-time
 */
class LocationMonitor(private val context: Context) {
    
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    
    /**
     * Checks if GPS is currently enabled
     */
    fun isGpsEnabled(): Boolean {
        return try {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Checks if any location provider is enabled (GPS or Network)
     */
    fun isLocationEnabled(): Boolean {
        return try {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Observes GPS/Location status as a Flow
     * Emits true when location is enabled, false when disabled
     * Uses both BroadcastReceiver and periodic polling for reliability
     */
    val locationStatus: Flow<Boolean> = callbackFlow {
        val locationReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                    trySend(isLocationEnabled())
                }
            }
        }
        
        // Register the receiver
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(locationReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(locationReceiver, filter)
        }
        
        // Send initial state
        trySend(isLocationEnabled())
        
        // Periodic polling as fallback (every 2 seconds) - runs in callbackFlow scope
        val pollingJob = launch {
            while (true) {
                delay(2000)
                trySend(isLocationEnabled())
            }
        }
        
        // Unregister when flow is cancelled
        awaitClose {
            pollingJob.cancel()
            context.unregisterReceiver(locationReceiver)
        }
    }.distinctUntilChanged() // Only emit when status actually changes
}

