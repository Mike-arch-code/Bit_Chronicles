package com.bit_chronicles.viewmodel.home

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.bit_chronicles.R
import com.bit_chronicles.viewmodel.character.CharacterActivity
import com.bit_chronicles.viewmodel.campaign.CampaignActivity
import android.widget.ImageButton

class HomeActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        val botoncampañas = findViewById<Button>(R.id.botoncampañas)
        val botonpjs = findViewById<Button>(R.id.botonpjs)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        botoncampañas.setOnClickListener {
            startActivity(Intent(this, CampaignActivity::class.java))
        }

        botonpjs.setOnClickListener {
            startActivity(Intent(this, CharacterActivity::class.java))
        }

        val images = listOf(
            R.drawable.banner1,
            R.drawable.banner2,
            R.drawable.banner3
        )

        viewPager = findViewById(R.id.imageSlider)
        viewPager.adapter = ImageSliderAdapter(images)

        // Autoplay cada 3 segundos
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                val nextItem = (viewPager.currentItem + 1) % images.size
                viewPager.setCurrentItem(nextItem, true)
                handler.postDelayed(this, 3000) // cada 3 segundos
            }
        }
        handler.postDelayed(runnable, 3000)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(runnable, 3000)
    }
}
