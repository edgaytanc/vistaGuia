package com.example.etiquetadodeimagenml_kotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        Splash()
    }

    fun Splash(){
        object : CountDownTimer(3000, 1000){
            override fun onTick(p0: Long) {
            }

            override fun onFinish() {
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
                finish()
            }

        }.start()
    }
}