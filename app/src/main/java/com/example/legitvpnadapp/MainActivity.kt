package com.example.legitvpnadapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class MainActivity : AppCompatActivity() {

    private var mInterstitialAd: InterstitialAd? = null

    // TODO: রিলিজের সময় নিজের Interstitial Ad Unit ID বসাবেন (ডেভেলপমেন্টে Test ID রাখুন)
    private val AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712" // TEST ID

    private lateinit var btnConnect: Button
    private lateinit var tvStatus: TextView

    // বৈধ কানেকশন সফল হলে এই ব্রডকাস্ট আসবে
    private val connReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.legitvpnadapp.AUTH_CONNECTED") {
                tvStatus.text = "Connected (Authorized)"
                showInterstitialAd()   // ✅ কানেক্টেড -> Ad রান
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnConnect = findViewById(R.id.btnConnect)
        tvStatus = findViewById(R.id.tvStatus)

        // AdMob SDK ইনিশিয়ালাইজ
        MobileAds.initialize(this) {}

        // Ad আগেই লোড করে রাখা (প্রি-লোড)
        loadInterstitialAd()

        registerReceiver(connReceiver, IntentFilter("com.example.legitvpnadapp.AUTH_CONNECTED"))

        btnConnect.setOnClickListener {
            // VPN পারমিশন ডায়লগ
            val vpnIntent = VpnService.prepare(this)
            if (vpnIntent != null) {
                startActivityForResult(vpnIntent, 1001)
            } else {
                onActivityResult(1001, RESULT_OK, null)
            }
        }
    }

    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this, AD_UNIT_ID, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                mInterstitialAd = ad
            }
            override fun onAdFailedToLoad(error: LoadAdError) {
                mInterstitialAd = null
            }
        })
    }

    private fun showInterstitialAd() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(this)
        } else {
            // Ad রেডি না থাকলে আবার লোড করার চেষ্টা
            loadInterstitialAd()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            // ইউজার VPN পারমিশন দিয়েছে -> সার্ভিস স্টার্ট
            startService(Intent(this, MyVpnService::class.java))
            tvStatus.text = "Connecting..."
        }
    }

    override fun onDestroy() {
        unregisterReceiver(connReceiver)
        super.onDestroy()
    }
}