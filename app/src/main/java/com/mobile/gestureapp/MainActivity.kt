package com.mobile.gesture

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private val gestures = hashMapOf(
        "Turn On Light" to "LightOn",
        "Turn Off Light" to "LightOff",
        "Turn On Fan" to "FanOn",
        "Turn Off Fan" to "FanOff",
        "Increase Fan Speed" to "FanUp",
        "Decrease Fan Speed" to "FanDown",
        "Set Thermostat to specified temperature" to "SetThermo",
        "Gesture 0" to "0",
        "Gesture 1" to "1",
        "Gesture 2" to "2",
        "Gesture 3" to "3",
        "Gesture 4" to "4",
        "Gesture 5" to "5",
        "Gesture 6" to "6",
        "Gesture 7" to "7",
        "Gesture 8" to "8",
        "Gesture 9" to "9"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_activity)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val spinner = findViewById<Spinner>(R.id.myDropdown)
        val list = mutableListOf("Select gesture")
        list.addAll(gestures.keys)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, list)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(0)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                if (position == 0) {
                    return
                }
                val gestDescr = parent.getItemAtPosition(position).toString()
                val gestID = gestures[gestDescr] ?: "Unknown"
                val intent = Intent(this@MainActivity, ActivityTwo::class.java)
                intent.putExtra("gestID", gestID)
                intent.putExtra("gestDescr", gestDescr)
                startActivity(intent)
                spinner.setSelection(0)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}