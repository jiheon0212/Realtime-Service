package com.example.realtimeserivce.adapter

import android.os.Handler
import android.os.Looper

class Timer {
    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null

    fun startTimer(delayMillis: Long, onTick: (Long) -> Unit, onFinish: () -> Unit) {
        val endTime = System.currentTimeMillis() + delayMillis
        runnable = object: Runnable {
            override fun run() {
                val currentTime = System.currentTimeMillis()
                val timeLeft = endTime - currentTime
                if (timeLeft > 0) {
                    onTick(timeLeft)
                    handler.postDelayed(this, 1000)
                } else {
                    onFinish()
                }
            }
        }
        handler.post(runnable!!)
    }

    fun cancelTimer() {
        runnable?.let { handler.removeCallbacks(it) }
    }
}