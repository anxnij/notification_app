package com.example.prac31_32

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
/**
 * MyService — получает команду из уведомления (Тема 1, п.3).
 * Стартуется из NotificationReceiver при ACTION_DO_WORK.
 */
class MyService : Service() {

    override fun onCreate() {
        super.onCreate()
        Log.d("MyService", "onCreate()")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val task = intent?.getStringExtra("task") ?: "none"
        Log.d("MyService", "onStartCommand() task=$task startId=$startId")

        // Тут — бизнес-логика по команде: синк/загрузка/обработка и т.д.

        stopSelf(startId) // завершили — сами себя остановили
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
