package com.example.sensorgame

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView


class MainActivity : AppCompatActivity(), SensorEventListener {

    var ball = ShapeDrawable()
    var sensorManager: SensorManager? = null
    var accelerometer: Sensor? = null
    var dWidth = 0
    var dHeight = 0
    var wTouched = false
    var animatedView: AnimatedView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            counter = 5
            animatedView = AnimatedView(this)
            val displayMetrics = DisplayMetrics()
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            accelerometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            dHeight = displayMetrics.heightPixels - 150
            dWidth = displayMetrics.widthPixels - 150
            Log.v("Y Size:", dHeight.toString())
            Log.v("X Size:", dWidth.toString())
            xAcc = dWidth / 3
            yAcc = dHeight / 3
            if (accelerometer != null) {
                sensorManager!!.registerListener(
                    this,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_FASTEST
                )
            }
            setContentView(animatedView)
    }


    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            xAcc -= event.values[0].toInt()
            yAcc += event.values[1].toInt()
            if (xAcc > dWidth) {
                xAcc = dWidth
                LostFunc()
            } else if (xAcc < 0) {
                xAcc = 0
                LostFunc()
            }
            if (yAcc > dHeight - 100) {
                yAcc = dHeight - 100
                LostFunc()
            } else if (yAcc < 0) {
                yAcc = 0
                LostFunc()
            }
        }
    }

    override fun onAccuracyChanged(
        sensor: Sensor,
        accuracy: Int
    ) {
    }

    fun LostFunc() {
        wTouched = true
        resetPosition()
        if (counter > 0) {
            counter--
            onPause()
        } else {
            showQuestionDialog()
        }
    }

    fun reset(){
        val handler = Handler()
        handler.postDelayed({
            val intent = Intent(this@MainActivity, MainActivity::class.java)
            startActivity(intent)
        }, 0)
        counter = 5
    }

    fun resetPosition() {
        yAcc = dHeight / 2
        xAcc = dWidth / 2
    }

    inner class AnimatedView(context: Context?) :

        AppCompatImageView(context!!) {
        var paint = Paint()
        override fun onDraw(canvas: Canvas) {
            paint.color = Color.BLACK
            paint.isFakeBoldText= true
            paint.typeface= Typeface.SERIF
            paint.textSize = 60f
            ball.setBounds(
                xAcc,
                yAcc,
                xAcc + Companion.width,
                yAcc + Companion.height
            )
            canvas.drawColor(Color.parseColor("#FAE0FF"))
            canvas.drawText(
                "Lifes you have: " + Integer.toString(counter),
                150f,
                100f,
                paint
            )
            ball.draw(canvas)
            invalidate()
        }

        init {
            ball = ShapeDrawable(OvalShape())
            ball.paint.color = Color.parseColor("Black")
        }
    }

    fun showQuestionDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_question, null)

        builder.setView(dialogView)
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                reset()
            }
            .setNegativeButton("Exit") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
        val dialog = builder.create()
        dialog.show()
    }

    companion object {
        const val width = 100
        const val height = 100
        var xAcc = 0
        var yAcc = 0
        var counter = 5
    }
}