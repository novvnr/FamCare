package com.app.famcare.view.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.app.famcare.R
import com.app.famcare.view.login.LoginActivity
import com.app.famcare.view.main.MainActivity
import com.app.famcare.view.onboarding.OnboardingActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private val splashTimeOut: Long = 5000
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        firebaseAuth = FirebaseAuth.getInstance()

        Handler().postDelayed({
            val currentUser = firebaseAuth.currentUser

            if (currentUser != null) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                val intent = if (isFirstTimeUser()) {
                    Intent(this, OnboardingActivity::class.java)
                } else {
                    Intent(this, LoginActivity::class.java)
                }
                startActivity(intent)
            }
            finish()
        }, splashTimeOut)
    }

    private fun isFirstTimeUser(): Boolean {
        val currentUser = firebaseAuth.currentUser
        return currentUser?.metadata?.creationTimestamp == currentUser?.metadata?.lastSignInTimestamp
    }
}