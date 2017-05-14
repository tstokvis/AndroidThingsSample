package tstok.raspberry

import android.os.Bundle
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


class RaspberryActivity : AppCompatActivity() {

    var greenGpio: Gpio? = null
    var greenButton: Gpio? = null
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.demo_activity)
        checkIOPorts()

        val manager = PeripheralManagerService()
        greenGpio = manager.openGpio(GREEN_PIN)
        greenButton = manager.openGpio(GREEN_BUTTON_PIN)

        // direct out for lights (digital write)
        greenGpio?.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)

        // direction in for buttons (digital read)
        setupOutput(greenButton, GREEN)

        // no analog read?!? smh

        findViewById(R.id.green_button).setOnClickListener {
            greenGpio?.run {
                greenGpio!!.value = !greenGpio!!.value
                updateFirebase(GREEN, greenGpio!!.value)
            }
        }

        database.getReference(GREEN).addValueEventListener(Listener(GREEN))

        super.onCreate(savedInstanceState)
    }

    fun setupOutput(gpio: Gpio?, color: String) {
        gpio?.setDirection(Gpio.DIRECTION_IN)
        gpio?.setActiveType(Gpio.ACTIVE_LOW)
        gpio?.setEdgeTriggerType(Gpio.EDGE_RISING)
        gpio?.registerGpioCallback(DemoCallback(color))
    }

    override fun onDestroy() {
        super.onDestroy()
        greenGpio?.close()
        greenGpio = null
    }

    fun updateFirebase(color: String, value: Boolean) = database.getReference(color).setValue(value)


    fun checkIOPorts() {

        (findViewById(R.id.demo_text) as TextView).text = "List of GPIO "
        val manager = PeripheralManagerService()
        val ports = manager.gpioList

        var portsString = ""
        for (port in ports) {
            portsString += port.toString() + " "
        }

        (findViewById(R.id.demo_text2) as TextView).text = portsString
    }

    open inner class DemoCallback(val color: String) : GpioCallback() {
        override fun onGpioEdge(gpio: Gpio?): Boolean {
            when (color) {
                GREEN -> greenGpio?.value = !greenGpio!!.value
            }

            greenGpio?.run { updateFirebase(color, greenGpio!!.value) }

            return true
        }
    }

    inner class Listener(val color: String) : ValueEventListener {
        override fun onCancelled(p0: DatabaseError?) {
            Log.d("Error with color $color", p0.toString())
        }

        override fun onDataChange(dataSnapshot: DataSnapshot?) {

            when (color){
                GREEN -> greenGpio?.value = dataSnapshot?.getValue(Boolean::class.java).isTrue()
            }
        }
    }

    companion object {

        const val GREEN_PIN = "BCM12"
        const val GREEN_BUTTON_PIN = "BCM20"
        const val GREEN = "green"
    }
}