package com.mobile.gesture

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivityThree : AppCompatActivity(), SurfaceHolder.Callback {

    private lateinit var mySurface: SurfaceView
    private lateinit var surfaceHolder: SurfaceHolder
    private lateinit var sendToServerBtn: Button

    private var myRecorder: MediaRecorder? = null
    private var myCamera: Camera? = null
    private var stopRecordJob: Runnable? = null
    private var isInRecord = false
    private var myFile: File? = null

    private val restClient = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_three)

        mySurface = findViewById(R.id.cameraPreview)
        surfaceHolder = mySurface.holder
        surfaceHolder.addCallback(this)

        sendToServerBtn = findViewById(R.id.uploadButton)
        sendToServerBtn.isEnabled = false
        sendToServerBtn.setOnClickListener {
            if (myFile != null && myFile!!.exists()) {
                uploadVideoToServer(myFile!!)
            } else {
                Toast.makeText(this, "No video to upload", Toast.LENGTH_SHORT).show()
            }
        }

        val permisReq = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permisReq.add(Manifest.permission.CAMERA)
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permisReq.add(Manifest.permission.RECORD_AUDIO)
        }
        if (permisReq.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permisReq.toTypedArray<String>(), 1001)
        } else {
            openCamera()
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopRecord()
        stopCamera()
    }

    private fun openCamera() {
        try {
            myCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT)
            myCamera?.apply {
                setDisplayOrientation(270)
                setPreviewDisplay(surfaceHolder)
                startPreview()
            }
            startRecording()
        } catch (e: Exception) {
            Log.e("ActivityThree", "Failed to initialize camera", e)
        }
    }

    private fun startRecording() {
        if (isInRecord) return

        try {
            myRecorder = MediaRecorder()
            myCamera?.unlock()
            myRecorder?.apply {
                setCamera(myCamera)
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setVideoSource(MediaRecorder.VideoSource.CAMERA)
                setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH))
                setOrientationHint(90)

                val formatter = SimpleDateFormat("HHmmss", Locale.getDefault())
                val currentTime = formatter.format(Date())
                myFile = File(
                    getExternalFilesDir(null),
                    "recorded_video_$currentTime.mp4"
                )

                setOutputFile(myFile?.absolutePath)
                setPreviewDisplay(surfaceHolder.surface)

                Log.d("ActivityThree", "Preparing MediaRecorder")
                prepare()
                Log.d("ActivityThree", "Starting MediaRecorder")
                start()
                isInRecord = true
            }

            stopRecordJob = Runnable {
                stopRecord()
                stopCamera()
                if (myFile != null && myFile!!.exists()) {
                    Toast.makeText(
                        this,
                        "Recorded video saved: ${myFile!!.absolutePath}",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Recording FAILED!!!. No video to display.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            mySurface.postDelayed(stopRecordJob, 5000)

        } catch (e: IOException) {
            Log.e("ActivityThree", "IOException during prepare/start: ${e.message}", e)
            Toast.makeText(this, "Error starting recording: ${e.message}", Toast.LENGTH_LONG).show()
            stopRecord()
            stopCamera()
        } catch (e: IllegalStateException) {
            Log.e("ActivityThree", "IllegalStateException during prepare/start: ${e.message}", e)
            Toast.makeText(this, "Error starting recording: ${e.message}", Toast.LENGTH_LONG).show()
            stopRecord()
            stopCamera()
        } catch (e: Exception) {
            Log.e("ActivityThree", "Error starting recording", e)
            Toast.makeText(this, "Error starting recording: ${e.message}", Toast.LENGTH_LONG).show()
            stopRecord()
            stopCamera()
        }
    }

    private fun stopRecord() {
        if (!isInRecord) return

        try {
            myRecorder?.apply {
                try {
                    stop()
                    Log.d("ActivityThree", "Recording stopped successfully")
                } catch (e: RuntimeException) {
                    Log.e("ActivityThree", "RuntimeException during stop: ${e.message}", e)
                }
                reset()
                release()
            }
            myRecorder = null
            myCamera?.lock()
            isInRecord = false
            if (stopRecordJob != null) {
                mySurface.removeCallbacks(stopRecordJob)
                stopRecordJob = null
            }
            sendToServerBtn.isEnabled = true
        } catch (e: Exception) {
            Log.e("ActivityThree", "Error stopping recording", e)
            Toast.makeText(this, "Error stopping recording: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun stopCamera() {
        try {
            myCamera?.apply {
                stopPreview()
                release()
            }
            myCamera = null
        } catch (e: Exception) {
            Log.e("ActivityThree", "Error releasing camera", e)
        }
    }

    private fun uploadVideoToServer(videoFile: File) {

        val request = Request.Builder()
            .url("http://10.0.2.2:9999/persistVideo")
            .post(
                MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "file",
                        videoFile.name,
                        videoFile.asRequestBody("video/mp4".toMediaTypeOrNull())
                    )
                    .build()
            )
            .build()

        restClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Log.e("ActivityThree", "Upload FAILED!!!", e)
                    Toast.makeText(
                        this@ActivityThree,
                        "Upload FAILED!!!: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Log.d("ActivityThree", "Upload successful")
                        Toast.makeText(this@ActivityThree, "Upload successful", Toast.LENGTH_LONG)
                            .show()
                        startActivity(Intent(this@ActivityThree, MainActivity::class.java))
                        finish()
                    } else {
                        Log.e("ActivityThree", "Upload FAILED!!! with response code ${response.code}")
                        Toast.makeText(this@ActivityThree, "Upload FAILED!!!", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                openCamera()
            } else {
                Toast.makeText(this, "PERMISSIONS ARE NEEDED!", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopRecord()
        stopCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecord()
        stopCamera()
    }
}