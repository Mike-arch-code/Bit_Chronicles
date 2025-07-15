package com.bit_chronicles.viewmodel

import android.animation.ObjectAnimator
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bit_chronicles.R

class DiceRollFragment : Fragment() {

    private lateinit var diceImage: ImageView
    private var finalResult: Int = -1

    var onDiceRolled: ((Int) -> Unit)? = null
    val resultado get() = finalResult

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.activity_dice_roll, container, false)

        diceImage = view.findViewById(R.id.diceImage)

        diceImage.setOnClickListener {
            rollDice()
        }

        return view
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
                val tempImageId = resources.getIdentifier(
                    "dado$tempResult", "drawable", requireContext().packageName
                )
                diceImage.setImageResource(tempImageId)

                elapsed += frameInterval
                if (elapsed < animationDuration) {
                    handler.postDelayed(this, frameInterval)
                } else {
                    finalResult = (1..20).random()
                    val finalImageId = resources.getIdentifier(
                        "dado$finalResult", "drawable", requireContext().packageName
                    )
                    diceImage.setImageResource(finalImageId)

                    onDiceRolled?.invoke(finalResult)
                }
            }
        }

        handler.post(imageCycler)
    }
}
