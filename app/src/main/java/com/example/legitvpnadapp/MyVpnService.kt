package com.example.legitvpnadapp

import android.content.Intent
import android.net.VpnService
import android.util.Log

class MyVpnService : VpnService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread {
            try {
                /* ============================================================
                   ⚠️ LEGAL USE ONLY — এখানে অবশ্যই আপনার নিজের/অনুমোদিত
                   VPN সার্ভারে রিয়েল কানেকশন (WireGuard/OpenVPN/handshake)
                   বসাবেন। অপারেটরের কোনো ফ্রি-ইন্টারনেট / চার্জিং বাইপাস /
                   ডার্ক টানেল কোড এখানে বসানো যাবে না।
                   ============================================================ */

                val builder = Builder()
                builder.setSession("LegitVPN")
                builder.addAddress("10.8.0.2", 24)
                builder.addRoute("0.0.0.0", 0)      // ট্রাফিক আপনার সার্ভারে ফরোয়ার্ড করতে হবে
                builder.addDnsServer("8.8.8.8")

                // TUN ইন্টারফেস তৈরি (স্থানীয় VPN)
                val fd = builder.establish()

                if (fd != null) {
                    /* রিয়েল অ্যাপে: fd থেকে প্যাকেট পড়ে protect() করা সকেটে
                       আপনার নিজের সার্ভারের সাথে ফরোয়ার্ড করুন।
                       নিচে শুধু ডেমো: "অথোরাইজড কানেকশন সফল" -> ব্রডকাস্ট */

                    sendBroadcast(Intent("com.example.legitvpnadapp.AUTH_CONNECTED"))
                    Log.d("MyVpnService", "VPN interface established (authorized).")
                } else {
                    Log.e("MyVpnService", "Failed to establish VPN interface.")
                }
            } catch (e: Exception) {
                Log.e("MyVpnService", "Error", e)
            }
        }.start()
        return START_STICKY
    }
}