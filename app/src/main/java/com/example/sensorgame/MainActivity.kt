package com.example.sensorgame

import android.content.Context
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
import android.util.DisplayMetrics
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.ViewModelProvider


class MainActivity : AppCompatActivity(), SensorEventListener {

    lateinit var ball: ShapeDrawable
    private var accelerometer: Sensor? = null
    var dWidth = 0
    var dHeight = 0
    var animatedView: AnimatedView? = null
    private lateinit var gameViewModel: GameViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gameViewModel = ViewModelProvider(this)[GameViewModel::class.java]

        initializeDisplayMetrics()
        resetPosition()
        setupAccelerometer()
        initializeAnimatedView()
    }

    private fun initializeDisplayMetrics() {
        val displayMetrics = DisplayMetrics().apply {
            val display = windowManager.currentWindowMetrics.bounds
            widthPixels = display.width()
            heightPixels = display.height()
        }
        dHeight = displayMetrics.heightPixels - 150
        dWidth = displayMetrics.widthPixels - 150
    }

    private fun setupAccelerometer() {
        val sensorService = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        sensorService?.let {
            accelerometer = it.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

            if (accelerometer != null) {
                it.registerListener(
                    this,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_FASTEST
                )
            }
        }
    }

    private fun initializeAnimatedView() {
        animatedView = AnimatedView(this)
        setContentView(animatedView)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER && gameViewModel.isBallMoving) {
            xAcc -= event.values[0].toInt()
            yAcc += event.values[1].toInt()

            if (xAcc > dWidth || xAcc < 0 || yAcc > dHeight - 100 || yAcc < 0) {
                xAcc = xAcc.coerceIn(0, dWidth)
                yAcc = yAcc.coerceIn(0, dHeight - 100)
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
        resetPosition()
        if (gameViewModel.counter > 0) {
            gameViewModel.decrementCounter()
        } else {
            gameViewModel.isBallMoving = false
            showQuestionDialog()
        }
    }

    fun reset() {
        gameViewModel.resetGame()
        resetPosition()
    }

    fun resetPosition() {
        yAcc = dHeight / 2
        xAcc = dWidth / 2
    }

    inner class AnimatedView(context: Context?) :

        AppCompatImageView(context!!) {
        private var paint = Paint()
        override fun onDraw(canvas: Canvas) {
            drawBackground(canvas)
            drawLivesText(canvas)
            drawBall(canvas)
            invalidate()
        }

        private fun drawBackground(canvas: Canvas) {
            canvas.drawColor(Color.parseColor("#FAE0FF"))
        }

        private fun drawLivesText(canvas: Canvas) {
            paint.color = Color.BLACK
            paint.isFakeBoldText = true
            paint.typeface = Typeface.SERIF
            paint.textSize = 60f
            canvas.drawText("Stay away from the edges!", 150f, 100f, paint)
            canvas.drawText("Lifes you have: ${gameViewModel.counter}", 150f, 200f, paint)

        }

        private fun drawBall(canvas: Canvas) {
            paint.color = Color.BLACK
            ball.setBounds(xAcc, yAcc, xAcc + Companion.width, yAcc + Companion.height)
            ball.draw(canvas)
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
    }
}