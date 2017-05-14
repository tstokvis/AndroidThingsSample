package tstok.raspberrycompanionapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.firebase.database.*
import tstok.raspberrycompanionapp.R.id.*

class MainActivity : AppCompatActivity() {

    val database = FirebaseDatabase.getInstance()
    var greenValue: Boolean? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val greenRef = database.getReference(GREEN)
        greenRef.addValueEventListener(LightChangeListener())

        findViewById(green_button).setOnClickListener {
            greenValue?.run {
                greenRef.setValue(!(greenValue as Boolean))
            }
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
            greenValue = dataSnapshot?.getValue(Boolean::class.java)
            findViewById(green_light).visibility = if (greenValue.isTrue()) View.VISIBLE else View.INVISIBLE
        }

    }

    companion object {
        val GREEN = "green"
    }
}
