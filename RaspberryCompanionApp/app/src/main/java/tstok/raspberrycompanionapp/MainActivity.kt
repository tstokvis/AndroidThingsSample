package tstok.raspberrycompanionapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.TextView
import com.google.firebase.database.*
import tstok.raspberrycompanionapp.R.id.*

class MainActivity : AppCompatActivity() {

    val database = FirebaseDatabase.getInstance()
    lateinit var greenRef: DatabaseReference
    lateinit var redRef: DatabaseReference
    lateinit var blueRef: DatabaseReference

    var greenValue: Boolean? = null
    var redValue:Boolean? = null
    var blueValue: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        greenRef = database.getReference(GREEN)
        redRef = database.getReference(RED)
        blueRef = database.getReference(BLUE)

        greenRef.addValueEventListener(Listener(GREEN))
        redRef.addValueEventListener(Listener(RED))
        blueRef.addValueEventListener(Listener(BLUE))

        findViewById(green_button).setOnClickListener {
            greenValue?.run {
                greenRef.setValue(!(greenValue as Boolean))
            }
        }

        findViewById(red_button).setOnClickListener {
            redValue?.run {
                redRef.setValue(!(redValue as Boolean))
            }
        }

        findViewById(blue_button).setOnClickListener {
            blueValue?.run {
                blueRef.setValue(!(blueValue as Boolean))
            }
        }
    }

    inner class Listener(val color: String) : ValueEventListener {
        override fun onCancelled(p0: DatabaseError?) {
           Log.d("Error with color $color", p0.toString())
        }

        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            when (color){
                GREEN -> {
                    greenValue = dataSnapshot?.getValue(Boolean::class.java)
                    findViewById(green_light).visibility = if (greenValue.isTrue()) View.VISIBLE else View.INVISIBLE
                }
                RED -> {
                    redValue = dataSnapshot?.getValue(Boolean::class.java)
                    findViewById(red_light).visibility = if (redValue.isTrue()) View.VISIBLE else View.INVISIBLE
                }
                BLUE -> {
                    blueValue = dataSnapshot?.getValue(Boolean::class.java)
                    findViewById(blue_light).visibility = if (blueValue.isTrue()) View.VISIBLE else View.INVISIBLE
                }
            }
        }

    }

    companion object {
        val GREEN = "green"
        val RED = "red"
        val BLUE ="blue"
    }
}
