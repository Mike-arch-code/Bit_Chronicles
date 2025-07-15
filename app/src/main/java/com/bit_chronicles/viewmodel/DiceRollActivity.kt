package com.bit_chronicles.viewmodel

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.os.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bit_chronicles.R

class DiceRollActivity : AppCompatActivity() {

    private lateinit var diceImage: ImageView
    private lateinit var diceResult: TextView
    private lateinit var rollButton: Button
    private var finalResult: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dice_roll)

        diceImage = findViewById(R.id.diceImage)
        diceResult = findViewById(R.id.diceResult)
        rollButton = findViewById(R.id.rollButton)

        rollButton.setOnClickListener {
            rollDice()
        }
    }

    private fun rollDice() {
        val animationDuration = 1000L
        val frameInterval = 50L

        val rotation = ObjectAnimator.ofFloat(diceImage, "rotation", 0f, 720f)
        rotation.duration = animationDuration
        rotation.interpolator = AccelerateDecelerateInterpolator()
        rotation.start()

        val handler = Handler(Looper.getMainLooper())
        var elapsed = 0L

        val imageCycler = object : Runnable {
            override fun run() {
                val tempResult = (1..20).random()
                val tempImageId = resources.getIdentifier("dado$tempResult", "drawable", packageName)
                diceImage.setImageResource(tempImageId)

                elapsed += frameInterval
                if (elapsed < animationDuration) {
                    handler.postDelayed(this, frameInterval)
                } else {
                    finalResult = (1..20).random()
                    val finalImageId = resources.getIdentifier("dado$finalResult", "drawable", packageName)
                    diceImage.setImageResource(finalImageId)
                    diceResult.text = "Resultado: $finalResult"

                    val resultIntent = Intent().apply {
                        putExtra("dice_result", finalResult)
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
            }
        }

        handler.post(imageCycler)
    }
}
