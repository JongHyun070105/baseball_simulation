package com.example.baseball_simulation_app.ui.main

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.baseball_simulation_app.MainActivity
import com.example.baseball_simulation_app.R
import com.example.baseball_simulation_app.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startLogoAnimation()

        // 1.5초 후에 인터넷 체크
        Handler(Looper.getMainLooper()).postDelayed({
            checkInternetAndProceed()
        }, 1500)
    }

    private fun startLogoAnimation() {
        val animation = AnimationUtils.loadAnimation(this, R.anim.splash_logo_animation)
        binding.ivLogo.startAnimation(animation)
    }

    private fun checkInternetAndProceed() {
        if (isInternetAvailable()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            showNoInternetDialog()
        }
    }

    private fun showNoInternetDialog() {
        AlertDialog.Builder(this)
            .setTitle("인터넷 연결 오류")
            .setMessage("인터넷이 연결되어 있지 않습니다.\n앱을 종료합니다.")
            .setCancelable(false)
            .setPositiveButton("확인") { _, _ ->
                finishAffinity()
            }
            .show()
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
