package com.example.geofenceapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.wear.ambient.AmbientModeSupport
import com.example.geofenceapp.databinding.ActivityMainBinding
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import java.nio.charset.StandardCharsets

class MainActivity : AppCompatActivity(), SensorEventListener, AmbientModeSupport.AmbientCallbackProvider, MessageClient.OnMessageReceivedListener {

    private lateinit var binding: ActivityMainBinding
    private var activityContext: Context? = null

    private var HeartEvent : Float? = null
    private var HeartReading: String = ""

    private var messageEvent: MessageEvent? = null
    private var mobileNodeUri: String? = null

    private val TAG_MESSAGE_RECEIVED = "receive1"
    private val APP_OPEN_WEARABLE_PAYLOAD_PATH = "/APP_OPEN_WEARABLE_PAYLOAD"

    private var mobileDeviceConnected: Boolean = false

    private val wearableAppCheckPayloadReturnACK = "AppOpenWearableACK"


    private lateinit var mSensorManager : SensorManager
    private var mHeartRate : Sensor ?= null

    private lateinit var ambientController: AmbientModeSupport.AmbientController

    private val MESSAGE_ITEM_RECEIVED_PATH: String = "/message-item-received"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        activityContext = this


        if(ContextCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.BODY_SENSORS) !== PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity, android.Manifest.permission.BODY_SENSORS)){
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(android.Manifest.permission.BODY_SENSORS), 1)
            } else {
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(android.Manifest.permission.BODY_SENSORS), 1)
            }
        }

        ambientController = AmbientModeSupport.attach(this)

        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mHeartRate = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

        mSensorManager.registerListener(this, mHeartRate, SensorManager.SENSOR_DELAY_NORMAL)

        Wearable.getMessageClient(this).addListener(this)




    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if ((ContextCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.BODY_SENSORS) === PackageManager.PERMISSION_GRANTED)) {
                        Log.d("Permission ", "Granted")
                    } else {
                        Log.d("Permission ", "Denied")
                    }
                    return
                }
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event != null)
        {
            if (event.sensor.type == Sensor.TYPE_HEART_RATE)
            {

                Log.d("Event Values: ", event.values[0].toString())
                HeartEvent = event.values[0]
                HeartReading = HeartEvent.toString()

                Log.d("Heart Reading: ", HeartReading)
                val messageBytes = HeartReading.toByteArray(Charsets.UTF_8)

                val nodeId = getHandHeldNodeId()
                val path = "/message"
                Wearable.getMessageClient(this).sendMessage(nodeId.toString(), path, messageBytes)
                Log.d("nodeId: ", nodeId.toString())
                Log.d("Path: ", path)
                Log.d("MessageBytes: ", messageBytes.toString())
//                Log.d("Event Values: ", event.values[0].toString())
//                HeartEvent = event.values[0]
//                HeartReading = HeartEvent.toString()
//                if(mobileDeviceConnected)
//                {
//                    val nodeId: String = messageEvent?.sourceNodeId!!
//
//                    val payload: ByteArray = HeartEvent.toString().toByteArray()
//
//                    Wearable.getMessageClient(activityContext!!)
//                            .sendMessage(nodeId, MESSAGE_ITEM_RECEIVED_PATH, payload)
//                }
//                Log.d("Event Values: ", event.values[0].toString())
//                HeartEvent = event.values[0]
//                HeartReading = HeartEvent.toString()
//
//                Log.d("Heart Reading: ", HeartReading)
//                val messageBytes = HeartReading.toByteArray(Charsets.UTF_8)
//
//                val nodeId = getHandHeldNodeId()
//                val path = "/message"
//                Wearable.getMessageClient(this).sendMessage(nodeId.toString(), path, messageBytes)
//                Log.d("nodeId: ", nodeId.toString())
//                Log.d("Path: ", path)
//                Log.d("MessageBytes: ", messageBytes.toString())

            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun getAmbientCallback(): AmbientModeSupport.AmbientCallback = MyAmbientCallback()

    private inner class MyAmbientCallback : AmbientModeSupport.AmbientCallback() {
        override fun onEnterAmbient(ambientDetails: Bundle) {
            super.onEnterAmbient(ambientDetails)
        }

        override fun onUpdateAmbient() {
            super.onUpdateAmbient()
        }

        override fun onExitAmbient() {
            super.onExitAmbient()
        }
    }

    private fun getHandHeldNodeId(){
        Wearable.getNodeClient(this).connectedNodes
    }

    @SuppressLint("SetTextI18n")
    override fun onMessageReceived(p0: MessageEvent) {
        try {
            Log.d(TAG_MESSAGE_RECEIVED, "onMessageReceived event received")
            val s1 = String(p0.data, StandardCharsets.UTF_8)
            val messageEventPath: String = p0.path

            Log.d(
                TAG_MESSAGE_RECEIVED,
                "onMessageReceived() A message from watch was received:"
                        + p0.requestId
                        + " "
                        + messageEventPath
                        + " "
                        + s1
            )

            //Send back a message back to the source node
            //This acknowledges that the receiver activity is open
            if (messageEventPath.isNotEmpty() && messageEventPath == APP_OPEN_WEARABLE_PAYLOAD_PATH) {
                try {
                    // Get the node id of the node that created the data item from the host portion of
                    // the uri.
                    val nodeId: String = p0.sourceNodeId.toString()
                    // Set the data of the message to be the bytes of the Uri.
                    val returnPayloadAck = wearableAppCheckPayloadReturnACK
                    val payload: ByteArray = returnPayloadAck.toByteArray()

                    // Send the rpc
                    // Instantiates clients without member variables, as clients are inexpensive to
                    // create. (They are cached and shared between GoogleApi instances.)
                    val sendMessageTask =
                        Wearable.getMessageClient(activityContext!!)
                            .sendMessage(nodeId, APP_OPEN_WEARABLE_PAYLOAD_PATH, payload)

                    Log.d(
                        TAG_MESSAGE_RECEIVED,
                        "Acknowledgement message successfully with payload : $returnPayloadAck"
                    )

                    messageEvent = p0
                    mobileNodeUri = p0.sourceNodeId

                    sendMessageTask.addOnCompleteListener {
                        if (it.isSuccessful) {
                            Log.d(TAG_MESSAGE_RECEIVED, "Message sent successfully")

                        } else {
                            Log.d(TAG_MESSAGE_RECEIVED, "Message failed.")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }//emd of if
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
