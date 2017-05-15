package tstok.raspberry

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.TextView
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback
import com.google.android.things.pio.PeripheralManagerService
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.IOException


class RaspberryActivity : AppCompatActivity() {

    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    lateinit var ledGpio: Gpio
    lateinit var buttonGpio: Gpio

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.demo_activity)
        displayIOPorts()

        val manager = PeripheralManagerService()
        ledGpio = manager.openGpio(LED_PIN)
        buttonGpio = manager.openGpio(BUTTON_PIN)

        var ledSuccessful = true
        var buttonSuccessful = true

        // direction in for buttons (digital read)
        try {
            buttonGpio.setDirection(Gpio.DIRECTION_IN)
            buttonGpio.setActiveType(Gpio.ACTIVE_LOW)
            buttonGpio.setEdgeTriggerType(Gpio.EDGE_RISING)
            buttonGpio.registerGpioCallback(DemoCallback())
        } catch (exception: IOException) {
            buttonSuccessful = false
        }

        // direction out for lights (digital write)
        try {
            ledGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)

            // listener for when UI element is clicked, changes LED state on click
            findViewById(R.id.green_button).setOnClickListener {
                ledGpio.value = !ledGpio.value
                findViewById(R.id.light_circle).visibility = if (ledGpio.value) View.VISIBLE else View.INVISIBLE
                updateFirebase()
            }

            database.getReference(GREEN).addValueEventListener(LightChangeListener())
        } catch (exception: IOException) {
            ledSuccessful = false
        }

        AlertDialog.Builder(this)
                .setMessage("LED Sucessful: $ledSuccessful, Button Successful: $buttonSuccessful")
                .setPositiveButton(if (!ledSuccessful && !buttonSuccessful) "Fuck!" else "Okay", null)
                .show()

        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        ledGpio.close()
        buttonGpio.close()
        super.onDestroy()
    }

    fun updateFirebase() = database.getReference(GREEN).setValue(ledGpio.value)

    /**
     *  Displays available IO ports at the top of the screen
     */
    fun displayIOPorts() {
        (findViewById(R.id.demo_text) as TextView).text = "List of GPIO "
        val manager = PeripheralManagerService()
        val ports = manager.gpioList

        var portsString = ""
        ports.forEach { portsString += it.toString() + " " }

        (findViewById(R.id.demo_text2) as TextView).text = portsString
    }

    /**
     * Listener for changes in button state. When button clicked, inverts the value of the LED
     */
    open inner class DemoCallback : GpioCallback() {
        override fun onGpioEdge(gpio: Gpio?): Boolean {
            ledGpio.value = !ledGpio.value
            findViewById(R.id.light_circle).visibility = if (ledGpio.value) View.VISIBLE else View.INVISIBLE
            updateFirebase()
            return true
        }
    }

    /**
     * Listener for changes in Firebase database
     */
    inner class LightChangeListener : ValueEventListener {
        override fun onCancelled(p0: DatabaseError?) {
            Log.d("Error onCancelled: ", p0.toString())
        }

        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            ledGpio.value = dataSnapshot?.getValue(Boolean::class.java).isTrue()
            findViewById(R.id.light_circle).visibility = if (ledGpio.value) View.VISIBLE else View.INVISIBLE
        }
    }

    companion object {

        const val LED_PIN = "BCM20"
        const val BUTTON_PIN = "BCM21"
        const val GREEN = "green"
    }
}