package tstok.raspberry

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback
import com.google.android.things.pio.PeripheralManagerService
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class RaspberryActivity : AppCompatActivity() {

    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    lateinit var greenGpio: Gpio
    lateinit var greenButton: Gpio

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.demo_activity)
        displayIOPorts()

        val manager = PeripheralManagerService()
        greenGpio = manager.openGpio(GREEN_LED_PIN)
        greenButton = manager.openGpio(GREEN_BUTTON_PIN)

        // direction out for lights (digital write)
        greenGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)

        // direction in for buttons (digital read)
        greenGpio.setDirection(Gpio.DIRECTION_IN)
        greenGpio.setActiveType(Gpio.ACTIVE_LOW)
        greenGpio.setEdgeTriggerType(Gpio.EDGE_RISING)
        greenGpio.registerGpioCallback(DemoCallback())

        // listener for when UI element is clicked, changes LED state on click
        findViewById(R.id.green_button).setOnClickListener {
            greenGpio.value = !greenGpio.value
            updateFirebase()
        }

        database.getReference(GREEN).addValueEventListener(LightChangeListener())

        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        greenGpio.close()
    }

    fun updateFirebase() = database.getReference(GREEN).setValue(greenGpio.value)

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
            greenGpio.value = !greenGpio.value
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
            greenGpio.value = dataSnapshot?.getValue(Boolean::class.java).isTrue()
        }
    }

    companion object {

        const val GREEN_LED_PIN = "BCM12"
        const val GREEN_BUTTON_PIN = "BCM20"
        const val GREEN = "green"
    }
}