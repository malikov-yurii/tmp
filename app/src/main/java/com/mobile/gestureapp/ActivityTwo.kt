package com.mobile.gesture

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ActivityTwo : AppCompatActivity() {

    private val practiceVideos = hashMapOf(
        "LightOn" to R.raw.h_lighton,
        "LightOff" to R.raw.h_lightoff,
        "FanOn" to R.raw.h_fanon,
        "FanOff" to R.raw.h_fanoff,
        "FanUp" to R.raw.h_increasefanspeed,
        "FanDown" to R.raw.h_decreasefanspeed,
        "SetThermo" to R.raw.h_setthermo,
        "0" to R.raw.h_0,
        "1" to R.raw.h_1,
        "2" to R.raw.h_2,
        "3" to R.raw.h_3,
        "4" to R.raw.h_4,
        "5" to R.raw.h_5,
        "6" to R.raw.h_6,
        "7" to R.raw.h_7,
        "8" to R.raw.h_8,
        "9" to R.raw.h_9
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_two)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val gestID = intent.getStringExtra("gestID")
        val gestDescr = intent.getStringExtra("gestDescr")
        val practiceButton = findViewById<Button>(R.id.myPractBtn)
        practiceButton.text = "Practice $gestDescr"
        practiceButton.setOnClickListener {
            val intent = Intent(this, ActivityThree::class.java)
            intent.putExtra("gestID", gestID)
            startActivity(intent)
        }

        val myVidView = findViewById<VideoView>(R.id.myVideoView)
        val videoResId = practiceVideos[gestID]
        if (videoResId != null) {
            val videoUri = Uri.parse("android.resource://" + packageName + "/" + videoResId)
            myVidView.setVideoURI(videoUri)
            val myMediaController = MediaController(this)
            myVidView.setMediaController(myMediaController)
            myMediaController.setAnchorView(myVidView)

            myVidView.setOnPreparedListener { mp ->
                mp.isLooping = true
                mp.setOnCompletionListener { _ ->
                    mp.start()
                    mp.setLooping(false)
                }
                myVidView.start()
            }
        }
    }
}