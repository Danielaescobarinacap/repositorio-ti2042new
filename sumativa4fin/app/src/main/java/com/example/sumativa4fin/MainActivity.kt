package com.example.sumativa4fin

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var mqttManager: MqttClientHelper
    private lateinit var tvStatusAndHumidity: TextView
    private lateinit var ivGreenIndicator: ImageView
    private lateinit var ivYellowIndicator: ImageView
    private lateinit var ivRedIndicator: ImageView
    private lateinit var btnOff: Button
    private lateinit var btnHumidifier: Button
    private lateinit var btnDehumidifier: Button

    private var humidityValue: Int = 50
    private var deviceStatus: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        tvStatusAndHumidity = findViewById(R.id.tvStatusAndHumidity)
        ivGreenIndicator = findViewById(R.id.ivGreenIndicator)
        ivYellowIndicator = findViewById(R.id.ivYellowIndicator)
        ivRedIndicator = findViewById(R.id.ivRedIndicator)
        btnOff = findViewById(R.id.btnOff)
        btnHumidifier = findViewById(R.id.btnHumidifier)
        btnDehumidifier = findViewById(R.id.btnDehumidifier)

        // Create an instance of MqttClientHelper
        mqttManager = MqttClientHelper()

        // Subscribe to relevant topics
        mqttManager.subscribeToTopic("Sensor", ArrayList<RadioButton>())

        // Set up actions for buttons
        btnOff.setOnClickListener { mqttManager.publishMessage("OFF", "device/control") }
        btnHumidifier.setOnClickListener { deviceStatus = 1 }
        btnDehumidifier.setOnClickListener { deviceStatus = -1 }

        // Launch the device operation at some relevant point
        GlobalScope.launch(context = Dispatchers.Main) {
            deviceOperation(1000)
        }
    }

    // Callback to handle MQTT messages about humidity
    private inner class HumidityCallback : MqttCallback {
        override fun connectionLost(cause: Throwable?) {
            // Connection loss handling logic
        }

        override fun messageArrived(topic: String?, message: MqttMessage?) {
            // Logic to handle new humidity messages
            val humidityMessage = String(message?.payload ?: byteArrayOf())
            // Update the interface with the humidity message
            tvStatusAndHumidity.text = humidityMessage
        }

        override fun deliveryComplete(token: IMqttDeliveryToken?) {
            // Delivery complete handling logic
        }
    }

    private suspend fun deviceOperation(sleepTime: Long) {
        while (true) {
            humidityValue += 5 * deviceStatus
            if (humidityValue > 100) humidityValue = 100
            else if (humidityValue < 0) humidityValue = 0
            val humidityStatus: String = when {
                humidityValue < 15 -> "RED-"
                humidityValue < 30 -> "YELLOW-"
                humidityValue < 65 -> "GREEN"
                humidityValue < 75 -> "YELLOW+"
                else -> "RED+"
            }
            mqttManager.publishMessage(humidityStatus, "humidity")
            delay(sleepTime)
        }
    }
}
