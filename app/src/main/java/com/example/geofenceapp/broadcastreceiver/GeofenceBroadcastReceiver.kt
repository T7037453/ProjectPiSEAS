package com.example.geofenceapp.broadcastreceiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.geofenceapp.R
import com.example.geofenceapp.data.GeofenceDao
import com.example.geofenceapp.data.GeofenceDatabase
import com.example.geofenceapp.data.GeofenceEntity
import com.example.geofenceapp.data.GeofenceRepository
import com.example.geofenceapp.util.Constants.NOTIFICATION_CHANNEL_ID
import com.example.geofenceapp.util.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.geofenceapp.util.Constants.NOTIFICATION_ID
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

private lateinit var databaseref: DatabaseReference
private lateinit var mFirebase: FirebaseDatabase

class GeofenceBroadcastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if(geofencingEvent.hasError()){
            val errorMessage = GeofenceStatusCodes
                .getStatusCodeString(geofencingEvent.errorCode)
            Log.e("BroadcastReceiver", errorMessage)
            return
        }
        when(geofencingEvent.geofenceTransition){
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                mFirebase = FirebaseDatabase.getInstance("https://projectpiseas-default-rtdb.europe-west1.firebasedatabase.app/")
                databaseref = mFirebase.getReference("messages")


                if (geofencingEvent.triggeringLocation.latitude > 54.7 && geofencingEvent.triggeringLocation.latitude < 54.8){
                    Log.d("BroadcastReceiver", "Geofence ENTER HOME")
                    displayNotification(context, "Geofence ENTER HOME")
                    databaseref.push().setValue("ENTER HOME")
                }
                else{
                    Log.d("BroadcastReceiver", "Geofence ENTER")
                    displayNotification(context, "Geofence ENTER")
                    databaseref.push().setValue("ENTER")
                }
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                if (geofencingEvent.triggeringLocation.latitude > 54.7 && geofencingEvent.triggeringLocation.latitude < 54.8){
                    Log.d("BroadcastReceiver", "Geofence EXIT HOME")
                    displayNotification(context, "Geofence EXIT HOME")
                    databaseref.push().setValue("EXIT HOME")
                }
                else{
                    Log.d("BroadcastReceiver", "Geofence EXIT")
                    displayNotification(context, "Geofence EXIT")
                    databaseref.push().setValue("EXIT")
                }


            }
            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                Log.d("BroadcastReceiver", "Geofence DWELL")
                displayNotification(context, "Geofence DWELL")
            }
            else -> {
                Log.d("BroadcastReceiver", "Invalid Type")
                displayNotification(context, "Geofence INVALID TYPE")
            }
        }
    }

    private fun displayNotification(context: Context, geofenceTransition: String){
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Geofence")
            .setContentText(geofenceTransition)
        notificationManager.notify(NOTIFICATION_ID, notification.build())
    }

    private fun createNotificationChannel(notificationManager: NotificationManager){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

}















